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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 刃雨（刀刃学派 L3）：发射扇形刀刃，每枚造成20-30物理伤害；
 * 每个敌人最多被命中3次，多余投射物无法被阻挡。
 */
public class BladeRainSpell extends DiceMageSpell {

    private static final int MAX_HITS_PER_ENEMY = 3;

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLADES;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 4;
    }
    @Override
    public String sndImageName() {
        return "blades";
    }



    @Override
    protected void onCast(Hero hero) {
        int hit = 0;
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (!Dungeon.level.heroFOV[mob.pos] || mob.alignment != Char.Alignment.ENEMY) continue;
            // 每个敌人最多被命中3次
            int times = Random.IntRange(1, MAX_HITS_PER_ENEMY);
            for (int i = 0; i < times; i++) {
                int dmg = Random.IntRange(20, 30);
                mob.damage(DamageInfo.physicalNoArmor(dmg, BladeRainSpell.this));
                if (mob.isAlive()) {
                    CellEmitter.center(mob.pos).burst(Speck.factory(Speck.RED_LIGHT), 3);
                }
                hit++;
            }
        }
        if (hit == 0) {
            GLog.i(Messages.get(BladeRainSpell.this, "none"));
        } else {
        }
        hero.spendAndNext(1f);
    }
}
