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
# Strategy: Rewrite vhost config properly with LiteSpeed-compatible no-cache
# The issue is LiteSpeed's expires module overrides extraHeaders
# Solution: Use proper context blocks with extraHeaders and disable expires at vhost level
# ============================

vhost_path = "/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf"

# First, check the document root situation
print("=== 1. Check document roots and symlinks ===")
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/app 2>/dev/null || echo 'NO APP FOLDER'")
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/downloads 2>/dev/null || echo 'NO DOWNLOADS FOLDER'")
ssh_exec(ssh, "ls -la /home/on-server2.com/public_html/ | head -10")

# Check if /app/ and /downloads/ are under www or non-www
print("\n=== 2. Where is the landing page actually served from? ===")
ssh_exec(ssh, "readlink -f /home/www.on-server2.com/public_html 2>/dev/null")
ssh_exec(ssh, "readlink -f /home/on-server2.com/public_html 2>/dev/null")

# Check if they're the same via inode
ssh_exec(ssh, "stat --format='%i' /home/www.on-server2.com/public_html 2>/dev/null")
ssh_exec(ssh, "stat --format='%i' /home/on-server2.com/public_html 2>/dev/null")

# Check if there's a symlink connecting them
ssh_exec(ssh, "file /home/www.on-server2.com/public_html")
ssh_exec(ssh, "file /home/on-server2.com/public_html")

# ============================
# 3. Write proper vhost config with no-cache via context extraHeaders
# ============================
print("\n=== 3. Write updated vhost config ===")

# Backup current config
ssh_exec(ssh, f"cp {vhost_path} {vhost_path}.bak.$(date +%Y%m%d%H%M%S)")

new_vhost = r'''docRoot                   /home/www.on-server2.com/public_html
vhDomain                  www.on-server2.com
vhAliases                 on-server2.com
adminEmails               maher0001921@gmail.com
enableGzip                1
enableIpGeo               1

# Disable expires at vhost level so our no-cache headers take effect
expires {
  enableExpires           0
}

index  {
  useServer               0
  indexFiles              index.html
}

errorlog /home/www.on-server2.com/logs/www.on-server2.com.error_log {
  useServer               0
  logLevel                WARN
  rollingSize             10M
}

accesslog /home/www.on-server2.com/logs/www.on-server2.com.access_log {
  useServer               0
  logFormat               "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\""
  logHeaders              5
  rollingSize             10M
  keepDays                10
  compressArchive         1
}

extprocessor backend_proxy {
  type                    proxy
  address                 127.0.0.1:3000
  maxConns                100
  pcKeepAliveTimeout      60
  initTimeout             60
  retryTimeout            0
  respBuffer              0
}

context /api/ {
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}

context /uploads/ {
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}

rewrite  {
  enable                  1
  autoLoadHtaccess        1

  RewriteRule ^/api(/.*)?$ - [L]
  RewriteRule ^/uploads(/.*)?$ - [L]

  RewriteRule ^/ctrl-7x9a3k/(?!.*\.[a-zA-Z0-9]{1,10}$)(.*)$ /ctrl-7x9a3k/index.html [L]
}

context /.well-known/acme-challenge {
  location                /usr/local/lsws/Example/html/.well-known/acme-challenge
  allowBrowse             1

  rewrite  {
     enable                  0
  }
  addDefaultCharset       off

  phpIniOverride  {

  }
}

vhssl  {
  keyFile                 /etc/letsencrypt/live/on-server2.com/privkey.pem
  certFile                /etc/letsencrypt/live/on-server2.com/fullchain.pem
  certChain               1
  sslProtocol             24
  enableECDHE             1
  renegProtection         1
  sslSessionCache         1
  enableSpdy              15
  enableStapling          1
  ocspRespMaxAge          86400
}

module cache {
  storagePath /usr/local/lsws/cachedata/www.on-server2.com
}
'''

sftp = ssh.open_sftp()
with sftp.open(vhost_path, 'w') as f:
    f.write(new_vhost)
sftp.close()
print(f"Written new vhost config to {vhost_path}")

# Verify
ssh_exec(ssh, f"cat {vhost_path}")

# ============================
# 4. Also handle on-server2.com domain if files are under /home/on-server2.com/
# ============================
print("\n=== 4. Check and fix on-server2.com domain ===")

# The landing page is at /home/on-server2.com/public_html/app/
# But the vhost docRoot is /home/www.on-server2.com/public_html
# Check if on-server2.com/public_html is a symlink to www
result = ssh_exec(ssh, "ls -la /home/ | grep on-server2")

# Check if app folder exists under www
app_check = ssh_exec(ssh, "ls /home/www.on-server2.com/public_html/app/index.html 2>/dev/null && echo 'FOUND' || echo 'NOT_FOUND'")
if app_check == 'NOT_FOUND':
    print("\n/app/ not found under www docRoot. Checking if there's a different path...")
    # Maybe there's a symlink or the content is only under /home/on-server2.com
    ssh_exec(ssh, "ln -sf /home/on-server2.com/public_html/app /home/www.on-server2.com/public_html/app 2>/dev/null || echo 'symlink exists or failed'")
    ssh_exec(ssh, "ln -sf /home/on-server2.com/public_html/downloads /home/www.on-server2.com/public_html/downloads 2>/dev/null || echo 'symlink exists or failed'")

# ============================
# 5. Restart LiteSpeed with graceful restart
# ============================
print("\n=== 5. Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(5)

# ============================
# 6. Test all headers
# ============================
print("\n=== 6. Test headers ===")

print("\n--- Landing page (on-server2.com/app/) ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/app/")

print("\n--- Dashboard (on-server2.com/ctrl-7x9a3k/) ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/ctrl-7x9a3k/")

print("\n--- API (on-server2.com/api/health) ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/api/health")

print("\n--- APK download ---")
ssh_exec(ssh, "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'")

# ============================
# 7. Quick functional test
# ============================
print("\n=== 7. Functional test ===")
ssh_exec(ssh, "curl -s https://on-server2.com/api/health")
ssh_exec(ssh, "curl -s -o /dev/null -w 'Landing: %{http_code}' https://on-server2.com/app/")
ssh_exec(ssh, "curl -s -o /dev/null -w 'Dashboard: %{http_code}' https://on-server2.com/ctrl-7x9a3k/")

print("\n=== DONE ===")
ssh.close()
