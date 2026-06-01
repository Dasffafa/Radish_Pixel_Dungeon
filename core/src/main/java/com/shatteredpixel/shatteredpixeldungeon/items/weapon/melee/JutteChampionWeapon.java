/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/**
 * 十手 - 十手冠军的专属武器
 * 属性固定：STR 10，攻速0.75，精准1.25，50点耐久
 * 阶数决定伤害和格挡
 */
public class JutteChampionWeapon extends MeleeWeapon {

    // 阶数：1-5阶
    public int tier = 1;

    // 基础属性
    private static final int BASE_STR = 10;
    private static final float ATTACK_DELAY = 0.75f;
    private static final float ACCURACY = 1.25f;
    private static final float MAX_DURABILITY = 50f;

    // 当前耐久
    private float durability = MAX_DURABILITY;

    // 每阶属性：伤害min~max，格挡min~max
    private static final int[][] TIER_DAMAGE = {
        {5, 20},   // 1阶
        {6, 24},   // 2阶
        {8, 32},   // 3阶
        {9, 36},   // 4阶
        {10, 40}   // 5阶
    };

    private static final int[][] TIER_BLOCK = {
        {2, 4},    // 1阶
        {2, 5},    // 2阶
        {3, 6},    // 3阶
        {3, 7},    // 4阶
        {4, 8}     // 5阶
    };

    {
        image = ItemSpriteSheet.SNAKE_BITED_YENDOR; // 使用十手贴图
        defaultAction = AC_THROW; // 可以投掷
    }

    public JutteChampionWeapon() {
        this(1);
    }

    public JutteChampionWeapon(int tier) {
        this.tier = Math.max(1, Math.min(5, tier));
        durability = getMaxDurability();
    }

    @Override
    public int min(int lvl) {
        return TIER_DAMAGE[tier - 1][0];
    }

    @Override
    public int max(int lvl) {
        int baseMax = TIER_DAMAGE[tier - 1][1];
        // 精铁淬炼天赋：升级数*百分比额外伤害
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.JUTTE_CHAMPION) {
            int points = Dungeon.hero.pointsInTalent(Talent.IRON_QUENCH);
            if (points > 0) {
                float bonus = lvl * 0.1f * points; // 10%/20%/30%
                baseMax = Math.round(baseMax * (1 + bonus));
            }
        }
        return baseMax;
    }

    @Override
    public int STRReq() {
        return BASE_STR; // 固定10
    }

    @Override
    public float delayFactor(Char owner) {
        return ATTACK_DELAY; // 固定0.75
    }

    @Override
    public float accuracyFactor(Char owner, Char target) {
        return ACCURACY; // 固定1.25
    }

    @Override
    public int defenseFactor(Char owner) {
        // 格挡：从阶数获取
        int[] block = TIER_BLOCK[tier - 1];
        return Random.IntRange(block[0], block[1]);
    }

    // 耐久度相关
    public float getMaxDurability() {
        float base = MAX_DURABILITY;
        // 一把十手天赋：耐久上升
        if (Dungeon.hero != null) {
            int points = Dungeon.hero.pointsInTalent(Talent.ONE_JUTTE);
            switch (points) {
                case 1: base += 10; break;
                case 2: base += 17; break;
                case 3: base += 25; break;
            }
        }
        return base;
    }

    public float getDurability() {
        return durability;
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public void consumeDurability(float amount) {
        // 出其不意天赋：伏击不消耗/减少消耗
        if (Dungeon.hero != null && Dungeon.hero.buff(SurpriseJutteTracker.class) != null) {
            int points = Dungeon.hero.pointsInTalent(Talent.SURPRISE_JUTTE);
            if (points >= 1) {
                if (points == 1) {
                    return; // 不消耗
                } else {
                    amount *= (points == 2 ? 0.66f : 0.33f);
                }
            }
        }
        durability -= amount;
        if (durability < 0) durability = 0;
    }

    @Override
    public int damageRoll(Char owner) {
        if (isBroken()) return 0;
        return super.damageRoll(owner);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (attacker instanceof Hero) {
            consumeDurability(1f); // 近战命中消耗1点
        }
        super.proc(attacker, defender, damage);
        return damage;
    }

    @Override
    public Item random() {
        return this; // 不随机生成
    }

    @Override
    public boolean isUpgradable() {
        return false; // 无法升级
    }

    @Override
    public int value() {
        return 0; // 无价值
    }

    @Override
    public String info() {
        String info = desc();
        info += "\n\n" + Messages.get(this, "stats", tier, min(), max(), STRReq(), 
                TIER_BLOCK[tier-1][0], TIER_BLOCK[tier-1][1], 
                (int)durability, (int)getMaxDurability());
        if (isBroken()) {
            info += "\n\n" + Messages.get(this, "broken");
        }
        return info;
    }

    // 伏击追踪Buff
    public static class SurpriseJutteTracker extends FlavourBuff {
        {
            type = buffType.POSITIVE;
        }
    }
}