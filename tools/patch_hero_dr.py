# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

old = """            if (armDr > 0) dr += armDr;"""
new = """            if (armDr > 0) dr += armDr;

            // 次要护甲：力量够就按它的格挡值减伤；力量不够则效果失效并承受相当于主要护甲一半的超力惩罚
            com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor sa = belongings.secondaryArmor();
            if (sa != null) {
                if (STR() >= sa.STRReq()) {
                    dr += Char.combatRoll(sa.DRMin(), sa.DRMax());
                } else {
                    dr -= Math.max(1, (sa.STRReq() - STR()) / 2);
                }
            }"""

assert s.count(old) == 1, s.count(old)
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('Hero patched')
