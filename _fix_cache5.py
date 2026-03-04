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
# APPROACH: Route /app/ and /downloads/ through Express reverse proxy
# so Node.js can add no-cache headers. LiteSpeed ignores .htaccess Header directives.
# ============================

print("=== 1. Add /app/ and /downloads/ proxy contexts to vhost ===")

vhost_path = "/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf"

# Read current config
current = ssh_exec(ssh, f"cat {vhost_path}")

# New vhost config: route /app/ and /downloads/ through backend proxy
new_vhost = r'''docRoot                   /home/www.on-server2.com/public_html
vhDomain                  www.on-server2.com
vhAliases                 on-server2.com
adminEmails               maher0001921@gmail.com
enableGzip                1
enableIpGeo               1

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

context /app/ {
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}

context /downloads/ {
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}

rewrite  {
  enable                  1
  autoLoadHtaccess        1

  RewriteRule ^/api(/.*)?$ - [L]
  RewriteRule ^/uploads(/.*)?$ - [L]
  RewriteRule ^/app(/.*)?$ - [L]
  RewriteRule ^/downloads(/.*)?$ - [L]

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
print("Written updated vhost config")

print("\n=== 2. Update Express backend to serve /app/ and /downloads/ ===")

# Add routes to serve the landing page and downloads with no-cache headers
# These will be served through Express, which already has no-cache middleware

express_patch = r'''
// ============================================
// Static files: Landing page & Downloads (served via Express for no-cache headers)
// ============================================
const landingPath = path.join(__dirname, '..', '..', 'on-server1-landing');
const downloadsPath = '/home/www.on-server2.com/public_html/downloads';
const localDownloadsPath = path.join(__dirname, '..', '..', 'on-server1-landing', 'downloads');

// Landing page
app.get('/app/', (_req, res) => {
  res.sendFile(path.join(landingPath, 'index.html'));
});
app.get('/app', (_req, res) => {
  res.redirect('/app/');
});

// APK downloads - try production path first, then local
app.use('/downloads', express.static(downloadsPath, { 
  setHeaders: (res) => {
    res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
}));
app.use('/downloads', express.static(localDownloadsPath, {
  setHeaders: (res) => {
    res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
}));
'''

# Read current index.ts
index_content = ssh_exec(ssh, "cat /opt/on-server1/on-server1-backend/src/index.ts")

# Check if already patched
if '/app/' in index_content:
    print("Backend already has /app/ route")
else:
    # Insert before the health check route
    ssh_exec(ssh, r"""cd /opt/on-server1/on-server1-backend && python3 -c "
import re
with open('src/index.ts', 'r') as f:
    content = f.read()

patch = '''
// ============================================
// Static files: Landing page & Downloads (served via Express for no-cache headers)
// ============================================
const landingPath = path.join(__dirname, '..', '..', 'on-server1-landing');

// Landing page
app.get('/app/', (_req, res) => {
  const fs = require('fs');
  const filePath = path.join(landingPath, 'index.html');
  if (fs.existsSync(filePath)) {
    res.sendFile(filePath);
  } else {
    // Fallback: try the public_html location
    res.sendFile('/home/www.on-server2.com/public_html/app/index.html');
  }
});
app.get('/app', (_req, res) => {
  res.redirect('/app/');
});

// APK downloads
app.use('/downloads', express.static('/home/www.on-server2.com/public_html/downloads', {
  setHeaders: (res) => {
    res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
}));
'''

# Insert before health check
content = content.replace(\"// Health check\", patch + '\n// Health check')

with open('src/index.ts', 'w') as f:
    f.write(content)
print('Patched index.ts')
" """)

    # Verify
    ssh_exec(ssh, "grep -n 'app\\|downloads\\|landing' /opt/on-server1/on-server1-backend/src/index.ts | head -20")

print("\n=== 3. Restart backend ===")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && pm2 restart on-server1-backend")
time.sleep(5)

# Check if backend is stable
ssh_exec(ssh, "pm2 status")

print("\n=== 4. Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(5)

print("\n=== 5. Test headers ===")

print("\n--- Landing page ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/app/")

print("\n--- APK download ---")
ssh_exec(ssh, "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'")

print("\n--- Dashboard ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/ctrl-7x9a3k/")

print("\n--- API ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/api/health")

# Functional test
print("\n=== 6. Functional test ===")
ssh_exec(ssh, "curl -s -o /dev/null -w 'Landing: %{http_code}' https://on-server2.com/app/")
ssh_exec(ssh, "curl -s https://on-server2.com/api/health")

print("\n=== DONE ===")
ssh.close()
