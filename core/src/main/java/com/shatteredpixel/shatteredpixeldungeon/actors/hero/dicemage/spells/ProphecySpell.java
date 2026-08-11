package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicProphecy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;

/**
 * 预知（法力学派 L2）：引导2回合，20回合后获得4点魔力。
 */
public class ProphecySpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_MANA;
    }

    @Override
    public int level() {
        return 2;
    }

    @Override
    public int mpCost() {
        return 3;
    }
    @Override
    public String sndImageName() {
        return "foretell";
    }



    @Override
    protected void onCast(Hero hero) {
        if (!spendMagic(hero)) return;
        MagicProphecy.apply(hero);
        hero.spendAndNext(1f);
    }
}
