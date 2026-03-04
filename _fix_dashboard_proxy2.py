import paramiko
import time

HOST = '153.92.208.129'
USER = 'root'
PASS = 'Mahe1000amd@'

def run(ssh, cmd, label=None, timeout=30):
    if label:
        print(f"\n{'='*60}")
        print(f"  {label}")
        print(f"{'='*60}")
    print(f"$ {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out:
        print(out)
    if err:
        print(f"[STDERR] {err}")
    return out

def upload_and_run(ssh, local_content, remote_path, label):
    """Write a python script to the remote server via SFTP, then execute it."""
    print(f"\n{'='*60}")
    print(f"  {label}")
    print(f"{'='*60}")
    sftp = ssh.open_sftp()
    with sftp.file(remote_path, 'w') as f:
        f.write(local_content)
    sftp.close()
    return run(ssh, f'python3 {remote_path}')

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print(f"Connecting to {HOST}...")
ssh.connect(HOST, username=USER, password=PASS)
print("Connected.\n")

# ── 1. Read current vhost config ──
vhost_path = '/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf'
current_vhost = run(ssh, f'cat {vhost_path}', "STEP 1: Current vhost config")

# ── 2. Modify vhost config ──
modify_vhost_py = r'''
import re

VHOST = '/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf'

with open(VHOST, 'r') as f:
    content = f.read()

changed = False

# --- Add proxy context for /ctrl-7x9a3k/ ---
proxy_context = """
context /ctrl-7x9a3k/ {
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}
"""

if 'context /ctrl-7x9a3k/' not in content:
    # Find the last "context /.../" block and insert after it
    # We'll find all context blocks and insert after the last one (before .well-known or rewrite)
    # Strategy: insert before "rewrite  {" line
    rewrite_pos = content.find('rewrite  {')
    if rewrite_pos == -1:
        rewrite_pos = content.find('rewrite {')
    if rewrite_pos != -1:
        content = content[:rewrite_pos] + proxy_context.strip() + '\n\n' + content[rewrite_pos:]
        changed = True
        print("Added proxy context for /ctrl-7x9a3k/")
    else:
        print("ERROR: Could not find rewrite block to insert before")
else:
    print("Proxy context for /ctrl-7x9a3k/ already exists")

# --- Add rewrite rule ---
if 'RewriteRule ^/ctrl-7x9a3k(/.*)?$ - [L]' not in content:
    # Insert before the first RewriteRule in the main rewrite block
    # Find "rewrite  {" then find the first RewriteRule after it
    rewrite_block_match = re.search(r'rewrite\s*\{[^}]*?(\n\s*RewriteRule\s)', content)
    if rewrite_block_match:
        # Find the position of the first RewriteRule in the rewrite block
        rewrite_start = content.find('rewrite  {')
        if rewrite_start == -1:
            rewrite_start = content.find('rewrite {')
        first_rule = content.find('RewriteRule', rewrite_start)
        if first_rule != -1:
            # Find the start of that line
            line_start = content.rfind('\n', 0, first_rule) + 1
            new_rule = '  RewriteRule ^/ctrl-7x9a3k(/.*)?$ - [L]\n'
            content = content[:line_start] + new_rule + content[line_start:]
            changed = True
            print("Added rewrite rule for /ctrl-7x9a3k/")
        else:
            print("ERROR: Could not find RewriteRule lines")
    else:
        print("ERROR: Could not find rewrite block")
else:
    print("Rewrite rule for /ctrl-7x9a3k/ already exists")

# Also remove the old SPA-style RewriteRule for ctrl-7x9a3k that does internal rewrite to index.html
# This line: RewriteRule ^/ctrl-7x9a3k/(?!.*\.[a-zA-Z0-9]{1,10}$)(.*)$ /ctrl-7x9a3k/index.html [L]
old_rule_pattern = r'\n\s*RewriteRule \^/ctrl-7x9a3k/\(\?!.*index\.html \[L\]'
# Simpler: just find the line containing ctrl-7x9a3k and index.html in rewrite block
lines = content.split('\n')
new_lines = []
for line in lines:
    if 'ctrl-7x9a3k' in line and 'index.html' in line and 'RewriteRule' in line:
        print(f"Removing old SPA rewrite rule: {line.strip()}")
        changed = True
        continue
    new_lines.append(line)
content = '\n'.join(new_lines)

if changed:
    with open(VHOST, 'w') as f:
        f.write(content)
    print("\nVhost config updated successfully.")
else:
    print("\nNo changes needed.")

print("\n--- Updated vhost config ---")
print(open(VHOST).read())
'''

upload_and_run(ssh, modify_vhost_py, '/tmp/_fix_vhost.py', "STEP 2: Modifying vhost config")

# ── 3. Modify Express backend index.ts ──
modify_index_py = r'''
INDEX = '/opt/on-server1/on-server1-backend/src/index.ts'

with open(INDEX, 'r') as f:
    content = f.read()

dashboard_code = """
// Dashboard (SPA) - served via Express for no-cache headers
const dashboardPath = '/home/www.on-server2.com/public_html/ctrl-7x9a3k';
app.use('/ctrl-7x9a3k', express.static(dashboardPath, {
  setHeaders: (res: any) => {
    res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
}));
// SPA fallback for dashboard
app.get('/ctrl-7x9a3k/*', (_req: any, res: any) => {
  res.sendFile(dashboardPath + '/index.html');
});

"""

if 'dashboardPath' not in content:
    marker = '// Health check'
    if marker in content:
        content = content.replace(marker, dashboard_code + marker)
        with open(INDEX, 'w') as f:
            f.write(content)
        print("Added dashboard routes before Health check")
    else:
        print("ERROR: Could not find '// Health check' marker")
else:
    print("Dashboard routes already exist")

print("Done modifying index.ts")
'''

upload_and_run(ssh, modify_index_py, '/tmp/_fix_index.py', "STEP 3: Modifying Express backend index.ts")

# ── 4. Restart backend ──
run(ssh, 'pm2 restart on-server1-backend', "STEP 4: Restart backend")

# ── 5. Wait 5 seconds ──
print("\nWaiting 5 seconds...")
time.sleep(5)

# ── 6. Restart LiteSpeed ──
run(ssh, '/usr/local/lsws/bin/lswsctrl restart', "STEP 6: Restart LiteSpeed")

# ── 7. Wait 5 seconds ──
print("\nWaiting 5 seconds...")
time.sleep(5)

# ── 8. Test headers ──
run(ssh, 'curl -sI https://on-server2.com/app/', "STEP 8a: Headers for /app/")
run(ssh, 'curl -sI https://on-server2.com/ctrl-7x9a3k/', "STEP 8b: Headers for /ctrl-7x9a3k/")
run(ssh, 'curl -sI https://on-server2.com/api/health', "STEP 8c: Headers for /api/health")
run(ssh, "curl -sI 'https://on-server2.com/downloads/on-server1.apk?v=20260303'", "STEP 8d: Headers for /downloads/on-server1.apk")

# Cleanup
run(ssh, 'rm -f /tmp/_fix_vhost.py /tmp/_fix_index.py')

ssh.close()
print("\n" + "="*60)
print("  ALL DONE")
print("="*60)
