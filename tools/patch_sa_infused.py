# -*- coding: utf-8 -*-
import io

# 1) Mob.damage：英雄用法杖（魔法伤害）命中后，给英雄灌能臂缠 buff + 护盾
p1 = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java'
s1 = io.open(p1, encoding='utf-8').read()
a1 = """		if(buff(HolyLowBurinng.class) !=null || buff(HalomethaneBurning.class)!=null){\n"""
assert s1.count(a1) == 1
add1 = """		// 灌能臂缠：英雄用法杖造成伤害后，10 回合内增伤并获得护盾
		if (Dungeon.hero != null && info.getAttacker() == Dungeon.hero) {
			com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor infArmor =
					Dungeon.hero.belongings.secondaryArmor();
			if (infArmor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.InfusedArmband
					&& Dungeon.hero.STR() >= infArmor.STRReq()
					&& info.getType() == DamageType.MAGICAL) {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.InfusedArmbandBuff.apply(
						Dungeon.hero, 1 + infArmor.level() / 2, 10f);
				Buff.affect(Dungeon.hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier.class)
						.setShield(2 + infArmor.level());
			}
		}

""" + a1
s1 = s1.replace(a1, add1)
io.open(p1, 'w', encoding='utf-8', newline='').write(s1)

# 2) Hero.damageRoll：灌能臂缠的增伤生效
p2 = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s2 = io.open(p2, encoding='utf-8').read()
a2 = """		// 闪亮手套：满血时造成的所有伤害 +（25+5*等级）%"""
assert s2.count(a2) == 1
add2 = """		// 灌能臂缠：法杖命中后的临时增伤
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.InfusedArmbandBuff infBuff =
				buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.InfusedArmbandBuff.class);
		if (infBuff != null) {
			dmg += infBuff.bonus();
		}

""" + a2
s2 = s2.replace(a2, add2)
io.open(p2, 'w', encoding='utf-8', newline='').write(s2)
print('InfusedArmband patched')
