import TelegramBot from 'node-telegram-bot-api';
import prisma from '../config/database';
import { orderService } from './orderService';

class TelegramService {
  private bot: TelegramBot | null = null;
  private chatId: string = '';
  private isRunning = false;

  /** Load settings from DB and (re)start bot */
  async init() {
    try {
      const settings = await prisma.setting.findMany({
        where: { key: { in: ['telegram_bot_token', 'telegram_chat_id'] } },
      });
      const map: Record<string, string> = {};
      settings.forEach((s: any) => { map[s.key] = s.value; });

      const token = map['telegram_bot_token'] || '';
      this.chatId = map['telegram_chat_id'] || '';

      if (!token || !this.chatId) {
        console.log('[Telegram] Bot token or chat ID not configured — skipping');
        return;
      }

      // Stop previous instance
      await this.stop();

      this.bot = new TelegramBot(token, { polling: true });
      this.isRunning = true;
      console.log('[Telegram] Bot started successfully');

      this.registerHandlers();
    } catch (err: any) {
      console.error('[Telegram] Failed to start:', err.message);
    }
  }

  async stop() {
    if (this.bot && this.isRunning) {
      try { await this.bot.stopPolling(); } catch {}
      this.bot = null;
      this.isRunning = false;
    }
  }

  /** Reload settings and restart (called after settings update) */
  async reload() {
    await this.init();
  }

  // ============================================
  // Command & Callback Handlers
  // ============================================
  private registerHandlers() {
    if (!this.bot) return;

    // /start command
    this.bot.onText(/\/start/, (msg) => {
      const chatId = msg.chat.id.toString();
      this.bot!.sendMessage(chatId,
        `🤖 *مرحباً بك في بوت ON-SERVER1*\n\nChat ID الخاص بك: \`${chatId}\`\n\nاستخدم هذا المعرف في إعدادات لوحة التحكم.`,
        {
          parse_mode: 'Markdown',
          reply_markup: {
            inline_keyboard: [
              [{ text: '📦 حالة طلب', callback_data: 'menu_order_status' }],
              [{ text: '💰 شحن رصيد مستخدم', callback_data: 'menu_charge_user' }],
              [{ text: '📊 إحصائيات', callback_data: 'menu_stats' }],
            ],
          },
        }
      );
    });

    // /menu command
    this.bot.onText(/\/menu/, (msg) => {
      this.sendMainMenu(msg.chat.id.toString());
    });

    // Callback queries
    this.bot.on('callback_query', async (query) => {
      if (!query.data || !query.message) return;
      const chatId = query.message.chat.id.toString();

      try {
        // Deposit approve/reject
        if (query.data.startsWith('deposit_approve_')) {
          const depositId = query.data.replace('deposit_approve_', '');
          await this.handleDepositApprove(chatId, depositId, query.message.message_id);
        } else if (query.data.startsWith('deposit_reject_')) {
          const depositId = query.data.replace('deposit_reject_', '');
          await this.handleDepositReject(chatId, depositId, query.message.message_id);
        }
        // Menu actions
        else if (query.data === 'menu_order_status') {
          await this.bot!.sendMessage(chatId, '🔎 أرسل رقم الطلب للاستعلام عن حالته:', { reply_markup: { force_reply: true } });
          this.waitForReply(chatId, 'order_status');
        } else if (query.data === 'menu_charge_user') {
          await this.bot!.sendMessage(chatId, '📧 أرسل بريد المستخدم لشحن رصيده:', { reply_markup: { force_reply: true } });
          this.waitForReply(chatId, 'charge_email');
        } else if (query.data === 'menu_stats') {
          await this.sendStats(chatId);
        } else if (query.data === 'menu_back') {
          this.sendMainMenu(chatId);
        }

        await this.bot!.answerCallbackQuery(query.id);
      } catch (err: any) {
        console.error('[Telegram] Callback error:', err.message);
        try { await this.bot!.answerCallbackQuery(query.id, { text: '❌ خطأ' }); } catch {}
      }
    });

    // Text messages (for reply flows)
    this.bot.on('message', async (msg) => {
      if (!msg.text || msg.text.startsWith('/')) return;
      const chatId = msg.chat.id.toString();
      await this.handleTextMessage(chatId, msg.text.trim());
    });
  }

  // ============================================
  // Reply flow state (simple in-memory)
  // ============================================
  private replyState: Record<string, { action: string; data?: any; expiresAt: number }> = {};

