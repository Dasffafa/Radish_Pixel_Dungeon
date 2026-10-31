# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

anchor = """		// 闪亮手套：满血时造成的所有伤害 +（25+5*等级）%"""
assert s.count(anchor) == 1

addition = """		// 三叉护手：造成的物理伤害下降 30%（元素增伤在法术/附魔侧另算）
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor triArmor = belongings.secondaryArmor();
		if (triArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.TridentGauntlet
				&& STR() >= triArmor.STRReq()) {
			dmg = Math.round(dmg * 0.7f);
		}

""" + anchor

s = s.replace(anchor, addition)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('TridentGauntlet patched')
