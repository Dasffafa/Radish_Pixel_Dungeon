# -*- coding: utf-8 -*-
import io

p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/abilities/rector/GodsPossesion.java'
s = io.open(p, encoding='utf-8').read()

a = """        Buff.affect(hero, GodsPossessionBuff.class,GodsPossessionBuff.DURATION);
    }"""
assert s.count(a) == 1
s = s.replace(a, """        Buff.affect(hero, GodsPossessionBuff.class,GodsPossessionBuff.DURATION);

        // 技能本身要吃掉一回合，并且立刻刷新一次视野/界面，否则玩家会觉得“点了没反应”
        Dungeon.observe();
        hero.spendAndNext( 1f );
    }""")

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('GodsPossesion patched')
