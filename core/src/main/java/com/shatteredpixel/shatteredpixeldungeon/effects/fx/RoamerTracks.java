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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.PointF;

/**
 * The prints a beast leaves: a dark little mark pressed into the floor at its feet on some of its
 * steps, fading out over a few seconds.
 *
 * Adapted from Warped's effects/RoamerTracks (which posts prints worked out by its overworld roamer
 * events, drawing them from effects/roamer_tracks.png — neither of which this repo has): here the
 * prints come straight from each step of a monster, and they are drawn with this repo's decals.
 * Only monsters leave them (the hero's own steps would clutter the floor he is looking at), only on
 * ground he has already seen, and not on every step.
 */
public final class RoamerTracks {

	/** How plain a print is, how long it holds, and how long it then takes to go. */
	private static final float ALPHA = 0.4f, HOLD = 8f, FADE = 6f;
	/** The colour of the dust a print scuffs up (readable on both light and dark floors). */
	private static final int COLOR = 0x8A7663;
	/** How often a step leaves one (a light-footed thing leaves fewer). */
	private static final float EVERY = 0.55f;

	/** Acceptance counter (Z_Tracks selftest). */
	public static int prints;

	private RoamerTracks(){}

	/** A monster stepped from `from` to `to`: maybe it left a print on the ground there. */
	public static void stepped( Char ch, int from, int to ){
		if (ch == null || ch == Dungeon.hero || !ch.isAlive()) return;
		if (Dungeon.level == null || to < 0 || to >= Dungeon.level.length()) return;
		if (!Dungeon.level.visited[to] && !Dungeon.level.heroFOV[to]) return;
		if (Dungeon.level.solid[to] || Dungeon.level.pit[to]) return;
		if (ch.flying || Dungeon.level.water[to]) return;
		if (!FxRandom.chance( EVERY )) return;

		PointF p = DungeonTilemap.tileToWorld( to );
		int[] frame = FxFrames.WET_9;
		FxDecal.at( p.x + 8f + FxRandom.Float( -3f, 3f ), p.y + 13f + FxRandom.Float( -2f, 2f ),
				frame, COLOR, ALPHA, HOLD, FADE, FxRandom.chance( 0.5f ) );
		prints++;
	}
}
