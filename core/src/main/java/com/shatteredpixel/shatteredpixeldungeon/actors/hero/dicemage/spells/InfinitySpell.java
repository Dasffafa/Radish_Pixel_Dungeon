package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 无限（特殊学派）：蓝耗100，直接杀死敌人，冷却10回合。
 */
public class InfinitySpell extends DiceMageSpell {

    private static final float COOLDOWN = 10f;

    @Override
    public Talent school() {
        return Talent.SCHOOL_SPECIAL;
    }

    @Override
    public int level() {
        return DiceMageSchools.specialSlot(getClass());
    }

    @Override
    public int mpCost() {
        return 100;
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidEnemy(target)) {
                    GLog.w(Messages.get(InfinitySpell.this, "invalid_target"));
                    return;
                }
                if (!spendMagic(hero)) return;

                target.HP = 0;
                target.damage(new DamageInfo(1, DamageType.TRUE, hero, null, InfinitySpell.this));
                if (!target.isAlive()) target.die(InfinitySpell.this);
                CellEmitter.center(target.pos).burst(ShadowParticle.CURSE, 18);
                startCooldown(hero, COOLDOWN);
                GLog.p(Messages.get(InfinitySpell.this, "cast", target.name()));
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(InfinitySpell.this, "prompt");
            }
        });
    }
}
