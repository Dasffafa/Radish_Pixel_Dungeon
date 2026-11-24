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
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.utils.PointF;

/**
 * The low mist that lies over still water: thin wisps hugging the surface, drifting with whatever
 * wind there is.
 *
 * Warped's `SwampMist` is a whole layer of fog banks over its wetlands, read off the climate and the
 * overworld. This repo has no wetlands and no climate to read, so the mist is made plainly: a wisp
 * every second or so off a water cell in view, a couple of pixels up, gone in a few seconds.
 *
 * Atmosphere only: it never hides a monster (the alpha is a tenth) and touches nothing.
 */
public final class LowMist {

	/** Wisps a second at most, how long one lingers, s, and how faint it is. */
	private static final float RATE = 1.4f, LIFE = 6f, ALPHA = 0.16f;
	private static final int COLOR = 0xDCE6F0;

	/** Acceptance counter (Z_LowMist selftest). */
	public static int mists;

	private static float acc;

	private LowMist(){}

	/** A new scene: the old one's mist went with it. */
	public static void reset(){
		acc = 0f;
	}

	/** Every frame over the water: let a wisp rise now and then. */
	public static void mist( Level level, float dt ){
		if (level == null || dt <= 0f) return;
		FxEmitter air = Fx.air();
		if (air == null) return;

		acc += dt * RATE;
		int made = 0;
		while (acc >= 1f && made < 3){
			acc -= 1f;
			made++;
			if (wisp( level, air )) mists++;
		}
	}

	private static boolean wisp( Level level, FxEmitter air ){
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
			FxParticle q = (FxParticle) air.recycle( FxParticle.class );
			if (q == null) return false;
			q.reset( p.x + FxRandom.Float( 12f ), p.y + DungeonTilemap.SIZE - FxRandom.Float( 5f ) )
					.look( FxFrames.Light.GLOW_23 )
					.paint( COLOR )
					.vel( Weather.windX * 12f, -2f - FxRandom.Float( 3f ) )
					.life( LIFE + FxRandom.Float( 2f ) )
					.alpha( ALPHA + FxRandom.Float( 0.06f ), 0.3f, 0.6f )
					.priority( FxBudget.P3 );
			return true;
		}
		return false;
	}
}
