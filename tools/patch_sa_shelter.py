# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

anchor = " 	public void rest( boolean fullRest ) {\n"
assert s.count(anchor) == 1, s.count(anchor)

addition = anchor + """
		// 庇护：原地等待时获得（3+等级）点护盾（覆盖式，不叠加）
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor shelterArmor = belongings.secondaryArmor();
		if (shelterArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Shelter
				&& STR() >= shelterArmor.STRReq()) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					this, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier.class)
					.setShield(3 + shelterArmor.level());
		}
"""

s = s.replace(anchor, addition)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('Shelter patched')
