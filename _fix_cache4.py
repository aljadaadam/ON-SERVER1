import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

def ssh_exec(ssh, cmd, timeout=60):
    print(f"\n>>> {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out.strip():
        print(out.strip())
    if err.strip():
        print(f"STDERR: {err.strip()}")
    return out.strip()

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

# The vhost docRoot is /home/www.on-server2.com/public_html/
# ALL .htaccess files must be there, not under /home/on-server2.com/

print("=== 1. Create .htaccess in correct locations ===")

sftp = ssh.open_sftp()

# No-cache headers (without IfModule - LiteSpeed Enterprise supports Header directly)
no_cache = """Header set Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0"
Header set Pragma "no-cache"
Header set Expires "Thu, 01 Jan 1970 00:00:00 GMT"
"""

# Main docRoot
with sftp.open("/home/www.on-server2.com/public_html/.htaccess", "w") as f:
    f.write(no_cache)
print("OK: /home/www.on-server2.com/public_html/.htaccess")

# App folder (landing page)
with sftp.open("/home/www.on-server2.com/public_html/app/.htaccess", "w") as f:
    f.write(no_cache)
print("OK: /home/www.on-server2.com/public_html/app/.htaccess")

# Downloads folder (APK)
with sftp.open("/home/www.on-server2.com/public_html/downloads/.htaccess", "w") as f:
    f.write(no_cache)
print("OK: /home/www.on-server2.com/public_html/downloads/.htaccess")

# Dashboard with SPA routing
dash_htaccess = """Header set Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0"
Header set Pragma "no-cache"
Header set Expires "Thu, 01 Jan 1970 00:00:00 GMT"

RewriteEngine On
RewriteBase /ctrl-7x9a3k/
RewriteRule ^index\\.html$ - [L]
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule . /ctrl-7x9a3k/index.html [L]
"""
with sftp.open("/home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess", "w") as f:
    f.write(dash_htaccess)
print("OK: /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess")

sftp.close()

# Fix ownership for ALL files (wwwon3882, not on3882!)
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/.htaccess")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/app/.htaccess")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/downloads/.htaccess")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess")

# Also fix the app folder ownership (was created by root)
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/app")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/app/index.html")

# Verify htaccess files are in place
print("\n=== 2. Verify .htaccess files ===")
ssh_exec(ssh, "cat /home/www.on-server2.com/public_html/app/.htaccess")
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/app/")
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/.htaccess")

# Verify autoLoadHtaccess is enabled in rewrite block
print("\n=== 3. Check autoLoadHtaccess ===")
ssh_exec(ssh, "grep -A2 'autoLoad' /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf")

# Restart LiteSpeed
print("\n=== 4. Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(5)

# Test
print("\n=== 5. Test headers ===")

print("\n--- Landing page ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/app/")

print("\n--- Dashboard ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/ctrl-7x9a3k/")

print("\n--- APK ---")
ssh_exec(ssh, "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'")

print("\n--- API ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/api/health")

print("\n=== DONE ===")
ssh.close()
