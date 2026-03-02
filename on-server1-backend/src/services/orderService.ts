import { v4 as uuidv4 } from 'uuid';
import prisma from '../config/database';
import { externalProvider } from './externalProvider';

export class OrderService {
  /**
   * Create order with SD-Unlocker integration
   * items: [{ productId, quantity, metadata: { imei?, fieldValues? } }]
   * metadata.imei: required for IMEI type services  
   * metadata.fieldValues: { fieldKey: value } for custom fields
   */
  async createOrder(userId: string, items: { productId: string; quantity: number; metadata?: any }[]) {
    // Fetch products and validate
    const productIds = items.map(i => i.productId);
    const products = await prisma.product.findMany({
      where: { id: { in: productIds }, isActive: true },
    });

    if (products.length !== items.length) {
      throw Object.assign(new Error('One or more products not found'), { statusCode: 400 });
    }

    // Validate IMEI for IMEI-type services (check both 'imei' and 'IMEI' keys)
    for (const item of items) {
      const product = products.find(p => p.id === item.productId)!;
      const imeiVal = item.metadata?.imei || item.metadata?.IMEI;
      if (product.serviceType === 'IMEI' && (!imeiVal || String(imeiVal).length < 10)) {
        throw Object.assign(new Error(`IMEI is required for "${product.name}"`), { statusCode: 400 });
      }
    }

    // Calculate total
    let totalAmount = 0;
    const orderItems = items.map(item => {
      const product = products.find(p => p.id === item.productId)!;
      const price = product.price * item.quantity;
      totalAmount += price;
      const imeiVal = item.metadata?.imei || item.metadata?.IMEI;
      return {
        productId: item.productId,
        quantity: item.quantity,
        price: product.price,
        metadata: item.metadata ? JSON.stringify(item.metadata) : null,
        imei: imeiVal || null,
      };
    });

    // Check user balance
    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user || user.balance < totalAmount) {
      throw Object.assign(new Error('Insufficient balance'), { statusCode: 400 });
    }

    // Create order with transaction
    const order = await prisma.$transaction(async (tx) => {
      // Deduct balance
      await tx.user.update({
        where: { id: userId },
        data: { balance: { decrement: totalAmount } },
      });

      // Create transaction record
      const updatedUser = await tx.user.findUnique({ where: { id: userId } });
      await tx.transaction.create({
        data: {
          userId,
          type: 'PURCHASE',
          amount: -totalAmount,
          balance: updatedUser!.balance,
          description: `Order purchase`,
        },
      });

      // Create order
      const orderNumber = `ON-${Date.now()}-${uuidv4().slice(0, 4).toUpperCase()}`;
      const newOrder = await tx.order.create({
        data: {
          orderNumber,
          userId,
          totalAmount,
          status: 'PENDING',
          items: { create: orderItems },
        },
        include: { items: { include: { product: true } } },
      });

      return newOrder;
    });

    // Submit to external provider and handle errors properly
    this.processWithExternalProvider(order).catch(err => {
      console.error('[OrderService] External processing failed:', err.message);
    });

