import sys
import traceback

OUTPUT_FILE = r"c:\Users\Eng-adam\ON-SERVER1\_tmp_colors_result.txt"

# Write immediately to confirm script starts
with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
    f.write("Script started\n")

try:
    import paramiko
    with open(OUTPUT_FILE, 'a', encoding='utf-8') as f:
        f.write("Paramiko imported OK\n")

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    with open(OUTPUT_FILE, 'a', encoding='utf-8') as f:
        f.write("Connecting to SSH...\n")

    ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@', timeout=15)

    with open(OUTPUT_FILE, 'a', encoding='utf-8') as f:
        f.write("Connected!\n")

    commands = [
        ("=== 1. Color hex codes (gold/cyan/#hex) ===",
         r"grep -n 'gold\|cyan\|#[0-9A-Fa-f]\{6\}\|#[0-9A-Fa-f]\{3\}' /home/www.on-server2.com/public_html/app/index.html | head -50"),
        ("=== 2. :root CSS variables (lines 15-35) ===",
         "sed -n '15,35p' /home/www.on-server2.com/public_html/app/index.html"),
        ("=== 3. stat-number references ===",
         "grep -n 'stat-number' /home/www.on-server2.com/public_html/app/index.html"),
        ("=== 4. gradient-text / hero h1 ===",
         r"grep -n 'gradient-text\|hero h1' /home/www.on-server2.com/public_html/app/index.html | head -10"),
        ("=== 5. btn-primary / btn-secondary ===",
         r"grep -n 'btn-primary\|btn-secondary' /home/www.on-server2.com/public_html/app/index.html | head -15"),
    ]

    with open(OUTPUT_FILE, 'a', encoding='utf-8') as f:
        for label, cmd in commands:
            f.write(label + "\n")
            stdin, stdout, stderr = ssh.exec_command(cmd, timeout=10)
            out = stdout.read().decode('utf-8', errors='replace')
            err = stderr.read().decode('utf-8', errors='replace')
            if out:
                f.write(out + "\n")
            if err:
                f.write("STDERR: " + err + "\n")
            f.write("\n")
        f.write("Done.\n")

    ssh.close()

except Exception as e:
    with open(OUTPUT_FILE, 'a', encoding='utf-8') as f:
        f.write(f"\nERROR: {type(e).__name__}: {e}\n")
        f.write(traceback.format_exc())
    sys.exit(1)
