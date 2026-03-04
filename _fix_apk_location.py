import paramiko

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

commands = [
    ("Check APK at wrong path",
     "ls -la /home/on-server2.com/public_html/downloads/on-server1.apk"),

    ("Copy APK to correct web root",
     "cp /home/on-server2.com/public_html/downloads/on-server1.apk /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("Fix ownership",
     "chown wwwon3882:wwwon3882 /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("Set permissions",
     "chmod 644 /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("Verify file at correct path",
     "ls -la /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("Download via public URL and verify size",
     "curl -s -o /tmp/verify_apk.apk 'https://on-server2.com/downloads/on-server1.apk?v=20260303' && ls -la /tmp/verify_apk.apk"),

    ("Validate APK structure",
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

    ("Check HTTP headers",
     "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'"),

    ("Landing page download link",
     "curl -s https://on-server2.com/app/ | grep -o 'href=\"[^\"]*apk[^\"]*\"'"),

    ("Cleanup",
     "rm -f /tmp/verify_apk.apk"),
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=15)
print("Connected.\n")

for label, cmd in commands:
    print(f"{'='*60}")
    print(f"  {label}")
    print(f"{'='*60}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=30)
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    print(out if out else "(ok)")
    if err and 'TERM' not in err:
        print(f"[stderr] {err}")
    print()

client.close()
print("Done.")
