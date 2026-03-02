import { Router, Request, Response, NextFunction } from 'express';
import { authenticate } from '../middleware/auth';
import { orderService } from '../services/orderService';
import prisma from '../config/database';

const router = Router();

// GET /api/users/profile - Get current user profile
router.get('/profile', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.user!.userId },
      select: {
        id: true,
        email: true,
        phone: true,
        name: true,
        avatar: true,
        balance: true,
        role: true,
        isVerified: true,
        createdAt: true,
      },
    });
    res.json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
});

// PUT /api/users/profile - Update profile
router.put('/profile', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { name, phone, avatar } = req.body;
    const user = await prisma.user.update({
      where: { id: req.user!.userId },
      data: { name, phone, avatar },
      select: { id: true, email: true, phone: true, name: true, avatar: true, balance: true },
    });
    res.json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
});

// POST /api/users/balance - Add balance
router.post('/balance', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { amount } = req.body;
    if (!amount || amount <= 0) {
      res.status(400).json({ success: false, message: 'Invalid amount' });
      return;
    }
    const result = await orderService.addBalance(req.user!.userId, amount, 'User deposit');
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// GET /api/users/transactions - Get user transactions
router.get('/transactions', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { page, limit } = req.query;
    const result = await orderService.getTransactions(
      req.user!.userId,
      page ? parseInt(page as string) : undefined,
      limit ? parseInt(limit as string) : undefined
    );
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// PUT /api/users/change-password - Change password
router.put('/change-password', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { currentPassword, newPassword } = req.body;
    if (!currentPassword || !newPassword || newPassword.length < 6) {
      res.status(400).json({ success: false, message: 'Invalid password data' });
      return;
    }
    const bcrypt = await import('bcryptjs');
    const user = await prisma.user.findUnique({ where: { id: req.user!.userId } });
    if (!user) {
      res.status(404).json({ success: false, message: 'User not found' });
      return;
    }
    const isValid = await bcrypt.compare(currentPassword, user.password);
    if (!isValid) {
      res.status(400).json({ success: false, message: 'Current password is incorrect' });
      return;
    }
    const hashedPassword = await bcrypt.hash(newPassword, 12);
    await prisma.user.update({ where: { id: user.id }, data: { password: hashedPassword } });
    res.json({ success: true, message: 'Password changed successfully' });
  } catch (error) {
    next(error);
  }
});

// GET /api/users/app-settings - Public app settings (contact info, policies)
router.get('/app-settings', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const settings = await prisma.setting.findMany();
    const settingsMap: Record<string, string> = {};
    settings.forEach(s => { settingsMap[s.key] = s.value; });
    res.json({ success: true, data: settingsMap });
  } catch (error) {
    next(error);
  }
});

export default router;
