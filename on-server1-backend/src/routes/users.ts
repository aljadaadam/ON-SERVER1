import { Router, Request, Response, NextFunction } from 'express';
import { authenticate } from '../middleware/auth';
import { orderService } from '../services/orderService';
import prisma from '../config/database';
import multer from 'multer';
import path from 'path';
import fs from 'fs';

const router = Router();

// ============================================
// Multer config for avatar uploads
// ============================================
const avatarsDir = path.join(__dirname, '..', '..', 'uploads', 'avatars');
if (!fs.existsSync(avatarsDir)) {
  fs.mkdirSync(avatarsDir, { recursive: true });
}

const avatarStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, avatarsDir),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `avatar-${Date.now()}-${Math.random().toString(36).slice(2)}${ext}`);
  },
});

const avatarUpload = multer({
  storage: avatarStorage,
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

// POST /api/users/avatar - Upload avatar image
router.post('/avatar', authenticate, avatarUpload.single('avatar'), async (req: Request, res: Response, next: NextFunction) => {
  try {
    if (!req.file) {
      res.status(400).json({ success: false, message: 'No image file provided' });
      return;
    }

    // Delete old avatar file if exists
    const currentUser = await prisma.user.findUnique({ where: { id: req.user!.userId }, select: { avatar: true } });
    if (currentUser?.avatar) {
      const oldPath = path.join(__dirname, '..', '..', currentUser.avatar);
      if (fs.existsSync(oldPath)) {
        fs.unlinkSync(oldPath);
      }
    }

    const avatarPath = `/uploads/avatars/${req.file.filename}`;
    const user = await prisma.user.update({
      where: { id: req.user!.userId },
      data: { avatar: avatarPath },
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
