import paramiko
import time

host = '153.92.208.129'
user = 'root'
pwd = 'Mahe1000amd@'

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(host, username=user, password=pwd, timeout=30)

commands = [
    'cd /opt/on-server1 && git pull origin main',
    'pm2 restart on-server1-backend',
    'sleep 5 && curl -s https://on-server2.com/app/ | grep -o \'href="[^"]*apk[^"]*"\'',
    'cp /opt/on-server1/on-server1-landing/index.html /home/www.on-server2.com/public_html/app/index.html',
    'chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/app/index.html',
]

for cmd in commands:
    print(f'>>> {cmd}')
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out:
        print(out)
    if err:
        print(err)
    print()

ssh.close()
print('Done.')
