const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();

(async () => {
  const cats = await p.category.findMany({ select: { id: true, name: true, nameAr: true } });
  console.log('=== Categories ===');
  cats.forEach(c => console.log(`${c.name} | ${c.nameAr}`));

  const groups = await p.product.findMany({
    where: { isActive: true },
    select: { serviceType: true, groupName: true },
    distinct: ['serviceType', 'groupName']
  });
  console.log('\n=== ServiceType + GroupName ===');
  groups.forEach(g => console.log(`${g.serviceType} | ${g.groupName}`));

  const counts = await p.product.groupBy({
    by: ['serviceType'],
    _count: true,
    where: { isActive: true }
  });
  console.log('\n=== Counts by ServiceType ===');
  counts.forEach(c => console.log(`${c.serviceType}: ${c._count}`));

  await p.$disconnect();
})();
