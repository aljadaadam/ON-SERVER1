import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

# Upload seed script
sftp = ssh.open_sftp()
with sftp.file('/opt/on-server1/on-server1-backend/_seed_gw.js', 'w') as f:
    f.write("""
const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const c = await p.paymentGateway.count();
  if (c === 0) {
    await p.paymentGateway.createMany({
      data: [
        { name: 'USDT', nameEn: 'USDT', type: 'CRYPTO', icon: 'CurrencyBitcoin', color: '#26A17B', sortOrder: 1, isActive: true },
        { name: '\\u0628\\u0646\\u0643\\u0643', nameEn: 'Bankak', type: 'BANK', icon: 'AccountBalance', color: '#E52228', sortOrder: 2, isActive: true },
      ],
    });
    console.log('Seeded 2 gateways');
  } else {
    console.log('Gateways already exist:', c);
  }
  await p.$disconnect();
})();
""")
sftp.close()

stdin, stdout, stderr = ssh.exec_command(
    'cd /opt/on-server1/on-server1-backend && npx prisma generate && npx prisma db push --accept-data-loss && node _seed_gw.js && rm _seed_gw.js',
    timeout=60
)
print('OUT:', stdout.read().decode())
err = stderr.read().decode()
if err.strip():
    print('ERR:', err)

ssh.close()
print('Done!')
