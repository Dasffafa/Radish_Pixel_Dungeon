package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;

/**
 * 劈砍（刀刃学派 L2，被动）：被麻痹时，每回合自动攻击周围敌人。
 * 具体行为由 HackTalent 事件监听器实现。
 */
public class HackPassive extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLADES;
    }

    @Override
    public int level() {
        return 2;
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
    public String sndImageName() {
        return "hack";
    }



    @Override
    protected void onCast(Hero hero) {
        // passive, nothing to cast
    }
}
