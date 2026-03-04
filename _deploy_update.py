import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"
ADMIN_SLUG = "ctrl-7x9a3k"

def ssh_exec(ssh, cmd, timeout=180):
    print(f"\n>>> {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    if out.strip():
        print(out.strip().encode('ascii', errors='replace').decode())
    if err.strip():
        print(f"STDERR: {err.strip().encode('ascii', errors='replace').decode()}")
    return out.strip()

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

# 1. Pull latest
print("=== Pull latest ===")
ssh_exec(ssh, "cd /opt/on-server1 && git pull origin main")

# 2. Backend: regenerate Prisma (schema changed) + push DB
print("\n=== Prisma generate + push ===")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && npx prisma generate")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && npx prisma db push --accept-data-loss")

# 3. Restart backend
print("\n=== Restart backend ===")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-backend && pm2 restart on-server1-backend")
time.sleep(3)
ssh_exec(ssh, "pm2 status")

# 4. Rebuild dashboard
print("\n=== Rebuild dashboard ===")
ssh_exec(ssh, "rm -rf /opt/on-server1/on-server1-dashboard/dist")
ssh_exec(ssh, "cd /opt/on-server1/on-server1-dashboard && npm run build", timeout=120)

# 5. Deploy dashboard
print("\n=== Deploy dashboard ===")
ssh_exec(ssh, f"rm -rf /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"mkdir -p /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"cp -r /opt/on-server1/on-server1-dashboard/dist/. /home/www.on-server2.com/public_html/{ADMIN_SLUG}/")
ssh_exec(ssh, f"chmod -R 755 /home/www.on-server2.com/public_html/{ADMIN_SLUG}")
ssh_exec(ssh, f"chown -R wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/{ADMIN_SLUG}")

# 6. Restart LiteSpeed
print("\n=== Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(3)

# 7. Test
print("\n=== Test ===")
ssh_exec(ssh, "curl -s https://on-server2.com/api/health")
ssh_exec(ssh, f"curl -s -o /dev/null -w '%{{http_code}}' https://on-server2.com/{ADMIN_SLUG}/login")

print("\n=== DONE ===")
ssh.close()
