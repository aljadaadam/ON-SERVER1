import paramiko

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

commands = [
    "rm -f /home/www.on-server2.com/public_html/downloads/on-server1.apk",
    "ls -la /home/www.on-server2.com/public_html/downloads/",
    "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'",
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password)

for cmd in commands:
    print(f"\n>>> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out:
        print(out)
    if err:
        print(err)

client.close()
print("\nDone.")
