import zipfile,os
from pathlib import Path
with zipfile.ZipFile('build/base.apk') as z: files={n:z.read(n) for n in z.namelist()}
for p in Path('build/dex').glob('*.dex'): files[p.name]=p.read_bytes()
for p in sorted(Path(os.environ['PLEOS_DEPS_DIR']).glob('*.jar')):
 with zipfile.ZipFile(p) as z:
  for n in z.namelist():
   if n.endswith('.kotlin_builtins') or (n.startswith('META-INF/services/') and not n.endswith('/')): files[n]=z.read(n)
for p in Path('licenses').rglob('*'):
 if p.is_file(): files['assets/third_party_notices/'+str(p.relative_to('licenses'))]=p.read_bytes()
files['assets/third_party_notices/NOTICE']=Path('NOTICE').read_bytes()
with zipfile.ZipFile('build/unaligned.apk','w') as z:
 for n,d in sorted(files.items()):z.writestr(n,d)
