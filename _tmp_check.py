import paramiko, time, json
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

def run(cmd):
    _, o, e = ssh.exec_command(cmd, timeout=30)
    return o.read().decode().strip(), e.read().decode().strip()

# Fix invalid order statuses on production DB
out, err = run("""cd /opt/on-server1/on-server1-backend && echo "UPDATE orders SET status = 'REJECTED' WHERE status NOT IN ('PENDING','WAITING','PROCESSING','COMPLETED','REJECTED');" | npx prisma db execute --stdin --schema prisma/schema.prisma""")
print("=== Fix invalid statuses ===")
print(out or err)

# Test stats endpoint
out, _ = run("""curl -s -X POST https://on-server2.com/api/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@onserver1.com","password":"admin123456"}'""")
data = json.loads(out)
token = data['data']['accessToken']

out, _ = run(f"curl -s https://on-server2.com/api/admin/stats -H 'Authorization: Bearer {token}'")
data = json.loads(out)
print("\n=== Stats test ===")
print(f"success: {data.get('success')}")
if data.get('success'):
    d = data['data']
    print(f"Users: {d['totalUsers']}, Products: {d['totalProducts']}, Orders: {d['totalOrders']}")
else:
    print(f"Error: {data.get('message')}")

ssh.close()
print("\nDONE")
