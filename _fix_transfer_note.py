import paramiko, json

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

def ssh_exec(cmd):
    print(f">>> {cmd}")
    _, stdout, stderr = ssh.exec_command(cmd, timeout=30)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    if out: print(out)
    if err: print(f"STDERR: {err}")
    return out

# Insert bankak_transfer_note setting if missing
print("=== Insert bankak_transfer_note ===")
ssh_exec("""cd /opt/on-server1/on-server1-backend && node -e "
const { PrismaClient } = require('@prisma/client');
const p = new PrismaClient();
(async () => {
  const existing = await p.setting.findUnique({ where: { key: 'bankak_transfer_note' } });
  if (existing) {
    console.log('Already exists:', JSON.stringify(existing));
  } else {
    const created = await p.setting.create({ data: { key: 'bankak_transfer_note', value: 'شحن رصيد محفظة' } });
    console.log('Created:', JSON.stringify(created));
  }
  await p.\\$disconnect();
})();
" """)

# Verify gateway-info now returns transferNote
print("\n=== Test gateway-info ===")
# Login first
login_result = ssh_exec("""curl -s -X POST https://on-server2.com/api/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@onserver1.com","password":"admin123456"}'""")
try:
    token = json.loads(login_result)['data']['token']
    print(f"Token: {token[:20]}...")
    
    gw = ssh_exec(f"""curl -s https://on-server2.com/api/deposits/gateway-info -H 'Authorization: Bearer {token}'""")
    data = json.loads(gw)
    print(f"\nUSDT: {json.dumps(data['data']['usdt'], indent=2)}")
    print(f"\nBankak: {json.dumps(data['data']['bankak'], indent=2, ensure_ascii=False)}")
    
    tn = data['data']['bankak'].get('transferNote', 'MISSING!')
    print(f"\n✅ transferNote = '{tn}'" if tn else "\n❌ transferNote is empty")
except Exception as e:
    print(f"Error: {e}")

ssh.close()
print("\nDone!")
