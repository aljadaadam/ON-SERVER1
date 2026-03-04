const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();

(async () => {
  const c = await p.paymentGateway.count();
  if (c === 0) {
    await p.paymentGateway.createMany({
      data: [
        {
          name: 'USDT',
          nameEn: 'USDT',
          type: 'CRYPTO',
          icon: 'CurrencyBitcoin',
          color: '#26A17B',
          sortOrder: 1,
          isActive: true,
        },
        {
          name: '\u0628\u0646\u0643\u0643',
          nameEn: 'Bankak',
          type: 'BANK',
          icon: 'AccountBalance',
          color: '#E52228',
          sortOrder: 2,
          isActive: true,
        },
      ],
    });
    console.log('Seeded 2 gateways');
  } else {
    console.log('Gateways already exist:', c);
  }
  await p.$disconnect();
})();
