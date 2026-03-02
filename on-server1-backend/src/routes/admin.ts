import { Router, Request, Response, NextFunction } from 'express';
import { authenticate, requireAdmin } from '../middleware/auth';
import prisma from '../config/database';
import { orderService } from '../services/orderService';
import { syncService } from '../services/syncService';
import { externalProvider } from '../services/externalProvider';
import nodemailer from 'nodemailer';

const router = Router();

// All admin routes require authentication + admin role
router.use(authenticate, requireAdmin);

// GET /api/admin/stats - Dashboard statistics
router.get('/stats', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const [
      totalUsers,
      totalProducts,
      totalOrders,
      pendingOrders,
      totalRevenue,
      recentOrders,
    ] = await Promise.all([
      prisma.user.count(),
      prisma.product.count({ where: { isActive: true } }),
      prisma.order.count(),
      prisma.order.count({ where: { status: 'PENDING' } }),
      prisma.order.aggregate({ _sum: { totalAmount: true }, where: { status: 'COMPLETED' } }),
      prisma.order.findMany({
        take: 10,
        orderBy: { createdAt: 'desc' },
        include: { user: { select: { name: true, email: true } } },
      }),
    ]);

    res.json({
      success: true,
      data: {
        totalUsers,
        totalProducts,
        totalOrders,
        pendingOrders,
        totalRevenue: totalRevenue._sum.totalAmount || 0,
        recentOrders,
      },
    });
  } catch (error) {
    next(error);
  }
});

// GET /api/admin/users - Get all users
router.get('/users', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 20;
    const skip = (page - 1) * limit;

    const [users, total] = await Promise.all([
      prisma.user.findMany({
        select: {
          id: true, email: true, phone: true, name: true,
          balance: true, role: true, isVerified: true, isActive: true,
          createdAt: true, _count: { select: { orders: true } },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
      prisma.user.count(),
    ]);

    res.json({ success: true, data: { users, pagination: { page, limit, total, totalPages: Math.ceil(total / limit) } } });
  } catch (error) {
    next(error);
  }
});

// PUT /api/admin/users/:id/balance - Add balance to user
router.put('/users/:id/balance', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { amount, description } = req.body;
    const result = await orderService.addBalance(req.params.id as string, amount, description || 'Admin deposit');
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// PUT /api/admin/users/:id/status - Toggle user status
router.put('/users/:id/status', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { isActive } = req.body;
    const user = await prisma.user.update({
      where: { id: req.params.id as string },
      data: { isActive },
      select: { id: true, name: true, isActive: true },
    });
    res.json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
});

// GET /api/admin/banners - Get all banners
router.get('/banners', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const banners = await prisma.banner.findMany({ orderBy: { sortOrder: 'asc' } });
    res.json({ success: true, data: banners });
  } catch (error) {
    next(error);
  }
});

// POST /api/admin/banners
router.post('/banners', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const banner = await prisma.banner.create({ data: req.body });
    res.status(201).json({ success: true, data: banner });
  } catch (error) {
    next(error);
  }
});

// PUT /api/admin/banners/:id
router.put('/banners/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const banner = await prisma.banner.update({ where: { id: req.params.id as string }, data: req.body });
    res.json({ success: true, data: banner });
  } catch (error) {
    next(error);
  }
});

// DELETE /api/admin/banners/:id
router.delete('/banners/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    await prisma.banner.delete({ where: { id: req.params.id as string } });
    res.json({ success: true, message: 'Banner deleted' });
  } catch (error) {
    next(error);
  }
});

// GET /api/admin/settings
router.get('/settings', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const settings = await prisma.setting.findMany();
    const settingsMap: Record<string, string> = {};
    settings.forEach(s => { settingsMap[s.key] = s.value; });
    res.json({ success: true, data: settingsMap });
  } catch (error) {
    next(error);
  }
});

// PUT /api/admin/settings
router.put('/settings', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const settings = req.body as Record<string, string>;
    for (const [key, value] of Object.entries(settings)) {
      await prisma.setting.upsert({
        where: { key },
        update: { value },
        create: { key, value },
      });
    }
    res.json({ success: true, message: 'Settings updated' });
  } catch (error) {
    next(error);
  }
});

