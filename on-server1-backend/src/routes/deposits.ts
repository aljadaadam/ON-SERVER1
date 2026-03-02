import { Router, Request, Response, NextFunction } from 'express';
import { authenticate, requireAdmin } from '../middleware/auth';
import prisma from '../config/database';
import { orderService } from '../services/orderService';
import multer from 'multer';
import path from 'path';
import fs from 'fs';

const router = Router();

// ============================================
// Multer config for receipt uploads
// ============================================
const uploadsDir = path.join(__dirname, '..', '..', 'uploads', 'receipts');
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, uploadsDir),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `receipt-${Date.now()}-${Math.random().toString(36).slice(2)}${ext}`);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB
  fileFilter: (_req, file, cb) => {
    const allowed = ['.jpg', '.jpeg', '.png', '.webp'];
    const ext = path.extname(file.originalname).toLowerCase();
    if (allowed.includes(ext)) {
      cb(null, true);
    } else {
      cb(new Error('Only image files are allowed'));
    }
  },
});

// ============================================
// Helper: Generate next deposit number (starts at 1000, increments by 70)
// ============================================
async function getNextDepositNumber(): Promise<number> {
  const result = await prisma.$transaction(async (tx) => {
    const counter = await tx.depositCounter.update({
      where: { id: 'deposit_counter' },
      data: { counter: { increment: 70 } },
    });
    return counter.counter;
  });
  return result;
}

// ============================================
// Helper: Verify BEP20 USDT transaction via BSCScan API
// ============================================
async function verifyBep20Transaction(txHash: string, expectedAmount: number, walletAddress: string): Promise<{
  verified: boolean;
  message: string;
  actualAmount?: number;
}> {
  try {
    // BSCScan API to check transaction
    const apiKey = process.env.BSCSCAN_API_KEY || '';
    const url = `https://api.bscscan.com/api?module=proxy&action=eth_getTransactionByHash&txhash=${txHash}&apikey=${apiKey}`;
    
    const response = await fetch(url);
    const data: any = await response.json();
    
    if (!data.result || data.result === null) {
      return { verified: false, message: 'Transaction not found on BEP20 network' };
    }

    const tx = data.result;
    
    // Check if transaction is to our wallet
    const toAddress = tx.to?.toLowerCase();
    if (toAddress !== walletAddress.toLowerCase()) {
      // For USDT BEP20 (contract transfer), the 'to' field is the USDT contract address
      // We need to decode the input data to check the actual recipient
      // USDT BEP20 contract on BSC: 0x55d398326f99059fF775485246999027B3197955
      const usdtContract = '0x55d398326f99059ff775485246999027b3197955';
      
      if (toAddress === usdtContract) {
        // Decode transfer(address,uint256) from input data
        const input = tx.input;
        if (input && input.length >= 138) {
          const recipientHex = '0x' + input.slice(34, 74);
          const amountHex = '0x' + input.slice(74, 138);
          const actualRecipient = recipientHex.toLowerCase();
          const actualAmount = parseInt(amountHex, 16) / 1e18; // USDT has 18 decimals on BSC
          
          if (actualRecipient !== walletAddress.toLowerCase()) {
            return { verified: false, message: 'Transaction recipient does not match our wallet' };
          }
          
          // Allow 1% tolerance for amount verification
          if (actualAmount < expectedAmount * 0.99) {
            return { verified: false, message: `Amount mismatch. Expected: ${expectedAmount}, Got: ${actualAmount}`, actualAmount };
          }
          
          return { verified: true, message: 'Transaction verified successfully', actualAmount };
        }
      }
      
      return { verified: false, message: 'Transaction recipient does not match' };
    }

    // Direct BNB transfer (not USDT)
    const amountWei = parseInt(tx.value, 16);
    const amountBnb = amountWei / 1e18;
    
    return { verified: true, message: 'Transaction verified', actualAmount: amountBnb };
  } catch (error: any) {
    console.error('[USDT Verify] Error:', error.message);
    return { verified: false, message: 'Failed to verify transaction. Please try again.' };
  }
}

// ============================================
// USER ROUTES
// ============================================

