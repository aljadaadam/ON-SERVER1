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

  // Create sample products
  const products = await Promise.all([
    // Mobile Recharge
    prisma.product.create({
      data: {
        name: 'Mobile Recharge $10',
        nameAr: 'شحن جوال $10',
        price: 10,
        type: 'SERVICE',
        categoryId: categories[0].id,
        isFeatured: true,
        image: '/images/mobile-recharge.png',
      },
    }),
    prisma.product.create({
      data: {
        name: 'Mobile Recharge $25',
        nameAr: 'شحن جوال $25',
        price: 25,
        type: 'SERVICE',
        categoryId: categories[0].id,
        image: '/images/mobile-recharge.png',
      },
    }),
    // Game Cards
    prisma.product.create({
      data: {
        name: 'PlayStation Gift Card $50',
        nameAr: 'بطاقة بلايستيشن $50',
        price: 50,
        type: 'GAME_CARD',
        categoryId: categories[1].id,
        isFeatured: true,
        image: '/images/ps-card.png',
      },
    }),
    prisma.product.create({
      data: {
        name: 'Xbox Gift Card $25',
        nameAr: 'بطاقة اكسبوكس $25',
        price: 25,
        type: 'GAME_CARD',
        categoryId: categories[1].id,
        image: '/images/xbox-card.png',
      },
    }),
    prisma.product.create({
      data: {
        name: 'Steam Wallet $20',
        nameAr: 'محفظة ستيم $20',
        price: 20,
        type: 'GAME_CARD',
        categoryId: categories[1].id,
        isFeatured: true,
        image: '/images/steam-card.png',
      },
    }),
    // Gift Cards
    prisma.product.create({
      data: {
        name: 'Amazon Gift Card $100',
        nameAr: 'بطاقة أمازون $100',
        price: 100,
        type: 'GIFT_CARD',
        categoryId: categories[2].id,
        isFeatured: true,
        image: '/images/amazon-card.png',
      },
    }),
    prisma.product.create({
      data: {
        name: 'Google Play $15',
        nameAr: 'جوجل بلاي $15',
        price: 15,
        type: 'GIFT_CARD',
        categoryId: categories[2].id,
        image: '/images/google-play.png',
      },
    }),
    // Streaming
    prisma.product.create({
      data: {
        name: 'Netflix 1 Month',
        nameAr: 'نتفلكس شهر واحد',
        price: 15.99,
        type: 'SUBSCRIPTION',
        categoryId: categories[3].id,
        isFeatured: true,
        image: '/images/netflix.png',
      },
    }),
    prisma.product.create({
      data: {
        name: 'Spotify Premium 1 Month',
        nameAr: 'سبوتيفاي بريميوم شهر',
        price: 9.99,
        type: 'SUBSCRIPTION',
        categoryId: categories[3].id,
        image: '/images/spotify.png',
      },
    }),
  ]);
  console.log(`✅ ${products.length} products created`);

  // Create banners
  const banners = await Promise.all([
    prisma.banner.create({
      data: {
        title: 'Welcome to ON-SERVER1',
        image: '/images/banner-1.png',
        link: '/products',
        sortOrder: 1,
      },
    }),
    prisma.banner.create({
      data: {
        title: 'Special Offers',
        image: '/images/banner-2.png',
        link: '/products?featured=true',
        sortOrder: 2,
      },
    }),
  ]);
  console.log(`✅ ${banners.length} banners created`);

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
