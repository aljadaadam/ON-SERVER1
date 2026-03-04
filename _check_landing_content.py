import paramiko

host = "153.92.208.129"
user = "root"
password = "Mahe1000amd@"

commands = [
    ("1. Check which version Express is serving (APK/download references)",
     r"curl -s http://localhost:3000/app/ | grep -i 'apk\|تحميل\|الإصدار\|version\|download\|href.*apk\|href.*download'"),

    ("2. Check the full download section",
     r"curl -s http://localhost:3000/app/ | grep -B2 -A5 'apk\|download'"),

    ("3. Check what CSS/fonts/images the page needs",
     r"""curl -s http://localhost:3000/app/ | grep -oP '(src|href)="[^"]*"' | head -20"""),

    ("4. Check for API calls (fetch/axios)",
     r"curl -s http://localhost:3000/app/ | grep -i 'fetch\|axios\|api\|XMLHttpRequest\|localhost\|on-server2'"),

    ("5. Compare the two file versions (first 20 lines)",
     r"diff <(head -20 /opt/on-server1/on-server1-landing/index.html) <(head -20 /home/www.on-server2.com/public_html/app/index.html)"),

    ("6. Check if the /opt version has cache meta tags",
     r"head -10 /opt/on-server1/on-server1-landing/index.html"),

    ("7. Check for broken image/resource references",
     r"""curl -s http://localhost:3000/app/ | grep -oP 'src="[^"]*"' | head -20"""),

    ("8. Check for JavaScript errors or missing script references",
     r"curl -s http://localhost:3000/app/ | grep -i 'script\|\.js'"),

    ("9. Does the page reference resources under /app/ path?",
     r"""curl -s http://localhost:3000/app/ | grep -oP '(src|href|url\()="?[^")\s]*"?' | grep -v 'http\|data:\|#\|mailto' | head -20"""),

    ("10. Check the full HTML body - data sections",
     r"""curl -s http://localhost:3000/app/ | grep -oP '<section[^>]*id="[^"]*"'"""),

    ("11. Check for dynamic content loaded via JS",
     r"curl -s http://localhost:3000/app/ | grep -c 'fetch\|api\|async\|await'"),

    ("12. Check visible text content (data presence)",
     r"curl -s http://localhost:3000/app/ | sed 's/<[^>]*>//g' | tr -s ' \n' | head -80"),
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=15)

for label, cmd in commands:
    print("=" * 80)
    print(f">>> {label}")
    print(f"CMD: {cmd}")
    print("-" * 80)
    # Use bash -c so process substitution works for diff
    stdin, stdout, stderr = client.exec_command(f"bash -c {repr(cmd)}", timeout=15)
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
