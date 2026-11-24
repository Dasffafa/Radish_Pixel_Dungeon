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
		com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
				com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( it );
		for (com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine l : q.lines()){
			if (b.length() > 0) b.append( "\n" );
			b.append( "\u00b7 " ).append( l.describe( it, q.strength( l ) ) );
		}
		//the rarity's own grant, said the way Warped says it
		b.append( "\n\n\u4ec5\u51ed\u7a00\u6709\u5ea6\uff0c\u5b83\u81ea\u8eab\u7684\u4f24\u5bb3\u3001\u9632\u5fa1\u6216\u5a01\u529b\u5c31\u63d0\u9ad8" )
				.append( Math.round( q.rarity().potency() * 100f ) ).append( "%\u3002" );

		//a vestige and its masterwork core, only on the top two tiers (text only for now)
		if (q.rarity().ordinal() >= com.shatteredpixel.shatteredpixeldungeon.items.rarity.Rarity.LEGENDARY.ordinal()){
			String ench = enchantName( it );
			if (ench.isEmpty()) ench = "\u70c8\u7130";
			b.append( "\n\n\u5b83\u8fd8\u5e26\u6709\u4e00\u4e2a\u6b8b\u8ff9\u9644\u9b54\uff0c\u90a3\u662f\u4e00\u7f15\u5b83\u59cb\u7ec8\u672a\u80fd\u771f\u6b63\u638c\u63e1\u7684\u9b54\u6cd5\u7684\u5fae\u5f31\u56de\u54cd\uff1a" );
			b.append( "\n\u2726 \u6b8b\u8ff9" ).append( ench ).append( "\u9644\u9b54\uff1a\u89e6\u53d1\u9891\u7387\u4e3a\u771f\u54c1\u768427%" );
			b.append( "\n\n\u6770\u4f5c " )
					.append( com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.masterworkLevel( it ) )
					.append( "/5\uff1a\u6bcf\u6761\u52a0\u6210\u8bcd\u6761\u6bcf\u7ea7\u63d0\u534715%\u3002\u5b83\u5df2\u6740\u6b7b" )
					.append( com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.masterworkKills( it ) )
					.append( "\u4e2a\u654c\u4eba\u3002\u4e0b\u4e00\u7ea7\u9700\u89815\u4e2a\u6740\u654c\u3002" );
		}
		return b.toString();
	}

	/** The item's family as the window names it ("\u7c7b\u578b\uff1a\u5251"). */
	public static String familyTitle( Item it ){
		if (!isGear( it )) return "";
		switch (com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.familyOf( it )){
			case SWORD:  return "\u5251";
			case AXE:    return "\u65a7";
			case SPEAR:  return "\u77db";
			case MACE:   return "\u9524";
			case DAGGER: return "\u5315\u9996";
			case STAFF:  return "\u6cd5\u6756";
			case ARMOR:  return "\u62a4\u7532";
			case SHIELD: return "\u76fe";
			case RING:   return "\u6212\u6307";
			case WAND:   return "\u6cd5\u6756";
			default:     return "\u6742\u9879";
		}
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
