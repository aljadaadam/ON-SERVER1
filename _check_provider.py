import paramiko, json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, e = ssh.exec_command(cmd, timeout=30)
    out = o.read().decode().strip()
    err = e.read().decode().strip()
    return out if out else err

# 1. Check DB settings
print("=== DB Provider Settings ===")
db_vals = run('cd /opt/on-server1/on-server1-backend && node -e "const{PrismaClient}=require(String.fromCharCode(64)+String.fromCharCode(112)+String.fromCharCode(114)+String.fromCharCode(105)+String.fromCharCode(115)+String.fromCharCode(109)+String.fromCharCode(97)+String.fromCharCode(47)+String.fromCharCode(99)+String.fromCharCode(108)+String.fromCharCode(105)+String.fromCharCode(101)+String.fromCharCode(110)+String.fromCharCode(116));const p=new PrismaClient();(async()=>{const s=await p.setting.findMany({where:{key:{in:[String.fromCharCode(112)+String.fromCharCode(114)+String.fromCharCode(111)+String.fromCharCode(118)+String.fromCharCode(105)+String.fromCharCode(100)+String.fromCharCode(101)+String.fromCharCode(114)+String.fromCharCode(95)+String.fromCharCode(117)+String.fromCharCode(114)+String.fromCharCode(108),String.fromCharCode(112)+String.fromCharCode(114)+String.fromCharCode(111)+String.fromCharCode(118)+String.fromCharCode(105)+String.fromCharCode(100)+String.fromCharCode(101)+String.fromCharCode(114)+String.fromCharCode(95)+String.fromCharCode(117)+String.fromCharCode(115)+String.fromCharCode(101)+String.fromCharCode(114)+String.fromCharCode(110)+String.fromCharCode(97)+String.fromCharCode(109)+String.fromCharCode(101),String.fromCharCode(112)+String.fromCharCode(114)+String.fromCharCode(111)+String.fromCharCode(118)+String.fromCharCode(105)+String.fromCharCode(100)+String.fromCharCode(101)+String.fromCharCode(114)+String.fromCharCode(95)+String.fromCharCode(97)+String.fromCharCode(112)+String.fromCharCode(105)+String.fromCharCode(95)+String.fromCharCode(107)+String.fromCharCode(101)+String.fromCharCode(121)]}}});console.log(JSON.stringify(s,null,2));await p.$disconnect();})()"')
print(db_vals if db_vals else "(no provider settings in DB)")

# 2. Check recent logs
print("\n=== Recent Logs ===")
logs = run('pm2 logs on-server1-backend --nostream --lines 40 2>&1 | grep -iE "dhru|provider|auth|error|failed|fusion"')
print(logs if logs else "(no relevant logs)")

ssh.close()
