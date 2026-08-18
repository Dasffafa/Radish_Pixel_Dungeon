package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HealingBlocked;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 恶咒（咒法学派 L3）：消耗3魔力点，给3格内的一个目标7-15回合的
 * 虚弱、易伤、幻惑、失明、残废、禁疗。冷却50回合。
 */
public class HexSpell extends DiceMageSpell {

    private static final float COOLDOWN = 50f;
    private static final int RANGE = 3;
    private static final int MIN_DURATION = 7;
    private static final int MAX_DURATION = 15;

    @Override
    public Talent school() {
        return Talent.SCHOOL_CONJURATION;
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
        return "hex";
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(HexSpell.this, "invalid_target"));
                    return;
                }
                if (!Dungeon.level.heroFOV[target.pos]) {
                    GLog.w(Messages.get(HexSpell.this, "not_in_view"));
                    return;
                }
                // 3格内
                if (Dungeon.level.distance(hero.pos, target.pos) > RANGE) {
                    GLog.w(Messages.get(HexSpell.this, "out_of_range"));
                    return;
                }
                if (!spendMagic(hero)) return;

                Buff.affect(target, Weakness.class, Random.IntRange(MIN_DURATION, MAX_DURATION));
                Buff.affect(target, Vulnerable.class, Random.IntRange(MIN_DURATION, MAX_DURATION));
                Buff.affect(target, Hex.class, Random.IntRange(MIN_DURATION, MAX_DURATION));
                Buff.affect(target, Blindness.class, Random.IntRange(MIN_DURATION, MAX_DURATION));
                Buff.affect(target, Cripple.class, Random.IntRange(MIN_DURATION, MAX_DURATION));
                HealingBlocked.block(target, Random.IntRange(MIN_DURATION, MAX_DURATION));

                target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(HexSpell.this, "executed"));
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 12);
                startCooldown(hero, COOLDOWN);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(HexSpell.this, "prompt");
            }
        });
    }
}
