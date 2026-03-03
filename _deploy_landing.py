import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

def ssh_exec(ssh, cmd, timeout=180):
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

# 1. Pull latest (landing page is in repo)
print("=== Pull latest ===")
ssh_exec(ssh, "cd /opt/on-server1 && git pull origin main")

# 2. Deploy landing page to /app path
print("\n=== Deploy landing page ===")
ssh_exec(ssh, "mkdir -p /home/www.on-server2.com/public_html/app")
ssh_exec(ssh, "cp /opt/on-server1/on-server1-landing/index.html /home/www.on-server2.com/public_html/app/index.html")
ssh_exec(ssh, "chmod 644 /home/www.on-server2.com/public_html/app/index.html")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/app/index.html")

# 3. Create downloads directory for APK
print("\n=== Setup downloads directory ===")
ssh_exec(ssh, "mkdir -p /home/www.on-server2.com/public_html/downloads")
ssh_exec(ssh, "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/downloads")

# 4. Copy APK if it exists in the build output on local (we'll upload it separately)
# For now, create a placeholder
ssh_exec(ssh, "ls -la /home/www.on-server2.com/public_html/downloads/ 2>/dev/null")

# 5. Restart LiteSpeed
print("\n=== Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(3)

# 6. Test
print("\n=== Test ===")
result = ssh_exec(ssh, "curl -s -o /dev/null -w '%{http_code}' https://on-server2.com/app/")
print(f"Landing page status: {result}")

print("\n=== DONE ===")
print("Landing page URL: https://on-server2.com/app/")
print("APK download URL: https://on-server2.com/downloads/on-server1.apk")
print("\nNote: Upload the APK file to /home/www.on-server2.com/public_html/downloads/on-server1.apk")
ssh.close()
