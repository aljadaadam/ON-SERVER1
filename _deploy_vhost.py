import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@', timeout=15)
sftp = ssh.open_sftp()

# server.on-server1.com vhost - proxy ALL to Node.js backend
server_vhost = (
    'docRoot                   $VH_ROOT/public_html\n'
    'vhDomain                  $VH_NAME\n'
    'vhAliases                 www.$VH_NAME\n'
    'adminEmails               admin@cyberpanel.net\n'
    'enableGzip                1\n'
    'enableIpGeo               1\n'
    '\n'
    'index  {\n'
    '  useServer               0\n'
    '  indexFiles              index.html\n'
    '}\n'
    '\n'
    'errorlog $VH_ROOT/logs/$VH_NAME.error_log {\n'
    '  useServer               0\n'
    '  logLevel                WARN\n'
    '  rollingSize             10M\n'
    '}\n'
    '\n'
    'accesslog $VH_ROOT/logs/$VH_NAME.access_log {\n'
    '  useServer               0\n'
    '  logFormat               "%h %l %u %t \\"%r\\" %>s %b"\n'
    '  logHeaders              5\n'
    '  rollingSize             10M\n'
    '  keepDays                10\n'
    '  compressArchive         1\n'
    '}\n'
    '\n'
    'extprocessor nodebackend {\n'
    '  type                    proxy\n'
    '  address                 http://127.0.0.1:3000\n'
    '  maxConns                100\n'
    '  pcKeepAliveTimeout      60\n'
    '  initTimeout             60\n'
    '  retryTimeout            0\n'
    '  respBuffer              0\n'
    '}\n'
    '\n'
    'context / {\n'
    '  type                    proxy\n'
    '  handler                 nodebackend\n'
    '  addDefaultCharset       off\n'
    '}\n'
    '\n'
    'context /.well-known/acme-challenge {\n'
    '  location                /usr/local/lsws/Example/html/.well-known/acme-challenge\n'
    '  allowBrowse             1\n'
    '  rewrite  {\n'
    '    enable                0\n'
    '  }\n'
    '  addDefaultCharset       off\n'
    '  phpIniOverride  {\n'
    '  }\n'
    '}\n'
    '\n'
    'rewrite  {\n'
    '  enable                  1\n'
    '  autoLoadHtaccess        1\n'
    '}\n'
    '\n'
    'module cache {\n'
    '  storagePath /usr/local/lsws/cachedata/$VH_NAME\n'
    '}\n'
    '\n'
    'vhssl  {\n'
    '  keyFile                 /etc/letsencrypt/live/server.on-server1.com/privkey.pem\n'
    '  certFile                /etc/letsencrypt/live/server.on-server1.com/fullchain.pem\n'
    '  certChain               1\n'
    '  sslProtocol             24\n'
    '  enableECDHE             1\n'
    '  renegProtection         1\n'
    '  sslSessionCache         1\n'
    '  enableSpdy              15\n'
    '  enableStapling          1\n'
    '  ocspRespMaxAge          86400\n'
    '}\n'
)

with sftp.open('/usr/local/lsws/conf/vhosts/server.on-server1.com/vhost.conf', 'w') as f:
    f.write(server_vhost)
print('server.on-server1.com vhost WRITTEN')

