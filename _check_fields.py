import paramiko, json
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Get a sample IMEI product's fields to see what the field key looks like
cmd = """cd /opt/on-server1/on-server1-backend && node -e "
const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const prod = await p.product.findFirst({ where: { name: { contains: 'Check Mi account' } } });
  if (prod) {
    console.log('Product:', prod.name);
    console.log('ServiceType:', prod.serviceType);
    console.log('Fields:', prod.fields);
  } else {
    // Get any IMEI product
    const prod2 = await p.product.findFirst({ where: { serviceType: 'IMEI' } });
    if (prod2) {
      console.log('Product:', prod2.name);
      console.log('ServiceType:', prod2.serviceType);
      console.log('Fields:', prod2.fields);
    }
  }
  await p.\$disconnect();
})();
"
"""
stdin,stdout,stderr = ssh.exec_command(cmd)
print(stdout.read().decode())
err = stderr.read().decode()
if err: print('ERR:', err[:500])

ssh.close()
