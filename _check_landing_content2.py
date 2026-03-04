import paramiko

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

commands = [
    ("3. Check what CSS/fonts/images the page needs",
     """curl -s http://localhost:3000/app/ | grep -oP '(src|href)="[^"]*"' | head -20"""),

    ("7. Check for broken image/resource references",
     """curl -s http://localhost:3000/app/ | grep -oP 'src="[^"]*"' | head -20"""),

    ("9. Does the page reference resources under /app/ path?",
     """curl -s http://localhost:3000/app/ | grep -oP '(src|href|url\\()="?[^")\\s]*"?' | grep -v 'http\\|data:\\|#\\|mailto' | head -20"""),

    ("10. Check the full HTML body - data sections",
     """curl -s http://localhost:3000/app/ | grep -oP '<section[^>]*id="[^"]*"' """),
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=15)

for label, cmd in commands:
    print("=" * 80)
    print(f">>> {label}")
    print("-" * 80)
    # Run directly without wrapping in bash -c
    stdin, stdout, stderr = client.exec_command(cmd, timeout=15)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    if out.strip():
        print(out)
    else:
        print("(no output)")
    if err.strip():
        print(f"STDERR: {err}")
    print()

client.close()
print("Done.")
