package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.watabou.utils.PathFinder;

/**
 * 劈砍（刀刃学派 L2，被动）：被麻痹时，每回合自动攻击周围敌人。
 */
public class HackTalent {
    @SubscribeEvent(event = HeroActEvent.class, priority = 0)
    public static void onHeroAct(HeroActEvent event) {
        Hero hero = event.getHero();
        if (hero == null || hero.subClass != HeroSubClass.DICE_MAGE) return;
        if (hero.pointsInTalent(Talent.SCHOOL_BLADES) != 2) return;
        if (hero.buff(Paralysis.class) == null) return;

        for (int i : PathFinder.NEIGHBOURS8) {
            int pos = hero.pos + i;
            if (pos < 0 || pos >= Dungeon.level.length()) continue;
            Char ch = com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(pos);
            if (ch == null || ch.alignment != Char.Alignment.ENEMY) continue;
            hero.attack(ch);
        }
    }
}
