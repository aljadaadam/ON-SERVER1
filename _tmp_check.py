import paramiko, json
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, e = ssh.exec_command(cmd, timeout=30)
    return o.read().decode().strip(), e.read().decode().strip()

# Get raw JSON to see the exact type of 'fields'
out, _ = run("curl -s 'https://on-server2.com/api/products?limit=2&serviceType=SERVER'")
data = json.loads(out)
for p in data['data']['products'][:2]:
    print(f"Name: {p['name'][:50]}")
    print(f"fields type: {type(p['fields']).__name__}")
    print(f"fields value: {repr(p['fields'])}")
    print()

ssh.close()
