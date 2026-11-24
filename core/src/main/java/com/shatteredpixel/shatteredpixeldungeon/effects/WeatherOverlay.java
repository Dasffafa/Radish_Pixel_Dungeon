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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.PrecipType;
import com.shatteredpixel.shatteredpixeldungeon.actors.Weather;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FlashGate;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxDecal;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.PointF;
import com.shatteredpixel.shatteredpixeldungeon.levels.ambience.WeatherSounds;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Camera;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

/**
 * What the weather looks like: the drops themselves, falling past the camera.
 *
 * Warped's WeatherOverlay is 1100 lines of rain, snow, hail, sleet, blizzards, mist, clouds, smoke
 * from burning trees, fireflies, sun motes, drifting leaves, dripping ceilings and thunder — it is
 * drawing a whole world's climate. This keeps what reads as weather on the screen: slanted rain as
 * little streaks, snow as slow flakes, hail as hard fast specks, all leaning with the wind.
 *
 * Each drop is a flat coloured block (no particle pool to run dry: a hundred of them are cheap), and
 * nothing here touches a character, a buff or a blob. Weather is scenery and only scenery.
 */
public final class WeatherOverlay {

	/** How many drops a second a full-strength weather makes. */
	private static final float RATE = 90f;

	private static final int RAIN_COLOR = 0xA8C8E8, SNOW_COLOR = 0xEEF6FF,
			HAIL_COLOR = 0xD8E8F0, SLEET_COLOR = 0xBCD4E8;

	/** Acceptance counter (Z_Weather selftest): drops made. */
	public static int spawned, flashes, splashes, mists;

	private static float acc, live;
	//when the next flash of a storm comes
	private static float flashIn = 4f;
	//when the next drop lands on the ground
	private static float splashAcc;
	//when the next wisp of ground mist rises
	private static float mistIn = 1f;

	private WeatherOverlay(){}

	/** A new scene: the old one's weather went with it. */
	public static void sceneCreated(){
		acc = 0f;
		live = 0f;
	}

