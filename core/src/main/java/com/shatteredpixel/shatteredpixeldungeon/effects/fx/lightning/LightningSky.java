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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning;

import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.LightningFlash;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FlashGate;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRecipes;
import com.shatteredpixel.shatteredpixeldungeon.levels.ambience.WeatherSounds;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Group;

/**
 * What the sky answers a lightning with: the whole view flashing for a moment (LightningFlash, on
 * FlashGate so flashes never strobe) and the thunder that follows, plus the sparks thrown where a
 * bolt lands.
 *
 * Warped hands all of this to its WeatherOverlay, which this repo never had - so it was a stand-in
 * that answered nothing. Now that this repo has a weather layer (WeatherOverlay / LightningFlash /
 * WeatherSounds), the stand-in does the job itself: the flash and the thunder are pictures and
 * sound only, they never strike or hurt anybody.
 */
public final class LightningSky {

	/** Acceptance counters (Z_LightningSky selftest). */
	public static int flashes, thunders, sparks;

	private LightningSky(){}

	/** No overlay carries a lightning here: it draws itself on the effect layers. */
	public static Object carry( Object lightning ){
		return null;
	}

	/** A major strike (a wand's, a potion's, a spell's): the flash and the thunder after it. */
	public static void struck( int cell ){
		flash();
	}

	/** A storm's own bolt: it flashes too (the storm's own gate), and thunders. */
	public static void stormStruck( Lightning lightning ){
		flash();
	}

	/** A zap's crack where it strikes: the thunder only (a creature's zap never flashes the screen). */
	public static void crackOnly( int cell ){
		WeatherSounds.thunder();
		thunders++;
	}

	/** A wand of lightning's thunder. */
	public static void wandThunder( int cell ){
		crackOnly( cell );
	}

	/** Sparks thrown where a bolt lands. */
	public static void spark( float x, float y, int n ){
		if (n <= 0) return;
		FxRecipes.sparks( x, y, Element.ELECTRIC, n );
		sparks++;
	}

	/** The screen's flash, as FlashGate lets the game's own flashes go (2 s apart, three in ten). */
	private static void flash(){
		LightningFlash f = LightningFlash.game();
		if (f != null){
			Group g = GameScene.layer( GameScene.Layer.EFFECTS );
			if (g != null){
				g.add( f );
				flashes++;
			}
		}
		WeatherSounds.thunder();
		thunders++;
	}
}
