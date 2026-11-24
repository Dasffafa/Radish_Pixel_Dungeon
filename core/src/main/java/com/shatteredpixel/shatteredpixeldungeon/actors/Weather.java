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

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;

/**
 * The weather, cut down to what it looks and sounds like.
 *
 * Warped's ClimateManager is a whole climate simulation (temperature, humidity, fronts, day and
 * night, storms that strike and blobs that spawn), and it has teeth: a blizzard freezes, a
 * heatwave burns. This is the part worth having here — what is falling and how hard, and the wind
 * that carries it — and **none** of the harm: it never touches a character, a buff, a blob or the
 * damage of anything. Weather is scenery.
 *
 * A level rolls its own weather when it is made (a little under a third of them get any); it can
 * also be set outright, which the self-tests do.
 */
public final class Weather {

	/** What falls, how hard (0..1), and the wind's sideways push (-1..1, positive is rightwards). */
	public static PrecipType precip = PrecipType.NONE;
	public static float intensity = 0f;
	public static float windX = 0.35f;
	/** Whether it is a storm (lightning flashes and heavier drops; still no harm). */
	public static boolean storming;

	/** Acceptance counter (Z_Weather selftest). */
	public static int rolls;

	private Weather(){}

	/** Set the weather outright. */
	public static void set( PrecipType type, float strength, float wind ){
		precip = type;
		intensity = Math.max( 0f, Math.min( 1f, strength ) );
		windX = Math.max( -1f, Math.min( 1f, wind ) );
		storming = type == PrecipType.BLIZZARD || (intensity > 0.8f && FxRandom.chance( 0.35f ));
	}

	/** No weather at all. */
	public static void clear(){
		precip = PrecipType.NONE;
		intensity = 0f;
		storming = false;
	}

	/**
	 * A level's own weather: a little under a third of them are wet or snowy, the rest clear. Depths
	 * below the caves are always clear (there is no sky down there to fall from).
	 */
	public static void rollForLevel(){
		rolls++;
		clear();
		if (Dungeon.level == null) return;
		//no sky in the deeper halls: keep them dry
		if (Dungeon.depth >= 21) return;
		float r = FxRandom.Float( 1f );
		if (r >= 0.30f) return;
		boolean snow = Dungeon.depth >= 11 || FxRandom.chance( 0.35f );
		float strength = 0.35f + FxRandom.Float( 0.65f );
		set( snow ? PrecipType.SNOW : PrecipType.RAIN, strength, FxRandom.Float( -0.5f, 0.5f ) );
	}
}
