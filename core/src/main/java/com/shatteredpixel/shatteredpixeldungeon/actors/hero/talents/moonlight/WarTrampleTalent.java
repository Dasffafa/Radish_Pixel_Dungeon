package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroTrampleGrassEvent;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 战争践踏天赋
 * 踩踏高草时，延长所有增益效果的持续时间（加上移动消耗的回合数）
 */
public class WarTrampleTalent {

    @SubscribeEvent(event = HeroTrampleGrassEvent.class, priority = 0)
    public static void onTrampleGrass(HeroTrampleGrassEvent event) {
        Hero hero = event.getHero();

        // 只对月华英雄生效
        if (hero.heroClass != HeroClass.MOONLIGHT) return;

        // 检查天赋点数
        int points = hero.pointsInTalent(Talent.WAR_TRAMPLE);
        if (points <= 0) return;

        float moveTime = hero.cooldown();

        // 收集所有需要延长的 FlavourBuff 类型
        // 注意：需要先收集类型列表，因为在遍历 buffs 时不能修改 buff 列表
        List<Class<? extends FlavourBuff>> buffsToExtend = new ArrayList<>();
        for (Buff buff : hero.buffs()) {
            // 只延长增益效果（POSITIVE 类型）且是 FlavourBuff
            if (buff.type == Buff.buffType.POSITIVE && buff instanceof FlavourBuff) {
                buffsToExtend.add((Class<? extends FlavourBuff>) buff.getClass());
            }
        }

        // 延长所有收集到的 Buff
        for (Class<? extends FlavourBuff> buffClass : buffsToExtend) {
            Buff.affect(hero, buffClass, moveTime);
        }

        if (buffsToExtend.size() > 0) {
            GLog.p("战争践踏：延长了 " + buffsToExtend.size() + " 个增益效果！");
        }
    }
}