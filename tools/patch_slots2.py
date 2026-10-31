# -*- coding: utf-8 -*-
import io

p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/InventoryPane.java'
s = io.open(p, encoding='utf-8').read()

old = """		equipped = new ArrayList<>();
		for (int i = 0; i < 5; i++){"""
new = """		equipped = new ArrayList<>();
		for (int i = 0; i < 6; i++){"""
assert s.count(old) == 1
s = s.replace(old, new)

# 侧栏布局：原来是 5 个槽一行，改成 6 个
old2 = """		equipped.get(5).item(stuff.secondaryArmor() == null ? new WndBag.Placeholder( "secondary_slot" ) : stuff.secondaryArmor());"""
assert s.count(old2) == 1

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('InventoryPane slots -> 6')
