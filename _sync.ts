import { syncService } from './src/services/syncService';
import prisma from './src/config/database';

async function run() {
  const r = await syncService.syncProducts(20);
  console.log('Sync:', JSON.stringify(r));
  
  // Tracfone (no custom fields - should get IMEI)
  const p1 = await prisma.product.findFirst({where:{externalId:'12893'}});
  console.log('Tracfone:', p1?.fields);
  
  // Honor FRP (has SN custom - should NOT get IMEI)  
  const p2 = await prisma.product.findFirst({where:{externalId:'18653'}});
  console.log('Honor FRP:', p2?.fields);
  
  // Count
  const noFields = await prisma.product.count({where:{serviceType:'IMEI', OR:[{fields:null},{fields:'[]'}]}});
  const withFields = await prisma.product.count({where:{serviceType:'IMEI', NOT:[{fields:null},{fields:'[]'}]}});
  console.log('IMEI: with fields=' + withFields + ', no fields=' + noFields);
  
  await prisma.$disconnect();
}
run().catch(e => { console.error(e); process.exit(1); });
