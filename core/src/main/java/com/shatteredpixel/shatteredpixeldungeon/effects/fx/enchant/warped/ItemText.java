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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

/**
 * The text side of the enchantment looks: a name in its enchantment's colour, and one short line
 * naming that enchantment under the item's description - the part of Warped's item window that does
 * not need the rarity system (its quality lines, perks and rune text are not here).
 */
public final class ItemText {

	private ItemText(){}

	public static EnchantLook lookOf( Item it ){
		return ItemAura.lookOf( it );
	}

	/** The colour the item's name is drawn in, or -1 for the window's own. */
	public static int titleColor( Item it ){
		EnchantLook l = lookOf( it );
		if (l == null) return -1;
		//curses read as their hex, a good enchantment as its own light
		return l.curse ? EnchantLook.paled( l.glow ) : l.glow;
	}

	/** The enchantment's own name (a glyph's for armour), or "" for none. */
	public static String enchantName( Item it ){
		if (it instanceof Weapon && ((Weapon)it).enchantment != null){
			return ((Weapon)it).enchantment.name();
		}
		if (it instanceof Armor && ((Armor)it).glyph != null){
			return ((Armor)it).glyph.name();
		}
		return "";
	}

	/** The line that goes under an item's description: its enchantment, or "". */
	public static String line( Item it ){
		EnchantLook l = lookOf( it );
		String name = enchantName( it );
		if (l == null || name.isEmpty()) return "";
		return "\u2726 " + name + (l.curse ? "  (\u8bc5\u5492)" : "");
	}

	/** The colour of an item's glow: its enchantment's, else its rarity's (0 for plain common gear). */
	public static int auraColor( Item it ){
		EnchantLook l = lookOf( it );
		if (l != null) return l.glow;
		if ((it instanceof Weapon) || (it instanceof Armor)){
			com.shatteredpixel.shatteredpixeldungeon.items.rarity.Rarity r =
					com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( it ).rarity();
			if (r != com.shatteredpixel.shatteredpixeldungeon.items.rarity.Rarity.COMMON) return r.color;
		}
		return 0;
	}

	/** Whether the item shows a rarity block at all. */
	public static boolean isGear( Item it ){
		return (it instanceof Weapon) || (it instanceof Armor);
	}

	/** The rarity's own title ("\u666e\u901a"...), or "" for non-gear. */
	public static String rarityTitle( Item it ){
		return isGear( it ) ? com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( it ).title() : "";
	}

	/** The rarity's colour. */
	public static int rarityColor( Item it ){
		return isGear( it ) ? com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( it ).rarity().color : 0xFFF1B0;
	}

	/** Its quality lines, one per line, or "". */
	public static String qualityLines( Item it ){
		if (!isGear( it )) return "";
		StringBuilder b = new StringBuilder();
		for (com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine l
				: com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( it ).lines()){
			if (b.length() > 0) b.append( "\n" );
			b.append( "\u00b7 " ).append( l.title() );
		}
		return b.toString();
	}

	/** The gear quality block: its rarity and the lines it carries, or "" for non-gear. */
	public static String qualityText( Item it ){
		if (!(it instanceof Weapon) && !(it instanceof Armor)) return "";
		com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
				com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( it );
		StringBuilder b = new StringBuilder();
		b.append( "\n\n\u54c1\u8d28\uff1a" ).append( q.title() );
		for (com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine l : q.lines()){
			b.append( "\n\u00b7 " ).append( l.title() );
		}
		return b.toString();
	}

	/** The same line, ready to be appended to a description. */
	public static String suffix( Item it ){
		String line = line( it );
		return line.isEmpty() ? "" : "\n\n" + line;
	}
}
