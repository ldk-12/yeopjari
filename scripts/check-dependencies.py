import os,json,hashlib
from pathlib import Path
root=Path(os.environ['PLEOS_DEPS_DIR'])
rows=json.loads(Path('licenses/dependencies-lock.json').read_text())
assert {p.name for p in root.glob('*.jar')}=={r['file'] for r in rows}, 'Dependency set mismatch'
for row in rows:
 assert hashlib.sha256((root/row['file']).read_bytes()).hexdigest()==row['sha256'], 'Dependency hash mismatch: '+row['file']
print('PASS exact dependency names and hashes:',len(rows))
