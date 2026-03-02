import { Router, Request, Response, NextFunction } from 'express';
import prisma from '../config/database';

const router = Router();

// GET /api/banners - Public banners
router.get('/', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const banners = await prisma.banner.findMany({
      where: { isActive: true },
      orderBy: { sortOrder: 'asc' },
    });
    res.json({ success: true, data: banners });
  } catch (error) {
    next(error);
  }
});

export default router;
