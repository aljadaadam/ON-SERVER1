import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Check order 100140 in DB + call provider API for live status
cmd = """cd /opt/on-server1/on-server1-backend && node -e '
const { PrismaClient } = require("@prisma/client");
const p = new PrismaClient();
(async () => {
  // 1. DB status
  const order = await p.order.findFirst({
    where: { orderNumber: "100140" },
    include: { items: { include: { product: true } }, user: { select: { name: true, email: true } } }
  });
  if (!order) { console.log("Order 100140 NOT FOUND"); return; }
  console.log("=== DB Status ===");
  console.log("Order:", order.orderNumber);
  console.log("Status:", order.status);
  console.log("ExternalOrderId:", order.externalOrderId);
  console.log("ResultCodes:", order.resultCodes || "none");
  console.log("ResponseData:", order.responseData ? order.responseData.substring(0,500) : "none");
  console.log("User:", order.user?.name, order.user?.email);
  console.log("Items:", order.items.map(i => i.product?.name).join(", "));
  console.log("Created:", order.createdAt);
  console.log("");

  // 2. Live provider status
  if (order.externalOrderId) {
    const settings = await p.setting.findMany({ where: { key: { in: ["provider_url", "provider_username", "provider_api_key"] } } });
    const cfg = {};
    settings.forEach(s => cfg[s.key] = s.value);
    const params = new URLSearchParams();
    params.append("username", cfg.provider_username || "");
    params.append("apiaccesskey", cfg.provider_api_key || "");
    params.append("requestformat", "JSON");
    params.append("action", "getimeiorder");
    params.append("parameters", "<PARAMETERS><ID>" + order.externalOrderId + "</ID></PARAMETERS>");
    const resp = await fetch(cfg.provider_url, { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded" }, body: params.toString() });
    const data = await resp.json();
    console.log("=== Provider Live Response ===");
    console.log(JSON.stringify(data, null, 2));
  }
  await p.$disconnect();
})();
'"""

stdin, stdout, stderr = ssh.exec_command(cmd)
print(stdout.read().decode())
err = stderr.read().decode()
if err and 'TERM' not in err:
    print("STDERR:", err[:500])

# Also check recent cron logs
cmd2 = "pm2 logs on-server1-backend --lines 20 --nostream 2>&1"
stdin, stdout, stderr = ssh.exec_command(cmd2)
output = stdout.read().decode()
print("\n=== Recent Logs (100140) ===")
for line in output.split('\n'):
    if '100140' in line or '28582' in line:
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
    if 'cron' in l or 'sd-unlocker' in l or 'orderstatus' in l or 'status' in l or 'rawstatus' in l or 'mapped' in l or 'completed' in l or 'rejected' in l or 'error' in l:
        print(line.strip())

ssh.close()

