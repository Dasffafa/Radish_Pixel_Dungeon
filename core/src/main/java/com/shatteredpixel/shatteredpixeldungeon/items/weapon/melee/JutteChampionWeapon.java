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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;


public class JutteChampionWeapon extends MeleeWeapon {

    public int tier = 1;

    private static final int BASE_STR = 10;
    private static final float ATTACK_DELAY = 0.75f;
    private static final float ACCURACY = 1.25f;
    private static final float MAX_DURABILITY = 50f;

    private float durability = MAX_DURABILITY;

    private static final int[][] TIER_DAMAGE = {
        {5, 20},   // 1
        {6, 24},   // 2
        {8, 32},   // 3
        {9, 36},   // 4
        {10, 40}   // 5
    };

    private static final int[][] TIER_BLOCK = {
        {2, 4},    // 1
        {2, 5},    // 2
        {3, 6},    // 3
        {3, 7},    // 4
        {4, 8}     // 5
    };

    {
        image = ItemSpriteSheet.SNAKE_BITED_YENDOR;
        defaultAction = AC_THROW;
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
        // 基础最小伤害 + 每次升级+1
        return TIER_DAMAGE[tier - 1][0] + lvl;
    }

    @Override
    public int max(int lvl) {
        int baseMax = TIER_DAMAGE[tier - 1][1];
        // 基础最大伤害 + 每次升级增加 (tier+1)
        int levelBonus = lvl * (tier + 1);
        
        // 铁淬炼天赋：额外增加升级数*10%/20%/30%
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.JUTTE_CHAMPION) {
            int points = Dungeon.hero.pointsInTalent(Talent.IRON_QUENCH);
            if (points > 0 && lvl > 0) {
                float extraBonus = lvl * 0.1f * points;
                levelBonus = Math.round(levelBonus * (1 + extraBonus));
            }
        }
        return baseMax + levelBonus;
    }

    @Override
    public int STRReq() {
        return BASE_STR;
    }

    @Override
    public float delayFactor(Char owner) {
        return ATTACK_DELAY;
    }

    @Override
    public float accuracyFactor(Char owner, Char target) {
        return ACCURACY;
    }

    @Override
    public int defenseFactor(Char owner) {
        int[] block = TIER_BLOCK[tier - 1];
        return Random.IntRange(block[0], block[1]);
    }

    // ?????
    public float getMaxDurability() {
        float base = MAX_DURABILITY;
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
        durability -= amount;
        if (durability < 0) durability = 0;

        if (durability <= 0 && Dungeon.hero != null) {
            Hero hero = Dungeon.hero;
            Sample.INSTANCE.play(Assets.Sounds.JUTTE_BREAK);
            if (isEquipped(hero)) {
                doUnequip(hero, true, true);
            }
            detach(hero.belongings.backpack);
        }
    }

    @Override
    public int damageRoll(Char owner) {
        if (isBroken()) return 0;
        return super.damageRoll(owner);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (attacker instanceof Hero) {
            Hero hero = (Hero) attacker;
            // 出其不意天赋：伏击时不消耗耐久度
            boolean surpriseAttack = defender instanceof Mob && ((Mob) defender).surprisedBy(attacker);
            boolean hasTalent = hero.subClass == HeroSubClass.JUTTE_CHAMPION 
                    && hero.hasTalent(Talent.SURPRISE_JUTTE);
            
            if (!surpriseAttack || !hasTalent) {
                consumeDurability(1f);
            }
            // 伏击且有天赋时，所有等级都不消耗耐久度
        }
        super.proc(attacker, defender, damage);
        return damage;
    }

    @Override
    public Item random() {
        return this;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public String statsInfo() {
        String info = Messages.get(this, "stats",
                TIER_BLOCK[tier-1][0], TIER_BLOCK[tier-1][1],
                (int)durability, (int)getMaxDurability());
        if (isBroken()) {
            info += "\n" + Messages.get(this, "broken");
        }
        return info;
    }

    @Override
    protected void onThrow(int cell) {
        Char enemy = Actor.findChar(cell);
        if (enemy == null || enemy == curUser) {
            super.onThrow(cell);
            return;
        }

        if (isBroken()) {
            super.onThrow(cell);
            return;
        }

        Hero hero = (Hero) curUser;
        boolean wasEnemy = enemy.alignment == Char.Alignment.ENEMY
                || (enemy instanceof Mimic && enemy.alignment == Char.Alignment.NEUTRAL);

        hero.belongings.thrownWeapon = this;
        boolean hit = hero.attack(enemy);
        Invisibility.dispel();
        hero.belongings.thrownWeapon = null;

        if (hit && hero.subClass == HeroSubClass.GLADIATOR && wasEnemy) {
            Buff.affect(hero, Combo.class).hit();
        }

        Dungeon.level.drop(this, cell).sprite.drop(cell);
    }
}