	/** Every frame: move the drops, and make as many new ones as this weather calls for. */
	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null) return;
		if (!(Game.scene() instanceof GameScene)) return;
		if (Weather.precip == PrecipType.NONE || Weather.intensity <= 0f){
			flashIn = 4f;
			return;
		}

		//a storm's lightning: the whole view flashes for a moment (pictures only, it strikes nothing)
		if (Weather.storming){
			flashIn -= dt;
			if (flashIn <= 0f){
				flashIn = 4f + (float)Math.random() * 7f;
				com.shatteredpixel.shatteredpixeldungeon.effects.LightningFlash f =
						com.shatteredpixel.shatteredpixeldungeon.effects.LightningFlash.flash();
				Group host = GameScene.layer( GameScene.Layer.EFFECTS );
				if (host != null && f != null){
					host.add( f );
					flashes++;
					WeatherSounds.thunder();
				}
			}
		}

		FxEmitter air = Fx.air();
		if (air == null) return;

		splash( dt );
		mist( dt, air );
		WeatherSounds.update( dt );

		acc += dt * RATE * (0.3f + Weather.intensity);
		int made = 0;
		while (acc >= 1f && made < 24){
			acc -= 1f;
			made++;
			drop( air );
		}
	}

	/**
	 * Ground mist: a slow, very faint wisp rising off damp floors while it rains or snows. Purely
	 * atmospheric - it never hides a monster (alpha is a tenth or so) and touches nothing.
	 */
	private static void mist( float dt, FxEmitter air ){
		if (Weather.intensity < 0.25f) return;
		mistIn -= dt;
		if (mistIn > 0f) return;
		mistIn = 0.45f + (float)Math.random() * 0.9f;

		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;
		int lvlW = Dungeon.level.width(), lvlH = Dungeon.level.height();

		int cx = (int)( (camX + (float)Math.random() * w) / DungeonTilemap.SIZE );
		int cy = (int)( (camY + (float)Math.random() * h) / DungeonTilemap.SIZE );
		if (cx < 0 || cy < 0 || cx >= lvlW || cy >= lvlH) return;
		int cell = cy * lvlW + cx;
		if (Dungeon.level.solid[cell]) return;

		PointF p = DungeonTilemap.tileToWorld( cell );
		boolean snowy = Weather.precip == PrecipType.SNOW || Weather.precip == PrecipType.BLIZZARD;
		FxParticle q = (FxParticle) air.recycle( FxParticle.class );
		if (q == null) return;
		q.reset( p.x + FxRandom.Float( 12f ), p.y + DungeonTilemap.SIZE - FxRandom.Float( 6f ) )
				.look( FxFrames.Light.GLOW_31 )
				.paint( snowy ? SNOW_COLOR : RAIN_COLOR )
				.vel( Weather.windX * 14f, -3f - FxRandom.Float( 4f ) )
				.life( 4f + FxRandom.Float( 3f ) )
				.alpha( 0.10f + 0.06f * Weather.intensity, 0.3f, 0.6f )
				.priority( FxBudget.P3 );
		mists++;
	}

	/** Drops landing on the ground: a little fading mark where they hit (never on water: that rings). */
	private static void splash( float dt ){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;
		int lvlW = Dungeon.level.width(), lvlH = Dungeon.level.height();

		float per = (Weather.precip == PrecipType.SNOW || Weather.precip == PrecipType.BLIZZARD) ? 4f : 16f;
		splashAcc += dt * per * (0.3f + Weather.intensity);

		int made = 0;
		while (splashAcc >= 1f && made < 10){
			splashAcc -= 1f;
			made++;
			int cx = (int)( (camX + (float)Math.random() * w) / DungeonTilemap.SIZE );
			int cy = (int)( (camY + (float)Math.random() * h) / DungeonTilemap.SIZE );
			if (cx < 0 || cy < 0 || cx >= lvlW || cy >= lvlH) continue;
			int cell = cy * lvlW + cx;
			if (Dungeon.level.solid[cell] || Dungeon.level.water[cell]) continue;
			if (!Dungeon.level.heroFOV[cell] && !Dungeon.level.visited[cell]) continue;

			PointF p = DungeonTilemap.tileToWorld( cell );
			float x = p.x + 3f + FxRandom.Float( 10f ), y = p.y + 9f + FxRandom.Float( 5f );
			boolean snow = Weather.precip == PrecipType.SNOW || Weather.precip == PrecipType.BLIZZARD;
			int color = snow ? SNOW_COLOR : RAIN_COLOR;
			FxDecal.at( x, y, snow ? FxFrames.MOTE_1 : FxFrames.RING_5, color,
					snow ? 0.35f : 0.30f, 0.03f, 0.22f, Math.random() < 0.5 );
			splashes++;
		}
	}

	private static void drop( FxEmitter air ){
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f;
		float x = camX + (float)Math.random() * w;
		float y = camY - 6f;

		PrecipType type = Weather.precip;
		boolean snowy = type == PrecipType.SNOW || type == PrecipType.BLIZZARD;
		boolean icy = type == PrecipType.HAIL || type == PrecipType.SLEET;

		int color; int[] frame; float vyv, vxv, life;
		if (snowy){
			color = SNOW_COLOR;
			frame = Math.random() < 0.5 ? FxFrames.MOTE_2 : FxFrames.MOTE_1;
			vyv = 30f + 44f * Weather.intensity;
			vxv = Weather.windX * 60f;
			life = 2.4f;
		} else if (icy){
			color = type == PrecipType.HAIL ? HAIL_COLOR : SLEET_COLOR;
			frame = FxFrames.PEBBLE_2;
			vyv = 150f + 120f * Weather.intensity;
			vxv = Weather.windX * 90f;
			life = 0.9f;
		} else {
			color = RAIN_COLOR;
			frame = FxFrames.MOTE_2;
			vyv = 230f + 170f * Weather.intensity;
			vxv = Weather.windX * 110f;
			life = 0.85f;
		}

		FxParticle p = (FxParticle) air.recycle( FxParticle.class );
		if (p == null) return;
		p.reset( x, y ).look( frame ).paint( color ).vel( vxv, vyv ).life( life )
				.alpha( 0.55f + 0.35f * Weather.intensity, 0f, 0.1f ).priority( FxBudget.P3 );
		spawned++;
	}
}
