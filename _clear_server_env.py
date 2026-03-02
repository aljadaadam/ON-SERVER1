import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, e = ssh.exec_command(cmd, timeout=15)
    out = o.read().decode().strip()
    err = e.read().decode().strip()
    if out: print(out)
    if err and 'TERM' not in err: print(f"ERR: {err}")
    return out

# 1. Clear EXTERNAL_PROVIDER lines from server .env
print("=== Clear provider from server .env ===")
run("""cd /opt/on-server1/on-server1-backend && sed -i '/^EXTERNAL_PROVIDER/d' .env""")

# 2. Verify
print("\n=== Verify .env (no EXTERNAL_PROVIDER) ===")
result = run("cd /opt/on-server1/on-server1-backend && grep -i 'EXTERNAL_PROVIDER' .env || echo '(none found - clean!)'")

# 3. Restart backend to pick up cleaned env
print("\n=== Restart backend ===")
run("cd /opt/on-server1/on-server1-backend && pm2 restart on-server1-backend")

print("\nDone!")
ssh.close()
