import paramiko, sys

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print("Connecting to 153.92.208.129...")
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@', timeout=15)
print("Connected!\n")

commands = [
    ("1. Landing page download link & version info",
     "grep -i 'apk\\|v=\\|الإصدار\\|version' /home/on-server2.com/public_html/app/index.html | head -10"),

    ("2. Landing page colors (cyan #00D2FF vs gold #FFD700)",
     "grep -i 'FFD700\\|00D2FF\\|--gold' /home/on-server2.com/public_html/app/index.html | head -5"),

    ("3. APK file details",
     "ls -la /home/on-server2.com/public_html/downloads/on-server1.apk"),

    ("4a. Dashboard build index.html",
     "ls -la /home/on-server2.com/public_html/ctrl-7x9a3k/index.html"),

    ("4b. Dashboard build assets",
     "ls -la /home/on-server2.com/public_html/ctrl-7x9a3k/assets/ | head -5"),

    ("5. Backend PM2 status",
     "pm2 list"),

    ("6a. Telegram in backend dist",
     "grep -l 'telegramService\\|telegram' /home/on-server2.com/on-server1-backend/dist/*.js 2>/dev/null || echo 'NOT IN DIST'"),

    ("6b. Telegram in backend src",
     "grep -l 'telegramService\\|telegram' /home/on-server2.com/on-server1-backend/src/services/*.ts 2>/dev/null || echo 'NOT IN SRC'"),

    ("7. Test download URL",
     "curl -sI https://on-server2.com/downloads/on-server1.apk 2>/dev/null | head -10"),

    ("8. Test landing page loads",
     "curl -sI https://on-server2.com/app/ 2>/dev/null | head -5"),

    ("9. Git log latest commits",
     "cd /home/on-server2.com/on-server1-backend && git log --oneline -3 2>/dev/null || echo 'NO GIT'"),
]

for title, cmd in commands:
    print(f"{'='*70}")
    print(f"  {title}")
    print(f"  CMD: {cmd}")
    print('='*70)
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    if out.strip():
        print(out)
    if err.strip():
        print(f"[STDERR] {err}")
    if not out.strip() and not err.strip():
        print("(no output)")
    print()

ssh.close()
print("Connection closed.")
