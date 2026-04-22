package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroGainExperienceEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 猎杀直觉天赋
 * 每获得15次经验，获得一块神秘的肉
 */
public class HuntingIntuitionTalent {

    // 经验计数器，通过 Buff 存储
    public static class ExperienceCounter extends Buff {
        public int count = 0;

        @Override
        public boolean act() {
            spend(TICK);
            return true;
        }
    }

    @SubscribeEvent(event = HeroGainExperienceEvent.class, priority = 0)
    public static void onGainExperience(HeroGainExperienceEvent event) {
        Hero hero = event.getHero();

        // 只对月华英雄生效
        if (hero.heroClass != HeroClass.MOONLIGHT) return;

        // 检查天赋点数
        int points = hero.pointsInTalent(Talent.HUNTING_INTUITION);
        if (points <= 0) return;

        // 获取或添加计数器
        ExperienceCounter counter = hero.buff(ExperienceCounter.class);
        if (counter == null) {
            counter = Buff.affect(hero, ExperienceCounter.class);
        }

        counter.count++;

        // 每获得15次经验，掉落一块神秘的肉
        int threshold = 15;
        if (counter.count >= threshold) {
            counter.count = 0;

            // 在英雄位置掉落神秘的肉
            MysteryMeat meat = new MysteryMeat();
            Level level = Dungeon.level;
            if (level != null) {
                level.drop(meat, hero.pos).sprite.drop();
                GLog.i("猎杀直觉：获得一块神秘的肉！");
            }
        }
    }
}