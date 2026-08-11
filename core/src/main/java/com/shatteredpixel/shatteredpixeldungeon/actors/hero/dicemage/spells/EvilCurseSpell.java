package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 恶咒（咒法学派 L3）：杀死视野内1个59-61生命值的敌人，冷却50回合。
 */
public class EvilCurseSpell extends DiceMageSpell {

    private static final float COOLDOWN = 50f;
    private static final int MIN_HP = 59;
    private static final int MAX_HP = 61;

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
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target) || !(target instanceof Mob)) {
                    GLog.w(Messages.get(EvilCurseSpell.this, "invalid_target"));
                    return;
                }
                if (!Dungeon.level.heroFOV[target.pos]) {
                    GLog.w(Messages.get(EvilCurseSpell.this, "not_in_view"));
                    return;
                }
                if (target.HP < MIN_HP || target.HP > MAX_HP) {
                    GLog.w(Messages.get(EvilCurseSpell.this, "out_of_range_hp", target.HP));
                    return;
                }
                if (!spendMagic(hero)) return;

                target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(EvilCurseSpell.this, "executed"));
                target.damage(new DamageInfo(target.HP, DamageType.TRUE, hero, null, EvilCurseSpell.this));
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 12);
                startCooldown(hero, COOLDOWN);
                GLog.p(Messages.get(EvilCurseSpell.this, "cast", target.name()));
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(EvilCurseSpell.this, "prompt");
            }
        });
    }
}
