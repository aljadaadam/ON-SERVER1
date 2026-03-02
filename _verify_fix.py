import paramiko
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Test orders endpoint (should return 401 not 500)
stdin,stdout,stderr = ssh.exec_command('curl -s https://on-server2.com/api/orders')
print('Orders response:', stdout.read().decode())

# Check recent error logs
stdin,stdout,stderr = ssh.exec_command('pm2 logs on-server1-backend --lines 10 --nostream')
out = stdout.read().decode()
err = stderr.read().decode()
print('--- LOGS ---')
print(out[-800:] if len(out) > 800 else out)
if 'X-Forwarded' in out or 'ValidationError' in out:
    print('WARNING: Rate limiter error still present!')
else:
    print('OK: No rate limiter errors!')

ssh.close()