# on-server1.com vhost - dashboard static + /api proxy
dashboard_vhost = (
    'docRoot                   /opt/on-server1/on-server1-dashboard/dist\n'
    'vhDomain                  $VH_NAME\n'
    'vhAliases                 www.$VH_NAME\n'
    'adminEmails               admin@cyberpanel.net\n'
    'enableGzip                1\n'
    'enableIpGeo               1\n'
    '\n'
    'index  {\n'
    '  useServer               0\n'
    '  indexFiles              index.html\n'
    '}\n'
    '\n'
    'errorlog $VH_ROOT/logs/$VH_NAME.error_log {\n'
    '  useServer               0\n'
    '  logLevel                WARN\n'
    '  rollingSize             10M\n'
    '}\n'
    '\n'
    'accesslog $VH_ROOT/logs/$VH_NAME.access_log {\n'
    '  useServer               0\n'
    '  logFormat               "%h %l %u %t \\"%r\\" %>s %b"\n'
    '  logHeaders              5\n'
    '  rollingSize             10M\n'
    '  keepDays                10\n'
    '  compressArchive         1\n'
    '}\n'
    '\n'
    'extprocessor nodeapi {\n'
    '  type                    proxy\n'
    '  address                 http://127.0.0.1:3000\n'
    '  maxConns                100\n'
    '  pcKeepAliveTimeout      60\n'
    '  initTimeout             60\n'
    '  retryTimeout            0\n'
    '  respBuffer              0\n'
    '}\n'
    '\n'
    'context /api/ {\n'
    '  type                    proxy\n'
    '  handler                 nodeapi\n'
    '  addDefaultCharset       off\n'
    '}\n'
    '\n'
    'context /uploads/ {\n'
    '  type                    proxy\n'
    '  handler                 nodeapi\n'
    '  addDefaultCharset       off\n'
    '}\n'
    '\n'
    'context / {\n'
    '  location                /opt/on-server1/on-server1-dashboard/dist/\n'
    '  allowBrowse             1\n'
    '  rewrite  {\n'
    '    enable                1\n'
    '  }\n'
    '  addDefaultCharset       off\n'
    '}\n'
    '\n'
    'context /.well-known/acme-challenge {\n'
    '  location                /usr/local/lsws/Example/html/.well-known/acme-challenge\n'
    '  allowBrowse             1\n'
    '  rewrite  {\n'
    '    enable                0\n'
    '  }\n'
    '  addDefaultCharset       off\n'
    '  phpIniOverride  {\n'
    '  }\n'
    '}\n'
    '\n'
    'rewrite  {\n'
    '  enable                  1\n'
    '  autoLoadHtaccess        1\n'
    '  rules                   <<<END_rules\n'
    'RewriteCond %{REQUEST_URI} !^/api/\n'
    'RewriteCond %{REQUEST_URI} !^/uploads/\n'
    'RewriteCond %{REQUEST_FILENAME} !-f\n'
    'RewriteCond %{REQUEST_FILENAME} !-d\n'
    'RewriteRule ^(.*)$ /index.html [L]\n'
    '  END_rules\n'
    '}\n'
    '\n'
    'module cache {\n'
    '  storagePath /usr/local/lsws/cachedata/$VH_NAME\n'
    '}\n'
    '\n'
    'vhssl  {\n'
    '  keyFile                 /etc/letsencrypt/live/on-server1.com/privkey.pem\n'
    '  certFile                /etc/letsencrypt/live/on-server1.com/fullchain.pem\n'
    '  certChain               1\n'
    '  sslProtocol             24\n'
    '  enableECDHE             1\n'
    '  renegProtection         1\n'
    '  sslSessionCache         1\n'
    '  enableSpdy              15\n'
    '  enableStapling          1\n'
    '  ocspRespMaxAge          86400\n'
    '}\n'
)

with sftp.open('/usr/local/lsws/conf/vhosts/on-server1.com/vhost.conf', 'w') as f:
    f.write(dashboard_vhost)
print('on-server1.com vhost WRITTEN')

sftp.close()

# Restart LiteSpeed
print('=== Restarting LiteSpeed ===')
stdin, stdout, stderr = ssh.exec_command('/usr/local/lsws/bin/lswsctrl restart 2>&1', timeout=15)
print(stdout.read().decode())
print(stderr.read().decode())

import time
time.sleep(3)

# Test
print('=== Test backend via server.on-server1.com ===')
stdin, stdout, stderr = ssh.exec_command('curl -sk https://server.on-server1.com/api/health 2>&1', timeout=15)
print(stdout.read().decode())

print('=== Test dashboard via on-server1.com ===')
stdin, stdout, stderr = ssh.exec_command('curl -sk https://on-server1.com/ 2>&1 | head -5', timeout=15)
print(stdout.read().decode())

print('=== Test API via on-server1.com/api ===')
stdin, stdout, stderr = ssh.exec_command('curl -sk https://on-server1.com/api/health 2>&1', timeout=15)
print(stdout.read().decode())

ssh.close()
print('ALL DONE')
