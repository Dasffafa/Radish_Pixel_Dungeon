/*
 * 碾压实 - 天赋法术
 * 消耗3点魔力，对视野内最上方和最下方的敌人造成25/38/40点伤害（不能是同一敌人）
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class CrushSpell extends DiceMageSpell {

    @Override
    public String nameKey() {
        return "spell_crush";
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        int points = hero.pointsInTalent(Talent.LEARN_CRUSH);
        if (points <= 0) return false;

        return super.canCast();
    }

    @Override
    protected void onCast(Hero hero) {
        int points = hero.pointsInTalent(Talent.LEARN_CRUSH);
        int damage = points == 1 ? 25 : (points == 2 ? 38 : 40);

        // 找出视野内最上方和最下方的敌人
        Mob topEnemy = null, bottomEnemy = null;
        int topY = Integer.MAX_VALUE, bottomY = Integer.MIN_VALUE;

        for (Mob mob : Dungeon.level.mobs) {
            if (!Dungeon.level.heroFOV[mob.pos] || mob.alignment != Char.Alignment.ENEMY) continue;

            int y = mob.pos / Dungeon.level.width();
            if (y < topY) {
                topY = y;
                topEnemy = mob;
            }
            if (y > bottomY) {
                bottomY = y;
                bottomEnemy = mob;
            }
        }

        // 不能是同一敌人
        if (topEnemy == bottomEnemy) {
            bottomEnemy = null;
        }

        int hitCount = 0;
        if (topEnemy != null) {
            topEnemy.damage(damage, this);
            hitCount++;
        }
        if (bottomEnemy != null) {
            bottomEnemy.damage(damage, this);
            hitCount++;
        }

        if (hitCount > 0) {
            GLog.p("碾压！对" + hitCount + "个敌人造成了" + damage + "点伤害！");
        } else {
            GLog.w("视野内没有足够的敌人！");
            MagicPoint mp = hero.buff(MagicPoint.class);
            if (mp != null) mp.addPoints(mpCost());
        }

        hero.spendAndNext(1f);
    }
}