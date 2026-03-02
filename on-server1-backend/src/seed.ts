import bcrypt from 'bcryptjs';
import prisma from './config/database';
import { env } from './config/env';

async function seed() {
  console.log('🌱 Seeding database...');

  // Create admin user
  const adminPassword = await bcrypt.hash(env.ADMIN_PASSWORD, 12);
  const admin = await prisma.user.upsert({
    where: { email: env.ADMIN_EMAIL },
    update: {},
    create: {
      email: env.ADMIN_EMAIL,
      password: adminPassword,
      name: 'Admin',
      role: 'ADMIN',
      isVerified: true,
      balance: 0,
    },
  });
  console.log(`✅ Admin user created: ${admin.email}`);

  // Create categories
  const categories = await Promise.all([
    prisma.category.create({
      data: { name: 'Mobile Recharge', nameAr: 'شحن الجوال', icon: '📱', sortOrder: 1 },
    }),
    prisma.category.create({
      data: { name: 'Game Cards', nameAr: 'بطاقات الألعاب', icon: '🎮', sortOrder: 2 },
    }),
    prisma.category.create({
      data: { name: 'Gift Cards', nameAr: 'بطاقات الهدايا', icon: '🎁', sortOrder: 3 },
    }),
    prisma.category.create({
      data: { name: 'Streaming', nameAr: 'البث المباشر', icon: '📺', sortOrder: 4 },
    }),
    prisma.category.create({
      data: { name: 'Software', nameAr: 'البرمجيات', icon: '💻', sortOrder: 5 },
    }),
  ]);
  console.log(`✅ ${categories.length} categories created`);

  // Create default settings
  const defaultSettings = [
    { key: 'site_name', value: 'ON-SERVER1' },
    { key: 'site_description', value: 'منصة الخدمات الرقمية المتكاملة' },
    { key: 'support_email', value: 'support@onserver1.com' },
    { key: 'support_phone', value: '+966500000000' },
    { key: 'whatsapp_number', value: '+966500000000' },
    { key: 'telegram_link', value: 'https://t.me/onserver1' },
    { key: 'currency', value: 'SAR' },
    { key: 'min_deposit', value: '10' },
    { key: 'max_deposit', value: '5000' },
    { key: 'maintenance_mode', value: 'false' },
    { key: 'announcement', value: '' },
    { key: 'terms_url', value: '' },
    { key: 'privacy_url', value: '' },
    { key: 'usdt_wallet_address', value: '0x0000000000000000000000000000000000000000' },
    { key: 'usdt_network', value: 'BEP20 (BSC)' },
    { key: 'usdt_min_amount', value: '5' },
    { key: 'usdt_max_amount', value: '10000' },
    { key: 'bankak_account_name', value: 'ON-SERVER1' },
    { key: 'bankak_account_number', value: '1234567890' },
    { key: 'bankak_bank_name', value: 'بنكك' },
    { key: 'bankak_exchange_rate', value: '600' },
    { key: 'bankak_min_amount', value: '5' },
    { key: 'bankak_max_amount', value: '5000' },
    { key: 'privacy_policy_text', value: 'سياسة الخصوصية لمنصة ON-SERVER1\n\nنحن نحترم خصوصيتك ونلتزم بحماية بياناتك الشخصية. هذه السياسة توضح كيفية جمع واستخدام وحماية معلوماتك.\n\n1. جمع المعلومات\nنقوم بجمع المعلومات التي تقدمها لنا مباشرة مثل الاسم والبريد الإلكتروني ورقم الهاتف.\n\n2. استخدام المعلومات\nنستخدم معلوماتك لتقديم خدماتنا وتحسينها والتواصل معك.\n\n3. حماية المعلومات\nنتخذ إجراءات أمنية مناسبة لحماية بياناتك من الوصول غير المصرح به.' },
    { key: 'terms_of_service_text', value: 'شروط الخدمة لمنصة ON-SERVER1\n\n1. القبول\nباستخدامك لمنصة ON-SERVER1، فإنك توافق على هذه الشروط والأحكام.\n\n2. الحسابات\nأنت مسؤول عن الحفاظ على سرية حسابك وكلمة المرور الخاصة بك.\n\n3. المشتريات\nجميع المشتريات نهائية ما لم ينص على خلاف ذلك. يتم خصم المبالغ من رصيد حسابك.\n\n4. الاسترداد\nيمكن طلب استرداد المبلغ خلال 24 ساعة من الشراء للمنتجات المؤهلة.\n\n5. إخلاء المسؤولية\nنقدم خدماتنا كما هي دون أي ضمانات.' },
  ];

  for (const setting of defaultSettings) {
    await prisma.setting.upsert({
      where: { key: setting.key },
      update: {},
      create: setting,
    });
  }
  console.log(`✅ ${defaultSettings.length} settings created`);

  // Initialize deposit counter
  await prisma.depositCounter.upsert({
    where: { id: 'deposit_counter' },
    update: {},
    create: { id: 'deposit_counter', counter: 1000 },
  });
  console.log('✅ Deposit counter initialized at 1000');

  console.log('🎉 Seeding completed!');
}

seed()
  .catch(e => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
