import paramiko
import sys

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

commands = [
    ("1. Landing page headers", "curl -sI https://on-server2.com/app/"),
    ("2. Dashboard headers", "curl -sI https://on-server2.com/ctrl-7x9a3k/"),
    ("3. APK download headers", "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'"),
    ("4. API health headers", "curl -sI https://on-server2.com/api/health"),
    ("5. Main public_html .htaccess", "cat /home/on-server2.com/public_html/.htaccess 2>/dev/null || echo 'NO FILE'"),
    ("6. App folder .htaccess", "cat /home/on-server2.com/public_html/app/.htaccess 2>/dev/null || echo 'NO FILE'"),
    ("7. Downloads folder .htaccess", "cat /home/on-server2.com/public_html/downloads/.htaccess 2>/dev/null || echo 'NO FILE'"),
    ("8. Dashboard .htaccess", "cat /home/www.on-server2.com/public_html/ctrl-7x9a3k/.htaccess 2>/dev/null || echo 'NO FILE'"),
    ("9. LiteSpeed cache config", "grep -ri 'cache\\|expires\\|etag' /usr/local/lsws/conf/httpd_config.conf 2>/dev/null | head -20"),
    ("10. LiteSpeed cache modules", "ls /usr/local/lsws/modules/ 2>/dev/null"),
    ("11a. Find vhost configs for on-server2", "find /usr/local/lsws/conf/ -name '*.conf' | xargs grep -l 'on-server2' 2>/dev/null"),
]

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print(f"Connecting to {HOST}...")
ssh.connect(HOST, username=USER, password=PASS, timeout=15)
print("Connected!\n")

def run(label, cmd):
    print("=" * 70)
    print(f">>> {label}")
    print(f"    CMD: {cmd}")
    print("-" * 70)
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    if out:
        print(out)
    if err:
        print(f"STDERR: {err}")
    if not out and not err:
        print("(no output)")
    print()
    return out

for label, cmd in commands:
    run(label, cmd)

# 11b: cat each vhost config found
stdin, stdout, stderr = ssh.exec_command("find /usr/local/lsws/conf/ -name '*.conf' | xargs grep -l 'on-server2' 2>/dev/null", timeout=30)
vhost_files = stdout.read().decode().strip().splitlines()
for f in vhost_files:
    f = f.strip()
    if f:
        run(f"11b. Vhost config: {f}", f"cat {f}")

# 12. Landing page content
run("12. Landing page HTML (first 50 lines)", "head -50 /home/on-server2.com/public_html/app/index.html")

# 13. www .htaccess
run("13. www public_html .htaccess", "cat /home/www.on-server2.com/public_html/.htaccess 2>/dev/null || echo 'NO FILE'")

ssh.close()
print("Done.")
