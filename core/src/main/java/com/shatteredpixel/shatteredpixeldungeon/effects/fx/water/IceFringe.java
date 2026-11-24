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
import com.shatteredpixel.shatteredpixeldungeon.actors.Weather;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxDecal;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.utils.PointF;

/**
 * The ice at the edge of a frozen pool: a pale crust creeping in along the bank, where the cold water
 * meets the dry stone first.
 *
 * Warped has a whole ice fringe tilemap for this; this repo has no ice tiles, so the fringe is drawn
 * as decals on the water cells that border land. Only in freezing weather, and only at the edges:
 * the middle of the pool takes the rime of {@link IceCrust} instead.
 */
public final class IceFringe {

	private static final float RATE = 3f, HOLD = 4f, FADE = 3f;
	private static final int TINT = 0xE4F4FF;
	private static final float ALPHA = 0.55f;

	/** Acceptance counter (Z_IceFringe selftest). */
	public static int fringes;

	private static float acc;

	private IceFringe(){}

	public static void reset(){
		acc = 0f;
	}

	/** Every frame: while it freezes, grow a crust along the banks in view. */
	public static void frost( Level level, float dt ){
		if (level == null || dt <= 0f || !Weather.freezing()) return;

		acc += dt * RATE * (0.4f + Weather.intensity);
		int made = 0;
		while (acc >= 1f && made < 6){
			acc -= 1f;
			made++;
			if (edge( level )) fringes++;
		}
	}

	private static boolean edge( Level level ){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;
		int lvlW = level.width(), lvlH = level.height();

		for (int tries = 0; tries < 8; tries++){
			int cx = (int)( (camX + (float)Math.random() * w) / DungeonTilemap.SIZE );
			int cy = (int)( (camY + (float)Math.random() * h) / DungeonTilemap.SIZE );
			if (cx < 0 || cy < 0 || cx >= lvlW || cy >= lvlH) continue;
			int cell = cy * lvlW + cx;
			if (!level.water[cell] || level.solid[cell] || level.pit[cell]) continue;
			if (!level.heroFOV[cell] && !level.visited[cell]) continue;

			//only at the bank: a dry (or solid) neighbour
			int landDx = 0, landDy = 0;
			int[][] sides = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
			for (int[] s : sides){
				int nx = cx + s[0], ny = cy + s[1];
				if (nx < 0 || ny < 0 || nx >= lvlW || ny >= lvlH) continue;
				int n = ny * lvlW + nx;
				if (!level.water[n] || level.solid[n]){
					landDx = s[0];
					landDy = s[1];
					break;
				}
			}
			if (landDx == 0 && landDy == 0) continue;

			PointF p = DungeonTilemap.tileToWorld( cell );
			float x = p.x + 1f + (landDx > 0 ? 10f : 0f) + FxRandom.Float( 2f );
			float y = p.y + 2f + (landDy > 0 ? 9f : 0f) + FxRandom.Float( 2f );
			int[] frame = FxFrames.RIME[FxRandom.Int( FxFrames.RIME.length )];
			FxDecal.at( x, y, frame, TINT, ALPHA, HOLD, FADE, FxRandom.chance( 0.5f ) );
			return true;
		}
		return false;
	}
}
