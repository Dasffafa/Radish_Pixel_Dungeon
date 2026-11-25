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
		return "\u00b7 " + name + (l.curse ? "  (\u8bc5\u5492)" : "");
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
		//the rarity's own grant, said the way Warped says it (common gives nothing, so no line)
		if (q.rarity().potency() > 0f){
			b.append( "\n\n\u4ec5\u51ed\u7a00\u6709\u5ea6\uff0c\u5b83\u81ea\u8eab\u7684\u4f24\u5bb3\u3001\u9632\u5fa1\u6216\u5a01\u529b\u5c31\u63d0\u9ad8" )
					.append( Math.round( q.rarity().potency() * 100f ) ).append( "%\u3002" );
		}

		//a vestige and its masterwork core, only on the top two tiers (text only for now)
		if (q.rarity().ordinal() >= com.shatteredpixel.shatteredpixeldungeon.items.rarity.Rarity.LEGENDARY.ordinal()){
			String ench = "";
			if (it instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon){
				ench = com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.vestigeName(
						(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon) it );
			}
			if (ench.isEmpty()) ench = enchantName( it );
			if (ench.isEmpty()) ench = "\u70c8\u7130";
			b.append( "\n\n\u5b83\u8fd8\u5e26\u6709\u4e00\u4e2a\u6b8b\u8ff9\u9644\u9b54\uff0c\u90a3\u662f\u4e00\u7f15\u5b83\u59cb\u7ec8\u672a\u80fd\u771f\u6b63\u638c\u63e1\u7684\u9b54\u6cd5\u7684\u5fae\u5f31\u56de\u54cd\uff1a" );
			b.append( "\n\u00b7 \u6b8b\u8ff9" ).append( ench ).append( "\u9644\u9b54\uff1a\u89e6\u53d1\u9891\u7387\u4e3a\u771f\u54c1\u768427%" );
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
			case BOW:    return "\u5f13";
			case THROWN: return "\u6295\u63b7\u6b66\u5668";
			case ARMOR:  return "\u62a4\u7532";
			case SHIELD: return "\u76fe";
			case RING:   return "\u6212\u6307";
			case WAND:   return "\u6cd5\u6756";
			default:     return "\u6742\u9879";
		}
	}

	/** A line or two about the item's family, for the type section. */
	public static String familyText( Item it ){
		if (!isGear( it )) return "";
		switch (com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.familyOf( it )){
			case SWORD:  return "\u5251\uff1a\u5747\u8861\u7684\u8fd1\u6218\u6b66\u5668\uff0c\u653b\u51fb\u4e0e\u901f\u5ea6\u90fd\u5c45\u4e2d\uff0c\u9002\u5408\u5927\u591a\u6570\u60c5\u51b5\u3002";
			case AXE:    return "\u65a7\uff1a\u4f24\u5bb3\u5f88\u9ad8\u4f46\u901f\u5ea6\u504f\u6162\uff0c\u9760\u91cd\u51fb\u538b\u5236\u5bf9\u624b\u3002";
			case SPEAR:  return "\u77db\uff1a\u653b\u51fb\u8ddd\u79bb\u66f4\u8fdc\uff0c\u9002\u5408\u4fdd\u6301\u8ddd\u79bb\u4f5c\u6218\u3002";
			case MACE:   return "\u9524\uff1a\u7834\u7532\u80fd\u529b\u5f3a\uff0c\u51fb\u4e2d\u540e\u66f4\u5bb9\u6613\u9020\u6210\u786c\u76f4\u51fb\u3002";
			case DAGGER: return "\u5315\u9996\uff1a\u8f7b\u5feb\u4e14\u9690\u853d\uff0c\u9002\u5408\u7a81\u88ad\u4e0e\u80cc\u523a\u3002";
			case STAFF:  return "\u6cd5\u6756\uff1a\u4ee5\u9b54\u529b\u9a71\u52a8\u7684\u6b66\u5668\uff0c\u6ce8\u91cd\u6cd5\u672f\u4e0e\u8fdc\u7a0b\u3002";
			case BOW:    return "\u5f13\uff1a\u8fdc\u7a0b\u6b66\u5668\uff0c\u9700\u8981\u7bad\u77e2\uff0c\u8ddd\u79bb\u8d8a\u8fdc\u8d8a\u5b89\u5168\u3002";
			case THROWN: return "\u6295\u63b7\u6b66\u5668\uff1a\u4e00\u6b21\u6027\u4f7f\u7528\uff0c\u8fdc\u8ddd\u6295\u51fa\u540e\u9700\u8981\u62a2\u56de\u3002";
			case ARMOR:  return "\u62a4\u7532\uff1a\u62a4\u4f4f\u8eab\u4f53\uff0c\u63d0\u4f9b\u9632\u5fa1\u4e0e\u6301\u7eed\u4f5c\u6218\u80fd\u529b\u3002";
			case SHIELD: return "\u76fe\uff1a\u4e13\u95e8\u7528\u4e8e\u683c\u6321\u4e0e\u9632\u5b88\u3002";
			case RING:   return "\u6212\u6307\uff1a\u4ee5\u6301\u7eed\u52a0\u6210\u56de\u62a5\u4f69\u6234\u8005\u3002";
			default:     return "\u6742\u9879\uff1a\u4e0d\u5c5e\u4e8e\u5e38\u89c4\u6b66\u5668\u6216\u62a4\u7532\u3002";
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
