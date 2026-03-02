import { Router, Request, Response, NextFunction } from 'express';
import { productService } from '../services/productService';
import { authenticate, requireAdmin } from '../middleware/auth';

const router = Router();

// GET /api/products - Get all products (public)
router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { page, limit, categoryId, type, serviceType, groupName, search, featured } = req.query;
    const result = await productService.getAll({
      page: page ? parseInt(page as string) : undefined,
      limit: limit ? parseInt(limit as string) : undefined,
      categoryId: categoryId as string,
      type: type as string,
      serviceType: serviceType as string,
      groupName: groupName as string,
      search: search as string,
      featured: featured === 'true',
    });
    res.json({ success: true, data: result });
  } catch (error) {
    next(error);
  }
});

// GET /api/products/featured - Get featured products
router.get('/featured', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const products = await productService.getFeatured();
    res.json({ success: true, data: products });
  } catch (error) {
    next(error);
  }
});

// GET /api/products/groups - Get distinct group names (optionally filtered by serviceType)
router.get('/groups', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { serviceType } = req.query;
    const groups = await productService.getGroups(serviceType as string);
    res.json({ success: true, data: groups });
  } catch (error) {
    next(error);
  }
});

// GET /api/products/categories - Get all categories
router.get('/categories', async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const categories = await productService.getCategories();
    res.json({ success: true, data: categories });
  } catch (error) {
    next(error);
  }
});

// GET /api/products/:id - Get product by ID
router.get('/:id', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const product = await productService.getById(req.params.id as string);
    res.json({ success: true, data: product });
  } catch (error) {
    next(error);
  }
});

// POST /api/products (Admin only)
router.post('/', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const product = await productService.create(req.body);
    res.status(201).json({ success: true, data: product });
  } catch (error) {
    next(error);
  }
});

// PUT /api/products/:id (Admin only)
router.put('/:id', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const product = await productService.update(req.params.id as string, req.body);
    res.json({ success: true, data: product });
  } catch (error) {
    next(error);
  }
});

// DELETE /api/products/:id (Admin only)
router.delete('/:id', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    await productService.delete(req.params.id as string);
    res.json({ success: true, message: 'Product deleted' });
  } catch (error) {
    next(error);
  }
});

// POST /api/products/categories (Admin only)
router.post('/categories', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const category = await productService.createCategory(req.body);
    res.status(201).json({ success: true, data: category });
  } catch (error) {
    next(error);
  }
});

// PUT /api/products/categories/:id (Admin only)
router.put('/categories/:id', authenticate, requireAdmin, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const category = await productService.updateCategory(req.params.id as string, req.body);
    res.json({ success: true, data: category });
  } catch (error) {
    next(error);
  }
});

export default router;
