import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

htaccess_content = """<IfModule mod_rewrite.c>
  RewriteEngine On
  RewriteBase /ctrl-7x9a3k/
  RewriteRule ^index\\.html$ - [L]
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteRule . /ctrl-7x9a3k/index.html [L]
</IfModule>
"""

# Write .htaccess via SFTP
sftp = ssh.open_sftp()
with sftp.open("/home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess", "w") as f:
    f.write(htaccess_content)
sftp.close()
print("Created .htaccess")

# Set ownership
stdin, stdout, stderr = ssh.exec_command("chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess")
stdout.read()
print("Set ownership")

# Verify
stdin, stdout, stderr = ssh.exec_command("cat /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess")
print("=== .htaccess content ===")
print(stdout.read().decode())

# Restart LiteSpeed
stdin, stdout, stderr = ssh.exec_command("/usr/local/lsws/bin/lswsctrl restart")
print(stdout.read().decode())
time.sleep(3)

# Test
stdin, stdout, stderr = ssh.exec_command("curl -s -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/login")
status = stdout.read().decode().strip()
print(f"Login page status: {status}")

stdin, stdout, stderr = ssh.exec_command("curl -s -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/categories")
status2 = stdout.read().decode().strip()
print(f"Categories page status: {status2}")

ssh.close()
print("\nDONE")
