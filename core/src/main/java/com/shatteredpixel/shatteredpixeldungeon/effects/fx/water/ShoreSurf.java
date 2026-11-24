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

import com.shatteredpixel.shatteredpixeldungeon.actors.Weather;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.WaterFX;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.utils.PointF;

/**
 * The little waves that run up a bank: a white ring breaking at the shore now and then, and a few
 * splashes flung up with it.
 *
 * Warped's `TideSurf` does this as a band of surf rolling onto its beaches; a dungeon's pool has no
 * beach to roll up, so this is the small version - one ring at a shore cell every second or two, a
 * pair of droplets with it. Water already draws its own lapping foam; this adds the wave that
 * arrives.
 *
 * Pictures only.
 */
public final class ShoreSurf {

	/** A wave every WAVE_EVERY s at most, and how long its ring lives, s. */
	private static final float WAVE_EVERY = 0.8f, RING_LIFE = 0.7f;

	/** Acceptance counter (Z_IceFringe selftest). */
	public static int waves;

	private static float waveIn = 1f;

	private ShoreSurf(){}

	public static void reset(){
		waveIn = 1f;
	}

	/** Every frame: now and then a wave breaks on a bank in view. */
	public static void surf( Level level, float dt ){
		if (level == null || dt <= 0f) return;
		//a frozen pool has no surf
		if (Weather.freezing()) return;

		waveIn -= dt;
		if (waveIn > 0f) return;
		waveIn = WAVE_EVERY + FxRandom.Float( 1.4f );
		if (breakAt( level )) waves++;
	}

	private static boolean breakAt( Level level ){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;
		int lvlW = level.width(), lvlH = level.height();

		int x0 = Math.max( 0, (int)( camX / DungeonTilemap.SIZE ) );
		int y0 = Math.max( 0, (int)( camY / DungeonTilemap.SIZE ) );
		int x1 = Math.min( lvlW - 1, (int)( (camX + w) / DungeonTilemap.SIZE ) );
		int y1 = Math.min( lvlH - 1, (int)( (camY + h) / DungeonTilemap.SIZE ) );

		//the banks in view: water with a dry (or solid) neighbour, that the hero can see
		int found = 0;
		int[] pick = new int[2];
		int[][] sides = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		for (int cy = y0; cy <= y1; cy++){
			for (int cx = x0; cx <= x1; cx++){
				int cell = cy * lvlW + cx;
				if (!level.water[cell] || level.solid[cell] || level.pit[cell]) continue;
				if (!level.heroFOV[cell] && !level.visited[cell]) continue;
				boolean bank = false;
				for (int[] s : sides){
					int nx = cx + s[0], ny = cy + s[1];
					if (nx < 0 || ny < 0 || nx >= lvlW || ny >= lvlH) continue;
					int n = ny * lvlW + nx;
					if (level.water[n] && !level.solid[n]) continue;
					bank = true;
					break;
				}
				if (!bank) continue;
				found++;
				//reservoir sampling: any of them, evenly
				if (FxRandom.Int( found ) == 0){
					pick[0] = cx;
					pick[1] = cy;
				}
			}
		}
		if (found == 0) return false;

		PointF p = DungeonTilemap.tileToWorld( pick[1] * lvlW + pick[0] );
		float x = p.x + 4f + FxRandom.Float( 8f ), y = p.y + 8f + FxRandom.Float( 4f );
		WaterFX.ring( x, y, WaterFX.M, WaterFX.liquid().foam, 0.5f, FxBudget.P2 );
		if (FxRandom.chance( 0.6f )){
			WaterFX.ring( x + FxRandom.Float( -6f, 6f ), y + FxRandom.Float( -3f, 3f ),
					WaterFX.S, WaterFX.liquid().foam, 0.35f, FxBudget.P2 );
		}
		return true;
	}
}

