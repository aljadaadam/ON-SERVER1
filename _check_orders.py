import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Fix order 100140 back to PENDING + check status
cmd = """cd /opt/on-server1/on-server1-backend && node -e '
const { PrismaClient } = require("@prisma/client");
const p = new PrismaClient();
(async () => {
  // Revert 100140 to PENDING since it was wrongly set to PROCESSING
  await p.order.updateMany({ where: { orderNumber: "100140", status: "PROCESSING" }, data: { status: "PENDING" } });
  const order = await p.order.findFirst({ where: { orderNumber: "100140" }, select: { orderNumber: true, status: true, externalOrderId: true } });
  console.log("Order 100140 fixed:", order.status);
  await p.$disconnect();
})();
'"""
stdin, stdout, stderr = ssh.exec_command(cmd)
print(stdout.read().decode())

# Wait for cron to run and check logs
import time
time.sleep(10)

cmd2 = "pm2 logs on-server1-backend --lines 15 --nostream 2>&1"
stdin2, stdout2, stderr2 = ssh.exec_command(cmd2)
output = stdout2.read().decode()
print("=== Recent Logs ===")
for line in output.split('\n'):
    if '100140' in line or '28582' in line or 'mapped' in line.lower():
        print(line.strip())

ssh.close()
stdin, stdout, stderr = ssh.exec_command(cmd)
print("=== Orders ===")
print(stdout.read().decode())
err = stderr.read().decode()
if err:
    print("STDERR:", err[:500])

# Get last cron logs
cmd2 = "pm2 logs on-server1-backend --lines 30 --nostream 2>&1"
stdin, stdout, stderr = ssh.exec_command(cmd2)
output = stdout.read().decode()
print("\n=== Recent Logs ===")
for line in output.split('\n'):
    l = line.lower()
    if 'cron' in l or 'dhru fusion' in l or 'dhru' in l or 'orderstatus' in l or 'status' in l or 'rawstatus' in l or 'mapped' in l or 'completed' in l or 'rejected' in l or 'error' in l:
        print(line.strip())

ssh.close()

