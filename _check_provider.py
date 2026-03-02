import paramiko, json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, _ = ssh.exec_command(cmd, timeout=15)
    return o.read().decode().strip()

# 1. Check .env file on server
print("=== .env Provider Settings ===")
env_vals = run("cd /opt/on-server1/on-server1-backend && grep -i 'EXTERNAL_PROVIDER\\|provider' .env")
print(env_vals if env_vals else "(empty)")

# 2. Check DB settings for provider keys
print("\n=== DB Provider Settings ===")
db_vals = run("""cd /opt/on-server1/on-server1-backend && node -e "
const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const s = await p.setting.findMany({ where: { key: { in: ['provider_url','provider_username','provider_api_key'] } } });
  console.log(JSON.stringify(s, null, 2));
  await p.\\$disconnect();
})();
" """)
print(db_vals if db_vals else "(no provider settings in DB)")

# 3. Try calling the provider to see if it's connected
print("\n=== Test Provider Connection ===")
test = run("""cd /opt/on-server1/on-server1-backend && node -e "
const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const settings = await p.setting.findMany({ where: { key: { in: ['provider_url','provider_username','provider_api_key'] } } });
  const map = {};
  settings.forEach(s => map[s.key] = s.value);
  
  const url = map['provider_url'] || process.env.EXTERNAL_PROVIDER_URL || '';
  const user = map['provider_username'] || process.env.EXTERNAL_PROVIDER_USERNAME || '';
  const key = map['provider_api_key'] || process.env.EXTERNAL_PROVIDER_API_KEY || '';
  
  console.log('URL:', url || '(empty)');
  console.log('Username:', user || '(empty)');
  console.log('API Key:', key ? key.substring(0,8) + '...' : '(empty)');
  
  if (!url || !user || !key) {
    console.log('\\nSTATUS: NOT CONFIGURED - waiting for admin to set provider credentials');
  } else {
    console.log('\\nSTATUS: Credentials set, testing connection...');
    try {
      const params = new URLSearchParams({ Username: user, ApiKey: key, Action: 'GetAccountInfo' });
      const res = await fetch(url, { method: 'POST', body: params });
      const text = await res.text();
      console.log('Response:', text.substring(0, 200));
    } catch(e) {
      console.log('Connection ERROR:', e.message);
    }
  }
  await p.\\$disconnect();
})();
" """)
print(test)

ssh.close()
