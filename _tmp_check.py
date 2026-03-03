import paramiko, json
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, e = ssh.exec_command(cmd, timeout=30)
    return o.read().decode().strip(), e.read().decode().strip()

out, _ = run("curl -s 'https://on-server2.com/api/products?limit=3&serviceType=SERVER'")
data = json.loads(out)
for p in data['data']['products'][:3]:
    print(f"Name: {p['name'][:50]}")
    print(f"fields type: {type(p['fields']).__name__}")
    print(f"fields value: {json.dumps(p['fields'], ensure_ascii=False)[:200]}")
    print()

ssh.close()
