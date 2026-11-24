/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Warped Pixel Dungeon
 * Copyright (C) 2026 Gabriel Duarte Guerra (gabriwar)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.warped;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.utils.PointF;

import java.util.HashMap;
import java.util.Map;

/**
 * The enchantment aura over items lying on the floor: a heap holding something enchanted breathes
 * its family's motif now and then (MotifFx.heap), a mote rising off it in the enchantment's colours.
 *
 * Warped's own ItemAura is far larger - it also draws an item's slot halo, runes, rarity lines and
 * the hot/cold grips - none of which exist here (this repo has no rarity system yet), so this is its
 * floor half only, which is what the enchant port needs to show something in the world.
 */
public final class ItemAura {

	/** Acceptance counter (Z_EnchantAura selftest): heap motes made. */
	public static int motes, wieldMotes;

	/** The next moment each heap may breathe again, by cell. */
	private static final HashMap<Integer, Float> next = new HashMap<>();

	private ItemAura(){}

	public static void reset(){
		next.clear();
		motes = 0;
		wieldMotes = 0;
	}

	/** Every frame: let the visible heaps with an enchanted thing in them breathe. */
	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null || Dungeon.level.heaps == null) return;
		Camera c = Camera.main;
		if (c == null) return;

		float camX = c.scroll.x, camY = c.scroll.y;
		int lvlW = Dungeon.level.width(), lvlH = Dungeon.level.height();
		int x0 = Math.max( 0, (int)( camX / DungeonTilemap.SIZE ) - 1 );
		int y0 = Math.max( 0, (int)( camY / DungeonTilemap.SIZE ) - 1 );
		int x1 = Math.min( lvlW - 1, (int)( (camX + c.width) / DungeonTilemap.SIZE ) + 1 );
		int y1 = Math.min( lvlH - 1, (int)( (camY + c.height) / DungeonTilemap.SIZE ) + 1 );

		// the hero's own weapon in hand: a mote at the weapon side now and then
		if (Dungeon.hero != null && Dungeon.hero.sprite != null && Dungeon.hero.belongings != null
				&& Dungeon.hero.belongings.weapon instanceof Weapon){
			EnchantLook wl = lookOf( Dungeon.hero.belongings.weapon );
			if (wl != null){
				Float due = next.get( -1 );
				if (due == null || Game.timeTotal >= due){
					float hx = Dungeon.hero.sprite.x + (Dungeon.hero.sprite.flipHorizontal ? 2f : 10f);
					float hy = Dungeon.hero.sprite.y + 6f;
					MotifFx.wield( wl, hx, hy );
					wieldMotes++;
					next.put( -1, Game.timeTotal + wl.motif.periodAt( 0 ) );
				}
			}
		}

		for (int cy = y0; cy <= y1; cy++){
			for (int cx = x0; cx <= x1; cx++){
				int cell = cy * lvlW + cx;
				Heap heap = Dungeon.level.heaps.get( cell );
				if (heap == null) continue;
				EnchantLook look = lookOf( heap );
				if (look == null) continue;

				Float due = next.get( cell );
				if (due != null && Game.timeTotal < due) continue;

				PointF p = DungeonTilemap.tileToWorld( cell );
				MotifFx.heap( look, p.x + 8f, p.y + 10f );
				motes++;
				next.put( cell, Game.timeTotal + look.motif.periodAt( 0 ) );
			}
		}
	}

	/** The look of an enchanted thing on the heap, or null (a plain heap has none). */
	private static EnchantLook lookOf( Heap heap ){
		for (Item it : heap.items){
			EnchantLook l = lookOf( it );
			if (l != null) return l;
		}
		return null;
	}

	/** The look of an item's enchantment or glyph, or null. */
	public static EnchantLook lookOf( Item it ){
		if (it instanceof Weapon){
			Weapon w = (Weapon)it;
			if (w.enchantment != null) return EnchantLooks.of( w.enchantment.getClass() );
		} else if (it instanceof Armor){
			Armor a = (Armor)it;
			if (a.glyph != null) return EnchantLooks.of( a.glyph.getClass() );
		}
		return null;
	}
}
