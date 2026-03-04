import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

commands = [
    ("1. APK file details",
     "ls -la /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("2. File hash",
     "md5sum /home/www.on-server2.com/public_html/downloads/on-server1.apk"),

    ("3. Test download",
     "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'"),

    ("4. Check aapt",
     "which aapt 2>/dev/null || which aapt2 2>/dev/null || echo 'NO_AAPT'"),

    ("5. Read APK META-INF files",
     'python3 -c "\nimport zipfile, re\nz = zipfile.ZipFile(\'/home/www.on-server2.com/public_html/downloads/on-server1.apk\')\nfor n in z.namelist():\n    if \'META-INF\' in n:\n        print(n)\n" 2>/dev/null || echo \'FAILED\''),

    ("6. Check APK signing",
     'python3 -c "\nimport zipfile\nz = zipfile.ZipFile(\'/home/www.on-server2.com/public_html/downloads/on-server1.apk\')\nsig_files = [n for n in z.namelist() if n.startswith(\'META-INF/\') and (n.endswith(\'.RSA\') or n.endswith(\'.SF\') or n.endswith(\'.MF\'))]\nfor f in sig_files:\n    print(f)\nif sig_files:\n    print(\'APK IS SIGNED\')\nelse:\n    print(\'APK IS NOT SIGNED\')\n" 2>/dev/null || echo \'FAILED\''),
]

for label, cmd in commands:
    print("=" * 60)
    print(f">>> {label}")
    print("=" * 60)
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out:
        print(out)
    if err:
        print("STDERR:", err)
    print()

ssh.close()
print("Done.")
