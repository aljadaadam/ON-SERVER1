import paramiko
import time

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(host, username=user, password=password, timeout=15)
print(f"Connected to {host}\n")

commands = [
    ("1. Install node-telegram-bot-api", "cd /opt/on-server1/on-server1-backend && npm install node-telegram-bot-api", 60),
    ("2. Restart backend", "pm2 restart on-server1-backend", 15),
    ("3. PM2 status (after 5s wait)", "sleep 5 && pm2 status", 20),
    ("4. Health check (after 3s wait)", "sleep 3 && curl -sk https://on-server2.com/api/health", 15),
    ("5. Products endpoint", "curl -sk https://on-server2.com/api/products | head -c 200", 15),
    ("6. PM2 logs", "pm2 logs on-server1-backend --lines 15 --nostream", 15),
]

for label, cmd, timeout in commands:
    print("=" * 60)
    print(f"{label}")
    print(f"CMD: {cmd}")
    print("-" * 60)
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    if out.strip():
        print(out)
    if err.strip():
        print(f"STDERR: {err}")
    print()

ssh.close()
print("Done - connection closed.")
