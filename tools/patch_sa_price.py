# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/SecondaryArmor.java'
s = io.open(p, encoding='utf-8').read()
old = """	@Override
	public int price() {
		// 卖价与同阶普通护甲相同
		return 20 * (quantity > 0 ? quantity : 1);
	}

"""
assert s.count(old) == 1
s = s.replace(old, "")
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('price() removed')
