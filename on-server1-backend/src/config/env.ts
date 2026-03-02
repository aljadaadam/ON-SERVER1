import dotenv from 'dotenv';
dotenv.config();

export const env = {
  NODE_ENV: process.env.NODE_ENV || 'development',
  PORT: parseInt(process.env.PORT || '3000', 10),

  // Database
  DATABASE_URL: process.env.DATABASE_URL!,

  // JWT
  JWT_SECRET: process.env.JWT_SECRET || 'default-secret',
  JWT_REFRESH_SECRET: process.env.JWT_REFRESH_SECRET || 'default-refresh-secret',
  JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN || '15m',
  JWT_REFRESH_EXPIRES_IN: process.env.JWT_REFRESH_EXPIRES_IN || '7d',

  // OTP
  OTP_EXPIRY_MINUTES: parseInt(process.env.OTP_EXPIRY_MINUTES || '5', 10),

  // SMTP
  SMTP_HOST: process.env.SMTP_HOST || 'smtp.gmail.com',
  SMTP_PORT: parseInt(process.env.SMTP_PORT || '587', 10),
  SMTP_USER: process.env.SMTP_USER || '',
  SMTP_PASS: process.env.SMTP_PASS || '',

  // External Provider (SD-Unlocker)
  EXTERNAL_PROVIDER_URL: process.env.EXTERNAL_PROVIDER_URL || '',
  EXTERNAL_PROVIDER_USERNAME: process.env.EXTERNAL_PROVIDER_USERNAME || '',
  EXTERNAL_PROVIDER_API_KEY: process.env.EXTERNAL_PROVIDER_API_KEY || '',
  EXTERNAL_PROVIDER_SECRET: process.env.EXTERNAL_PROVIDER_SECRET || '',

  // Admin
  ADMIN_EMAIL: process.env.ADMIN_EMAIL || 'admin@onserver1.com',
  ADMIN_PASSWORD: process.env.ADMIN_PASSWORD || 'admin123456',

  // Upload
  UPLOAD_DIR: process.env.UPLOAD_DIR || './uploads',
  MAX_FILE_SIZE: parseInt(process.env.MAX_FILE_SIZE || '5242880', 10),

  // URLs
  DASHBOARD_URL: process.env.DASHBOARD_URL || 'http://localhost:5173',
  WEB_URL: process.env.WEB_URL || 'http://localhost:4000',
};
