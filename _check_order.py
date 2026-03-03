import paramiko, json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Check PM2 logs for order placement details and the raw DHRU response
cmd = "pm2 logs on-server1-backend --nostream --lines 500 2>&1 | grep -i '100700\\|Custom fields\\|Placing order.*20912\\|placeimeiorder\\|IMEI'"
stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
print("=== LOGS ===")
print(stdout.read().decode())

# Also check what the DHRU service 20912 actually looks like in sync data
cmd2 = """cd /opt/on-server1/on-server1-backend && node -e "
const{PrismaClient}=require('@prisma/client');
const p=new PrismaClient();
(async()=>{
  const prod=await p.product.findFirst({where:{externalId:'20912'}});
  if(prod){
    console.log('Product:', prod.name);
    console.log('Fields:', prod.fields);
    console.log('ServiceType:', prod.serviceType);
  }
  await p['\\$disconnect']();
})();
"
"""
stdin2, stdout2, stderr2 = ssh.exec_command(cmd2, timeout=15)
print("\n=== PRODUCT 20912 ===")
print(stdout2.read().decode())

ssh.close()
