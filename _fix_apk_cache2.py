import paramiko, time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

# Restart LiteSpeed to pick up .htaccess changes
print("Restarting LiteSpeed...")
_, stdout, stderr = ssh.exec_command("/usr/local/lsws/bin/lswsctrl restart")
time.sleep(3)
print(stdout.read().decode())
print(stderr.read().decode())

# Test again
print("Testing headers after restart...")
_, stdout, _ = ssh.exec_command("curl -sI https://on-server2.com/downloads/on-server1.apk 2>/dev/null | head -20")
time.sleep(3)
headers = stdout.read().decode()
print(headers)

if "no-cache" in headers or "no-store" in headers:
    print("✅ Cache headers applied!")
else:
    print("⚠️ Cache headers not applied - trying alternative approach...")
    # Use LiteSpeed's native context approach via .htaccess with different syntax
    htaccess = """<IfModule mod_headers.c>
  <FilesMatch "\\.apk$">
    Header set Cache-Control "no-cache, no-store, must-revalidate"
    Header set Pragma "no-cache"  
    Header set Expires "0"
  </FilesMatch>
</IfModule>

# Fallback: use mod_expires
<IfModule mod_expires.c>
  <FilesMatch "\\.apk$">
    ExpiresActive On
    ExpiresDefault "access plus 0 seconds"
  </FilesMatch>
</IfModule>
"""
    sftp = ssh.open_sftp()
    with sftp.file("/home/on-server2.com/public_html/downloads/.htaccess", "w") as f:
        f.write(htaccess)
    sftp.close()
    
    # Restart again
    ssh.exec_command("/usr/local/lsws/bin/lswsctrl restart")
    time.sleep(3)
    
    _, stdout, _ = ssh.exec_command("curl -sI https://on-server2.com/downloads/on-server1.apk 2>/dev/null | head -20")
    time.sleep(3)
    print(stdout.read().decode())

ssh.close()
print("Done!")
