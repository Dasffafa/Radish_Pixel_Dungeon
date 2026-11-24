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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.PrecipType;
import com.shatteredpixel.shatteredpixeldungeon.actors.Weather;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.WaterFX;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.utils.PointF;

/**
 * The rain on the water: small rings where drops land.
 *
 * Warped's WaterRain listens to its whole precipitation system and draws the drops' own sprites
 * above the water. This repo had no weather when the water kit was ported, so it was left as an
 * empty shell; now that {@link Weather} exists it can do its job, simply: a few rings a second on
 * water cells the camera can see, as hard as the weather is falling. Snow and sleet only ripple
 * lightly (melting as they land), hail forks a couple of splashes.
 *
 * Pictures only: no water is disturbed, nothing lands on a character.
 */
public final class WaterRain {

	/** Rings a second at full strength, per kind of weather. */
	private static final float RAIN_RATE = 26f, HAIL_RATE = 34f, SLEET_RATE = 16f, SNOW_RATE = 5f;

	/** Acceptance counter (Z_RainWater selftest). */
	public static int ripples;

	private static float acc;

	private WaterRain(){}

	/** A new level or scene: its rings went with it. */
	public static void reset(){
		acc = 0f;
	}

	/** Every frame over the water: as many landing rings as this weather makes. */
	public static void rain( Level level, float dt ){
		if (level == null || dt <= 0f) return;
		PrecipType type = Weather.precip;
		if (type == PrecipType.NONE || Weather.intensity <= 0f) return;
		//ice takes the drops without a ring
		if (Weather.freezing()) return;

		float rate;
		switch (type){
			case HAIL:     rate = HAIL_RATE; break;
			case SLEET:    rate = SLEET_RATE; break;
			case SNOW:     rate = SNOW_RATE; break;
			case BLIZZARD: rate = HAIL_RATE * 1.4f; break;
			case RAIN:
			default:       rate = RAIN_RATE; break;
		}

		acc += dt * rate * Weather.intensity;
		int made = 0;
		while (acc >= 1f && made < 12){
			acc -= 1f;
			made++;
			if (ring( level, type )) ripples++;
		}
	}

	private static boolean ring( Level level, PrecipType type ){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;

		for (int tries = 0; tries < 8; tries++){
			int cx = (int)( (camX + (float)Math.random() * w) / DungeonTilemap.SIZE );
			int cy = (int)( (camY + (float)Math.random() * h) / DungeonTilemap.SIZE );
			if (cx < 0 || cy < 0 || cx >= level.width() || cy >= level.height()) continue;
			int cell = cy * level.width() + cx;
			if (!level.water[cell] || level.solid[cell]) continue;

			PointF p = DungeonTilemap.tileToWorld( cell );
			float x = p.x + 4f + FxRandom.Float( 8f ), y = p.y + 6f + FxRandom.Float( 6f );
			int size = type == PrecipType.HAIL ? WaterFX.M : WaterFX.S;
			WaterFX.ring( x, y, size, WaterFX.liquid().foam, 0.35f + 0.35f * Weather.intensity, FxBudget.P3 );
			//hail throws a couple of splashes beside its ring
			if (type == PrecipType.HAIL && FxRandom.chance( 0.6f )){
				WaterFX.ring( x + FxRandom.Float( -5f, 5f ), y + FxRandom.Float( -3f, 3f ),
						WaterFX.S, WaterFX.liquid().foam, 0.25f, FxBudget.P3 );
			}
			return true;
		}
		return false;
	}
}