  private waitForReply(chatId: string, action: string, data?: any) {
    this.replyState[chatId] = { action, data, expiresAt: Date.now() + 5 * 60 * 1000 };
  }

  private async handleTextMessage(chatId: string, text: string) {
    const state = this.replyState[chatId];
    if (!state || Date.now() > state.expiresAt) {
      // No active state — try as order number
      if (/^\d+$/.test(text)) {
        await this.checkOrderStatus(chatId, text);
      }
      return;
    }

    const action = state.action;
    delete this.replyState[chatId];

    if (action === 'order_status') {
      await this.checkOrderStatus(chatId, text);
    } else if (action === 'charge_email') {
      // Find user by email
      const user = await prisma.user.findUnique({ where: { email: text }, select: { id: true, name: true, email: true, balance: true } });
      if (!user) {
        await this.bot!.sendMessage(chatId, '❌ لم يتم العثور على المستخدم بهذا البريد.');
        return;
      }
      await this.bot!.sendMessage(chatId,
        `👤 *${user.name}*\n📧 ${user.email}\n💰 الرصيد: $${user.balance.toFixed(2)}\n\nأرسل المبلغ المراد شحنه (دولار):`,
        { parse_mode: 'Markdown', reply_markup: { force_reply: true } }
      );
      this.waitForReply(chatId, 'charge_amount', { userId: user.id, userName: user.name, email: user.email });
    } else if (action === 'charge_amount') {
      const amount = parseFloat(text);
      if (isNaN(amount) || amount <= 0) {
        await this.bot!.sendMessage(chatId, '❌ المبلغ غير صالح.');
        return;
      }
      try {
        const result = await orderService.addBalance(state.data.userId, amount, 'شحن رصيد عبر تليجرام');
        await this.bot!.sendMessage(chatId,
          `✅ *تم شحن الرصيد بنجاح*\n\n👤 ${state.data.userName}\n📧 ${state.data.email}\n💵 المبلغ: $${amount.toFixed(2)}\n💰 الرصيد الجديد: $${result.balance.toFixed(2)}`,
          { parse_mode: 'Markdown' }
        );
      } catch (err: any) {
        await this.bot!.sendMessage(chatId, `❌ فشل الشحن: ${err.message}`);
      }
    }
  }

