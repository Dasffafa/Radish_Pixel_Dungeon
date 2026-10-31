# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

anchor = "		damage = super.attackProc( enemy, damage );\n"
assert s.count(anchor) == 1

addition = anchor + """
		// 冠军腰带：对精英与 Boss 的伤害 +（10+5*等级）%
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor champBelt = belongings.secondaryArmor();
		if (champBelt instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ChampionBelt
				&& STR() >= champBelt.STRReq()
				&& (enemy.properties.contains(Char.Property.BOSS)
					|| enemy.properties.contains(Char.Property.MINIBOSS)
					|| enemy instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChampionEnemy)) {
			damage = Math.round(damage * (1f + (10 + 5 * champBelt.level()) / 100f));
		}
"""

s = s.replace(anchor, addition)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('ChampionBelt patched')
