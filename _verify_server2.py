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

# Test with --resolve to bypass DNS
print("=== Test API with --resolve ===")
ssh_exec(ssh, "curl -sk --resolve on-server2.com:443:127.0.0.1 https://on-server2.com/api/health")

print("\n=== Test Admin Panel with --resolve ===")
ssh_exec(ssh, "curl -sk --resolve on-server2.com:443:127.0.0.1 -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/")

print("\n=== Check dashboard files exist ===")
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/ctrl-7x9a3k/")
ssh_exec(ssh, "head -20 /home/www.on-server2.com/public_html/ctrl-7x9a3k/index.html")

print("\n=== Check DNS resolution for on-server2.com ===")
ssh_exec(ssh, "dig +short on-server2.com A")
ssh_exec(ssh, "dig +short www.on-server2.com A")

print("\n=== Check if TypeScript build error affected dashboard ===")
ssh_exec(ssh, "ls -la /opt/on-server1/on-server1-dashboard/dist/")

print("\n=== Backend health check via localhost ===")
ssh_exec(ssh, "curl -s http://localhost:3000/api/health")

ssh.close()
