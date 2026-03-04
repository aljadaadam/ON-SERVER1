import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import path from 'path';
import { env } from './config/env';
import { errorHandler, notFoundHandler } from './middleware/errorHandler';
import { integrityCheck } from './middleware/integrityCheck';
import { cronService } from './services/cronService';
import { telegramService } from './services/telegramService';

// Routes
import authRoutes from './routes/auth';
import productRoutes from './routes/products';
import orderRoutes from './routes/orders';
import userRoutes from './routes/users';
import adminRoutes from './routes/admin';
import bannerRoutes from './routes/banners';
import depositRoutes from './routes/deposits';

const app = express();

// Trust first proxy (LiteSpeed reverse proxy)
app.set('trust proxy', 1);

// ============================================
// Middleware
// ============================================
app.use(helmet());

// Disable caching for all API responses
app.use((_req, res, next) => {
  res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
  res.set('Pragma', 'no-cache');
  res.set('Expires', '0');
  res.set('Surrogate-Control', 'no-store');
  next();
});

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

// App integrity verification
app.use('/api/', integrityCheck);

// Static files for uploads
app.use('/uploads', express.static(path.join(__dirname, '..', 'uploads')));

// ============================================
// Static files: Landing page & Downloads & Dashboard
// Served via Express for no-cache headers (LiteSpeed ignores .htaccess Header directives)
// ============================================
const landingPath = path.join(__dirname, '..', '..', 'on-server1-landing');

// Landing page (no helmet CSP - has inline scripts/styles/fonts)
app.get('/app/', (_req, res) => {
  const fs = require('fs');
  const filePath = path.join(landingPath, 'index.html');
  res.removeHeader('Content-Security-Policy');
  if (fs.existsSync(filePath)) {
    res.sendFile(filePath);
  } else {
    res.sendFile('/home/www.on-server2.com/public_html/app/index.html');
  }
});
app.get('/app', (_req, res) => {
  res.redirect('/app/');
});

// APK downloads
app.use('/downloads', express.static('/home/www.on-server2.com/public_html/downloads', {
  setHeaders: (res) => {
    res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
}));

// Dashboard (SPA) - served via Express for no-cache headers
const dashboardPath = '/home/www.on-server2.com/public_html/ctrl-7x9a3k';
app.use('/ctrl-7x9a3k', express.static(dashboardPath, {
  setHeaders: (res) => {
    res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
}));
// SPA fallback for dashboard
app.get('/ctrl-7x9a3k/*', (_req, res) => {
  res.sendFile(dashboardPath + '/index.html');
});

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

  // Start Telegram bot
  telegramService.init().catch(err => {
    console.error('[Telegram] Init failed:', err.message);
  });
});

export default app;
