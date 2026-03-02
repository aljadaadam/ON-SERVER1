import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Check orders with external IDs using node
cmd = """cd /opt/on-server1/on-server1-backend && node -e '
const { PrismaClient } = require("@prisma/client");
const p = new PrismaClient();
(async () => {
  const orders = await p.order.findMany({
    where: { externalOrderId: { not: null } },
    select: { id: true, orderNumber: true, status: true, externalOrderId: true, responseData: true, resultCodes: true },
    orderBy: { createdAt: "desc" },
    take: 20
  });
  orders.forEach(o => {
    console.log(o.orderNumber + " | " + o.status + " | extId=" + o.externalOrderId + " | codes=" + (o.resultCodes||"none") + " | resp=" + (o.responseData ? o.responseData.substring(0,300) : "none"));
  });
  await p.$disconnect();
})();
'"""
stdin, stdout, stderr = ssh.exec_command(cmd)
print("=== Orders ===")
print(stdout.read().decode())
err = stderr.read().decode()
if err:
    print("STDERR:", err[:500])

# Get last cron logs
cmd2 = "pm2 logs on-server1-backend --lines 100 --nostream 2>&1"
stdin, stdout, stderr = ssh.exec_command(cmd2)
output = stdout.read().decode()
print("\n=== Cron Logs ===")
for line in output.split('\n'):
    l = line.lower()
    if 'cron' in l or 'checking' in l or 'completed' in l or 'rejected' in l or 'pending' in l or 'processing' in l or 'status' in l:
        print(line.strip())

ssh.close()

