import paramiko, json

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

def run(cmd):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=15)
    return stdout.read().decode().strip()

# Login
login = run('curl -s -X POST http://localhost:3000/api/auth/login -H "Content-Type: application/json" -d \'{"email":"admin@onserver1.com","password":"admin123456"}\'')
print("LOGIN:", login[:100])
data = json.loads(login)
token = data.get('data', {}).get('accessToken', '')

# Gateway info
gw = run(f'curl -s http://localhost:3000/api/deposits/gateway-info -H "Authorization: Bearer {token}"')
print("\nGATEWAY INFO:")
print(json.dumps(json.loads(gw), indent=2, ensure_ascii=False))

# Check if bankak_transfer_note setting exists in DB
note = run('cd /opt/on-server1/on-server1-backend && node -e "const {PrismaClient}=require(\'@prisma/client\');const p=new PrismaClient();p.setting.findUnique({where:{key:\'bankak_transfer_note\'}}).then(r=>console.log(JSON.stringify(r))).finally(()=>p.\\$disconnect())"')
print("\nTRANSFER NOTE SETTING:", note)

# Check all bankak settings
bankak = run('cd /opt/on-server1/on-server1-backend && node -e "const {PrismaClient}=require(\'@prisma/client\');const p=new PrismaClient();p.setting.findMany({where:{key:{startsWith:\'bankak\'}}}).then(r=>console.log(JSON.stringify(r,null,2))).finally(()=>p.\\$disconnect())"')
print("\nALL BANKAK SETTINGS:", bankak)

ssh.close()
