/*
 * 愈合法术
 * 消耗2点魔力，将自身HP设置为50，每次使用降低5
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class HealSpell extends DiceMageSpell {

    private static int healValue = 50;
    private static int useCount = 0;

    private static final String USE_COUNT = "use_count";
    private static final String HEAL_VALUE = "heal_value";

    @Override
    public String nameKey() {
        return "spell_heal";
    }

    @Override
    public int mpCost() {
        return 2;
    }

    @Override
    protected void onCast(Hero hero) {
        if (healValue <= 0) {
            GLog.w("愈合效果已耗尽！");
            MagicPoint mp = hero.buff(MagicPoint.class);
            if (mp != null) mp.addPoints(mpCost());
            return;
        }

        int oldHP = hero.HP;
        hero.HP = Math.min(healValue, hero.HT);
        int healed = hero.HP - oldHP;

        // 降低下次治疗值
        healValue -= 5;
        useCount++;

        if (healed > 0) {
            GLog.p("愈合！恢复了" + healed + "点生命。（下次治疗值：" + Math.max(0, healValue) + "）");
        } else {
            GLog.i("愈合！但你的生命值已经很高了。");
        }

        hero.spendAndNext(1f);
    }

    public static int getHealValue() {
        return healValue;
    }

    public static void reset() {
        healValue = 50;
        useCount = 0;
    }

    public static void storeInBundle(Bundle bundle) {
        bundle.put(USE_COUNT, useCount);
        bundle.put(HEAL_VALUE, healValue);
    }

    public static void restoreFromBundle(Bundle bundle) {
        if (bundle.contains(USE_COUNT)) {
            useCount = bundle.getInt(USE_COUNT);
            healValue = bundle.getInt(HEAL_VALUE);
        } else {
            reset();
        }
    }
}