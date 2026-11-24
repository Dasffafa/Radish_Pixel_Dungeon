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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/**
 * The damage numbers a blow shows. Warped's is a little card (Head: number, chips, stamps, hops...);
 * 本仓库暂时用现有的 FloatingText 顶上：数字照常浮出来，没有卡片的 chips/stamps/hop 细节。
 *
 * 这是打击演示管线（effects/fx/combat 的 Blow / BlowPresenter）所需要的最小接口，
 * 以后搬 Warped 的完整 HitCard 时只要替换这个类即可。
 */
public class HitCard {

	/** The icon code a hero skill's image starts at (see Blow.skillChip). */
	public static final int SKILL_BASE = 1000;

	public static final int MAIN = 0, SMALL = 1, GAIN = 2;

	//acceptance counter (Z_Blow selftest)
	public static int shown;

	private HitCard(){}

	private static HitCard note( CharSprite s, int number, int color ){
		if (s != null && number >= 0) {
			s.showStatus( color, Integer.toString( number ) );
			shown++;
		}
		return new HitCard();
	}

	public static HitCard show( CharSprite s, int attackerId, int number, int color, int icon, boolean crit, float critIn,
			int[] codes, int[] tints, float[] lands, int n ){
		return note( s, number, color );
	}

	public static HitCard small( CharSprite s, int number, int color, int chipCode, int chipTint ){
		return note( s, number, color );
	}

	public static HitCard small( CharSprite s, int number, int color, int[] chipCodes, int[] chipTints, int n ){
		return note( s, number, color );
	}

	public static HitCard late( CharSprite s, int code, int tint ){
		return note( s, -1, 0 );
	}

	public static HitCard late( CharSprite s, int[] chipCodes, int[] chipTints, int n ){
		return note( s, -1, 0 );
	}

	public static HitCard gain( CharSprite s, int icon, int amount, int color ){
		return note( s, amount, color );
	}

	public static HitCard counter( CharSprite s, int icon, int value, int color ){
		return note( s, value, color );
	}

	public static float textTop( CharSprite s ){
		return s == null ? 0 : s.y - 4f;
	}

	public static int alive(){
		return 0;
	}

	public static void reset(){
		shown = 0;
	}
}
