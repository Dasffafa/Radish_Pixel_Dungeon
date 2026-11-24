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

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Camera;
import com.watabou.utils.PointF;
import com.shatteredpixel.shatteredpixeldungeon.effects.WeatherOverlay;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.watabou.noosa.audio.Sample;

/**
 * A bolt down onto one character out of the sky above the view, leaning with the wind: what a charge
 * of lightning looks like when it lands on somebody.
 *
 * Warped drew it as two arcs (a leader out of the sky, its foot onto the sprite) built with explicit
 * forks and legs, played three sounds and popped sparks, wired into its ShockingBrew and Potion of
 * Thunder. This repo's `Lightning` builds its own branches from two points, so the bolt is one major
 * lightning from over the view's top down to the sprite; the sparks and the flash the wand already
 * shows stay the wand's (this only adds the sky's part). Pictures and sound: the damage is whatever
 * the caller already did, never this.
 */
public final class ThunderBolt {

	/** How far below the view's top the bolt starts, and the shortest bolt worth drawing, px. */
	private static final float TOP = 12f, SHORTEST = 16f;

	/** Acceptance counter (Z_ThunderBolt selftest). */
	public static int bolts;

	private ThunderBolt(){}

	/** A bolt striking down onto the given sprite, from over the top of the view. */
	public static void thunderEffect( CharSprite sprite ){
		if (sprite == null) return;
		if (hero == null || hero.sprite == null || hero.sprite.parent == null) return;
		Camera cam = Camera.main;
		PointF to = sprite.center();
		float top = (cam != null ? cam.scroll.y : 0f) + TOP;
		PointF from = new PointF( to.x + WeatherOverlay.skyLean(), top );
		if (to.y - from.y < SHORTEST){
			from.y = to.y - 120f - FxRandom.Float( 40f );
		}
		hero.sprite.parent.addToFront( new Lightning( from, to, null ).major() );
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING, 0.6f );
		bolts++;
	}
}
