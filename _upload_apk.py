import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

LOCAL_APK = r"c:\Users\Eng-adam\ON-SERVER1\on-server1-app\app\build\outputs\apk\release\app-release.apk"
REMOTE_APK = "/home/www.on-server2.com/public_html/downloads/on-server1.apk"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print("Connecting...")
ssh.connect(HOST, username=USER, password=PASS)
print("Connected. Ensuring directory exists...")
ssh.exec_command("mkdir -p /home/www.on-server2.com/public_html/downloads")
import time; time.sleep(1)
print("Uploading APK...")
sftp = ssh.open_sftp()
sftp.put(LOCAL_APK, REMOTE_APK)
print("APK uploaded successfully!")
# Verify
stat = sftp.stat(REMOTE_APK)
print(f"Remote file size: {stat.st_size} bytes")
sftp.close()
ssh.close()
print("Done!")
