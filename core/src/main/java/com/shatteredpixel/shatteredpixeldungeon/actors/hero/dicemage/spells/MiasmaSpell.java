package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.watabou.utils.Random;

/**
 * 瘴气（自然学派 L3）：特殊毒气，令怪物同时中毒并受到毒素伤害。
 */
public class MiasmaSpell extends DiceMageSpell {

    private static final int RADIUS = 2;

    @Override
    public Talent school() {
        return Talent.SCHOOL_NATURE;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 3;
    }

    @Override
    public String sndImageName() {
        return "miasma";
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
                for (int x = cx - RADIUS; x <= cx + RADIUS; x++) {
                    for (int y = cy - RADIUS; y <= cy + RADIUS; y++) {
                        if (x < 0 || x >= width) continue;
                        int pos = y * width + x;
                        if (pos < 0 || pos >= Dungeon.level.length()) continue;
                        Char ch = Actor.findChar(pos);
                        if (ch == null) continue;
                        int dmg = Random.IntRange(6, 10);
                        ch.damage(DamageInfo.poison(dmg, MiasmaSpell.this));
                        if (ch.isAlive()) {
                            Buff.affect(ch, Poison.class).set(dmg);
                            CellEmitter.center(ch.pos).burst(PoisonParticle.SPLASH, 10);
                        }
                        hit++;
                    }
                }
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(MiasmaSpell.this, "prompt");
            }
        });
    }
}
