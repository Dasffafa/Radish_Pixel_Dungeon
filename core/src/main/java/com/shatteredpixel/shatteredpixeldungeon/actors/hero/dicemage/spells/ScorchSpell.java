package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 烧焦（火焰学派 L1）：3×3范围，3-10火焰伤害并点燃。冷却20回合。
 */
public class ScorchSpell extends DiceMageSpell {

    private static final float COOLDOWN = 20f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_FIRE;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public int mpCost() {
        return 2;
    }
    @Override
    public String sndImageName() {
        return "scorch";
    }



    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                if (!spendMagic(hero)) return;

                int width = Dungeon.level.width();
                int cx = cell % width, cy = cell / width;
                int hit = 0;
                for (int x = cx - 1; x <= cx + 1; x++) {
                    for (int y = cy - 1; y <= cy + 1; y++) {
                        int pos = y * width + x;
                        if (x < 0 || x >= width || pos < 0 || pos >= Dungeon.level.length()) continue;
                        Char ch = Actor.findChar(pos);
                        if (ch == null) continue;
                        int dmg = Random.IntRange(3, 10);
                        ch.damage(DamageInfo.fire(dmg, ScorchSpell.this));
                        if (ch.isAlive()) {
                            Buff.affect(ch, Burning.class).reignite(ch);
                            CellEmitter.center(ch.pos).burst(FlameParticle.FACTORY, 8);
                        } else {
                            ch.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(dmg));
                        }
                        hit++;
                    }
                }
                MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE, hero.sprite, cell, new Callback() {
                    @Override
                    public void call() {
                        Sample.INSTANCE.play(Assets.Sounds.BURNING);
                    }
                });
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(ScorchSpell.this, "prompt");
            }
        });
    }
}
