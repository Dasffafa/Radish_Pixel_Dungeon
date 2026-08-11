package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class BlazeSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_FIRE;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 6;
    }

    @Override
    public String sndImageName() {
        return "blaze";
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;

                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(BlazeSpell.this, "invalid_target"));
                    return;
                }

                int damage = Random.IntRange(100, 150);

                if (!spendMagic(hero)) return;

                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE, hero.sprite, target.pos, new Callback() {
                    @Override
                    public void call() {
                        target.damage(DamageInfo.fire(damage, BlazeSpell.this));
                        if (target.isAlive()) {
                            CellEmitter.center(target.pos).burst(FlameParticle.FACTORY, 10);
                        }
                        Sample.INSTANCE.play(Assets.Sounds.BLAST);
                    }
                });
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(BlazeSpell.this, "prompt");
            }
        });
    }
}
