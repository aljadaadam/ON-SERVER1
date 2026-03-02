import prisma from '../config/database';
import { externalProvider } from './externalProvider';

/**
 * Cron Service
 * Periodically checks pending/processing order statuses with SD-Unlocker
 */
export class CronService {
  private intervalId: NodeJS.Timeout | null = null;
  private isRunning = false;

  /**
   * Start the cron job
   * @param intervalMs - Check interval in milliseconds (default: 3 minutes)
   */
  start(intervalMs: number = 3 * 60 * 1000) {
    if (this.intervalId) {
      console.log('[Cron] Already running');
      return;
    }

    console.log(`[Cron] Starting order status checker (every ${intervalMs / 1000}s)`);
    
    // Run immediately on start
    this.checkPendingOrders().catch(err => {
      console.error('[Cron] Initial check failed:', err.message);
    });

    // Then run on interval
    this.intervalId = setInterval(() => {
      this.checkPendingOrders().catch(err => {
        console.error('[Cron] Periodic check failed:', err.message);
      });
    }, intervalMs);
  }

  /**
   * Stop the cron job
   */
  stop() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
      console.log('[Cron] Stopped');
    }
  }

  /**
   * Check all pending/processing orders that have an externalOrderId
   */
  async checkPendingOrders() {
    if (this.isRunning) {
      console.log('[Cron] Previous check still running, skipping...');
      return;
    }

    this.isRunning = true;
    try {
      // Find orders that are PROCESSING with a reference ID
      const pendingOrders = await prisma.order.findMany({
        where: {
          status: 'PROCESSING',
          externalOrderId: { not: null },
        },
        include: {
          items: { include: { product: true } },
          user: { select: { id: true, name: true, email: true } },
        },
        take: 50, // Process max 50 at a time
      });

      if (pendingOrders.length === 0) {
        return;
      }

      console.log(`[Cron] Checking ${pendingOrders.length} pending orders...`);

      let completed = 0;
      let failed = 0;
      let stillPending = 0;

      for (const order of pendingOrders) {
        try {
          if (!order.externalOrderId) continue;

          const result = await externalProvider.getOrderStatus(order.externalOrderId);

          if (!result.success) {
            stillPending++;
            continue;
          }

          if (result.status === 'COMPLETED') {
            // Order completed — update status and store result codes
            await prisma.order.update({
              where: { id: order.id },
              data: {
                status: 'COMPLETED',
                resultCodes: result.codes || null,
                responseData: JSON.stringify(result.rawResponse),
              },
            });
            completed++;
            console.log(`[Cron] Order ${order.orderNumber} COMPLETED${result.codes ? ' (codes: ' + result.codes.substring(0, 30) + '...)' : ''}`);
          } else if (result.status === 'FAILED') {
            // Order failed — refund user
            await prisma.order.update({
              where: { id: order.id },
              data: {
                status: 'FAILED',
                responseData: JSON.stringify(result.rawResponse),
              },
            });

            // Auto-refund
            await this.refundOrder(order);
            failed++;
            console.log(`[Cron] Order ${order.orderNumber} FAILED — refunded ${order.totalAmount}`);
          } else {
            stillPending++;
          }

          // Small delay between API calls to avoid rate limiting
          await new Promise(r => setTimeout(r, 500));
        } catch (err: any) {
          console.error(`[Cron] Error checking order ${order.orderNumber}:`, err.message);
        }
      }

      if (completed + failed > 0) {
        console.log(`[Cron] Results: ${completed} completed, ${failed} failed, ${stillPending} still pending`);
      }
    } finally {
      this.isRunning = false;
    }
  }

  /**
   * Refund user for a failed order
   */
  private async refundOrder(order: any) {
    try {
      await prisma.$transaction(async (tx) => {
        const user = await tx.user.update({
          where: { id: order.userId },
          data: { balance: { increment: order.totalAmount } },
        });

        await tx.transaction.create({
          data: {
            userId: order.userId,
            type: 'REFUND',
            amount: order.totalAmount,
            balance: user.balance,
            description: `Auto-refund for failed order ${order.orderNumber}`,
          },
        });
      });
    } catch (err: any) {
      console.error(`[Cron] Failed to refund order ${order.orderNumber}:`, err.message);
    }
  }
}

export const cronService = new CronService();
