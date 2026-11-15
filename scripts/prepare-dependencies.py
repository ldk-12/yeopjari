"""Export hash-verified dependencies from an existing authorized Gradle cache."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('--cache', type=Path, default=Path.home() / '.gradle/caches/modules-2/files-2.1')
parser.add_argument('--output', type=Path, default=Path('build/dependencies'))
args = parser.parse_args()
root = Path(__file__).resolve().parent.parent
rows = json.loads((root / 'licenses/dependencies-lock.json').read_text())
exports = []
for row in rows:
    matched = None
    for origin in row['origins']:
        group, artifact, version = origin['coordinate'].split(':')
        for path in (args.cache / group / artifact / version).glob('*/*'):
            if path.name != origin['artifact']:
                continue
            raw = path.read_bytes()
            if hashlib.sha256(raw).hexdigest() != origin['sha256']:
                continue
            if origin['extract']:
                with zipfile.ZipFile(path) as archive:
                    raw = archive.read(origin['extract'])
            if hashlib.sha256(raw).hexdigest() == row['sha256']:
                matched = raw
                break
    if matched is None:
        raise SystemExit('Missing verified artifact: ' + row['file'] + '. Obtain the recorded Maven version through the official SDK setup first.')
    exports.append((row['file'], matched))
args.output.mkdir(parents=True, exist_ok=True)
for name, data in exports:
    (args.output / name).write_bytes(data)
print('Exported', len(exports), 'verified dependencies to', args.output)
