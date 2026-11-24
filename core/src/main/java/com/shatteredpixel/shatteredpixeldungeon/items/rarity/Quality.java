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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greataxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Mace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

/**
 * The gear quality, Warped's cut down to what this repo has: an item's rarity and the family of
 * weapon (or armour) it belongs to. Warped's own Quality rolls perks, keeps a reroll wheel and reads
 * its whole enchantment catalogue; none of that is here yet - this is the floor the item window's
 * quality line and the gear display stand on.
 */
public final class Quality {

	/** What kind of gear it is: Warped's families, without its guns, bows and instrument set. */
	public enum Family {
		SWORD, AXE, SPEAR, MACE, DAGGER, STAFF, ARMOR, SHIELD, RING, WAND, OTHER
	}

	private final Item item;
	private final Rarity rarity;
	private final Family family;

	private Quality( Item item, Rarity rarity, Family family ){
		this.item = item;
		this.rarity = rarity;
		this.family = family;
	}

	/** The quality of an item: COMMON for anything not gear (this repo does not roll rarities). */
	public static Quality of( Item item ){
		return new Quality( item, Rarity.COMMON, familyOf( item ) );
	}

	public static Family familyOf( Item item ){
		if (item instanceof Armor) return Family.ARMOR;
		if (item instanceof Sword) return Family.SWORD;
		if (item instanceof Greataxe) return Family.AXE;
		if (item instanceof Spear) return Family.SPEAR;
		if (item instanceof Mace) return Family.MACE;
		if (item instanceof Dagger) return Family.DAGGER;
		if (item instanceof Weapon) return Family.OTHER;
		return Family.OTHER;
	}

	public Rarity rarity(){
		return rarity;
	}

	public Family family(){
		return family;
	}

	public Item item(){
		return item;
	}

	/** Its weapon's upgrade level (armour's too); 0 for anything else. */
	public int level(){
		if (item instanceof Weapon) return ((Weapon)item).level();
		if (item instanceof Armor) return ((Armor)item).level();
		return 0;
	}

	/** Whether it has a quality line of that kind: none yet (the lines come with RarityLine). */
	public boolean has( Object line ){
		return false;
	}

	/** A short label for the window: the rarity's own title. */
	public String title(){
		return rarity.title();
	}

	/** The vestige of a weapon's spent enchantment, or null: none here. */
	public static Weapon.Enchantment vestigeOf( Weapon w ){
		return null;
	}
}
