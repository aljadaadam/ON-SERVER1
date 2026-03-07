import { PrismaClient } from '@prisma/client';
const p = new PrismaClient();

async function main() {
  // Get order 101820 with all details
  const o = await p.order.findFirst({
    where: { orderNumber: '101820' },
    include: { items: { include: { product: true } } }
  });
  if (o) {
    console.log('Order:', o.id);
    console.log('Status:', o.status);
    console.log('ExtRef:', o.externalOrderId);
    console.log('ResponseData:', o.responseData);
    console.log('ResultCodes:', o.resultCodes);
    console.log('Notes:', o.notes);
    for (const i of o.items) {
      console.log('---');
      console.log('IMEI stored:', i.imei);
      console.log('Metadata:', i.metadata);
      console.log('Product fields:', i.product?.fields);
      console.log('Product serviceType:', i.product?.serviceType);
    }
  }
  await p.$disconnect();
}
main();
