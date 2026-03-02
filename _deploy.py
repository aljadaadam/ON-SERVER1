import paramiko
import sys

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@', timeout=15)

env_content = """NODE_ENV=production
PORT=3000
DATABASE_URL="file:./dev.db"
JWT_SECRET=onserver1-prod-jwt-secret-x9k2m4p7
JWT_REFRESH_SECRET=onserver1-prod-refresh-secret-h3j5n8q1
JWT_EXPIRES_IN=24h
JWT_REFRESH_EXPIRES_IN=30d
OTP_EXPIRY_MINUTES=5
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=
SMTP_PASS=
EXTERNAL_PROVIDER_URL=https://sd-unlocker.com/api/index.php
EXTERNAL_PROVIDER_USERNAME=aljadadm654
EXTERNAL_PROVIDER_API_KEY=Z4U-MIH-600-V7V-JNQ-ZTP-W3B-A7W
EXTERNAL_PROVIDER_SECRET=
ADMIN_EMAIL=admin@onserver1.com
ADMIN_PASSWORD=admin123456
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=5242880
DASHBOARD_URL=https://on-server1.com
WEB_URL=https://www.on-server1.com
"""

# Write .env via SFTP
sftp = ssh.open_sftp()
with sftp.open('/opt/on-server1/on-server1-backend/.env', 'w') as f:
    f.write(env_content)
sftp.close()
print("ENV_CREATED_OK")

# Install backend dependencies
print("=== Installing backend dependencies ===")
stdin, stdout, stderr = ssh.exec_command('cd /opt/on-server1/on-server1-backend && npm install --production=false 2>&1', timeout=120)
out = stdout.read().decode()
print(out[-500:] if len(out) > 500 else out)

# Generate Prisma client
print("=== Generating Prisma client ===")
stdin, stdout, stderr = ssh.exec_command('cd /opt/on-server1/on-server1-backend && npx prisma generate 2>&1', timeout=60)
print(stdout.read().decode())

# Run Prisma DB push
print("=== Pushing DB schema ===")
stdin, stdout, stderr = ssh.exec_command('cd /opt/on-server1/on-server1-backend && npx prisma db push 2>&1', timeout=60)
print(stdout.read().decode())

# Create uploads dir
stdin, stdout, stderr = ssh.exec_command('mkdir -p /opt/on-server1/on-server1-backend/uploads/receipts', timeout=10)
stdout.read()

# Seed the database
print("=== Seeding database ===")
stdin, stdout, stderr = ssh.exec_command('cd /opt/on-server1/on-server1-backend && npx tsx src/seed.ts 2>&1', timeout=60)
print(stdout.read().decode())

ssh.close()
print("BACKEND_SETUP_DONE")
