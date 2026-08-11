package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;

/**
 * 收集（法力学派 L1，被动）：被残废、冻伤、麻痹、冰冻等状态影响时，
 * 每回合获得 0.3 点魔力。
 */
public class GatherTalent {
    @SubscribeEvent(event = HeroActEvent.class, priority = 0)
    public static void onHeroAct(HeroActEvent event) {
        Hero hero = event.getHero();
        if (hero == null || hero.subClass != HeroSubClass.DICE_MAGE) return;
        if (hero.pointsInTalent(Talent.SCHOOL_MANA) != 1) return;

        boolean slowed = hero.buff(Cripple.class) != null
                || hero.buff(Paralysis.class) != null
                || hero.buff(Chill.class) != null
                || hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost.class) != null;
        if (!slowed) return;

        Buff.affect(hero, MagicPoint.class).addPoints(0.3f);
    }
}
