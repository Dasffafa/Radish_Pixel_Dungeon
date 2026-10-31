# -*- coding: utf-8 -*-
"""给 12 件次要护甲补「特色描述」（flavor），并让 desc() 把它排在效果说明之前。"""
import io

FLAVOR = {
    'championbelt': '只有最伟大的人才能带上这条腰带。',
    'blackbelt': '代表跆拳道最高水平的象征。',
    'andromeda': '这对金光闪闪的手套似乎是由黄金打造的，这种珍贵的金属上附着的魔力似乎可以使你对危险状况预警。',
    'infusedarmband': '这两条绘有符文的布缠可以从法杖中汲取能量。',
    'catalystbracer': '你在饮用药水之后，似乎感受到它回馈了一部分能量给你。',
    'shelter': '手臂上坚硬的触感使你有种厚重的安心感。',
    'griffineye': '冠以狮鹫之名的头冠，上面镶嵌了一颗明亮的黄色宝石。',
    'webbelt': '似乎带有毒性的腰带，让人不敢轻易碰触。',
    'thepure': '在平时暗淡无光的领带。',
    'tridentgauntlet': '在前端分出了三支锋利的叉，但看上去并不能直接用于攻击敌人。那是做什么用的呢？',
    'shininggloves': '一双有时闪着光，有时暗淡无光的手套。',
    'greaves': '能够对你的小腿提供有效保护。',
}

P = 'core/src/main/assets/messages/items/items_zh.properties'
t = io.open(P, encoding='utf-8', newline='').read()
added = 0
for cls, line in FLAVOR.items():
    key = 'items.armor.secondary.%s.flavor' % cls
    if key + '=' in t:
        continue
    t = t.rstrip('\n') + '\n%s=%s\n' % (key, line)
    added += 1
io.open(P, 'w', encoding='utf-8', newline='').write(t)
print('flavor keys added:', added)

J = 'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/SecondaryArmor.java'
s = io.open(J, encoding='utf-8').read()
old = '		sb.append(Messages.get(this, "effect_desc", effectArgs()));'
new = ('		sb.append(Messages.get(this, "flavor"));\n'
       '		sb.append("\\n\\n");\n'
       '		sb.append(Messages.get(this, "effect_desc", effectArgs()));')
assert s.count(old) == 1
io.open(J, 'w', encoding='utf-8', newline='').write(s.replace(old, new))
print('desc patched')
