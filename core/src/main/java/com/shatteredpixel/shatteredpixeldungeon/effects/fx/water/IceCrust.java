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
 * The rime that gathers on freezing water: pale patches of ice creeping over the surface while the
 * sky is below freezing, and gone again once it warms.
 *
 * Warped freezes the very cell (`Terrain.FROZEN_WATER`, water to ice and back, walkable ice with its
 * own art). This repo's terrain has no separate ice tile to show, so the freeze is drawn the other
 * way: the water's *state* changes (no ripples, no splashes, no wetness - see Weather.freezing) and
 * the surface takes a skin of rime, laid on the floor layer.
 *
 * Pictures only: nobody is slowed, slipped or hurt on it.
 */
public final class IceCrust {

	/** Rime laid a second over a frozen surface in view, and how long one lasts (hold + fade), s. */
	private static final float RATE = 6f, HOLD = 2.5f, FADE = 2f;
	private static final int TINT = 0xD8F0FF;
	private static final float ALPHA = 0.5f;

	/** Acceptance counters (Z_Freeze selftest). */
	public static int patches;

	private static float acc;

	private IceCrust(){}

	/** A new scene: the old one's rime went with it. */
	public static void reset(){
		acc = 0f;
	}

	/** Every frame: while the sky freezes, patch the water in view with rime. */
	public static void frost( Level level, float dt ){
		if (level == null || dt <= 0f) return;
		if (!Weather.freezing()) return;

		acc += dt * RATE * (0.4f + Weather.intensity);
		int made = 0;
		while (acc >= 1f && made < 8){
			acc -= 1f;
			made++;
			if (patch( level )) patches++;
		}
	}

	private static boolean patch( Level level ){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;

		for (int tries = 0; tries < 6; tries++){
			int cx = (int)( (camX + (float)Math.random() * w) / DungeonTilemap.SIZE );
			int cy = (int)( (camY + (float)Math.random() * h) / DungeonTilemap.SIZE );
			if (cx < 0 || cy < 0 || cx >= level.width() || cy >= level.height()) continue;
			int cell = cy * level.width() + cx;
			if (!level.water[cell] || level.solid[cell] || level.pit[cell]) continue;
			if (!level.heroFOV[cell] && !level.visited[cell]) continue;

			PointF p = DungeonTilemap.tileToWorld( cell );
			int[] frame = FxFrames.RIME[FxRandom.Int( FxFrames.RIME.length )];
			FxDecal.at( p.x + 1f + FxRandom.Float( 6f ), p.y + 3f + FxRandom.Float( 6f ), frame,
					TINT, ALPHA, HOLD, FADE, FxRandom.chance( 0.5f ) );
			return true;
		}
		return false;
	}
}
