import paramiko, json, time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Test OPTIONS preflight
cmd1 = 'curl -s -X OPTIONS https://on-server2.com/api/auth/login -H "Origin: https://on-server2.com" -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: Content-Type" -D - -o /dev/null'
stdin, stdout, stderr = ssh.exec_command(cmd1)
print("=== OPTIONS preflight ===")
print(stdout.read().decode())

# Test POST with Origin header like browser
cmd2 = """curl -s -X POST https://on-server2.com/api/auth/login -H "Content-Type: application/json" -H "Origin: https://on-server2.com" -D - -d '{"email":"admin@onserver1.com","password":"admin123456"}'"""
stdin, stdout, stderr = ssh.exec_command(cmd2)
print("=== POST with Origin (response headers + body) ===")
print(stdout.read().decode()[:800])

# Also flush logs and check after this test
stdin, stdout, stderr = ssh.exec_command('pm2 flush on-server1-backend 2>&1')
stdout.read()

ssh.close()
