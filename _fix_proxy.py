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

extprocessor backend_proxy {{
  type                    proxy
  address                 127.0.0.1:3000
  maxConns                100
  pcKeepAliveTimeout      60
  initTimeout             60
  retryTimeout            0
  respBuffer              0
}}

context /api/ {{
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}}

context /uploads/ {{
  type                    proxy
  handler                 backend_proxy
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

print("=== Writing fixed vhost config ===")
ssh_exec(ssh, f"""cat > /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf << 'VHOSTEOF'
{vhost_conf}
VHOSTEOF""")

print("\n=== Verify config ===")
ssh_exec(ssh, "cat /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf")

print("\n=== Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(4)
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl status")

print("\n=== Test API ===")
ssh_exec(ssh, "curl -sk --resolve on-server2.com:443:127.0.0.1 https://on-server2.com/api/health")

print("\n=== Test Admin Panel ===")
ssh_exec(ssh, f"curl -sk --resolve on-server2.com:443:127.0.0.1 -o /dev/null -w '%{{http_code}}' https://on-server2.com/{ADMIN_SLUG}/")

print("\n=== Test Admin Login page ===")
ssh_exec(ssh, f"curl -sk --resolve on-server2.com:443:127.0.0.1 -o /dev/null -w '%{{http_code}}' https://on-server2.com/{ADMIN_SLUG}/login")

ssh.close()