  // ============================================
  // Order Status
  // ============================================
  private async checkOrderStatus(chatId: string, orderNumber: string) {
    try {
      const order = await prisma.order.findFirst({
        where: { orderNumber: orderNumber.trim() },
        include: {
          user: { select: { name: true, email: true } },
          items: { include: { product: { select: { name: true } } } },
        },
      });

      if (!order) {
        await this.bot!.sendMessage(chatId, `❌ لم يتم العثور على طلب برقم *${orderNumber}*`, { parse_mode: 'Markdown' });
        return;
      }

      const statusMap: Record<string, string> = {
        PENDING: '⏳ قيد الانتظار',
        WAITING: '⌛ في الانتظار',
        PROCESSING: '🔄 قيد المعالجة',
        COMPLETED: '✅ مكتمل',
        REJECTED: '❌ مرفوض',
      };

      const productsList = order.items.map((i: any) => `  • ${i.productName || i.product?.name || 'منتج'} × ${i.quantity}`).join('\n');

      let msg = `📦 *طلب #${order.orderNumber}*\n\n`;
      msg += `👤 ${order.user?.name || 'مجهول'}\n`;
      msg += `📧 ${order.user?.email || ''}\n`;
      msg += `📊 الحالة: ${statusMap[order.status] || order.status}\n`;
      msg += `💰 المبلغ: $${order.totalAmount.toFixed(2)}\n`;
      msg += `📅 التاريخ: ${order.createdAt.toLocaleDateString('ar-EG')}\n`;
      msg += `\n📋 المنتجات:\n${productsList}`;

      if (order.resultCodes) {
        try {
          const codes = JSON.parse(order.resultCodes);
          if (codes.length > 0) {
            msg += `\n\n🔑 النتائج:\n`;
            codes.forEach((c: any) => {
              msg += `  • ${c.code || c.log || JSON.stringify(c)}\n`;
            });
          }
        } catch {}
      }

      await this.bot!.sendMessage(chatId, msg, {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [[{ text: '◀️ القائمة', callback_data: 'menu_back' }]],
        },
      });
    } catch (err: any) {
      await this.bot!.sendMessage(chatId, `❌ خطأ: ${err.message}`);
    }
  }

  // ============================================
  // Stats
  // ============================================
  private async sendStats(chatId: string) {
    try {
      const [users, products, orders, pendingOrders, pendingDeposits, totalRevenue] = await Promise.all([
        prisma.user.count(),
        prisma.product.count({ where: { isActive: true } }),
        prisma.order.count(),
        prisma.order.count({ where: { status: { in: ['PENDING', 'WAITING'] } } }),
        prisma.deposit.count({ where: { status: 'PENDING' } }),
        prisma.order.aggregate({ _sum: { totalAmount: true }, where: { status: 'COMPLETED' } }),
      ]);

      const msg = `📊 *إحصائيات ON-SERVER1*\n\n` +
        `👥 المستخدمين: ${users}\n` +
        `📦 المنتجات: ${products}\n` +
        `🛒 الطلبات: ${orders}\n` +
        `⏳ طلبات معلقة: ${pendingOrders}\n` +
        `💳 إيداعات معلقة: ${pendingDeposits}\n` +
        `💰 إجمالي الإيرادات: $${(totalRevenue._sum.totalAmount || 0).toFixed(2)}`;

      await this.bot!.sendMessage(chatId, msg, {
        parse_mode: 'Markdown',
        reply_markup: {
          inline_keyboard: [[{ text: '◀️ القائمة', callback_data: 'menu_back' }]],
        },
      });
    } catch (err: any) {
      await this.bot!.sendMessage(chatId, `❌ خطأ: ${err.message}`);
    }
  }

  // ============================================
  // Main Menu
  // ============================================
  private sendMainMenu(chatId: string) {
    this.bot!.sendMessage(chatId, '📋 *القائمة الرئيسية*', {
      parse_mode: 'Markdown',
      reply_markup: {
        inline_keyboard: [
          [{ text: '📦 حالة طلب', callback_data: 'menu_order_status' }],
          [{ text: '💰 شحن رصيد مستخدم', callback_data: 'menu_charge_user' }],
          [{ text: '📊 إحصائيات', callback_data: 'menu_stats' }],
        ],
      },
    });
  }

  // ============================================
  // Notifications
  // ============================================

  /** Notify admin about new order */
  async notifyNewOrder(order: any) {
    if (!this.bot || !this.chatId) return;
    try {
      const user = await prisma.user.findUnique({ where: { id: order.userId }, select: { name: true, email: true } });
      const items = order.items || [];
      const productsList = items.map((i: any) => `  • ${i.productName || 'منتج'} × ${i.quantity}`).join('\n');

      const msg = `🛒 *طلب جديد #${order.orderNumber}*\n\n` +
        `👤 ${user?.name || 'مجهول'}\n` +
        `📧 ${user?.email || ''}\n` +
        `💰 المبلغ: $${order.totalAmount.toFixed(2)}\n` +
        `📋 المنتجات:\n${productsList}\n` +
        `📅 ${new Date().toLocaleString('ar-EG')}`;

      await this.bot.sendMessage(this.chatId, msg, { parse_mode: 'Markdown' });
    } catch (err: any) {
      console.error('[Telegram] Failed to send order notification:', err.message);
    }
  }

  /** Notify admin about new deposit and allow approve/reject */
  async notifyNewDeposit(deposit: any, userName: string, userEmail: string) {
    if (!this.bot || !this.chatId) return;
    try {
      let msg = `💳 *إيداع جديد #${deposit.depositNumber}*\n\n`;
      msg += `👤 ${userName}\n`;
      msg += `📧 ${userEmail}\n`;
      msg += `💵 المبلغ: $${deposit.amount.toFixed(2)}\n`;
      msg += `🏦 البوابة: ${deposit.gateway === 'BANKAK' ? 'بنكك' : 'USDT'}\n`;

      if (deposit.gateway === 'BANKAK') {
        msg += `💱 المبلغ المحلي: ${deposit.amountLocal?.toFixed(0) || '—'} SDG\n`;
        msg += `📈 سعر الصرف: ${deposit.exchangeRate || '—'}\n`;
      }
      if (deposit.txHash) {
        msg += `🔗 TxHash: \`${deposit.txHash}\`\n`;
      }
      if (deposit.note) {
        msg += `📝 ملاحظة: ${deposit.note}\n`;
      }
      msg += `📅 ${new Date().toLocaleString('ar-EG')}`;

      const keyboard: TelegramBot.InlineKeyboardButton[][] = [];

      // Only show approve/reject for PENDING deposits
      if (deposit.status === 'PENDING') {
        keyboard.push([
          { text: '✅ موافقة', callback_data: `deposit_approve_${deposit.id}` },
          { text: '❌ رفض', callback_data: `deposit_reject_${deposit.id}` },
        ]);
      }

      const opts: TelegramBot.SendMessageOptions = { parse_mode: 'Markdown' };
      if (keyboard.length > 0) opts.reply_markup = { inline_keyboard: keyboard };

      // Send receipt image if bankak
      if (deposit.gateway === 'BANKAK' && deposit.receiptImage) {
        try {
          const imgPath = deposit.receiptImage.startsWith('/') 
            ? require('path').join(__dirname, '..', '..', deposit.receiptImage) 
            : deposit.receiptImage;
          const fs = require('fs');
          if (fs.existsSync(imgPath)) {
            await this.bot.sendPhoto(this.chatId, imgPath, { caption: msg, parse_mode: 'Markdown', reply_markup: keyboard.length > 0 ? { inline_keyboard: keyboard } : undefined });
            return;
          }
        } catch {}
      }

      await this.bot.sendMessage(this.chatId, msg, opts);
    } catch (err: any) {
      console.error('[Telegram] Failed to send deposit notification:', err.message);
    }
  }

  // ============================================
  // Deposit Actions from Telegram
  // ============================================
  private async handleDepositApprove(chatId: string, depositId: string, messageId: number) {
    try {
      const deposit = await prisma.deposit.findUnique({
        where: { id: depositId },
        include: { user: { select: { id: true, name: true, email: true } } },
      });

      if (!deposit) {
        await this.bot!.sendMessage(chatId, '❌ الإيداع غير موجود');
        return;
      }
      if (deposit.status !== 'PENDING') {
        await this.bot!.sendMessage(chatId, `⚠️ الإيداع تمت معالجته مسبقاً (${deposit.status})`);
        return;
      }

      // Approve
      await prisma.deposit.update({
        where: { id: depositId },
        data: { status: 'CONFIRMED', adminNote: 'تمت الموافقة عبر تليجرام' },
      });

      // Add balance
      await orderService.addBalance(deposit.userId, deposit.amount, `${deposit.gateway} Deposit #${deposit.depositNumber} approved`);

      // Update the message
      await this.bot!.editMessageText(
        `✅ *تمت الموافقة على الإيداع #${deposit.depositNumber}*\n\n👤 ${deposit.user?.name}\n💵 $${deposit.amount.toFixed(2)}\n🏦 ${deposit.gateway}\n\n✅ تم شحن الرصيد`,
        { chat_id: chatId, message_id: messageId, parse_mode: 'Markdown' }
      );
    } catch (err: any) {
      await this.bot!.sendMessage(chatId, `❌ فشل: ${err.message}`);
    }
  }

  private async handleDepositReject(chatId: string, depositId: string, messageId: number) {
    try {
      const deposit = await prisma.deposit.findUnique({
        where: { id: depositId },
        include: { user: { select: { id: true, name: true, email: true } } },
      });

      if (!deposit) {
        await this.bot!.sendMessage(chatId, '❌ الإيداع غير موجود');
        return;
      }
      if (deposit.status !== 'PENDING') {
        await this.bot!.sendMessage(chatId, `⚠️ الإيداع تمت معالجته مسبقاً (${deposit.status})`);
        return;
      }

      await prisma.deposit.update({
        where: { id: depositId },
        data: { status: 'REJECTED', adminNote: 'تم الرفض عبر تليجرام' },
      });

      await this.bot!.editMessageText(
        `❌ *تم رفض الإيداع #${deposit.depositNumber}*\n\n👤 ${deposit.user?.name}\n💵 $${deposit.amount.toFixed(2)}\n🏦 ${deposit.gateway}`,
        { chat_id: chatId, message_id: messageId, parse_mode: 'Markdown' }
      );
    } catch (err: any) {
      await this.bot!.sendMessage(chatId, `❌ فشل: ${err.message}`);
    }
  }
}

export const telegramService = new TelegramService();
