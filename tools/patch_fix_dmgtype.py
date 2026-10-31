# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()
old = """					&& info.getDamageType() == com.shatteredpixel.shatteredpixeldungeon.items.DamageType.PHYSICAL) {"""
new = """					&& info.getType() == com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.PHYSICAL) {"""
assert s.count(old) == 1, s.count(old)
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('fixed')
