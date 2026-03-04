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

# ============================
# 1. Create .htaccess files WITHOUT IfModule wrappers (LiteSpeed supports Header directly)
# ============================
print("=== 1. Create .htaccess files (no-cache) ===")

# Main public_html .htaccess for on-server2.com
htaccess_main = r'''Header set Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0"
Header set Pragma "no-cache"
Header set Expires "Thu, 01 Jan 1970 00:00:00 GMT"
'''

sftp = ssh.open_sftp()

# Write to on-server2.com public_html
with sftp.open("/home/on-server2.com/public_html/.htaccess", "w") as f:
    f.write(htaccess_main)
print("Created /home/on-server2.com/public_html/.htaccess")

# Write to app folder
with sftp.open("/home/on-server2.com/public_html/app/.htaccess", "w") as f:
    f.write(htaccess_main)
print("Created /home/on-server2.com/public_html/app/.htaccess")

# Write to downloads folder
with sftp.open("/home/on-server2.com/public_html/downloads/.htaccess", "w") as f:
    f.write(htaccess_main)
print("Created /home/on-server2.com/public_html/downloads/.htaccess")

# Dashboard .htaccess (no-cache + SPA routing)
htaccess_dash = r'''Header set Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0"
Header set Pragma "no-cache"  
Header set Expires "Thu, 01 Jan 1970 00:00:00 GMT"

RewriteEngine On
RewriteBase /ctrl-7x9a3k/
RewriteRule ^index\.html$ - [L]
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule . /ctrl-7x9a3k/index.html [L]
'''

with sftp.open("/home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess", "w") as f:
    f.write(htaccess_dash)
print("Created /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess")

# www root .htaccess
with sftp.open("/home/www.on-server2.com/public_html/.htaccess", "w") as f:
    f.write(htaccess_main)
print("Created /home/www.on-server2.com/public_html/.htaccess")

sftp.close()

# Fix ownership
ssh_exec(ssh, "chown on3882:on3882 /home/on-server2.com/public_html/.htaccess /home/on-server2.com/public_html/app/.htaccess /home/on-server2.com/public_html/downloads/.htaccess")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/.htaccess /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess")

# ============================
# 2. Update LiteSpeed expires config to include text/html=A0
# ============================
print("\n=== 2. Update LiteSpeed expires config ===")

httpd_conf = "/usr/local/lsws/conf/httpd_config.conf"

# Read current config
current_conf = ssh_exec(ssh, f"cat {httpd_conf}")

# Update expiresByType to include text/html=A0 and application/octet-stream=A0
if "text/html=A0" not in current_conf:
    ssh_exec(ssh, f"sed -i 's|expiresByType.*image|expiresByType           text/html=A0,application/octet-stream=A0,image|' {httpd_conf}")
    print("Added text/html=A0 to expires config")
else:
    print("text/html=A0 already in expires config")

# Verify
ssh_exec(ssh, f"grep expiresByType {httpd_conf}")

# ============================
# 3. Pull latest & deploy landing page with meta tags
# ============================
print("\n=== 3. Pull latest & deploy ===")
ssh_exec(ssh, "cd /opt/on-server1 && git pull origin main")

# Copy updated landing page
ssh_exec(ssh, "cp /opt/on-server1/on-server1-landing/index.html /home/on-server2.com/public_html/app/index.html")
ssh_exec(ssh, "chmod 644 /home/on-server2.com/public_html/app/index.html")
ssh_exec(ssh, "chown on3882:on3882 /home/on-server2.com/public_html/app/index.html")

# ============================
# 4. Restart backend with new no-cache middleware
# ============================
print("\n=== 4. Restart backend ===")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && pm2 restart on-server1-backend")
time.sleep(5)
ssh_exec(ssh, "pm2 status")

# ============================
# 5. Restart LiteSpeed
# ============================
print("\n=== 5. Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(5)

# ============================
# 6. Test all headers
# ============================
print("\n=== 6. Test headers ===")

print("\n--- Landing page ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/app/")

print("\n--- APK download ---")
ssh_exec(ssh, "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'")

print("\n--- Dashboard ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/ctrl-7x9a3k/")

print("\n--- API health ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/api/health")

print("\n=== DONE ===")
ssh.close()
