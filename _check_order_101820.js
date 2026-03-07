const { PrismaClient } = require("@prisma/client");
const p = new PrismaClient();
(async () => {
  const order = await p.order.findFirst({
    where: { orderNumber: "101820" },
    include: { items: { include: { product: true } } }
  });
  console.log("=== ORDER ===");
  console.log(JSON.stringify(order, null, 2));
  await p.$disconnect();
})();
