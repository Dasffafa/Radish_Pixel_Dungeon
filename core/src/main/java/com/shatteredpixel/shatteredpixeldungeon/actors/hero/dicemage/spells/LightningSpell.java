package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

/**
 * 电闪（咒法学派 L2）：弹道雷电，杀死1个19-21生命值的敌人，冷却50回合。
 */
public class LightningSpell extends DiceMageSpell {

    private static final float COOLDOWN = 50f;
    private static final int MIN_HP = 19;
    private static final int MAX_HP = 21;

    @Override
    public Talent school() {
        return Talent.SCHOOL_CONJURATION;
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
        return "bolt";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || !(target instanceof Mob)) {
                    GLog.w(Messages.get(LightningSpell.this, "invalid_target"));
                    return;
                }
                if (target.HP < MIN_HP || target.HP > MAX_HP) {
                    GLog.w(Messages.get(LightningSpell.this, "out_of_range_hp", target.HP));
                    return;
                }
                if (!spendMagic(hero)) return;

                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.STAR, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(LightningSpell.this, "executed"));
                        target.damage(new DamageInfo(target.HP, DamageType.TRUE, hero, null, LightningSpell.this));
                        CellEmitter.center(target.pos).burst(SparkParticle.FACTORY, 10);
                        Sample.INSTANCE.play(Assets.Sounds.ZAP);
                    }
                });
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(LightningSpell.this, "prompt");
            }
        });
    }
}
