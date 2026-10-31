# -*- coding: utf-8 -*-
import io

p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java'
s = io.open(p, encoding='utf-8').read()

a = """		if (incarnationReady != null && !action.equals(AC_DROP) && !action.equals(AC_THROW)) {"""
assert s.count(a) == 1
s = s.replace(a, """		// 投掷类武器（索命弯刀等）的正常用法就是投掷，所以 AC_THROW 对它也要放行，
		// 否则这三个形态里只有靠神器/法杖的两个能触发。
		if (incarnationReady != null && !action.equals(AC_DROP)
				&& (!action.equals(AC_THROW) || this instanceof MissileWeapon)) {""")

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('Item incarnation throw patched')
