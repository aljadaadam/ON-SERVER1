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

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print(f"Connecting to {HOST}...")
ssh.connect(HOST, username=USER, password=PASS)
print("Connected.\n")

# ── 1. Read current vhost config ──
vhost_path = '/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf'
current_vhost = run(ssh, f'cat {vhost_path}', "STEP 1: Current vhost config")

# ── 2. Modify vhost config ──
print("\n" + "="*60)
print("  STEP 2: Modifying vhost config")
print("="*60)

modify_vhost_script = r'''
import re

VHOST = '/usr/local/lsws/conf/vhosts/www.on-server2.com/vhost.conf'

with open(VHOST, 'r') as f:
    content = f.read()

# --- Add proxy context for /ctrl-7x9a3k/ ---
proxy_context = """
context /ctrl-7x9a3k/ {
  type                    proxy
  handler                 backend_proxy
  addDefaultCharset       off
}
"""

if 'context /ctrl-7x9a3k/' not in content:
    # Insert before the last closing brace of the virtualhost (or at end of contexts)
    # Find the last context block and add after it
    last_context_pos = content.rfind('context /')
    if last_context_pos != -1:
        # Find the closing brace of that context block
        brace_count = 0
        i = content.index('{', last_context_pos)
        for j in range(i, len(content)):
            if content[j] == '{':
                brace_count += 1
            elif content[j] == '}':
                brace_count -= 1
                if brace_count == 0:
                    insert_pos = j + 1
                    break
        content = content[:insert_pos] + proxy_context + content[insert_pos:]
        print("Added proxy context for /ctrl-7x9a3k/")
    else:
        print("ERROR: Could not find existing context blocks")
else:
    print("Proxy context for /ctrl-7x9a3k/ already exists")

# --- Add rewrite rule ---
rewrite_rule = '  RewriteRule ^/ctrl-7x9a3k(/.*)?$ - [L]\n'
if 'ctrl-7x9a3k' not in content.split('rewrite')[1] if 'rewrite' in content else True:
    # Find the rewrite block and add the rule before the existing RewriteRule lines
    # Look for the first RewriteRule in the rewrite block
    match = re.search(r'(  RewriteRule\s+\^/api/)', content)
    if match:
        insert_pos = match.start()
        content = content[:insert_pos] + rewrite_rule + content[insert_pos:]
        print("Added rewrite rule for /ctrl-7x9a3k/")
    else:
        # Try another approach - find any RewriteRule line
        match = re.search(r'(  RewriteRule\s)', content)
        if match:
            insert_pos = match.start()
            content = content[:insert_pos] + rewrite_rule + content[insert_pos:]
            print("Added rewrite rule for /ctrl-7x9a3k/")
        else:
            print("ERROR: Could not find rewrite rules")
else:
    print("Rewrite rule for /ctrl-7x9a3k/ already exists")

with open(VHOST, 'w') as f:
    f.write(content)

print("\nUpdated vhost config:")
print(open(VHOST).read())
'''

run(ssh, f"python3 -c {repr(modify_vhost_script)}", None)

# ── 3. Modify Express backend index.ts ──
print("\n" + "="*60)
print("  STEP 3: Modifying Express backend index.ts")
print("="*60)

modify_index_script = r'''
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
        print("Added dashboard routes before Health check")
    else:
        print("ERROR: Could not find '// Health check' marker")
else:
    print("Dashboard routes already exist")

with open(INDEX, 'w') as f:
    f.write(content)

print("Done modifying index.ts")
'''

run(ssh, f"python3 -c {repr(modify_index_script)}", None)

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

ssh.close()
print("\n" + "="*60)
print("  ALL DONE")
print("="*60)
