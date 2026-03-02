import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import path from 'path';
import { env } from './config/env';
import { errorHandler, notFoundHandler } from './middleware/errorHandler';
import { cronService } from './services/cronService';

// Routes
import authRoutes from './routes/auth';
import productRoutes from './routes/products';
import orderRoutes from './routes/orders';
import userRoutes from './routes/users';
import adminRoutes from './routes/admin';
import bannerRoutes from './routes/banners';
import depositRoutes from './routes/deposits';

const app = express();

// ============================================
// Middleware
// ============================================
app.use(helmet());
app.use(cors({
  origin: [env.DASHBOARD_URL, env.WEB_URL, '*'],
  credentials: true,
}));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 1000,
  message: { success: false, message: 'Too many requests, please try again later' },
});
app.use('/api/', limiter);

// Static files for uploads
app.use('/uploads', express.static(path.join(__dirname, '..', 'uploads')));

// ============================================
// API Routes
// ============================================
app.use('/api/auth', authRoutes);
app.use('/api/products', productRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/users', userRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/banners', bannerRoutes);
app.use('/api/deposits', depositRoutes);

// Health check
app.get('/api/health', (_req, res) => {
  res.json({ success: true, message: 'ON-SERVER1 API is running', timestamp: new Date().toISOString() });
});

// ============================================
// Error Handling
// ============================================
app.use(notFoundHandler);
app.use(errorHandler);

// ============================================
// Start Server
// ============================================
app.listen(env.PORT, () => {
  console.log(`
  ╔══════════════════════════════════════════╗
  ║   ON-SERVER1 API                         ║
  ║   Running on port ${env.PORT}                  ║
  ║   Environment: ${env.NODE_ENV}              ║
  ╚══════════════════════════════════════════╝
  `);

  // Start cron job for checking order statuses (every 3 minutes)
  cronService.start(3 * 60 * 1000);
});

export default app;
