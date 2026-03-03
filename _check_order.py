import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Get full PM2 logs around order 100630
cmd = "pm2 logs on-server1-backend --nostream --lines 500 2>&1 | grep -A2 -B2 '100630\\|placeOrder\\|placeimeiorder\\|Placing order\\|CUSTOMFIELD\\|20912'"
stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
print(stdout.read().decode())

ssh.close()
