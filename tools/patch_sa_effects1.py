# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

old = """		if (!(src instanceof Char)){
			//the same also applies to challenge scroll damage reduction"""
new = """		// 次要护甲：护胫甲的物理伤害减免 / 纯洁者的无状态减伤（力量不足时效果失效）
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor secArmor = belongings.secondaryArmor();
		if (secArmor != null && STR() >= secArmor.STRReq()) {
			if (secArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Greaves
					&& info.getDamageType() == com.shatteredpixel.shatteredpixeldungeon.items.DamageType.PHYSICAL) {
				dmg = Math.max(1, Math.round(dmg * (1f - (10 + 2 * secArmor.level) / 100f)));
			}
			if (secArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ThePure
					&& buffs().isEmpty()) {
				dmg = Math.max(1, dmg - Math.round(2 + 0.2f * secArmor.level));
			}
		}

		if (!(src instanceof Char)){
			//the same also applies to challenge scroll damage reduction"""

assert s.count(old) == 1, s.count(old)
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('Hero secondary armor effects patched')
