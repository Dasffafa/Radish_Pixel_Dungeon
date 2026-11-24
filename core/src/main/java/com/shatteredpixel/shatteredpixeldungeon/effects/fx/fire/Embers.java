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
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxDecal;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFactories;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRecipes;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;

/**
 * Ground burnt to embers (a cell turned to EMBERS, as the render side sees the terrain change -
 * a co-op guest too: effects/fx/modules/FireHeatFx.terrainChanged): a scorch under the burnt
 * grass fading over SCORCH_FOR s, specks of ember rising off it and a thin smoke curling up for
 * 2-4 s. Render thread.
 */
public final class Embers {

	private Embers(){}

	public static final int SPECKS = 3, SMOKE = 0x6E6A64;
	public static final float SCORCH_HOLD = 1.5f, SCORCH_FOR = 6f, SMOKE_EVERY = 0.6f;

	/** A cell gone to embers. */
	//acceptance counter (Z_Embers selftest)
	public static int laid;

	public static void lay( int cell ){
		laid++;
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length() || !Dungeon.level.heroFOV[cell]) return;
		int w = Dungeon.level.width();
		float x = (cell % w + 0.5f) * DungeonTilemap.SIZE, y = (cell / w + 0.5f) * DungeonTilemap.SIZE;
		FxDecal.at( x, y + 3, FxFrames.SCORCH_S, FxRecipes.SCORCH_COLOR, 0.6f, SCORCH_HOLD, SCORCH_FOR - SCORCH_HOLD,
				FxRandom.chance( 0.5f ) );
		FxRecipes.embers( x, y + 2, Element.FIRE, SPECKS );
		FxEmitter smoke = GameScene.fxEmitter();
		if (smoke != null){
			smoke.pos( x - 5, y - 2, 10, 6 );
			smoke.start( FxFactories.wisp( SMOKE ), SMOKE_EVERY, Math.round( FxRandom.Float( 2f, 4f ) / SMOKE_EVERY ) );
		}
	}
}