// GET /api/deposits/gateway-info - Get payment gateway info
router.get('/gateway-info', authenticate, async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const settings = await prisma.setting.findMany({
      where: {
        key: {
          in: [
            'usdt_wallet_address', 'usdt_network', 'usdt_min_amount', 'usdt_max_amount',
            'bankak_account_name', 'bankak_account_number', 'bankak_bank_name',
            'bankak_exchange_rate', 'bankak_min_amount', 'bankak_max_amount',
            'bankak_transfer_note',
            'currency',
          ],
        },
      },
    });
    
    const settingsMap: Record<string, string> = {};
    settings.forEach(s => { settingsMap[s.key] = s.value; });
    
    res.json({
      success: true,
      data: {
        usdt: {
          walletAddress: settingsMap['usdt_wallet_address'] || '',
          network: settingsMap['usdt_network'] || 'BEP20 (BSC)',
          minAmount: parseFloat(settingsMap['usdt_min_amount'] || '5'),
          maxAmount: parseFloat(settingsMap['usdt_max_amount'] || '10000'),
        },
        bankak: {
          accountName: settingsMap['bankak_account_name'] || '',
          accountNumber: settingsMap['bankak_account_number'] || '',
          bankName: settingsMap['bankak_bank_name'] || 'بنكك',
          transferNote: settingsMap['bankak_transfer_note'] || '',
          exchangeRate: parseFloat(settingsMap['bankak_exchange_rate'] || '600'),
          minAmount: parseFloat(settingsMap['bankak_min_amount'] || '5'),
          maxAmount: parseFloat(settingsMap['bankak_max_amount'] || '5000'),
        },
        currency: settingsMap['currency'] || 'USD',
      },
    });
  } catch (error) {
    next(error);
  }
});

// POST /api/deposits/usdt - Create USDT deposit
router.post('/usdt', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { amount, txHash } = req.body;
    
    if (!amount || amount <= 0) {
      res.status(400).json({ success: false, message: 'Invalid amount' });
      return;
    }
    if (!txHash || txHash.length < 60) {
      res.status(400).json({ success: false, message: 'Invalid transaction hash' });
      return;
    }

    // Check if txHash already used
    const existing = await prisma.deposit.findFirst({ where: { txHash } });
    if (existing) {
      res.status(400).json({ success: false, message: 'This transaction hash has already been used' });
      return;
    }

    // Get wallet address from settings
    const walletSetting = await prisma.setting.findUnique({ where: { key: 'usdt_wallet_address' } });
    const walletAddress = walletSetting?.value || '';

    // Generate deposit number
    const depositNumber = await getNextDepositNumber();

    // Verify BEP20 transaction
    const verification = await verifyBep20Transaction(txHash, amount, walletAddress);

    const status = verification.verified ? 'CONFIRMED' : 'PENDING';

    // Create deposit
    const deposit = await prisma.deposit.create({
      data: {
        depositNumber,
        userId: req.user!.userId,
        amount: verification.actualAmount || amount,
        gateway: 'USDT',
        status,
        txHash,
      },
    });

    // If auto-verified, add balance immediately
    if (verification.verified) {
      await orderService.addBalance(
        req.user!.userId,
        verification.actualAmount || amount,
        `USDT Deposit #${depositNumber}`
      );
    }

    res.status(201).json({
      success: true,
      data: {
        deposit: {
          id: deposit.id,
          depositNumber: deposit.depositNumber,
          amount: deposit.amount,
          gateway: deposit.gateway,
          status: deposit.status,
          txHash: deposit.txHash,
          createdAt: deposit.createdAt,
        },
        verification,
      },
    });
  } catch (error) {
    next(error);
  }
});

// POST /api/deposits/bankak - Create Bankak deposit with receipt
router.post('/bankak', authenticate, upload.single('receipt'), async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { amount, note } = req.body;
    const parsedAmount = parseFloat(amount);
    
    if (!parsedAmount || parsedAmount <= 0) {
      res.status(400).json({ success: false, message: 'Invalid amount' });
      return;
    }
    if (!req.file) {
      res.status(400).json({ success: false, message: 'Receipt image is required' });
      return;
    }

    // Get exchange rate
    const rateSetting = await prisma.setting.findUnique({ where: { key: 'bankak_exchange_rate' } });
    const exchangeRate = parseFloat(rateSetting?.value || '600');
    const amountLocal = parsedAmount * exchangeRate;

    // Generate deposit number
    const depositNumber = await getNextDepositNumber();

    const receiptPath = `/uploads/receipts/${req.file.filename}`;

    // Create deposit (pending admin approval)
    const deposit = await prisma.deposit.create({
      data: {
        depositNumber,
        userId: req.user!.userId,
        amount: parsedAmount,
        amountLocal,
        exchangeRate,
        gateway: 'BANKAK',
        status: 'PENDING',
        receiptImage: receiptPath,
        note: note || null,
      },
    });

    res.status(201).json({
      success: true,
      data: {
        id: deposit.id,
        depositNumber: deposit.depositNumber,
        amount: deposit.amount,
        amountLocal: deposit.amountLocal,
        exchangeRate: deposit.exchangeRate,
        gateway: deposit.gateway,
        status: deposit.status,
        receiptImage: deposit.receiptImage,
        createdAt: deposit.createdAt,
      },
    });
  } catch (error) {
    next(error);
  }
});

