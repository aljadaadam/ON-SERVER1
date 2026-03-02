import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

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

# 1. Check SSL cert details - does it cover on-server2.com (bare)?
print("=== SSL Cert SANs ===")
ssh_exec(ssh, "openssl x509 -in /etc/letsencrypt/live/www.on-server2.com/fullchain.pem -noout -text | grep -A1 'Subject Alternative Name'")

# 2. Check if on-server2.com is mapped in listener
print("\n=== Full listener config ===")
ssh_exec(ssh, "grep -B2 -A20 'listener' /usr/local/lsws/conf/httpd_config.conf | head -80")

# 3. Add on-server2.com mapping to listener if missing
print("\n=== Check if bare domain mapped ===")
result = ssh_exec(ssh, "grep 'map.*on-server2.com on-server2.com' /usr/local/lsws/conf/httpd_config.conf")
if 'on-server2.com on-server2.com' not in result:
    print("\n*** BARE DOMAIN NOT MAPPED - need to check listener ***")

# 4. Issue SSL cert that covers both domains
print("\n=== Issue cert for both domains ===")
ssh_exec(ssh, "certbot certificates 2>/dev/null | grep -A5 'on-server2'")

# 5. Try issuing new cert covering both
print("\n=== Issuing cert for on-server2.com + www.on-server2.com ===")
ssh_exec(ssh, "certbot certonly --webroot -w /usr/local/lsws/Example/html -d on-server2.com -d www.on-server2.com --non-interactive --agree-tos --email maher0001921@gmail.com --cert-name on-server2.com 2>&1", timeout=60)

# 6. Check new cert
print("\n=== Check new cert ===")
ssh_exec(ssh, "ls -la /etc/letsencrypt/live/ | grep server2")

# 7. Update vhost to use the new cert if issued
print("\n=== Check if new cert covers both ===")
cert_check = ssh_exec(ssh, "openssl x509 -in /etc/letsencrypt/live/on-server2.com/fullchain.pem -noout -text 2>/dev/null | grep -A1 'Subject Alternative Name'")
if 'on-server2.com' in cert_check:
    print("\n*** New cert covers both domains, updating vhost ***")
    ssh_exec(ssh, """sed -i 's|/etc/letsencrypt/live/www.on-server2.com/privkey.pem|/etc/letsencrypt/live/on-server2.com/privkey.pem|' /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf""")
    ssh_exec(ssh, """sed -i 's|/etc/letsencrypt/live/www.on-server2.com/fullchain.pem|/etc/letsencrypt/live/on-server2.com/fullchain.pem|' /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf""")

# 8. Add bare domain mapping to httpd_config.conf listeners
print("\n=== Add bare domain to listeners ===")
# For each listener that has www.on-server2.com, add on-server2.com mapping
ssh_exec(ssh, r"""sed -i '/map.*www\.on-server2\.com www\.on-server2\.com/a\  map                     on-server2.com www.on-server2.com' /usr/local/lsws/conf/httpd_config.conf""")

# 9. Verify changes
print("\n=== Verify listener mapping ===")
ssh_exec(ssh, "grep -B1 -A1 'on-server2' /usr/local/lsws/conf/httpd_config.conf")

# 10. Restart LiteSpeed
print("\n=== Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(4)
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl status")

# 11. Test
print("\n=== Final Tests ===")
ssh_exec(ssh, "curl -s -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/login")
ssh_exec(ssh, "curl -s -o /dev/null -w '%{http_code}' https://www.on-server2.com/ctrl-7x9a3k/login")
ssh_exec(ssh, "curl -s https://on-server2.com/api/health")

ssh.close()
