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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Weather;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;

/**
 * The dust in the air of a dungeon: pale motes drifting slowly across the light, too faint to follow
 * and too many to miss.
 *
 * Warped keeps `DustParticle` and `SunMoteParticle` for this, drawn about its weather and its light
 * shafts. This repo has neither, so the motes are made plainly here: a few a second somewhere in the
 * view, drifting with the wind and settling back down, gone after several seconds.
 *
 * Atmosphere only: nothing here touches a character or the game's state.
 */
public final class AmbientDust {

	/** Motes a second, their colour, and how long one lingers, s. */
	private static final float RATE = 1.6f;
	private static final int COLOR = 0xE4E8F0, COLOR_WARM = 0xFFF2D8;
	private static final float HOLD = 5f;

	/** Acceptance counter (Z_Dust selftest). */
	public static int motes;

	private static float acc;

	private AmbientDust(){}

	/** A new scene: the old one's dust went with it. */
	public static void reset(){
		acc = 0f;
	}

	/** Every frame: let a mote or two loose in the view. */
	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null) return;
		if (!(Game.scene() instanceof GameScene)) return;
		if (Emitter.freezeEmitters){
			return;
		}
		if (Game.timeTotal < 1f) return;

		acc += dt * RATE;
		int made = 0;
		while (acc >= 1f && made < 4){
			acc -= 1f;
			made++;
			if (mote()) motes++;
		}
	}

	private static boolean mote(){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f;
		float fh = c != null ? c.height : 800f;
		int lvlW = Dungeon.level.width(), lvlH = Dungeon.level.height();

		for (int tries = 0; tries < 5; tries++){
			int cx = (int)( (camX + (float)Math.random() * w) / DungeonTilemap.SIZE );
			int cy = (int)( (camY + (float)Math.random() * fh) / DungeonTilemap.SIZE );
			if (cx < 0 || cy < 0 || cx >= lvlW || cy >= lvlH) continue;
			int cell = cy * lvlW + cx;
			if (Dungeon.level.solid[cell]) continue;
			if (!Dungeon.level.heroFOV[cell] && !Dungeon.level.visited[cell]) continue;

			PointF p = DungeonTilemap.tileToWorld( cell );
			boolean warm = Math.random() < 0.3f;
			FxParticle q = (FxParticle) Fx.air().recycle( FxParticle.class );
			if (q == null) return false;
			q.reset( p.x + FxRandom.Float( 14f ), p.y + FxRandom.Float( 12f ) )
					.look( Math.random() < 0.5f ? FxFrames.MOTE_1 : FxFrames.MOTE_2 )
					.paint( warm ? COLOR_WARM : COLOR )
					.vel( Weather.windX * 6f + FxRandom.Float( -2f, 2f ), -1f + FxRandom.Float( -1.5f, 1.5f ) )
					.sway( FxRandom.Float( 1f, 2f ), FxRandom.Float( 0.3f, 0.7f ) )
					.life( HOLD + FxRandom.Float( 2f ) )
					.alpha( 0.14f + FxRandom.Float( 0.1f ), 0.25f, 0.5f )
					.priority( FxBudget.P3 );
			return true;
		}
		return false;
	}
}
