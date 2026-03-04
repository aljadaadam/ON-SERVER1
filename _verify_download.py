import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

commands = [
    ("1. Download APK via exact URL and check it",
     "curl -s -o /tmp/test_download.apk 'https://on-server2.com/downloads/on-server1.apk?v=20260303' && ls -la /tmp/test_download.apk && file /tmp/test_download.apk"),

    ("2. Check first bytes (should start with PK)",
     "xxd /tmp/test_download.apk | head -3"),

    ("3. Check APK can be read as ZIP",
     "python3 -c \"\nimport zipfile\nz = zipfile.ZipFile('/tmp/test_download.apk')\nprint('Valid ZIP/APK')\nprint(f'Files count: {len(z.namelist())}')\nhas_manifest = 'AndroidManifest.xml' in z.namelist()\nprint(f'Has AndroidManifest.xml: {has_manifest}')\ndex_files = [n for n in z.namelist() if n.endswith('.dex')]\nprint(f'DEX files: {dex_files}')\nprint(f'Total size: {sum(i.file_size for i in z.infolist())} bytes uncompressed')\nz.close()\n\""),

    ("4. Compare downloaded file with source file",
     "md5sum /tmp/test_download.apk /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("5. Test downloading via localhost (Express directly)",
     "curl -s -o /tmp/test_download2.apk 'http://localhost:3000/downloads/on-server1.apk?v=20260303' && ls -la /tmp/test_download2.apk && md5sum /tmp/test_download2.apk"),

    ("6. Check if /downloads route exists in backend",
     "grep -n 'downloads' /opt/on-server1/on-server1-backend/src/index.ts"),

    ("7. PM2 status",
     "pm2 status"),

    ("8. Last git pull status",
     "cd /opt/on-server1 && git log --oneline -3"),

    ("9. Clean up",
     "rm -f /tmp/test_download.apk /tmp/test_download2.apk"),
]

for label, cmd in commands:
    print("=" * 70)
    print(f">>> {label}")
    print(f"CMD: {cmd}")
    print("-" * 70)
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    if out:
        print(out)
    if err:
        print(f"STDERR: {err}")
    print()

ssh.close()
print("Done.")
