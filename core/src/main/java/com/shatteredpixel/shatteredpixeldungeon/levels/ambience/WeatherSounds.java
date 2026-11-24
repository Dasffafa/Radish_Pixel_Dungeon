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

package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

import com.shatteredpixel.shatteredpixeldungeon.actors.PrecipType;
import com.shatteredpixel.shatteredpixeldungeon.actors.Weather;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;

/**
 * What the weather sounds like: rain on stone and leaves, the wind that drives it, and the thunder
 * that follows a flash.
 *
 * Warped's WeatherScape decides which of many rain beds a piece of ground hears (a roof, leaves, the
 * water) from its surroundings, and times thunder by how far the strike was. This is the plain
 * version: one bed at a time, picked by what is falling, from this repo's own ambience sounds (their
 * files were already here), and a thunderclap when a storm's flash goes.
 *
 * Sound is all it is: nothing here has an effect on the game.
 */
public final class WeatherSounds {

	/** Acceptance counters (Z_WeatherSound selftest). */
	public static int played, thunders;
	public static String lastSound = "";

	/** How long until the next bed plays, s. */
	private static float nextIn = 3f;

	private WeatherSounds(){}

	/** A new scene: the old one's weather sounds stop with it. */
	public static void reset(){
		nextIn = 3f;
	}

	/** Every frame: keep a bed of weather sound going while something is falling. */
	public static void update( float dt ){
		if (dt <= 0f) return;
		if (Weather.precip == PrecipType.NONE || Weather.intensity <= 0f){
			nextIn = 3f;
			return;
		}
		if (AmbientSounds.full()) return;

		nextIn -= dt;
		if (nextIn > 0f) return;

		AmbientSound s = pick();
		nextIn = s.length * (0.6f + FxRandom.Float( 0.5f ));

		AmbientSounds.played( s.length );
		AmbientPlayer.play( s, s.gain * (0.5f + 0.5f * Weather.intensity),
				1f + FxRandom.Float( -0.05f, 0.05f ), FxRandom.Float( -0.5f, 0.5f ) );
		lastSound = s.name();
		played++;
	}

	/** A storm's flash went: the thunder that follows it (closer or further). */
	public static void thunder(){
		if (Weather.precip == PrecipType.NONE || AmbientSounds.full()) return;
		AmbientSound s = FxRandom.chance( 0.35f ) ? AmbientSound.THUNDER_NEAR : AmbientSound.THUNDER_FAR;
		AmbientSounds.played( s.length );
		AmbientPlayer.play( s, s.gain, 0.9f + FxRandom.Float( 0.2f ), FxRandom.Float( -0.3f, 0.3f ) );
		lastSound = s.name();
		thunders++;
	}

	private static AmbientSound pick(){
		switch (Weather.precip){
			case SNOW:
				return AmbientSound.SNOW;
			case BLIZZARD:
				return FxRandom.chance( 0.5f ) ? AmbientSound.BLIZZARD : AmbientSound.GALE;
			case HAIL:
				return AmbientSound.HAIL;
			case SLEET:
				return AmbientSound.SLEET;
			case RAIN:
			default:
				float r = FxRandom.Float( 1f );
				if (r < 0.45f) return AmbientSound.RAIN;
				if (r < 0.7f) return AmbientSound.RAIN_LIGHT;
				if (r < 0.85f) return AmbientSound.RAIN_HEAVY;
				return FxRandom.chance( 0.5f ) ? AmbientSound.RAIN_WATER : AmbientSound.RAIN_LEAVES;
		}
	}
}
