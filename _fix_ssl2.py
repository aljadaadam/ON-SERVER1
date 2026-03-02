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

# 1. Check SSL handshake externally
print("=== SSL Handshake Test ===")
ssh_exec(ssh, "echo | openssl s_client -connect on-server2.com:443 -servername on-server2.com 2>/dev/null | grep -E 'subject|issuer|Verify'")

# 2. Check LiteSpeed error logs
print("\n=== LiteSpeed Server Error Log ===")
ssh_exec(ssh, "tail -50 /usr/local/lsws/logs/error.log 2>/dev/null | grep -i 'server2\\|error\\|fail\\|ssl'")

# 3. Check vhost SSL config
print("\n=== Current vhost SSL ===")
ssh_exec(ssh, "grep -A10 'vhssl' /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf")

# 4. Verify cert files exist and are valid
print("\n=== Cert files ===")
ssh_exec(ssh, "ls -la /etc/letsencrypt/live/on-server2.com/")
ssh_exec(ssh, "openssl x509 -in /etc/letsencrypt/live/on-server2.com/fullchain.pem -noout -subject -dates")

# 5. Check if LiteSpeed picked up changes (full restart needed, not graceful)
print("\n=== Full restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl stop")
time.sleep(3)
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl start")
time.sleep(4)
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl status")

# 6. Test again after full restart
print("\n=== Test after full restart ===")
ssh_exec(ssh, "curl -sv https://on-server2.com/ctrl-7x9a3k/login 2>&1 | head -40")

# 7. Check if port 443 is listening
print("\n=== Port 443 ===")
ssh_exec(ssh, "ss -tlnp | grep 443")

ssh.close()
