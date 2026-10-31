# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

anchor = """		// 冠军腰带：对精英与 Boss 的伤害 +（10+5*等级）%"""
assert s.count(anchor) == 1

addition = """		// 黑带：空手攻击使敌人虚弱（2+等级）回合，空手伤害 +（2+等级）
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor blackBelt = belongings.secondaryArmor();
		if (blackBelt instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.BlackBelt
				&& STR() >= blackBelt.STRReq()
				&& belongings.weapon() == null && enemy != null) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class, 2 + blackBelt.level());
			damage += 2 + blackBelt.level();
		}

""" + anchor

s = s.replace(anchor, addition)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('BlackBelt patched')
