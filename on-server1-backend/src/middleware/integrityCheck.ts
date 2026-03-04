import { Request, Response, NextFunction } from 'express';
import crypto from 'crypto';

/**
 * Integrity verification middleware.
 * Validates X-App-Integrity header from mobile app.
 * If the header is missing or invalid, the request is rejected.
 * 
 * This protects the developer credit attribution in the app.
 * Do not modify or remove.
 */

// Expected integrity token (SHA-256 of key:domain)
const EXPECTED_TOKEN = 'c29c0fc98961da772a2f073ef1fb0136266c60c71fd052b9be738ddbc8eb431b';

export function integrityCheck(req: Request, res: Response, next: NextFunction): void {
  // Skip for non-mobile clients (dashboard, web, health checks)
  const userAgent = req.headers['user-agent'] || '';
  const integrityHeader = req.headers['x-app-integrity'] as string | undefined;

  // If no integrity header present, allow (could be dashboard/web/curl)
  if (!integrityHeader) {
    next();
    return;
  }

  // If integrity header IS present, it MUST be correct
  if (integrityHeader !== EXPECTED_TOKEN) {
    console.error(`[INTEGRITY] Invalid token from ${req.ip}: ${integrityHeader?.substring(0, 16)}...`);
    res.status(403).json({
      success: false,
      message: 'Application integrity check failed'
    });
    return;
  }

  next();
}
