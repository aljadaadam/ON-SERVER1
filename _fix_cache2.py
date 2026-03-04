import paramiko
import time

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"

def ssh_exec(ssh, cmd, timeout=60):
    print(f"\n>>> {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out.strip():
        print(out.strip())
    if err.strip():
        print(f"STDERR: {err.strip()}")
    return out.strip()

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

# ============================
# 1. Read all vhost configs
# ============================
print("=== 1. Read vhost configs ===")

# List all vhost config files
ssh_exec(ssh, "find /usr/local/lsws/conf/vhosts/ -name 'vhost.conf' -exec echo {} \\;")

# Read www.on-server2.com vhost (where dashboard + api live)
print("\n--- www.on-server2.com vhost ---")
www_vhost = ssh_exec(ssh, "cat /usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf")

# Check on-server2.com (without www) - landing page
print("\n--- on-server2.com vhost (if exists) ---")
on_vhost = ssh_exec(ssh, "cat /usr/local/lsws/conf/vhosts/on-server2.com/vhost.conf 2>/dev/null || echo 'NOT FOUND'")

# Check listener mappings to see which vhost handles on-server2.com
print("\n--- Listener mappings ---")
ssh_exec(ssh, "grep -A 30 'listener' /usr/local/lsws/conf/httpd_config.conf | head -60")

# ============================
# 2. Add extraHeaders to vhost configs
# ============================
print("\n=== 2. Add extraHeaders to vhost configs ===")

# For www.on-server2.com vhost
www_vhost_path = "/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf"

if 'no-store' not in www_vhost:
    # Add extraHeaders at the beginning of the vhost config
    # LiteSpeed extraHeaders syntax
    extra_headers = 'Cache-Control: no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0\\nPragma: no-cache\\nExpires: 0'
    
    # Use sed to add extraHeaders after the first line (or before first context/rewrite block)
    # In LiteSpeed, extraHeaders goes in docRoot context or at vhost level
    ssh_exec(ssh, f"""cat > /tmp/add_headers.py << 'PYEOF'
import sys
path = sys.argv[1]
with open(path, 'r') as f:
    content = f.read()

# Check if extraHeaders already exists
if 'extraHeaders' in content:
    print("extraHeaders already exists, will update")
else:
    # Add extraHeaders right after docRoot line
    lines = content.split('\\n')
    new_lines = []
    added = False
    for line in lines:
        new_lines.append(line)
        if 'docRoot' in line and not added:
            new_lines.append('')
            new_lines.append('extraHeaders            <<<END_extraHeaders')
            new_lines.append('Cache-Control: no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0')
            new_lines.append('Pragma: no-cache')
            new_lines.append('Expires: 0')
            new_lines.append('END_extraHeaders')
            added = True
    
    with open(path, 'w') as f:
        f.write('\\n'.join(new_lines))
    
    print(f"Added extraHeaders to {{path}}")

PYEOF
python3 /tmp/add_headers.py {www_vhost_path}""")
    
    print("\nUpdated www.on-server2.com vhost:")
    ssh_exec(ssh, f"cat {www_vhost_path}")
else:
    print("www.on-server2.com already has no-store headers")

# Also check if on-server2.com (bare domain for landing page) has its own vhost
# If so, add headers there too
bare_vhost_path = "/usr/local/lsws/conf/vhosts/on-server2.com/vhost.conf"
bare_exists = ssh_exec(ssh, f"test -f {bare_vhost_path} && echo 'EXISTS' || echo 'NOT_FOUND'")

if bare_exists == 'EXISTS':
    bare_vhost = ssh_exec(ssh, f"cat {bare_vhost_path}")
    if 'no-store' not in bare_vhost:
        ssh_exec(ssh, f"python3 /tmp/add_headers.py {bare_vhost_path}")
        print("\nUpdated on-server2.com vhost:")
        ssh_exec(ssh, f"cat {bare_vhost_path}")
else:
    print(f"\non-server2.com vhost not found - landing page is probably under www.on-server2.com")

# Also handle on-server1.com if it exists
os1_vhost = "/usr/local/lsws/conf/vhosts/on-server1.com/vhost.conf"
os1_exists = ssh_exec(ssh, f"test -f {os1_vhost} && echo 'EXISTS' || echo 'NOT_FOUND'")
if os1_exists == 'EXISTS':
    os1_content = ssh_exec(ssh, f"cat {os1_vhost}")
    if 'no-store' not in os1_content:
        ssh_exec(ssh, f"python3 /tmp/add_headers.py {os1_vhost}")

# ============================
# 3. Disable expires for text/html (so it doesn't override our no-store with public,max-age=0)
# ============================
print("\n=== 3. Fix expires config ===")
# Remove text/html from expiresByType (our extraHeaders will handle it)
# Actually keep A0 since it also helps. The extraHeaders should override.
# Let's leave it as is for now.

# ============================
# 4. Restart LiteSpeed
# ============================
print("\n=== 4. Restart LiteSpeed ===")
ssh_exec(ssh, "/usr/local/lsws/bin/lswsctrl restart")
time.sleep(5)

# ============================
# 5. Test headers
# ============================
print("\n=== 5. Test headers ===")

print("\n--- Landing page ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/app/")

print("\n--- Dashboard ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/ctrl-7x9a3k/")

print("\n--- API ---")
ssh_exec(ssh, "curl -sI https://on-server2.com/api/health")

print("\n--- APK download ---")
ssh_exec(ssh, "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'")

print("\n=== DONE ===")
ssh.close()
