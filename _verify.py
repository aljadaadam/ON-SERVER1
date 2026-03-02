import paramiko, time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@', timeout=15)

# Full stop/start LiteSpeed
stdin, stdout, stderr = ssh.exec_command('/usr/local/lsws/bin/lswsctrl stop; sleep 2; /usr/local/lsws/bin/lswsctrl start 2>&1', timeout=20)
print('Restart:', stdout.read().decode())
time.sleep(3)

# Test all endpoints
print('=== server.on-server1.com/api/health ===')
stdin, stdout, stderr = ssh.exec_command('curl -sk https://server.on-server1.com/api/health 2>&1', timeout=15)
print(stdout.read().decode())

print('=== on-server1.com (dashboard) ===')
stdin, stdout, stderr = ssh.exec_command("curl -sk -o /dev/null -w '%{http_code}' https://on-server1.com/ 2>&1", timeout=15)
print('Status:', stdout.read().decode())

print('=== on-server1.com/api/health ===')
stdin, stdout, stderr = ssh.exec_command('curl -sk https://on-server1.com/api/health 2>&1', timeout=15)
print(stdout.read().decode())

print('=== on-server1.com/api/products (first 200 chars) ===')
stdin, stdout, stderr = ssh.exec_command('curl -sk https://on-server1.com/api/products 2>&1 | head -c 200', timeout=15)
print(stdout.read().decode())

print('=== PM2 status ===')
stdin, stdout, stderr = ssh.exec_command('pm2 list 2>&1', timeout=15)
print(stdout.read().decode())

ssh.close()
print('VERIFICATION DONE')
