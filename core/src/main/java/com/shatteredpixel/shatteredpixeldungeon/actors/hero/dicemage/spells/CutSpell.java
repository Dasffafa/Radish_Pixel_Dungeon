/*
 * 切割法术
 * 消耗3点魔力，对视野内所有敌人造成100点魔法伤害（均分，每个最多25）
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class CutSpell extends DiceMageSpell {

    private static final int TOTAL_DAMAGE = 100;
    private static final int MAX_PER_ENEMY = 25;

    @Override
    public String nameKey() {
        return "spell_cut";
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    protected void onCast(Hero hero) {
        // 统计视野内敌人数量
        int enemyCount = 0;
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                enemyCount++;
            }
        }

        if (enemyCount == 0) {
            GLog.w("视野内没有敌人！");
            // 退还魔力
            MagicPoint mp = hero.buff(MagicPoint.class);
            if (mp != null) mp.addPoints(mpCost());
            return;
        }

        // 计算每个敌人受到的伤害
        int damagePerEnemy = Math.min(MAX_PER_ENEMY, TOTAL_DAMAGE / enemyCount);

        // 对所有敌人造成伤害
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                mob.damage(damagePerEnemy, this);
            }
        }

        GLog.p("切割！对所有敌人造成了" + damagePerEnemy + "点伤害！");

        hero.spendAndNext(1f);
    }
}