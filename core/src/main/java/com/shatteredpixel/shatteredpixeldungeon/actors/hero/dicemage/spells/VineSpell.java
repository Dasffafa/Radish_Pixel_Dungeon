package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 藤蔓（自然学派 L2）：提供3-11护盾，或造成3-7伤害并有33%概率缠绕2回合（二选一）。
 * 目标为友方时提供护盾；目标为敌人时造成伤害并概率缠绕。
 */
public class VineSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_NATURE;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 1;
    }
    @Override
    public String sndImageName() {
        return "vine";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (target == null || (target != hero && !isValidAlly(target) && !isValidEnemy(target))) {
                    GLog.w(Messages.get(VineSpell.this, "invalid_target"));
                    return;
                }
                if (!spendMagic(hero)) return;

                if (target == hero || isValidAlly(target)) {
                    int shield = Random.IntRange(3, 11);
                    Buff.affect(target, Barrier.class).incShield(shield);
                    CellEmitter.center(target.pos).burst(SparkParticle.FACTORY, 5);
                    GLog.p(Messages.get(VineSpell.this, "shield", shield));
                } else {
                    int dmg = Random.IntRange(3, 7) + 2 * (hero.STR() - 10);
                    target.damage(DamageInfo.physicalNoArmor(dmg, VineSpell.this));
                    if (target.isAlive()) {
                        if (Random.Float() < 0.33f) {
                            // 缠绕时长可叠加（affect 对已存在的 buff 会累加 duration）
                            Buff.affect(target, Roots.class, 2f);
                        }
                        CellEmitter.center(target.pos).burst(EarthParticle.FACTORY, 8);
                    }
                    GLog.p(Messages.get(VineSpell.this, "attack", dmg));
                }
                hero.spendAndNext(0.33f);
            }

            @Override
            public String prompt() {
                return Messages.get(VineSpell.this, "prompt");
            }
        });
    }
}
