"""Keep the repository source-only; this is not a full secret audit."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
blocked = {'.apk', '.aab', '.aar', '.jar', '.zip', '.jks', '.keystore', '.p12', '.pfx', '.pem', '.key'}
patterns = [r'/Users/[a-zA-Z0-9_-]+/', r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----', r'gh[pousr]_[A-Za-z0-9]{30,}']
count = 0
for path in root.rglob('*'):
    relative = path.relative_to(root)
    if not path.is_file() or any(part in {'.git', 'build', '__pycache__'} for part in relative.parts):
        continue
    assert path.suffix not in blocked, f'Binary or signing artifact: {relative}'
    assert path.name not in {'.env', 'signing-password.txt', 'local.properties'}, relative
    text = path.read_text(encoding='utf-8')
    assert not any(re.search(pattern, text) for pattern in patterns), f'Private content pattern: {relative}'
    count += 1
print(f'PASS source-only file and private-content pattern checks: {count} files. Not a comprehensive secret audit.')
