import paramiko

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

commands = [
    ("Find APK files on server",
     "find /home/www.on-server2.com -name '*.apk' -type f 2>/dev/null"),

    ("Check public_html/downloads dir",
     "ls -la /home/www.on-server2.com/public_html/downloads/ 2>/dev/null || echo 'Directory does not exist'"),

    ("Check public_html structure",
     "ls -la /home/www.on-server2.com/public_html/ 2>/dev/null | head -30"),

    ("Check if downloads folder exists anywhere",
     "find /home/www.on-server2.com -type d -name downloads 2>/dev/null"),

    ("Check recent APK upload script output",
     "cat /home/www.on-server2.com/public_html/app/downloads/ 2>/dev/null; ls -la /home/www.on-server2.com/public_html/app/ 2>/dev/null | head -20"),

    ("Check for any APK anywhere in /home",
     "find /home -name 'on-server1.apk' -type f 2>/dev/null"),

    ("Check /tmp for recent APK",
     "find /tmp -name '*.apk' -type f 2>/dev/null"),
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=15)

for label, cmd in commands:
    print(f"{'='*60}")
    print(f"  {label}")
    print(f"{'='*60}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=15)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    print(out if out else "(empty)")
    if err and 'TERM' not in err:
        print(f"[stderr] {err}")
    print()

client.close()
