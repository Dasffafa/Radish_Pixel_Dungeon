# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

# 闪亮手套：满血时所有伤害 +（25+5*等级）%
old = """		return dmg;
"""
assert s.count(old) >= 1
# 只替换 damageRoll 里的那一次（第 1059 行）
lines = s.split("\n")
idx = 1058  # 0-based -> 1059 行
assert lines[idx].strip() == "return dmg;", lines[idx]
lines[idx] = """		// 闪亮手套：满血时造成的所有伤害 +（25+5*等级）%
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor sa = belongings.secondaryArmor();
		if (sa instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ShiningGloves
				&& STR() >= sa.STRReq() && HP >= HT) {
			dmg = Math.round(dmg * (1f + (25 + 5 * sa.level) / 100f));
		}

		return dmg;"""
s = "\n".join(lines)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('ShiningGloves patched')
