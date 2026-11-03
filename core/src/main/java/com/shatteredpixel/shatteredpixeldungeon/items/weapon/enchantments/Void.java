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
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class Void extends Weapon.Enchantment {
    private static final ItemSprite.Glowing VIOLET = new ItemSprite.Glowing(0x6f42c1);

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        if (defender.isImmune(Void.class)) return damage;
        int level = Math.max(0, weapon.buffedLvl());
        VoidTracker tracker = Buff.affect(defender, VoidTracker.class);
        tracker.maxChance = (0.5f + .05f * level) * procChanceMultiplier(attacker);
        tracker.weapon = weapon;
        tracker.qualifiesForBadge = attacker instanceof Hero;
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return VIOLET;
    }

    public static class VoidTracker extends Buff {
        { actPriority = Actor.VFX_PRIO; }
        public float maxChance;
        public Weapon weapon;
        public boolean qualifiesForBadge;
        @Override public boolean act() { detach(); return true; }
    }

    public static boolean canTearTile(Char target) {
        if (Dungeon.level == null || target == null || target.pos < 0 || target.pos >= Dungeon.level.length()) return false;
        int terrain = Dungeon.level.map[target.pos];
        if ((Terrain.flags[terrain] & Terrain.PASSABLE) == 0) return false;
        return terrain != Terrain.ENTRANCE && terrain != Terrain.ENTRANCE_SP
                && terrain != Terrain.EXIT && terrain != Terrain.UNLOCKED_EXIT
                && terrain != Terrain.DOOR && terrain != Terrain.OPEN_DOOR
                && terrain != Terrain.LOCKED_DOOR && terrain != Terrain.CRYSTAL_DOOR;
    }

    public static void tearTile(Char target) {
        if (!canTearTile(target)) return;
        Dungeon.level.set(target.pos, Terrain.CHASM);
        GameScene.updateMap(target.pos);
        target.die(Void.class);
    }
}
