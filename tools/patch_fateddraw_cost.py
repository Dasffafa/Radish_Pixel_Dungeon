# -*- coding: utf-8 -*-
"""护甲技能窗口会自动追加「充能消耗：N」一行，而月华·命运抽卡(?) 的描述里又手写了一遍，
导致面板上看到两行。这里把描述里手写的那行删掉（天赋文本 fated_draw 不动）。"""
import io

p = r'core/src/main/assets/messages/actors/actors_zh.properties'
s = io.open(p, encoding='utf-8', newline='').read()
lines = s.split('\n')
hit = 0
key = 'actors.hero.abilities.moonlight.fateddraw.desc='
for i, line in enumerate(lines):
    if line.startswith(key) and line.endswith('\\n\\n充能消耗：30'):
        lines[i] = line[:-len('\\n\\n充能消耗：30')]
        hit += 1
assert hit == 1, hit
io.open(p, 'w', encoding='utf-8', newline='').write('\n'.join(lines))
print('patched', hit)
