import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

cmd = '''cd /opt/on-server1/on-server1-backend && node -e "
const {PrismaClient} = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const o = await p.order.findFirst({
    where: { orderNumber: '100770' },
    include: {
      items: { include: { product: { select: { name: true, serviceType: true, externalId: true, fields: true } } } },
      user: { select: { name: true, email: true } }
    }
  });
  if (o) {
    console.log('ORDER:', JSON.stringify(o, null, 2));
  } else {
    console.log('NOT FOUND');
  }
  await p.\\$disconnect();
})();
"'''

stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
out = stdout.read().decode('utf-8', errors='replace')
err = stderr.read().decode('utf-8', errors='replace')
print(out)
if err.strip():
    print("STDERR:", err)
ssh.close()
