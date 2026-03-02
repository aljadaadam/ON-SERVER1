import paramiko
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Get the full error log around the order failure
stdin,stdout,stderr = ssh.exec_command('pm2 logs on-server1-backend --lines 100 --nostream 2>&1 | grep -A5 -B5 "createOrder\|Order failed\|products not found\|orderService\|orders.ts"')
out = stdout.read().decode()
print('--- ORDER ERROR DETAILS ---')
print(out if out else 'No order-related errors found')

# Also check if there are any recent orders in DB
stdin,stdout,stderr = ssh.exec_command('cd /opt/on-server1/on-server1-backend && npx prisma db execute --stdin <<EOF\nSELECT id, orderNumber, status, totalAmount, createdAt FROM "Order" ORDER BY createdAt DESC LIMIT 5;\nEOF')
print('\n--- RECENT ORDERS ---')
print(stdout.read().decode())
print(stderr.read().decode())

ssh.close()
