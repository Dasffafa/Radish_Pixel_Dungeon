package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 劈砍（刀刃学派 L2，被动）：被麻痹时，每回合自动攻击范围内的一名敌人。
 */
public class HackTalent {
    @SubscribeEvent(event = HeroActEvent.class, priority = 0)
    public static void onHeroAct(HeroActEvent event) {
        Hero hero = event.getHero();
        if (hero == null || hero.subClass != HeroSubClasses.DICE_MAGE) return;
        if (hero.pointsInTalent(Talent.SCHOOL_BLADES) != 2) return;
        if (hero.buff(Paralysis.class) == null) return;

        List<Char> candidates = new ArrayList<>();
        for (Char ch : Actor.chars()) {
            if (ch == null || ch.alignment != Char.Alignment.ENEMY || !ch.isAlive()) continue;
            if (hero.canAttack(ch)) candidates.add(ch);
        }
        if (candidates.isEmpty()) return;

        Char target = candidates.get(0);
        for (Char ch : candidates) {
            if (Dungeon.level.distance(hero.pos, ch.pos) < Dungeon.level.distance(hero.pos, target.pos)) {
                target = ch;
            }
        }
        hero.attack(target);
    }
}