// GET /api/admin/provider/settings - Get provider connection settings
router.get('/provider/settings', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const settings = await externalProvider.getProviderSettings();
    res.json({ success: true, data: settings });
  } catch (error) {
    next(error);
  }
});

// PUT /api/admin/provider/settings - Update provider connection settings
router.put('/provider/settings', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { url, username, apiKey } = req.body;
    await externalProvider.updateProviderSettings({ url, username, apiKey });
    res.json({ success: true, message: 'Provider settings updated' });
  } catch (error) {
    next(error);
  }
});

// POST /api/admin/provider/sync - Sync products from provider
router.post('/provider/sync', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { markupPercent } = req.body;
    const markup = typeof markupPercent === 'number' ? markupPercent : 0;
    const result = await syncService.syncProducts(markup);
    res.json({ success: true, data: result });
  } catch (error: any) {
    res.status(500).json({ success: false, message: error.message || 'فشلت المزامنة' });
  }
});

// DELETE /api/admin/provider/products - Delete all provider products
router.delete('/provider/products', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const result = await prisma.product.deleteMany({
      where: { externalId: { not: null } },
    });
    res.json({ success: true, data: { deleted: result.count }, message: `تم حذف ${result.count} منتج` });
  } catch (error) {
    next(error);
  }
});

// GET /api/admin/provider/balance - Get SD-Unlocker balance
router.get('/provider/balance', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const result = await syncService.getProviderBalance();
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// POST /api/admin/test-email - Send test email
router.post('/test-email', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { to } = req.body;
    if (!to) throw Object.assign(new Error('البريد المستلم مطلوب'), { statusCode: 400 });

    // Read SMTP settings from DB
    const smtpSettings = await prisma.setting.findMany({
      where: { key: { in: ['smtp_host', 'smtp_port', 'smtp_user', 'smtp_pass', 'smtp_from_email', 'smtp_from_name', 'smtp_secure'] } },
    });
    const smtp: Record<string, string> = {};
    smtpSettings.forEach(s => { smtp[s.key] = s.value; });

    if (!smtp.smtp_host || !smtp.smtp_user || !smtp.smtp_pass) {
      throw Object.assign(new Error('إعدادات SMTP غير مكتملة. الرجاء حفظ الإعدادات أولاً'), { statusCode: 400 });
    }

    const transporter = nodemailer.createTransport({
      host: smtp.smtp_host,
      port: parseInt(smtp.smtp_port || '587'),
      secure: smtp.smtp_secure === 'true',
      auth: {
        user: smtp.smtp_user,
        pass: smtp.smtp_pass,
      },
    });

    await transporter.sendMail({
      from: `"${smtp.smtp_from_name || 'ON-SERVER1'}" <${smtp.smtp_from_email || smtp.smtp_user}>`,
      to,
      subject: '✅ اختبار اتصال البريد — ON-SERVER1',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 30px; background: #1A1A2E; border-radius: 16px; color: #fff;">
          <div style="text-align: center; margin-bottom: 20px;">
            <div style="display: inline-block; background: #FFD700; color: #000; font-weight: bold; padding: 8px 20px; border-radius: 8px; font-size: 18px;">ON-SERVER1</div>
          </div>
          <h2 style="text-align: center; color: #4CAF50;">✅ تم الاتصال بنجاح!</h2>
          <p style="text-align: center; color: #ccc; font-size: 14px;">إعدادات SMTP تعمل بشكل صحيح.<br/>هذا بريد اختبار تلقائي.</p>
          <hr style="border: none; border-top: 1px solid #374151; margin: 20px 0;" />
          <p style="text-align: center; color: #888; font-size: 11px;">ON-SERVER1 — ${new Date().toLocaleString('ar-SA')}</p>
        </div>
      `,
    });

    res.json({ success: true, message: 'تم إرسال بريد الاختبار بنجاح' });
  } catch (error: any) {
    if (error.statusCode) return next(error);
    res.status(500).json({ success: false, message: error.message || 'فشل إرسال البريد' });
  }
});

export default router;
