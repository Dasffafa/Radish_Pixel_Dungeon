/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Warped Pixel Dungeon
 * Copyright (C) 2026 Gabriel Duarte Guerra (gabriwar)
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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.fire;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;

/**
 * Ash: what a burnt scroll or a burnt heap leaves curling up after its green flash - a few grey
 * flakes thrown up, tumbling a quarter turn at a time as they sway down, settling where they land
 * and fading there. Matter, P3. From any thread (handed to the render thread); sent on to co-op
 * (co-op networking dropped here).
 */
public final class Ash {

	private Ash(){}

	/** Its two greys. */
	public static final int LIGHT = 0x6A6468, DARK = 0x3E3A3C;

	/** n flakes off a cell, seen where the hero sees it. */
	//acceptance counter (Z_Embers selftest)
	public static int flakes;

	public static void flakes( int cell, int n ){
		flakes++;
		fall( cell, n );
	}

	static void fall( final int cell, final int n ){
		if (!Fx.onRenderThread()){
			Fx.post( () -> fall( cell, n ) );
			return;
		}
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length() || !Dungeon.level.heroFOV[cell]) return;
		int w = Dungeon.level.width();
		float x = (cell % w + 0.5f) * DungeonTilemap.SIZE, y = (cell / w + 0.5f) * DungeonTilemap.SIZE;
		FxEmitter air = Fx.air();
		if (air == null) return;
		for (int i = 0; i < n; i++){
			if (!FxBudget.keep( FxBudget.P3, x, y )) continue;
			float life = FxRandom.Float( 1.2f, 1.8f );
			((FxParticle) air.recycle( FxParticle.class )).reset( x + FxRandom.Float( -4f, 4f ), y + FxRandom.Float( -2f, 2f ) )
					.look( FxRandom.chance( 0.5f ) ? FxFrames.MOTE_2 : FxFrames.PEBBLE_3 ).paint( FxRandom.chance( 0.5f ) ? LIGHT : DARK )
					.life( life ).alpha( 0.9f, 0, 0.4f ).vel( FxRandom.Float( -6f, 6f ), -FxRandom.Float( 8f, 14f ) ).gravity( 20f )
					.drag( 0.6f ).sway( FxRandom.Float( 3f, 6f ), FxRandom.Float( 3f, 5f ) ).spin( 0.25f )
					.ground( y + FxRandom.Float( 3f, 6f ), FxParticle.SETTLE, 0 ).priority( FxBudget.P3 );
		}
	}
}
