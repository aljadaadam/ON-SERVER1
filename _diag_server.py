import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

commands = [
    ("1. Landing page content (curl /app/)", "curl -s https://on-server2.com/app/ | head -50"),
    ("2. Landing page HTTP headers", "curl -sI https://on-server2.com/app/"),
    ("3. PM2 status", "pm2 status"),
    ("4. Backend logs (last 30 lines)", "pm2 logs on-server1-backend --lines 30 --nostream"),
    ("5a. Landing file in public_html", "ls -la /home/www.on-server2.com/public_html/app/index.html"),
    ("5b. Landing file in /opt", "ls -la /opt/on-server1/on-server1-landing/index.html"),
    ("6. Express localhost /app/", "curl -s http://localhost:3000/app/ | head -50"),
    ("7. Express index.ts /app/ route", "grep -n -A5 'app/' /opt/on-server1/on-server1-backend/src/index.ts | head -30"),
    ("8. Landing directory listing", "ls -la /opt/on-server1/on-server1-landing/"),
    ("9. Recent backend logs (dirname check)", "pm2 logs on-server1-backend --lines 5 --nostream"),
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, username=USER, password=PASS, timeout=15)

for label, cmd in commands:
    print(f"\n{'='*70}")
    print(f"  {label}")
    print(f"  CMD: {cmd}")
    print('='*70)
    stdin, stdout, stderr = client.exec_command(cmd, timeout=20)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    if out.strip():
        print(out)
    if err.strip():
        print("[STDERR]", err)
    if not out.strip() and not err.strip():
        print("(no output)")

client.close()
print("\n--- Done ---")
