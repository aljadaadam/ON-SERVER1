import paramiko, json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Login
cmd = 'curl -s -X POST https://on-server2.com/api/auth/login -H "Content-Type: application/json" -d \'{"email":"admin@onserver1.com","password":"admin123456"}\''
_, o, _ = ssh.exec_command(cmd)
token = json.loads(o.read().decode())['data']['accessToken']

# Gateway info
cmd2 = f'curl -s https://on-server2.com/api/deposits/gateway-info -H "Authorization: Bearer {token}"'
_, o, _ = ssh.exec_command(cmd2)
data = json.loads(o.read().decode())
print(json.dumps(data['data'], indent=2, ensure_ascii=False))

ssh.close()
