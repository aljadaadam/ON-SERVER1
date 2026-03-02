import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

def ssh_exec(ssh, cmd, timeout=30):
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

# 1. Check DNS
print("=== DNS Resolution ===")
ssh_exec(ssh, "dig +short on-server2.com A")
ssh_exec(ssh, "dig +short www.on-server2.com A")

# 2. Check LiteSpeed status
print("\n=== LiteSpeed Status ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl status")

# 3. Check PM2
print("\n=== PM2 Status ===")
ssh_exec(ssh, "pm2 status")

# 4. Check vhost config
print("\n=== Vhost Config ===")
ssh_exec(ssh, "cat /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf")

# 5. Check dashboard files
print("\n=== Dashboard Files ===")
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/ctrl-7x9a3k/")
ssh_exec(ssh, "cat /home/www.on-server2.com/public_html/ctrl-7x9a3k/index.html")

# 6. Check LiteSpeed error log
print("\n=== LiteSpeed Error Log ===")
ssh_exec(ssh, "tail -30 /home/www.on-server2.com/logs/www.on-server2.com.error_log")

# 7. Check httpd_config - listener mapping
print("\n=== Listener Config ===")
ssh_exec(ssh, "grep -A5 'on-server2' /usr/local/lsws/conf/httpd_config.conf")

# 8. Test locally with resolve
print("\n=== Local Test ===")
ssh_exec(ssh, "curl -sk --resolve on-server2.com:443:127.0.0.1 -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/login")

# 9. Test externally (real DNS)
print("\n=== External Test ===")
ssh_exec(ssh, "curl -sk -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/login")

ssh.close()