    return order;
  }

  /**
   * Process order items with external provider API
   * Handles two error cases:
   *   1. Provider rejected the order → REJECTED + refund + notify
   *   2. Connection error → stays PENDING + admin notification
   */
  private async processWithExternalProvider(order: any) {
    const orderId = order.id;
    const orderNumber = order.orderNumber;
    const orderItems = order.items;
    let lastRefId = '';

    for (const item of orderItems) {
      const product = item.product || await prisma.product.findUnique({ where: { id: item.productId } });
      if (!product || !product.externalId) {
        console.warn(`[OrderService] Product ${item.productId} has no externalId, skipping provider submit`);
        continue;
      }

      // Determine IMEI
      let imei = item.imei || '';
      if (!imei) {
        if (product.serviceType === 'SERVER' || product.serviceType === 'REMOTE') {
          imei = externalProvider.generateRandomImei();
        } else {
          // IMEI type but no IMEI provided — reject
          await this.rejectOrderWithRefund(orderId, orderNumber, order.userId, order.totalAmount, 'IMEI مطلوب ولم يتم توفيره');
          return;
        }
      }

      // Parse custom field values from metadata
      let customFields: Record<string, string> | undefined;
      if (item.metadata) {
        try {
          const meta = typeof item.metadata === 'string' ? JSON.parse(item.metadata) : item.metadata;
          if (meta.fieldValues && typeof meta.fieldValues === 'object') {
            customFields = meta.fieldValues;
          }
        } catch {}
      }

      try {
        // Call external provider
        const result = await externalProvider.placeOrder(
          product.externalId,
          imei,
          item.quantity > 1 ? item.quantity : undefined,
          customFields
        );

        if (result.success) {
          lastRefId = result.referenceId;
          // Update IMEI on order item
          await prisma.orderItem.update({
            where: { id: item.id },
            data: { imei },
          });
        } else {
          // ════════════════════════════════════════
          // الحالة 1: المصدر رفض الطلب صراحةً
          // ════════════════════════════════════════
          const errorMsg = result.message || 'الطلب مرفوض من المصدر';
          await this.rejectOrderWithRefund(orderId, orderNumber, order.userId, order.totalAmount, errorMsg);
          return;
        }
      } catch (err: any) {
        // ════════════════════════════════════════
        // الحالة 2: مشكلة اتصال (timeout, شبكة...)
        // ════════════════════════════════════════
        // الطلب يبقى PENDING — لا نعرف هل المصدر استلمه
        // لا استرجاع (المصدر قد يكون نفّذه)
        console.error(`[OrderService] Connection error for order ${orderNumber}:`, err.message);
        await prisma.order.update({
          where: { id: orderId },
          data: {
            responseData: JSON.stringify({ error: `CONNECTION_ERROR: ${err.message}` }),
          },
        });
        // The order stays PENDING — admin should check manually
        return;
      }
    }

    // All items submitted successfully → PROCESSING
    await prisma.order.update({
      where: { id: orderId },
      data: {
        status: 'PROCESSING',
        externalOrderId: lastRefId || null,
      },
    });
  }

  /**
   * Reject order: set REJECTED status, refund wallet, log transaction
   */
  private async rejectOrderWithRefund(orderId: string, orderNumber: string, userId: string, totalAmount: number, reason: string) {
    // Update order status to REJECTED
    await prisma.order.update({
      where: { id: orderId },
      data: {
        status: 'REJECTED',
        responseData: JSON.stringify({ error: reason }),
      },
    });

    // Refund to wallet
    await prisma.$transaction(async (tx) => {
      const user = await tx.user.update({
        where: { id: userId },
        data: { balance: { increment: totalAmount } },
      });

      await tx.transaction.create({
        data: {
          userId,
          type: 'REFUND',
          amount: totalAmount,
          balance: user.balance,
          description: `استرجاع تلقائي: طلب #${orderNumber} مرفوض من المصدر — ${reason}`,
        },
      });
    });

    console.log(`[OrderService] Order ${orderNumber} REJECTED — refunded ${totalAmount}. Reason: ${reason}`);
  }

  async getOrdersByUser(userId: string, page: number = 1, limit: number = 20) {
    const skip = (page - 1) * limit;
    const [orders, total] = await Promise.all([
      prisma.order.findMany({
        where: { userId },
        include: { items: { include: { product: true } } },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
      prisma.order.count({ where: { userId } }),
    ]);

    return { orders, pagination: { page, limit, total, totalPages: Math.ceil(total / limit) } };
  }

  async getOrderById(orderId: string, userId?: string) {
    const where: any = { id: orderId };
    if (userId) where.userId = userId;

    const order = await prisma.order.findFirst({
      where,
      include: { items: { include: { product: true } }, user: { select: { id: true, name: true, email: true } } },
    });

    if (!order) {
      throw Object.assign(new Error('Order not found'), { statusCode: 404 });
    }
    return order;
  }

  async getAllOrders(page: number = 1, limit: number = 20, status?: string) {
    const skip = (page - 1) * limit;
    const where: any = {};
    if (status) where.status = status;

    const [orders, total] = await Promise.all([
      prisma.order.findMany({
        where,
        include: {
          items: { include: { product: true } },
          user: { select: { id: true, name: true, email: true } },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
      prisma.order.count({ where }),
    ]);

    return { orders, pagination: { page, limit, total, totalPages: Math.ceil(total / limit) } };
  }

  async updateOrderStatus(orderId: string, status: string) {
    return prisma.order.update({
      where: { id: orderId },
      data: { status: status as any },
    });
  }

  // Add balance to user
  async addBalance(userId: string, amount: number, description: string = 'Balance deposit') {
    const result = await prisma.$transaction(async (tx) => {
      const user = await tx.user.update({
        where: { id: userId },
        data: { balance: { increment: amount } },
      });

      await tx.transaction.create({
        data: {
          userId,
          type: 'DEPOSIT',
          amount,
          balance: user.balance,
          description,
        },
      });

      return user;
    });

    return { balance: result.balance };
  }

  async getTransactions(userId: string, page: number = 1, limit: number = 20) {
    const skip = (page - 1) * limit;
    const [transactions, total] = await Promise.all([
      prisma.transaction.findMany({
        where: { userId },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
      prisma.transaction.count({ where: { userId } }),
    ]);

    return { transactions, pagination: { page, limit, total, totalPages: Math.ceil(total / limit) } };
  }
}

export const orderService = new OrderService();
