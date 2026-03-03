import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

htaccess_content = """# Prevent caching of index.html so new JS/CSS hashes are loaded
<FilesMatch "index\\.html$">
    Header set Cache-Control "no-cache, no-store, must-revalidate"
    Header set Pragma "no-cache"
    Header set Expires "0"
</FilesMatch>

# Cache JS/CSS assets (they have hash in filename)
<FilesMatch "\\.(js|css)$">
    Header set Cache-Control "public, max-age=31536000, immutable"
</FilesMatch>
"""

sftp = ssh.open_sftp()
with sftp.file('/home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess', 'w') as f:
    f.write(htaccess_content)
sftp.close()

cmds = [
    'chmod 644 /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess',
    'chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess',
    'rm -rf /tmp/lshttpd/cachedata/* 2>/dev/null; echo "cache flushed"',
    '/usr/local/lsws/bin/lswsctrl restart',
]

for c in cmds:
    _, stdout, stderr = ssh.exec_command(c)
    out = stdout.read().decode().strip()
    if out:
        print(out)

# Verify
import time
time.sleep(2)
_, stdout, _ = ssh.exec_command('curl -sI https://on-server2.com/ctrl-7x9a3k/ 2>&1')
print("\n--- Response Headers ---")
print(stdout.read().decode().strip())

ssh.close()
print("\nDone!")