// GET /api/deposits/my - Get user's deposits
router.get('/my', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 20;
    const skip = (page - 1) * limit;

    const [deposits, total] = await Promise.all([
      prisma.deposit.findMany({
        where: { userId: req.user!.userId },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
        select: {
          id: true, depositNumber: true, amount: true, amountLocal: true,
          exchangeRate: true, gateway: true, status: true, txHash: true,
          receiptImage: true, adminNote: true, createdAt: true,
        },
      }),
      prisma.deposit.count({ where: { userId: req.user!.userId } }),
    ]);

    res.json({
      success: true,
      data: { deposits, pagination: { page, limit, total, totalPages: Math.ceil(total / limit) } },
    });
  } catch (error) {
    next(error);
  }
});

// ============================================
// ADMIN ROUTES
// ============================================

// GET /api/deposits/admin/all - Get all deposits
router.get('/admin/all', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 20;
    const status = req.query.status as string;
    const skip = (page - 1) * limit;

    const where: any = {};
    if (status) where.status = status;

    const [deposits, total] = await Promise.all([
      prisma.deposit.findMany({
        where,
        include: { user: { select: { id: true, name: true, email: true, phone: true } } },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
      prisma.deposit.count({ where }),
    ]);

    res.json({
      success: true,
      data: { deposits, pagination: { page, limit, total, totalPages: Math.ceil(total / limit) } },
    });
  } catch (error) {
    next(error);
  }
});

// PUT /api/deposits/admin/:id/approve - Approve deposit
router.put('/admin/:id/approve', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { adminNote } = req.body;
    
    const deposit = await prisma.deposit.findUnique({ where: { id: req.params.id as string } });
    if (!deposit) {
      res.status(404).json({ success: false, message: 'Deposit not found' });
      return;
    }
    if (deposit.status !== 'PENDING') {
      res.status(400).json({ success: false, message: 'Deposit already processed' });
      return;
    }

    // Update deposit status
    const updated = await prisma.deposit.update({
      where: { id: req.params.id as string },
      data: { status: 'CONFIRMED', adminNote },
      include: { user: { select: { id: true, name: true, email: true } } },
    });

    // Add balance to user
    await orderService.addBalance(
      deposit.userId,
      deposit.amount,
      `${deposit.gateway} Deposit #${deposit.depositNumber} approved`
    );

    res.json({ success: true, data: updated });
  } catch (error) {
    next(error);
  }
});

// PUT /api/deposits/admin/:id/reject - Reject deposit
router.put('/admin/:id/reject', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { adminNote } = req.body;
    
    const deposit = await prisma.deposit.findUnique({ where: { id: req.params.id as string } });
    if (!deposit) {
      res.status(404).json({ success: false, message: 'Deposit not found' });
      return;
    }
    if (deposit.status !== 'PENDING') {
      res.status(400).json({ success: false, message: 'Deposit already processed' });
      return;
    }

    const updated = await prisma.deposit.update({
      where: { id: req.params.id as string },
      data: { status: 'REJECTED', adminNote },
      include: { user: { select: { id: true, name: true, email: true } } },
    });

    res.json({ success: true, data: updated });
  } catch (error) {
    next(error);
  }
});

// GET /api/deposits/admin/stats - Deposit statistics
router.get('/admin/stats', authenticate, requireAdmin, async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const [total, pending, confirmed, rejected, totalAmount] = await Promise.all([
      prisma.deposit.count(),
      prisma.deposit.count({ where: { status: 'PENDING' } }),
      prisma.deposit.count({ where: { status: 'CONFIRMED' } }),
      prisma.deposit.count({ where: { status: 'REJECTED' } }),
      prisma.deposit.aggregate({ _sum: { amount: true }, where: { status: 'CONFIRMED' } }),
    ]);

    res.json({
      success: true,
      data: { total, pending, confirmed, rejected, totalAmount: totalAmount._sum.amount || 0 },
    });
  } catch (error) {
    next(error);
  }
});

export default router;
