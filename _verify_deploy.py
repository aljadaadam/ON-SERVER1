import paramiko
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

checks = [
    ("Search placeholder in JS", 'grep -c "بحث بالبريد" /home/www.on-server2.com/public_html/ctrl-7x9a3k/assets/*.js'),
    ("z-[60] in CSS", 'grep -c "z-\\[60\\]" /home/www.on-server2.com/public_html/ctrl-7x9a3k/assets/*.css'),
    ("z-\\[60\\] in JS", 'grep -c "z-\\[60\\]" /home/www.on-server2.com/public_html/ctrl-7x9a3k/assets/*.js'),
    ("h-screen in CSS", 'grep -c "h-screen" /home/www.on-server2.com/public_html/ctrl-7x9a3k/assets/*.css'),
    ("Cache headers", 'curl -sI https://on-server2.com/ctrl-7x9a3k/ | grep -i cache'),
]

for label, cmd in checks:
    _, stdout, stderr = ssh.exec_command(cmd)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    print(f"{label}: {out or err}")

ssh.close()
