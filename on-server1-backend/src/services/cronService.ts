import prisma from '../config/database';
import { externalProvider } from './externalProvider';
import { sendOrderCompletedEmail, sendOrderRejectedEmail } from './emailService';

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
      let rejected = 0;
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

            // Send completion email
            if (order.user?.email) {
              sendOrderCompletedEmail(order.user.email, order.user.name, {
                orderNumber: order.orderNumber, totalAmount: order.totalAmount, resultCodes: result.codes,
              }).catch(() => {});
            }
          } else if (result.status === 'REJECTED') {
            // Order rejected — refund user
            await prisma.order.update({
              where: { id: order.id },
              data: {
                status: 'REJECTED',
                responseData: JSON.stringify(result.rawResponse),
              },
            });

            // Auto-refund
            await this.refundOrder(order);
            rejected++;
            console.log(`[Cron] Order ${order.orderNumber} REJECTED — refunded ${order.totalAmount}`);

            // Send rejection + refund email
            if (order.user?.email) {
              sendOrderRejectedEmail(order.user.email, order.user.name, {
                orderNumber: order.orderNumber, totalAmount: order.totalAmount, reason: 'مرفوض من المزود',
              }).catch(() => {});
            }
          } else {
            stillPending++;
          }

          // Small delay between API calls to avoid rate limiting
          await new Promise(r => setTimeout(r, 500));
        } catch (err: any) {
          console.error(`[Cron] Error checking order ${order.orderNumber}:`, err.message);
        }
      }

      if (completed + rejected > 0) {
        console.log(`[Cron] Results: ${completed} completed, ${rejected} rejected, ${stillPending} still pending`);
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
            description: `استرجاع تلقائي: طلب ${order.orderNumber} مرفوض من المصدر`,
          },
        });
      });
    } catch (err: any) {
      console.error(`[Cron] Failed to refund order ${order.orderNumber}:`, err.message);
    }
  }
}

export const cronService = new CronService();
