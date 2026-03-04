import paramiko
import time

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=15)

commands = [
    ("1. Pull latest code", "cd /opt/on-server1 && git pull origin main"),
    ("2a. Deploy landing page", "cp /opt/on-server1/on-server1-landing/index.html /home/www.on-server2.com/public_html/app/index.html"),
    ("2b. Fix ownership", "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/app/index.html"),
    ("3. Restart backend", "cd /opt/on-server1/on-server1-backend && pm2 restart on-server1-backend"),
    ("4. Wait 5s then check status", "sleep 5 && pm2 status"),
    ("5. Check landing page headers (CSP check)", "curl -sI https://on-server2.com/app/"),
    ("6. Check page content loads", "curl -s https://on-server2.com/app/ | grep -c 'script\\|canvas\\|starsCanvas'"),
    ("7. Test API health", "curl -s https://on-server2.com/api/health"),
    ("8. Test dashboard", "curl -sI https://on-server2.com/ctrl-7x9a3k/"),
]

for label, cmd in commands:
    print(f"\n{'='*60}")
    print(f"  {label}")
    print(f"  CMD: {cmd}")
    print('='*60)
    stdin, stdout, stderr = client.exec_command(cmd, timeout=30)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out:
        print(out)
    if err:
        print(f"[STDERR] {err}")

# Final CSP verdict
print("\n" + "="*60)
print("  CSP VERDICT")
print("="*60)
stdin, stdout, stderr = client.exec_command("curl -sI https://on-server2.com/app/ | grep -i content-security-policy", timeout=15)
csp = stdout.read().decode().strip()
if csp:
    print(f"WARNING: Content-Security-Policy header IS PRESENT:\n{csp}")
else:
    print("Content-Security-Policy header is ABSENT (good).")

client.close()
