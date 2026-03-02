import paramiko, time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@', timeout=15)

# 1. Check existing on-server2 setup
print('=== VPS IP ===')
stdin, stdout, stderr = ssh.exec_command("curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}'", timeout=10)
print(stdout.read().decode().strip())

print('\n=== on-server2.com vhosts ===')
stdin, stdout, stderr = ssh.exec_command('ls /usr/local/lsws/conf/vhosts/ | grep server2', timeout=10)
print(stdout.read().decode())

print('=== on-server2.com vhost.conf ===')
stdin, stdout, stderr = ssh.exec_command('cat /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf 2>/dev/null', timeout=10)
print(stdout.read().decode())

print('=== /home dirs for on-server2 ===')
stdin, stdout, stderr = ssh.exec_command('ls -la /home/ | grep server2', timeout=10)
print(stdout.read().decode())

print('=== /home/www.on-server2.com ===')
stdin, stdout, stderr = ssh.exec_command('ls -la /home/www.on-server2.com/ 2>/dev/null', timeout=10)
print(stdout.read().decode())

print('=== DNS for on-server2.com ===')
stdin, stdout, stderr = ssh.exec_command('dig +short on-server2.com A 2>/dev/null; dig +short www.on-server2.com A 2>/dev/null', timeout=10)
print(stdout.read().decode())

print('=== SSL certs for on-server2 ===')
stdin, stdout, stderr = ssh.exec_command('ls /etc/letsencrypt/live/ | grep server2 2>/dev/null', timeout=10)
print(stdout.read().decode())

print('=== PM2 status ===')
stdin, stdout, stderr = ssh.exec_command('pm2 list 2>&1', timeout=10)
print(stdout.read().decode())

print('=== Ports in use ===')
stdin, stdout, stderr = ssh.exec_command('ss -tlnp | grep -E ":(3000|3001|3002|5173)" 2>/dev/null', timeout=10)
print(stdout.read().decode())

ssh.close()
