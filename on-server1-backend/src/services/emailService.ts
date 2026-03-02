import nodemailer from 'nodemailer';
import prisma from '../config/database';

// ════════════════════════════════════════════════════════════
//  Email Service — Central email sender with branded templates
// ════════════════════════════════════════════════════════════

interface SmtpConfig {
  host: string;
  port: number;
  secure: boolean;
  user: string;
  pass: string;
  fromEmail: string;
  fromName: string;
}

/**
 * Load SMTP settings from DB (Setting table)
 */
async function getSmtpConfig(): Promise<SmtpConfig | null> {
  const rows = await prisma.setting.findMany({
    where: { key: { in: ['smtp_host', 'smtp_port', 'smtp_user', 'smtp_pass', 'smtp_from_email', 'smtp_from_name', 'smtp_secure'] } },
  });
  const m: Record<string, string> = {};
  rows.forEach(r => { m[r.key] = r.value; });

  if (!m.smtp_host || !m.smtp_user || !m.smtp_pass) return null;

  return {
    host: m.smtp_host,
    port: parseInt(m.smtp_port || '587'),
    secure: m.smtp_secure === 'true',
    user: m.smtp_user,
    pass: m.smtp_pass,
    fromEmail: m.smtp_from_email || m.smtp_user,
    fromName: m.smtp_from_name || 'ON-SERVER1',
  };
}

/**
 * Send an email (fire-and-forget safe — never throws to caller)
 */
async function sendEmail(to: string, subject: string, html: string): Promise<boolean> {
  try {
    const cfg = await getSmtpConfig();
    if (!cfg) {
      console.warn('[Email] SMTP not configured — skipping email');
      return false;
    }

    const transporter = nodemailer.createTransport({
      host: cfg.host,
      port: cfg.port,
      secure: cfg.secure,
      auth: { user: cfg.user, pass: cfg.pass },
    });

    await transporter.sendMail({
      from: `"${cfg.fromName}" <${cfg.fromEmail}>`,
      to,
      subject,
      html,
    });

    console.log(`[Email] Sent "${subject}" → ${to}`);
    return true;
  } catch (err: any) {
    console.error(`[Email] Failed "${subject}" → ${to}:`, err.message);
    return false;
  }
}

// ════════════════════════════════════════════════════════════
//  Base Layout Wrapper
// ════════════════════════════════════════════════════════════

function wrap(body: string): string {
  return `
<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
<body style="margin:0;padding:0;background:#0F0F1A;font-family:'Segoe UI',Tahoma,Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#0F0F1A;padding:30px 10px;">
    <tr><td align="center">
      <table width="520" cellpadding="0" cellspacing="0" style="background:#1A1A2E;border-radius:16px;overflow:hidden;border:1px solid #2A2A4A;">
        <!-- Header -->
        <tr>
          <td style="background:linear-gradient(135deg,#1E3A5F 0%,#0F0F1A 100%);padding:28px 30px;text-align:center;">
            <div style="display:inline-block;background:#FFD700;color:#000;font-weight:800;padding:8px 24px;border-radius:10px;font-size:20px;letter-spacing:1px;">ON-SERVER1</div>
          </td>
        </tr>
        <!-- Body -->
        <tr>
          <td style="padding:30px 30px 20px;">
            ${body}
          </td>
        </tr>
        <!-- Footer -->
        <tr>
          <td style="padding:16px 30px 24px;border-top:1px solid #2A2A4A;text-align:center;">
            <p style="margin:0;color:#666;font-size:11px;">© ${new Date().getFullYear()} ON-SERVER1 — هذا بريد تلقائي، لا ترد عليه.</p>
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
</body>
</html>`;
}

// Helpers
const badge = (text: string, bg: string, color: string = '#fff') =>
  `<span style="display:inline-block;background:${bg};color:${color};padding:5px 16px;border-radius:8px;font-weight:700;font-size:14px;">${text}</span>`;

const infoRow = (label: string, value: string) =>
  `<tr><td style="padding:6px 12px;color:#999;font-size:13px;white-space:nowrap;">${label}</td><td style="padding:6px 12px;color:#E0E0E0;font-size:13px;font-weight:600;">${value}</td></tr>`;

const infoTable = (rows: string) =>
  `<table width="100%" cellpadding="0" cellspacing="0" style="background:#141425;border-radius:10px;margin:16px 0;">${rows}</table>`;

