/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.rarity;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

/**
 * A stand-in for Warped's item quality (rarity): this repo has no quality system, so an item has
 * none, and the enchant framework's "vestige" (a spent enchantment's echo) is simply absent.
 * Warped's Quality knows its own rarities; only what the enchant port asks for is here.
 */
public final class Quality {

	private Quality(){}

	/** The vestige of a weapon's spent enchantment, or null: none here. */
	public static Weapon.Enchantment vestigeOf( Weapon w ){
		return null;
	}
}
