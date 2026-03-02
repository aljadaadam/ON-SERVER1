import { Router, Request, Response, NextFunction } from 'express';
import { authenticate, requireAdmin } from '../middleware/auth';
import { orderService } from '../services/orderService';

const router = Router();

// POST /api/orders - Create a new order
router.post('/', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { items } = req.body;
    const order = await orderService.createOrder(req.user!.userId, items);
    res.status(201).json({ success: true, data: order });
  } catch (error) {
    next(error);
  }
});

// GET /api/orders - Get user's orders
router.get('/', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { page, limit } = req.query;
    const result = await orderService.getOrdersByUser(
      req.user!.userId,
      page ? parseInt(page as string) : undefined,
      limit ? parseInt(limit as string) : undefined
    );
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// GET /api/orders/:id - Get order by ID
router.get('/:id', authenticate, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const isAdmin = req.user!.role === 'ADMIN';
    const order = await orderService.getOrderById(
      req.params.id as string,
      isAdmin ? undefined : req.user!.userId
    );
    res.json({ success: true, data: order });
  } catch (error) {
    next(error);
  }
});

// GET /api/orders/admin/all - Get all orders (Admin)
router.get('/admin/all', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { page, limit, status } = req.query;
    const result = await orderService.getAllOrders(
      page ? parseInt(page as string) : undefined,
      limit ? parseInt(limit as string) : undefined,
      status as string
    );
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// PUT /api/orders/:id/status - Update order status (Admin)
router.put('/:id/status', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { status } = req.body;
    const order = await orderService.updateOrderStatus(req.params.id as string, status);
    res.json({ success: true, data: order });
  } catch (error) {
    next(error);
  }
});

export default router;
