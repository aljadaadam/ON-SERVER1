import paramiko, json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('153.92.208.129', username='root', password='Mahe1000amd@')

cmd = """cd /opt/on-server1/on-server1-backend && node -e "
const{PrismaClient}=require('@prisma/client');
const p=new PrismaClient();
(async()=>{
  const products=await p.product.findMany({where:{serviceType:'IMEI',isActive:true},select:{name:true,externalId:true,fields:true,groupName:true}});
  const fieldMap={};
  for(const pr of products){
    let f=[];
    try{f=typeof pr.fields==='string'?JSON.parse(pr.fields):pr.fields||[];}catch{}
    const keys=f.map(x=>x.name||x.key).join(', ');
    if(!fieldMap[keys])fieldMap[keys]={count:0,samples:[]};
    fieldMap[keys].count++;
    if(fieldMap[keys].samples.length<3)fieldMap[keys].samples.push(pr.name);
  }
  console.log('Total IMEI products:',products.length);
  console.log(JSON.stringify(fieldMap,null,2));
  await p['\\$disconnect']();
})();
"
"""

stdin, stdout, stderr = ssh.exec_command(cmd, timeout=30)
print(stdout.read().decode())
err = stderr.read().decode()
if 'Error' in err:
    print("ERR:", err)

ssh.close()
