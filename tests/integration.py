from pathlib import Path
import xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1];s=(r/'android/src/main/java/app/pleos/coachboundary/MainActivity.java').read_text();a=r/'android/src/main/assets'
m=ET.parse(r/'android/src/main/AndroidManifest.xml').getroot();ns='{http://schemas.android.com/apk/res/android}'
assert m.get('package')=='app.pleos.coachboundary.integrated'
assert not any('LOCATION' in x.get(ns+'name','') or 'INTERNET' in x.get(ns+'name','') for x in m.findall('uses-permission'))
assert not any(x.get(ns+'name')=='distractionOptimized' for x in m.iter('meta-data'))
assert m.find('application').get(ns+'appCategory')=='productivity'
for token in ['uxKnown && !uxRequiresDo && uxFlags == 0','getCurrentCarUxRestrictions()','registerListener','onServiceDisconnected','webView.setVisibility(View.GONE)','Type.systemBars()','unregisterListener()']:
 assert token in s,token
assert s.index('webView.setVisibility(View.GONE)') < s.index('webView.loadUrl(START_URL)')
assert 'addJavascriptInterface' not in s
assert '<script>' not in (a/'index.html').read_text()
assert '<script src="app.js"></script>' in (a/'index.html').read_text()
assert "script-src 'self'" in s
assert 'engine.js' not in [p.name for p in a.iterdir()]
print('PASS manifest, fail-closed source structure, CSP asset split, SDK/synthetic boundary. Static only; no Android runtime.')
