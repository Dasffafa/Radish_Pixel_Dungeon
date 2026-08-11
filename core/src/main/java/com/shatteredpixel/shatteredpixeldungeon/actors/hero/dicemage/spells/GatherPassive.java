package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;

/**
 * 收集（法力学派 L1，被动）：被残废、冻伤、麻痹、冰冻等状态影响时每回合获得0.3魔力。
 * 具体行为由 GatherTalent 事件监听器实现。
 */
public class GatherPassive extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_MANA;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public boolean isPassive() {
        return true;
    }

    @Override
    public int mpCost() {
        return 0;
    }

    @Override
    protected void onCast(Hero hero) {
        // passive, nothing to cast
    }
}
