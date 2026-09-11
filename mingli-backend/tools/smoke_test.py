import json, sys, urllib.request
sys.stdout.reconfigure(encoding='utf-8')

BASE = 'http://127.0.0.1:8080/api/bazi/analyze'

CASES = [
    ("夜子时·广州",   dict(gender="M", year=1990, month=5, day=15, hour=23, minute=40, longitude=113.2644)),
    ("子时初·北京",   dict(gender="F", year=2000, month=1, day=1,  hour=0,  minute=10, longitude=116.4074)),
    ("跨年·上海",     dict(gender="M", year=1965, month=12, day=31, hour=12, minute=0,  longitude=121.4737)),
    ("立春临界·北京", dict(gender="F", year=2026, month=2, day=4,  hour=3,  minute=50, longitude=116.4074)),
    ("建国日·北京",   dict(gender="M", year=1949, month=10, day=1, hour=10, minute=0,  longitude=116.4074)),
    ("夏至·西安",     dict(gender="F", year=2010, month=6, day=21, hour=6,  minute=30, longitude=108.9402)),
    ("乌鲁木齐",      dict(gender="M", year=1988, month=8, day=8,  hour=8,  minute=8,  longitude=87.6168)),
    ("美东·纽约",     dict(gender="F", year=1995, month=3, day=21, hour=14, minute=0,  longitude=-74.006, tzOffset=-5)),
]

def call(payload):
    req = urllib.request.Request(BASE, data=json.dumps(payload).encode('utf-8'),
                                 headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req, timeout=20) as r:
        return json.loads(r.read().decode('utf-8'))

ok = 0
for label, p in CASES:
    p.setdefault('tzOffset', 8)
    p['useTrueSolarTime'] = True
    p['name'] = label
    try:
        d = call(p)
        assert d.get('code') == 0, d.get('msg')
        c = d['data']['chart']
        a = d['data']['analysis']
        print('%-12s %s %s %s %s | 日主%s | %s | 起运%.1f岁 | 规则%d条' % (
            label, c['year']['ganZhi'], c['month']['ganZhi'], c['day']['ganZhi'], c['hour']['ganZhi'],
            c['dayMaster'], c['strengthText'], c['qiYunAge'], len(a['ruleHits'])))
        ok += 1
    except Exception as e:
        print('%-12s FAILED: %r' % (label, e))

print('\n%d/%d 通过' % (ok, len(CASES)))
