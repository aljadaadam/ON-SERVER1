import paramiko
import time
import sys

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

checks = [
    ("PM2 Status", "pm2 status"),
    ("Backend API Health", "curl -sk https://on-server2.com/api/health"),
    ("Backend API Products", "curl -sk https://on-server2.com/api/products | head -c 200"),
    ("Dashboard Page Load", "curl -sk -o /dev/null -w '%{http_code}' https://on-server2.com/ctrl-7x9a3k/"),
    ("Landing Page", "curl -sk -o /dev/null -w '%{http_code}' https://on-server2.com/app/"),
    ("APK Download", "curl -sk -o /dev/null -w '%{http_code}' 'https://on-server2.com/downloads/on-server1.apk?v=20260303'"),
    ("PM2 Backend Logs (last 10)", "pm2 logs on-server1-backend --lines 10 --nostream"),
    ("Dashboard Files Exist", "ls -la /home/www.on-server2.com/public_html/ctrl-7x9a3k/ | head -10"),
    ("telegramService Exists", "ls -la /opt/on-server1/on-server1-backend/src/services/telegramService.ts"),
]

def run_cmd(ssh, cmd, timeout=15):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace").strip()
    err = stderr.read().decode("utf-8", errors="replace").strip()
    return out, err

def judge(name, out, err):
    combined = out + "\n" + err
    if name == "PM2 Status":
        return "PASS" if "online" in combined.lower() else "FAIL"
    elif name == "Backend API Health":
        return "PASS" if '"success"' in out or '"status"' in out else "FAIL"
    elif name == "Backend API Products":
        return "PASS" if '"success"' in out or '"data"' in out or '"products"' in out else "FAIL"
    elif name in ("Dashboard Page Load", "Landing Page", "APK Download"):
        return "PASS" if out.strip() == "200" else "FAIL"
    elif name == "PM2 Backend Logs (last 10)":
        return "PASS" if out or err else "FAIL"
    elif name == "Dashboard Files Exist":
        return "PASS" if "index.html" in combined else "FAIL"
    elif name == "telegramService Exists":
        return "PASS" if "telegramService.ts" in out and "No such file" not in err else "FAIL"
    return "UNKNOWN"

def main():
    print(f"Connecting to {HOST}...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(HOST, username=USER, password=PASS, timeout=10)
    print("Connected. Waiting 5 seconds for backend to initialize...\n")
    time.sleep(5)

    results = []
    for name, cmd in checks:
        print(f"{'='*60}")
        print(f"CHECK: {name}")
        print(f"CMD:   {cmd}")
        print(f"{'-'*60}")
        try:
            out, err = run_cmd(ssh, cmd)
            status = judge(name, out, err)
            if out:
                print(f"STDOUT:\n{out}")
            if err:
                print(f"STDERR:\n{err}")
            print(f"\n>>> [{status}] <<<")
            results.append((name, status))
        except Exception as e:
            print(f"ERROR: {e}")
            results.append((name, "FAIL"))
        print()

    ssh.close()

    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    passed = 0
    failed = 0
    for name, status in results:
        icon = "✅" if status == "PASS" else "❌"
        print(f"  {icon} {status:4s} | {name}")
        if status == "PASS":
            passed += 1
        else:
            failed += 1
    print(f"\nTotal: {passed} passed, {failed} failed out of {len(results)}")

if __name__ == "__main__":
    main()
