package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 切割（刀刃学派 L1）：视野内所有敌人受到7-11物理伤害。
 */
public class CutSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLADES;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public String sndImageName() {
        return "cut";
    }

    @Override
    protected void onCast(Hero hero) {
        int enemyCount = 0;
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                enemyCount++;
            }
        }

        if (enemyCount == 0) {
            GLog.w(Messages.get(this, "no_enemy"));
            return;
        }

        if (!spendMagic(hero)) return;

        int hit = 0;
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
                int dmg = Random.IntRange(7, 11);
                mob.damage(DamageInfo.physicalNoArmor(dmg, CutSpell.this));
                if (mob.isAlive()) {
                    CellEmitter.center(mob.pos).burst(BlastParticle.FACTORY, 6);
                    CellEmitter.center(mob.pos).burst(Speck.factory(Speck.RED_LIGHT), 2);
                }
                hit++;
            }
        }

        hero.spendAndNext(1f);
    }
}
