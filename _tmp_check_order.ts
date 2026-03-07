import { PrismaClient } from '@prisma/client';
const p = new PrismaClient();

async function main() {
  const o = await p.order.findFirst({
    where: { orderNumber: '102030' },
    include: { items: { include: { product: true } } }
  });
  if (o) {
    console.log('Order:', o.id);
    console.log('Status:', o.status);
    console.log('ExtRef:', o.externalOrderId);
    console.log('ResponseData:', o.responseData);
    console.log('ResultCodes:', o.resultCodes);
    for (const i of o.items) {
      console.log('---');
      console.log('IMEI stored:', i.imei);
      console.log('Metadata:', i.metadata);
      console.log('Product name:', i.product?.name);
      console.log('Product fields:', i.product?.fields);
      console.log('Product serviceType:', i.product?.serviceType);
      console.log('Product externalId:', i.product?.externalId);
    }
  } else {
    console.log('Not found');
  }
  await p.$disconnect();
}
main();
