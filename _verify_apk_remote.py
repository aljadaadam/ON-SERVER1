import paramiko

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

commands = [
    ("1. File exists and details",
     "ls -la /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("2. Download via public URL",
     "curl -s -o /tmp/verify_apk.apk 'https://on-server2.com/downloads/on-server1.apk?v=20260303' && ls -la /tmp/verify_apk.apk"),

    ("3. Verify valid APK",
     '''python3 -c "
import zipfile
apk_path = '/tmp/verify_apk.apk'
z = zipfile.ZipFile(apk_path)
has_manifest = 'AndroidManifest.xml' in z.namelist()
dex_files = [n for n in z.namelist() if n.endswith('.dex')]
print(f'Valid APK: {has_manifest}')
print(f'DEX files: {dex_files}')
print(f'Total files: {len(z.namelist())}')
z.fp.seek(0,2)
print(f'File size: {z.fp.tell()} bytes')
z.close()
"'''),

    ("4. HTTP headers",
     "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'"),

    ("5. Landing page download link",
     "curl -s https://on-server2.com/app/ | grep -o 'href=\"[^\"]*apk[^\"]*\"'"),

    ("6. Cleanup",
     "rm -f /tmp/verify_apk.apk"),
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print(f"Connecting to {host}...")
client.connect(host, username=user, password=password, timeout=15)
print("Connected!\n")

for label, cmd in commands:
    print(f"{'='*60}")
    print(f"  {label}")
    print(f"{'='*60}")
    print(f"$ {cmd[:120]}{'...' if len(cmd)>120 else ''}\n")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=30)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out:
        print(out)
    if err:
        print(f"[stderr] {err}")
    print()

client.close()
print("Done. Connection closed.")
