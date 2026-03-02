import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"
ADMIN_SLUG = "ctrl-7x9a3k"

def ssh_exec(ssh, cmd, timeout=120):
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

# 1. Get IPv4 address
print("="*60)
print("STEP 1: Get VPS IPv4")
print("="*60)
ipv4 = ssh_exec(ssh, "curl -4 -s ifconfig.me")
print(f"\n*** VPS IPv4: {ipv4} ***")

# 2. Pull latest code from GitHub
print("\n" + "="*60)
print("STEP 2: Pull latest code")
print("="*60)
ssh_exec(ssh, "cd /opt/on-server1 && git pull origin main")

# 3. Install dashboard deps and build
print("\n" + "="*60)
print("STEP 3: Build dashboard")
print("="*60)
ssh_exec(ssh, "cd /opt/on-server1/on-server1-dashboard && npm install", timeout=180)
ssh_exec(ssh, "cd /opt/on-server1/on-server1-dashboard && npm run build", timeout=180)

# 4. Copy dashboard dist to on-server2.com docroot
print("\n" + "="*60)
print("STEP 4: Deploy dashboard files")
print("="*60)
ssh_exec(ssh, f"mkdir -p /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"cp -r /opt/on-server1/on-server1-dashboard/dist/* /home/www.on-server2.com/public_html/{ADMIN_SLUG}/")
ssh_exec(ssh, f"ls -la /home/www.on-server2.com/public_html/{ADMIN_SLUG}/")

# 5. Update backend .env
print("\n" + "="*60)
print("STEP 5: Update backend .env")
print("="*60)
ssh_exec(ssh, """cd /opt/on-server1/on-server1-backend && sed -i 's|DASHBOARD_URL=.*|DASHBOARD_URL=https://on-server2.com|' .env""")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && grep DASHBOARD_URL .env")

# 6. Restart backend with PM2
print("\n" + "="*60)
print("STEP 6: Restart backend")
print("="*60)
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && pm2 restart on-server1-backend")
time.sleep(3)
ssh_exec(ssh, "pm2 status")

# 7. Configure LiteSpeed vhost for www.on-server2.com
print("\n" + "="*60)
print("STEP 7: Configure LiteSpeed vhost")
print("="*60)

vhost_conf = f'''docRoot                   /home/www.on-server2.com/public_html
vhDomain                  www.on-server2.com
vhAliases                 on-server2.com
adminEmails               maher0001921@gmail.com
enableGzip                1
enableIpGeo               1

index  {{
  useServer               0
  indexFiles              index.html
}}

errorlog /home/www.on-server2.com/logs/www.on-server2.com.error_log {{
  useServer               0
  logLevel                WARN
  rollingSize             10M
}}

accesslog /home/www.on-server2.com/logs/www.on-server2.com.access_log {{
  useServer               0
  logFormat               "%h %l %u %t \\"%r\\" %>s %b \\"%{{Referer}}i\\" \\"%{{User-Agent}}i\\""
  logHeaders              5
  rollingSize             10M
  keepDays                10
  compressArchive         1
}}

context /api/ {{
  type                    proxy
  handler                 localhost:3000
  addDefaultCharset       off
}}

context /uploads/ {{
  type                    proxy
  handler                 localhost:3000
  addDefaultCharset       off
}}

rewrite  {{
  enable                  1
  autoLoadHtaccess        1

  RewriteRule ^/api(/.*)?$ - [L]
  RewriteRule ^/uploads(/.*)?$ - [L]

  RewriteRule ^/{ADMIN_SLUG}/(?!.*\\.[a-zA-Z0-9]{{1,10}}$)(.*)$ /{ADMIN_SLUG}/index.html [L]
}}

context /.well-known/acme-challenge {{
  location                /usr/local/lsws/Example/html/.well-known/acme-challenge
  allowBrowse             1

  rewrite  {{
     enable                  0
  }}
  addDefaultCharset       off

  phpIniOverride  {{

  }}
}}

vhssl  {{
  keyFile                 /etc/letsencrypt/live/www.on-server2.com/privkey.pem
  certFile                /etc/letsencrypt/live/www.on-server2.com/fullchain.pem
  certChain               1
  sslProtocol             24
  enableECDHE             1
  renegProtection         1
  sslSessionCache         1
  enableSpdy              15
  enableStapling          1
  ocspRespMaxAge          86400
}}

module cache {{
  storagePath /usr/local/lsws/cachedata/www.on-server2.com
}}
'''

# Write vhost config
ssh_exec(ssh, f"cp /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf.bak")
ssh_exec(ssh, f"""cat > /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf << 'VHOSTEOF'
{vhost_conf}
VHOSTEOF""")

# 8. Verify config was written
print("\n" + "="*60)
print("STEP 8: Verify vhost config")
print("="*60)
ssh_exec(ssh, "cat /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf")

# 9. Restart LiteSpeed
print("\n" + "="*60)
print("STEP 9: Restart LiteSpeed")
print("="*60)
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(3)
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl status")

# 10. Test endpoints
print("\n" + "="*60)
print("STEP 10: Test endpoints")
print("="*60)
ssh_exec(ssh, "curl -s -o /dev/null -w '%{http_code}' https://on-server2.com/api/health")
ssh_exec(ssh, f"curl -s -o /dev/null -w '%{{http_code}}' https://on-server2.com/{ADMIN_SLUG}/")
ssh_exec(ssh, f"curl -s https://on-server2.com/api/health")

print("\n" + "="*60)
print("DEPLOYMENT COMPLETE!")
print("="*60)
print(f"\nVPS IPv4: {ipv4}")
print(f"API: https://on-server2.com/api/")
print(f"Admin Panel: https://on-server2.com/{ADMIN_SLUG}/")
print(f"\nDNS: Point on-server2.com A record to {ipv4}")

ssh.close()