const divider = `<hr style="border:none;border-top:1px solid #2A2A4A;margin:20px 0;">`;

// ════════════════════════════════════════════════════════════
//  1. Welcome — تسجيل حساب جديد
// ════════════════════════════════════════════════════════════

export async function sendWelcomeEmail(to: string, name: string) {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#4CAF50,#2E7D32);margin-bottom:8px;">
        <span style="font-size:32px;">🎉</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">مرحباً بك، ${name}!</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">تم إنشاء حسابك بنجاح في ON-SERVER1</p>
    </div>
    ${divider}
    <p style="color:#ccc;font-size:14px;line-height:1.7;text-align:center;">
      يمكنك الآن تصفح الخدمات وتقديم طلباتك.<br/>
      قم بشحن رصيدك للبدء في استخدام المنصة.
    </p>
    <div style="text-align:center;margin-top:20px;">
      ${badge('حسابك جاهز ✓', '#4CAF50')}
    </div>
  `);
  return sendEmail(to, '🎉 مرحباً بك في ON-SERVER1', html);
}

// ════════════════════════════════════════════════════════════
//  2. OTP — رمز التحقق
// ════════════════════════════════════════════════════════════

export async function sendOtpEmail(to: string, name: string, code: string, type: 'EMAIL_VERIFICATION' | 'PHONE_VERIFICATION' | 'PASSWORD_RESET') {
  const typeLabel = type === 'PASSWORD_RESET' ? 'إعادة تعيين كلمة المرور' : 'تأكيد البريد الإلكتروني';
  const icon = type === 'PASSWORD_RESET' ? '🔐' : '✉️';

  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#FF9800,#F57C00);margin-bottom:8px;">
        <span style="font-size:32px;">${icon}</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">${typeLabel}</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    <p style="color:#ccc;font-size:14px;text-align:center;">استخدم الرمز التالي:</p>
    <div style="text-align:center;margin:20px 0;">
      <div style="display:inline-block;background:#141425;border:2px solid #FFD700;border-radius:12px;padding:16px 40px;letter-spacing:12px;font-size:32px;font-weight:800;color:#FFD700;">${code}</div>
    </div>
    <p style="color:#999;font-size:12px;text-align:center;">الرمز صالح لمدة 10 دقائق فقط. لا تشاركه مع أي شخص.</p>
  `);
  return sendEmail(to, `${icon} ${typeLabel} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  3. إعادة تعيين كلمة المرور بنجاح
// ════════════════════════════════════════════════════════════

export async function sendPasswordResetSuccessEmail(to: string, name: string) {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#4CAF50,#2E7D32);margin-bottom:8px;">
        <span style="font-size:32px;">🔒</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">تم تغيير كلمة المرور</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    <p style="color:#ccc;font-size:14px;line-height:1.7;text-align:center;">
      تم إعادة تعيين كلمة المرور الخاصة بك بنجاح.<br/>
      إذا لم تقم بهذا الإجراء، يرجى التواصل معنا فوراً.
    </p>
    <div style="text-align:center;margin-top:20px;">
      ${badge('تم التحديث ✓', '#4CAF50')}
    </div>
  `);
  return sendEmail(to, '🔒 تم تغيير كلمة المرور — ON-SERVER1', html);
}

// ════════════════════════════════════════════════════════════
//  4. إيداع جديد (New Deposit)
// ════════════════════════════════════════════════════════════

export async function sendDepositCreatedEmail(
  to: string, name: string,
  data: { depositNumber: number; amount: number; gateway: string; status: string; txHash?: string }
) {
  const gatewayLabel = data.gateway === 'USDT' ? 'USDT (BEP20)' : 'بنكك';
  const statusLabel = data.status === 'CONFIRMED' ? 'مؤكد' : 'قيد المراجعة';
  const statusColor = data.status === 'CONFIRMED' ? '#4CAF50' : '#FF9800';

  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#2196F3,#1565C0);margin-bottom:8px;">
        <span style="font-size:32px;">💰</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">إيداع جديد</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    ${infoTable(
      infoRow('رقم الإيداع', `#${data.depositNumber}`) +
      infoRow('المبلغ', `$${data.amount.toFixed(2)}`) +
      infoRow('طريقة الدفع', gatewayLabel) +
      infoRow('الحالة', `<span style="color:${statusColor};font-weight:700;">${statusLabel}</span>`) +
      (data.txHash ? infoRow('Hash', `<span style="font-size:11px;word-break:break-all;">${data.txHash}</span>`) : '')
    )}
    <p style="color:#999;font-size:12px;text-align:center;margin-top:16px;">
      ${data.status === 'CONFIRMED' ? 'تم إضافة الرصيد إلى حسابك تلقائياً.' : 'سيتم مراجعة الإيداع وتأكيده من قبل الإدارة.'}
    </p>
  `);
  return sendEmail(to, `💰 إيداع جديد #${data.depositNumber} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  5. تأكيد الإيداع (Deposit Approved)
