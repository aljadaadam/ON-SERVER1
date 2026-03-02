import paramiko

HOST = "153.92.208.129"
USER = "root"
PASS = "Mahe1000amd@"
BACKEND = "/opt/on-server1/on-server1-backend"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(HOST, username=USER, password=PASS)

def run(cmd):
    print(f"\n> {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out: print(out)
    if err: print(err)
    return out

# Delete default products that have no externalId (seed products don't have externalId)
# Also delete default banners
run(f"""cd {BACKEND} && node -e "
const {{ PrismaClient }} = require('@prisma/client');
const prisma = new PrismaClient();
(async () => {{
  // Delete products without externalId (these are seed/default products)
  const defaultProducts = await prisma.product.findMany({{ where: {{ externalId: null }} }});
  console.log('Default products found:', defaultProducts.length);
  for (const p of defaultProducts) {{
    console.log('  -', p.name);
  }}
  
  if (defaultProducts.length > 0) {{
    const del = await prisma.product.deleteMany({{ where: {{ externalId: null }} }});
    console.log('Deleted', del.count, 'default products');
  }}
  
  // Delete default banners
  const banners = await prisma.banner.findMany();
  console.log('Banners found:', banners.length);
  for (const b of banners) {{
    console.log('  -', b.title);
  }}
  
  // Count remaining products
  const remaining = await prisma.product.count();
  console.log('Remaining products:', remaining);
  
  await prisma.\$disconnect();
}})();
"
""")

# Pull updated seed.ts
run(f"cd /opt/on-server1 && git pull origin main")

ssh.close()
print("\n✅ Done")
