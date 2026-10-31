# -*- coding: utf-8 -*-
import io

p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RadishEnemy/GoblinRider.java'
s = io.open(p, encoding='utf-8').read()

# 1) launchSpear 的提前返回：next() 之前必须 spend，否则时间不推进，actor 会被反复调度成死循环
a = """        if (sprite == null || sprite.parent == null || enemy == null) {
            next();
            return;
        }"""
assert s.count(a) == 1
s = s.replace(a, """        if (sprite == null || sprite.parent == null || enemy == null) {
            // 必须先 spend 再 next：只 next 不 spend 会让时间停在同一刻，actor 被无限重排
            spend(attackDelay());
            next();
            return;
        }""")

# 2) onZapComplete 的兜底分支同样补 spend
a = """        if (rangedAttack) {
            launchSpear();
            rangedAttack = false;
        } else {
            next();
        }"""
assert s.count(a) == 1
s = s.replace(a, """        if (rangedAttack) {
            launchSpear();
            rangedAttack = false;
        } else {
            spend(attackDelay());
            next();
        }""")

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('GoblinRider patched')
