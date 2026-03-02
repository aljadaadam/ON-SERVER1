import paramiko, json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, e = ssh.exec_command(cmd, timeout=30)
    out = o.read().decode().strip()
    err = e.read().decode().strip()
    if out: print(out)
    if err and 'TERM' not in err: print(f"ERR: {err}")
    return out

# 1. Check PM2 logs for errors
print("=== PM2 Error Logs (last 50 lines) ===")
run("pm2 logs on-server1-backend --err --lines 50 --nostream")

print("\n=== PM2 Output Logs (last 30 lines) ===")
run("pm2 logs on-server1-backend --out --lines 30 --nostream")

# 2. Check provider settings in DB
print("\n=== Provider Settings in DB ===")
run("""cd /opt/on-server1/on-server1-backend && node -e "
const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const s = await p.setting.findMany({ where: { key: { in: ['provider_url','provider_username','provider_api_key'] } } });
  s.forEach(x => console.log(x.key + ' = ' + (x.key === 'provider_api_key' ? x.value.substring(0,8)+'...' : x.value)));
  if (s.length === 0) console.log('(no provider settings found)');
  await p.\\$disconnect();
})();
" """)

# 3. Test sync manually
print("\n=== Test Sync API ===")
login = run("""curl -s -X POST https://on-server2.com/api/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@onserver1.com","password":"admin123456"}'""")
try:
    token = json.loads(login)['data']['accessToken']
    sync = run(f"""curl -s -X POST https://on-server2.com/api/admin/provider/sync -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' -d '{{"markupPercent":0}}'""")
    print(f"Sync response: {sync[:500]}")
except Exception as e:
    print(f"Error: {e}")

ssh.close()
