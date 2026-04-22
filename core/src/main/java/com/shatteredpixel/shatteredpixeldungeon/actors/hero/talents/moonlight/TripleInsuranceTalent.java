package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroEatFoodEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.ItemUseEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 三重保险天赋
 * 进食、使用药水或卷轴后获得护盾（6点/9点）
 */
public class TripleInsuranceTalent {

    @SubscribeEvent(event = HeroEatFoodEvent.class, priority = 0)
    public static void onEatFood(HeroEatFoodEvent event) {
        applyShield(event.getHero());
    }

    @SubscribeEvent(event = ItemUseEvent.class, priority = 0)
    public static void onItemUse(ItemUseEvent event) {
        // 只对药水和卷轴生效
        Item item = event.getItem();
        if (!(item instanceof Potion) && !(item instanceof Scroll)) return;

        applyShield(event.getUser() instanceof Hero ? (Hero) event.getUser() : null);
    }

    private static void applyShield(Hero hero) {
        if (hero == null) return;

        // 只对月华英雄生效
        if (hero.heroClass != HeroClass.MOONLIGHT) return;

        // 检查天赋点数
        int points = hero.pointsInTalent(Talent.TRIPLE_INSURANCE);
        if (points <= 0) return;

        // 计算护盾值：+1时6点，+2时9点
        int shieldAmount = 3 * (points + 1);

        // 获取或添加护盾 Buff
        Barrier barrier = hero.buff(Barrier.class);
        if (barrier == null) {
            barrier = Buff.affect(hero, Barrier.class);
        }
        barrier.incShield(shieldAmount);

        GLog.p("三重保险：获得 " + shieldAmount + " 点护盾！");
    }
}