// ════════════════════════════════════════════════════════════

export async function sendDepositApprovedEmail(
  to: string, name: string,
  data: { depositNumber: number; amount: number; gateway: string; adminNote?: string }
) {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#4CAF50,#2E7D32);margin-bottom:8px;">
        <span style="font-size:32px;">✅</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">تم تأكيد الإيداع</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    ${infoTable(
      infoRow('رقم الإيداع', `#${data.depositNumber}`) +
      infoRow('المبلغ', `$${data.amount.toFixed(2)}`) +
      infoRow('طريقة الدفع', data.gateway === 'USDT' ? 'USDT (BEP20)' : 'بنكك') +
      infoRow('الحالة', '<span style="color:#4CAF50;font-weight:700;">مؤكد ✓</span>') +
      (data.adminNote ? infoRow('ملاحظة', data.adminNote) : '')
    )}
    <p style="color:#ccc;font-size:14px;text-align:center;margin-top:16px;">
      تم إضافة <strong style="color:#FFD700;">$${data.amount.toFixed(2)}</strong> إلى رصيدك.
    </p>
  `);
  return sendEmail(to, `✅ تم تأكيد إيداعك #${data.depositNumber} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  6. رفض الإيداع (Deposit Rejected)
// ════════════════════════════════════════════════════════════

export async function sendDepositRejectedEmail(
  to: string, name: string,
  data: { depositNumber: number; amount: number; gateway: string; adminNote?: string }
) {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#F44336,#C62828);margin-bottom:8px;">
        <span style="font-size:32px;">❌</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">تم رفض الإيداع</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    ${infoTable(
      infoRow('رقم الإيداع', `#${data.depositNumber}`) +
      infoRow('المبلغ', `$${data.amount.toFixed(2)}`) +
      infoRow('طريقة الدفع', data.gateway === 'USDT' ? 'USDT (BEP20)' : 'بنكك') +
      infoRow('الحالة', '<span style="color:#F44336;font-weight:700;">مرفوض ✗</span>') +
      (data.adminNote ? infoRow('السبب', data.adminNote) : '')
    )}
    <p style="color:#999;font-size:12px;text-align:center;margin-top:16px;">
      إذا كان لديك استفسار، يرجى التواصل مع الدعم.
    </p>
  `);
  return sendEmail(to, `❌ تم رفض إيداعك #${data.depositNumber} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  7. طلب جديد (Order Created)
// ════════════════════════════════════════════════════════════

export async function sendOrderCreatedEmail(
  to: string, name: string,
  data: { orderNumber: string; totalAmount: number; productNames: string[] }
) {
  const productsList = data.productNames.map(p => `<li style="color:#E0E0E0;padding:4px 0;font-size:13px;">${p}</li>`).join('');

  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#9C27B0,#6A1B9A);margin-bottom:8px;">
        <span style="font-size:32px;">📦</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">تم استلام طلبك</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    ${infoTable(
      infoRow('رقم الطلب', `#${data.orderNumber}`) +
      infoRow('المبلغ الإجمالي', `$${data.totalAmount.toFixed(2)}`) +
      infoRow('الحالة', `${badge('قيد المعالجة', '#FF9800')}`)
    )}
    <div style="background:#141425;border-radius:10px;padding:12px 16px;margin:12px 0;">
      <p style="color:#999;font-size:12px;margin:0 0 8px;">المنتجات:</p>
      <ul style="margin:0;padding-right:16px;list-style:none;">${productsList}</ul>
    </div>
    <p style="color:#999;font-size:12px;text-align:center;margin-top:16px;">
      سيتم تحديثك عند اكتمال الطلب أو تغييره.
    </p>
  `);
  return sendEmail(to, `📦 طلب جديد #${data.orderNumber} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  8. طلب مكتمل (Order Completed)
// ════════════════════════════════════════════════════════════

