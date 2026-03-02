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

# 1. Pull latest (includes vite-env.d.ts fix)
print("=== Pull latest ===")
ssh_exec(ssh, "cd /opt/on-server1 && git pull origin main")

# 2. Rebuild dashboard
print("\n=== Rebuild dashboard ===")
# Remove old dist first
ssh_exec(ssh, "rm -rf /opt/on-server1/on-server1-dashboard/dist")
result = ssh_exec(ssh, "cd /opt/on-server1/on-server1-dashboard && npm run build 2>&1", timeout=180)
print(f"\nBuild result: {result[-200:]}")

# 3. Check new dist has correct base path in assets
print("\n=== Check built index.html ===")
ssh_exec(ssh, "cat /opt/on-server1/on-server1-dashboard/dist/index.html")

# 4. Re-copy to docroot
print("\n=== Copy to docroot ===")
ssh_exec(ssh, f"rm -rf /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"mkdir -p /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"cp -r /opt/on-server1/on-server1-dashboard/dist/* /home/www.on-server2.com/public_html/{ADMIN_SLUG}/")
ssh_exec(ssh, f"ls -la /home/www.on-server2.com/public_html/{ADMIN_SLUG}/")

# 5. Fix file permissions
print("\n=== Fix permissions ===")
ssh_exec(ssh, f"chmod -R 755 /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"chown -R wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/{ADMIN_SLUG}")

# 6. Graceful restart LiteSpeed
print("\n=== Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(3)

# 7. Test again with --resolve
print("\n=== Test with --resolve ===")
print("\n--- Admin Panel ---")
ssh_exec(ssh, f"curl -sk --resolve on-server2.com:443:127.0.0.1 https://on-server2.com/{ADMIN_SLUG}/")
print("\n--- API Health ---")
ssh_exec(ssh, "curl -sk --resolve on-server2.com:443:127.0.0.1 https://on-server2.com/api/health")

ssh.close()
