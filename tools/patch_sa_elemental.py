# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java'
s = io.open(p, encoding='utf-8').read()

anchor = "		if(buff(HolyLowBurinng.class) !=null || buff(HalomethaneBurning.class)!=null){\n"
assert s.count(anchor) == 1

addition = """		// 次要护甲：格里芬之眼（受闪电伤害增加）与蛛网腰带（受毒素伤害增加）
		if (Dungeon.hero != null) {
			com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor sbArmor =
					Dungeon.hero.belongings.secondaryArmor();
			if (sbArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.GriffinEye
					&& Dungeon.hero.STR() >= sbArmor.STRReq()
					&& info.type == com.watabou.utils.DamageType.LIGHTNING) {
				info.addFinalMultModifier(1f + (25 + 15 * sbArmor.level()) / 100f, "griffin eye");
			}
			if (sbArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.WebBelt
					&& Dungeon.hero.STR() >= sbArmor.STRReq()
					&& info.type == com.watabou.utils.DamageType.POISON) {
				info.addFinalMultModifier(1f + (15 + 10 * sbArmor.level()) / 100f, "web belt");
			}
		}

""" + anchor

s = s.replace(anchor, addition)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('GriffinEye+WebBelt patched')
