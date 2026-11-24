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

	/** The line as the window reads it, with the number this item's roll gives (Warped's style). */
	public String describe( com.shatteredpixel.shatteredpixeldungeon.items.Item item, float strength ){
		com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
				com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( item );
		switch (this){
			case CRIT:        return "\u66b4\u51fb\u51e0\u7387 +" + pct( 0.08f + 0.22f * strength );
			case LIFESTEAL:   return "\u5438\u8840 " + pct( 0.30f * strength );
			case EXECUTIONER: return "\u5904\u51b3\uff1a\u76ee\u6807\u534a\u8840\u65f6\u4f24\u5bb3 +" + pct( 0.35f * strength );
			case DEFIANT:     return "\u4e0d\u5c48\uff1a\u81ea\u8eab\u534a\u8840\u65f6\u4f24\u5bb3 +" + pct( 0.35f * strength );
			case STORMBORN:   return "\u98ce\u66b4\u4e4b\u5b50\uff1a\u96f7\u66b4\u65f6\u4f24\u5bb3 +" + pct( 0.20f * strength );
			case MOONLIT:     return "\u6708\u534e\uff1a\u591c\u665a\u4f24\u5bb3 +" + pct( 0.25f * strength );
			case SLAYER:      return "\u5c60\u622e" + q.slayer().title() + "\uff1a\u5bf9\u8be5\u65cf\u4f24\u5bb3 +" + pct( 0.30f * strength );
			case HUNTER:      return "\u4f60\u6740\u6b7b\u7684\u91ce\u517d\u6709" + pct( 0.15f + 0.35f * strength ) + "\u51e0\u7387\u7559\u4e0b\u8089";
			case WARM_GRIP:   return "\u6c14\u6e29\u611f\u53d7 +" + Math.round( 3f * strength ) + "\u00b0C";
			case FLEET:       return "\u79fb\u52a8\u901f\u5ea6 +" + pct( 0.15f * strength );
			case WINDBREAK:   return "\u98ce\u5bd2\u51cf\u5c11 " + pct( 0.60f * strength );
			case COOLING:     return "\u9ad8\u6e29\u964d\u4f4e " + Math.round( 5f * strength ) + "\u00b0C";
			case RAINPROOF:   return "\u6dcb\u96e8\u6d78\u900f\u51cf\u6162 " + pct( 0.60f * strength );
			case SUREFOOT:    return "\u5760\u843d\u4f24\u5bb3 -" + pct( 0.40f * strength );
			case HUSHED:      return "\u6f5c\u884c\u66f4\u597d";
			case STEADY:      return "\u5c04\u51fb\u66f4\u7a33";
			default:          return title();
		}
	}

	private static String pct( float f ){
		return Math.round( f * 100f ) + "%";
	}

	/** The lines that fit this family, in table order. */
	public static List<RarityLine> forFamily( Quality.Family family ){
		List<RarityLine> out = new ArrayList<>();
		for (RarityLine l : values()) if (l.fits( family )) out.add( l );
		return out;
	}
}
