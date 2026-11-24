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
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;

/**
 * How warm it is, and how warm the hero is - **without any harm**.
 *
 * Warped's ClimateManager works out an air temperature from season, biome, depth, weather and wind;
 * the hero's body temperature drifts toward it; and below -5 or above 30 he takes frost/heat damage
 * through Hypothermia and Heatstroke (slowed, chilled, burnt). This is the first half only: the air,
 * the drift, and what it looks like (breath in the cold, a shimmer of heat when it is too warm).
 * **Nothing here touches HP, buffs, speed or anything else of the game's** - no Hypothermia, no
 * Heatstroke, no damage. Temperature is climate, not a hazard.
 *
 * The numbers are Warped's thresholds where they had one: cold from 0 °C, freezing under -5 °C,
 * hot over 30 °C; the drift is slower here (clothes and a dungeon's still air), and the air itself
 * is a simple read of depth and weather rather than a simulation of fronts and seasons.
 */
public final class Thermal {

	/** The dungeon's still air by depth (0-based index is depth 1). */
	private static final float[] DEPTH_AIR = { 13f, 12f, 11f, 10f, 9f, 8f, 8f, 9f, 10f, 12f };
	/** What each kind of weather does to it, and what a wind does per unit of it. */
	private static final float WIND_CHILL = 9f;

	/** Where the hero is comfortable, and where he is in real trouble (Warped's thresholds). */
	public static final float COLD = 0f, FREEZING = -5f, HOT = 30f;
	/** His own body's normal heat, and how fast it follows the air (degrees a second, at most). */
	public static final float WARM = 36.5f, DRIFT = 0.06f;

	/** The hero's own warmth now, °C. */
	private static float body = WARM;
	/** puffs made by the cold breath (tests) */
	public static int breaths;
	/** Acceptance counters. */
	public static int updates;

	private static float breathIn;

	private Thermal(){}

	/** A new game or level: the hero starts warm, as any hero would. */
	public static void reset(){
		body = WARM;
		breathIn = 0f;
	}

	/** The air about the hero, °C: depth, weather and wind, nothing else. */
	public static float ambient(){
		int idx = Dungeon.depth <= 0 ? 0 : Math.min( DEPTH_AIR.length - 1, Dungeon.depth - 1 );
		float t = DEPTH_AIR[idx];
		switch (Weather.precip){
			case RAIN:     t -= 3f; break;
			case SLEET:    t -= 5f; break;
			case SNOW:     t -= 7f; break;
			case HAIL:     t -= 6f; break;
			case BLIZZARD: t -= 12f; break;
			default: break;
		}
		t -= Math.abs( Weather.windX ) * WIND_CHILL;
		return t;
	}

	/** Sets the hero's warmth outright: the self-tests use it (a body does not cool by 30 degrees a frame). */
	public static void debugBody( float t ){
		body = t;
	}

	/** The hero's body temperature now, °C. */
	public static float body(){
		return body;
	}

	/** COLD, FREEZING, HOT or 0 when comfortable. */
	public static float stress(){
		if (body >= HOT) return HOT;
		if (body <= FREEZING) return FREEZING;
		if (body <= COLD) return COLD;
		return 0f;
	}

	/** Every frame: the body follows the air, and the cold or the heat shows. */
	public static void update( float dt ){
		if (dt <= 0f || Dungeon.hero == null || Dungeon.level == null) return;
		updates++;

		float air = ambient();
		float step = DRIFT * dt;
		if (body > air) body = Math.max( air, body - step );
		else if (body < air) body = Math.min( air, body + step );

		if (!(Game.scene() instanceof GameScene)) return;
		CharSprite s = Dungeon.hero.sprite;
		if (s == null || !s.visible) return;

		//cold breath: a little white puff from the mouth now and then
		breathIn -= dt;
		if (body <= 12f && breathIn <= 0f){
			breathIn = 1.1f + FxRandom.Float( 0.8f );
			FxEmitter air2 = Fx.air();
			if (air2 != null){
				FxParticle p = (FxParticle) air2.recycle( FxParticle.class );
				if (p != null){
					p.reset( s.x + s.width() / 2f + (s.flipHorizontal ? -3f : 3f), s.y + s.height() * 0.35f )
							.look( FxRandom.chance( 0.5f ) ? FxFrames.MOTE_2 : FxFrames.MOTE_1 )
							.paint( 0xEAF2FF )
							.vel( -2f + FxRandom.Float( 4f ), -8f - FxRandom.Float( 6f ) )
							.life( 0.9f ).alpha( 0.45f, 0.1f, 0.5f )
							.priority( FxBudget.P3 );
					breaths++;
				}
			}
		}

		//too warm: a shimmer of heat off the hero
		if (body >= 30f && FxRandom.chance( dt * 2f )){
			FxEmitter air2 = Fx.air();
			if (air2 != null){
				FxParticle p = (FxParticle) air2.recycle( FxParticle.class );
				if (p != null){
					p.reset( s.x + FxRandom.Float( s.width() ), s.y + s.height() * 0.6f )
							.look( FxFrames.MOTE_1 ).paint( 0xFFD9A0 )
							.vel( Weather.windX * 6f, -10f ).life( 1.2f )
							.alpha( 0.3f, 0.2f, 0.6f ).priority( FxBudget.P3 );
				}
			}
		}
	}
}
