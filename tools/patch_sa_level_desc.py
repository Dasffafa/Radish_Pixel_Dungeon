# -*- coding: utf-8 -*-
"""次要护甲效果描述：把「（10+5*等级）%」这类公式改成按当前等级算好的数字。

做法：基类 desc() 把 effect_desc 改成带变参调用，并加 effectArgs() 钩子；
12 个子类各自覆写 effectArgs()；中英文文本把公式换成占位符。
"""
import io, os, re

BASE = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon'
SEC = BASE + r'/items/armor/SecondaryArmor.java'
SUB = BASE + r'/items/armor/secondary'
ZH = r'core/src/main/assets/messages/items/items_zh.properties'
EN = r'core/src/main/assets/messages/items/items.properties'

# ---------- 1) 基类 ----------
s = io.open(SEC, encoding='utf-8').read()
old = 'sb.append(Messages.get(this, "effect_desc"));'
assert s.count(old) == 1, s.count(old)
s = s.replace(old, 'sb.append(Messages.get(this, "effect_desc", effectArgs()));')
anchor = 'public String desc() {'
assert s.count(anchor) == 1
s = s.replace(anchor, '''/**
	 * 效果描述里要显示的当前等级数值，按顺序对应 effect_desc 里的 %d。
	 * 子类覆写；没有数值的子类保持空数组。
	 */
	public Object[] effectArgs() {
		return new Object[0];
	}

	public String desc() {''')
io.open(SEC, 'w', encoding='utf-8', newline='').write(s)

# ---------- 2) 子类 ----------
ARGS = {
    'ChampionBelt': 'return new Object[]{ 10 + 5 * level() };',
    'BlackBelt': 'return new Object[]{ 2 + level(), 4 + level() };',
    'InfusedArmband': 'return new Object[]{ 2 + level() };',
    'CatalystBracer': 'return new Object[]{ Math.round( 1 + 0.2f * level() ) };',
    'Shelter': 'return new Object[]{ 3 + level() };',
    'GriffinEye': 'return new Object[]{ 25 + 15 * level() };',
    'WebBelt': 'return new Object[]{ 15 + 10 * level() };',
    'ThePure': 'return new Object[]{ Math.round( 2 + 0.2f * level() ) };',
    'TridentGauntlet': 'return new Object[]{ 20 + 10 * level() };',
    'ShiningGloves': 'return new Object[]{ 25 + 5 * level() };',
    'Greaves': 'return new Object[]{ 10 + 2 * level() };',
}

for name, body in ARGS.items():
    p = os.path.join(SUB, name + '.java')
    t = io.open(p, encoding='utf-8').read()
    if 'effectArgs' in t:
        continue
    m = re.search(r'public\s+class\s+' + name + r'[^{]*\{', t)
    assert m, name
    ins = m.group(0) + '''

	@Override
	public Object[] effectArgs() {
		%s
	}
''' % body
    t = t[:m.start()] + ins + t[m.end():]
    io.open(p, 'w', encoding='utf-8', newline='').write(t)

# ---------- 3) 文本 ----------
ZH_MAP = [
    ('（10+5*等级）%', '%d%%'),
    ('（2+等级）回合', '%d 回合'),
    ('（2~4+等级）点', '（2~%d）点'),
    ('（2+等级）点护盾', '%d 点护盾'),
    ('（1+0.2*等级）点炼金能量', '%d 点炼金能量'),
    ('（3+等级）点护盾', '%d 点护盾'),
    ('（25+15*等级）%', '%d%%'),
    ('（15+10*等级）%', '%d%%'),
    ('（2+等级*0.2）点', '%d 点'),
    ('（20+10*等级）%', '%d%%'),
    ('（25+5*等级）%', '%d%%'),
    ('（10+2*等级）%', '%d%%'),
]
EN_MAP = [
    ('(10+5*lvl)%', '%d%%'), ('(10 + 5*lvl)%', '%d%%'),
    ('(2+lvl) turns', '%d turns'),
    ('(2~4+lvl)', '(2~%d)'),
    ('(2+lvl) shield', '%d shield'),
    ('(1+0.2*lvl)', '%d'), ('(1+0.2*lvl)', '%d'),
    ('(3+lvl) shield', '%d shield'),
    ('(25+15*lvl)%', '%d%%'), ('(15+10*lvl)%', '%d%%'),
    ('(2+lvl*0.2)', '%d'), ('(20+10*lvl)%', '%d%%'),
    ('(25+5*lvl)%', '%d%%'), ('(10+2*lvl)%', '%d%%'),
]

def patch(path, pairs):
    t = io.open(path, encoding='utf-8', newline='').read()
    n = 0
    for a, b in pairs:
        if a in t:
            t = t.replace(a, b)
            n += 1
    io.open(path, 'w', encoding='utf-8', newline='').write(t)
    print(os.path.basename(path), 'replaced patterns:', n)

patch(ZH, ZH_MAP)
patch(EN, EN_MAP)
print('done')
