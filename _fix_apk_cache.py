import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print("Connecting...")
ssh.connect(HOST, username=USER, password=PASS)

# 1. Create .htaccess in downloads folder to prevent APK caching
htaccess_content = """# Prevent caching of APK files
<FilesMatch "\\.apk$">
    Header set Cache-Control "no-cache, no-store, must-revalidate"
    Header set Pragma "no-cache"
    Header set Expires "0"
    FileETag None
</FilesMatch>
"""

print("Creating downloads/.htaccess...")
sftp = ssh.open_sftp()
with sftp.file("/home/on-server2.com/public_html/downloads/.htaccess", "w") as f:
    f.write(htaccess_content)
print("Done: .htaccess created")

# 2. Deploy updated landing page
import os
local_landing = r"c:\Users\Eng-adam\ON-SERVER1\on-server1-landing\index.html"
remote_landing = "/home/on-server2.com/public_html/app/index.html"

print("Uploading landing page...")
ssh.exec_command("mkdir -p /home/on-server2.com/public_html/app")
import time; time.sleep(1)
sftp.put(local_landing, remote_landing)
print("Done: landing page updated")

# 3. Verify
stat = sftp.stat(remote_landing)
print(f"Landing page size: {stat.st_size} bytes")

# 4. Test download headers
print("\nTesting download headers...")
_, stdout, _ = ssh.exec_command("curl -sI https://on-server2.com/downloads/on-server1.apk 2>/dev/null | head -15")
time.sleep(3)
print(stdout.read().decode())

sftp.close()
ssh.close()
print("All done!")
