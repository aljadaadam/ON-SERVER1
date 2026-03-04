import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

# Check stat-number styles on deployed page
stdin, stdout, stderr = ssh.exec_command('grep -n "stat-number" /home/www.on-server2.com/public_html/app/index.html')
print("=== stat-number lines ===")
print(stdout.read().decode())

# Check for any cyan/blue color codes
stdin, stdout, stderr = ssh.exec_command("grep -in 'cyan\\|00BCD4\\|22D3EE\\|06B6D4\\|0EA5E9\\|38BDF8\\|00D4FF\\|00BFFF\\|1CC7D0\\|0891b2\\|67E8F9' /home/www.on-server2.com/public_html/app/index.html")
print("=== Cyan/blue colors ===")
print(stdout.read().decode())

# Get the stat-number CSS block context
stdin, stdout, stderr = ssh.exec_command("grep -n -A5 'stat-number' /home/www.on-server2.com/public_html/app/index.html | head -30")
print("=== stat-number with context ===")
print(stdout.read().decode())

# Check what --gold resolves to
stdin, stdout, stderr = ssh.exec_command("grep -n 'gold' /home/www.on-server2.com/public_html/app/index.html | head -10")
print("=== gold variable ===")
print(stdout.read().decode())

ssh.close()