export async function sendOrderCompletedEmail(
  to: string, name: string,
  data: { orderNumber: string; totalAmount: number; resultCodes?: string | null }
) {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#4CAF50,#2E7D32);margin-bottom:8px;">
        <span style="font-size:32px;">🎉</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">اكتمل طلبك!</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    ${infoTable(
      infoRow('رقم الطلب', `#${data.orderNumber}`) +
      infoRow('المبلغ', `$${data.totalAmount.toFixed(2)}`) +
      infoRow('الحالة', '<span style="color:#4CAF50;font-weight:700;">مكتمل ✓</span>')
    )}
    ${data.resultCodes ? `
    <div style="background:#141425;border-radius:10px;padding:14px 16px;margin:12px 0;">
      <p style="color:#999;font-size:12px;margin:0 0 8px;">النتيجة / الأكواد:</p>
      <p style="color:#FFD700;font-size:13px;word-break:break-all;margin:0;font-family:monospace;">${data.resultCodes}</p>
    </div>` : ''}
    <p style="color:#ccc;font-size:14px;text-align:center;margin-top:16px;">
      شكراً لاستخدامك ON-SERVER1! 🙏
    </p>
  `);
  return sendEmail(to, `🎉 اكتمل طلبك #${data.orderNumber} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  9. طلب مرفوض + استرجاع (Order Rejected + Refund)
// ════════════════════════════════════════════════════════════

export async function sendOrderRejectedEmail(
  to: string, name: string,
  data: { orderNumber: string; totalAmount: number; reason: string }
) {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#F44336,#C62828);margin-bottom:8px;">
        <span style="font-size:32px;">🔄</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">طلب مرفوض — تم الاسترجاع</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">مرحباً ${name}</p>
    </div>
    ${divider}
    ${infoTable(
      infoRow('رقم الطلب', `#${data.orderNumber}`) +
      infoRow('المبلغ', `$${data.totalAmount.toFixed(2)}`) +
      infoRow('الحالة', '<span style="color:#F44336;font-weight:700;">مرفوض ✗</span>') +
      infoRow('السبب', data.reason) +
      infoRow('الاسترجاع', `<span style="color:#4CAF50;font-weight:700;">$${data.totalAmount.toFixed(2)} تم إعادته ✓</span>`)
    )}
    <p style="color:#ccc;font-size:14px;text-align:center;margin-top:16px;">
      تم إعادة المبلغ إلى رصيدك تلقائياً.
    </p>
  `);
  return sendEmail(to, `🔄 طلب مرفوض + استرجاع #${data.orderNumber} — ON-SERVER1`, html);
}

// ════════════════════════════════════════════════════════════
//  Test Email (used by admin dashboard)
// ════════════════════════════════════════════════════════════

export async function sendTestEmail(to: string): Promise<{ success: boolean; message: string }> {
  const html = wrap(`
    <div style="text-align:center;margin-bottom:16px;">
      <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background:linear-gradient(135deg,#4CAF50,#2E7D32);margin-bottom:8px;">
        <span style="font-size:32px;">✅</span>
      </div>
      <h2 style="color:#fff;margin:8px 0 4px;font-size:22px;">تم الاتصال بنجاح!</h2>
      <p style="color:#aaa;margin:0;font-size:14px;">إعدادات SMTP تعمل بشكل صحيح</p>
    </div>
    ${divider}
    <p style="color:#ccc;font-size:14px;text-align:center;">هذا بريد اختبار تلقائي من لوحة التحكم.</p>
    <p style="color:#888;font-size:12px;text-align:center;">${new Date().toLocaleString('ar-SA')}</p>
  `);

  try {
    const cfg = await getSmtpConfig();
    if (!cfg) {
      return { success: false, message: 'إعدادات SMTP غير مكتملة. الرجاء حفظ الإعدادات أولاً' };
    }

    const transporter = nodemailer.createTransport({
      host: cfg.host, port: cfg.port, secure: cfg.secure,
      auth: { user: cfg.user, pass: cfg.pass },
    });

    await transporter.sendMail({
      from: `"${cfg.fromName}" <${cfg.fromEmail}>`,
      to,
      subject: '✅ اختبار اتصال البريد — ON-SERVER1',
      html,
    });

    return { success: true, message: 'تم إرسال بريد الاختبار بنجاح' };
  } catch (err: any) {
    return { success: false, message: err.message || 'فشل إرسال البريد' };
  }
}
