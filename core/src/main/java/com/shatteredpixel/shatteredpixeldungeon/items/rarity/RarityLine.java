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

package com.shatteredpixel.shatteredpixeldungeon.items.rarity;

import java.util.ArrayList;
import java.util.List;

/**
 * The quality lines an item can show in its window: Warped's set, cut to the gear this repo has
 * (no rings, trinkets, missiles or guns). They are names only for now - the numbers behind them
 * (crit, lifesteal, warmth...) come with the perks, which this repo has not ported.
 */
public enum RarityLine {

	//weapon lines
	CRIT( Quality.Family.SWORD, Quality.Family.AXE, Quality.Family.SPEAR, Quality.Family.MACE, Quality.Family.DAGGER ),
	LIFESTEAL( Quality.Family.SWORD, Quality.Family.AXE, Quality.Family.SPEAR, Quality.Family.MACE, Quality.Family.DAGGER ),
	WARM_GRIP( Quality.Family.SWORD, Quality.Family.AXE, Quality.Family.SPEAR, Quality.Family.MACE, Quality.Family.DAGGER, Quality.Family.STAFF ),
	SLAYER( Quality.Family.SWORD, Quality.Family.AXE, Quality.Family.SPEAR, Quality.Family.MACE, Quality.Family.DAGGER ),
	EXECUTIONER( Quality.Family.SWORD, Quality.Family.AXE, Quality.Family.SPEAR, Quality.Family.MACE, Quality.Family.DAGGER ),
	MOONLIT( Quality.Family.SWORD, Quality.Family.STAFF ),
	STORMBORN( Quality.Family.SPEAR, Quality.Family.STAFF ),
	DEFIANT( Quality.Family.MACE, Quality.Family.AXE ),
	HUNTER( Quality.Family.SWORD, Quality.Family.SPEAR ),
	STEADY( Quality.Family.DAGGER, Quality.Family.STAFF ),

	//armour lines
	FLEET( Quality.Family.ARMOR ),
	HUSHED( Quality.Family.ARMOR ),
	RAINPROOF( Quality.Family.ARMOR ),
	COOLING( Quality.Family.ARMOR ),
	WINDBREAK( Quality.Family.ARMOR ),
	SUREFOOT( Quality.Family.ARMOR );

	private final Quality.Family[] families;

	RarityLine( Quality.Family... families ){
		this.families = families;
	}

	/** Whether gear of this family can carry the line. */
	public boolean fits( Quality.Family family ){
		for (Quality.Family f : families) if (f == family) return true;
		return false;
	}

	/** The line's name, in the item window. */
	public String title(){
		switch (this){
			case CRIT:        return "\u66b4\u51fb";
			case LIFESTEAL:   return "\u5438\u8840";
			case WARM_GRIP:   return "\u6696\u624b";
			case SLAYER:      return "\u5c60\u622e";
			case EXECUTIONER: return "\u5904\u51b3";
			case MOONLIT:     return "\u6708\u534e";
			case STORMBORN:   return "\u98ce\u66b4\u4e4b\u5b50";
			case DEFIANT:     return "\u4e0d\u5c48";
			case HUNTER:      return "\u730e\u624b";
			case STEADY:      return "\u7a33\u5b9a";
			case FLEET:       return "\u8f7b\u6377";
			case HUSHED:      return "\u9759\u97f3";
			case RAINPROOF:   return "\u9632\u96e8";
			case COOLING:     return "\u6e05\u51c9";
			case WINDBREAK:   return "\u6321\u98ce";
			case SUREFOOT:    return "\u7a33\u6b65";
			default:          return name();
		}
	}

	/** The lines that fit this family, in table order. */
	public static List<RarityLine> forFamily( Quality.Family family ){
		List<RarityLine> out = new ArrayList<>();
		for (RarityLine l : values()) if (l.fits( family )) out.add( l );
		return out;
	}
}
