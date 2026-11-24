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
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

import java.util.HashMap;

/**
 * The grass swaying, done the way Warped does it: the tile's **top half is drawn again, shifted**,
 * and the tile itself is left out of the tilemap's chunk so nothing is drawn twice.
 *
 * This is the real thing at last. The tilemap skips grass cells (`DungeonTerrainTilemap.swayable`),
 * and this layer draws each of them as two strips - the foot, still, and the top, leaning a pixel or
 * two with the wind - each exactly once, so the colour is the art's own (no doubling, no glow).
 *
 * Only cells in the camera's view are made; a cell keeps its two pictures once made, and they are
 * simply off-screen when it is. A hole would show if a cell in view were not drawn, so cells are
 * drawn whether the hero has seen them or not (the fog hides them as it always does).
 */
public final class GrassSwayLayer {

	/** The row the tile is split at (its top part leans, its foot stays), and its pace. */
	private static final int SPLIT = 9;
	private static final float PACE = 1.05f;

	/** Acceptance counter (Z_GrassSway selftest): tiles swaying now. */
	public static int swaying;

	private static final class Tile {
		Image foot, top;
		float baseX, phase;
	}

	private static final HashMap<Integer, Tile> tiles = new HashMap<>();
	private static Group host;
	private static float time;

	private GrassSwayLayer(){}

	/** A new scene: its grass went with the old one. */
	public static void sceneCreated(){
		tiles.clear();
		host = null;
		time = 0f;
		swaying = 0;
	}

	/** Every frame: make the grass in view and lean its top with the wind. */
	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null) return;
		if (!(Game.scene() instanceof GameScene)) return;
		if (host == null){
			host = GameScene.grassSway();
			if (host == null) return;
		}

		time += dt;
		Camera c = Camera.main;
		float camX = c != null ? c.scroll.x : 0f, camY = c != null ? c.scroll.y : 0f;
		float w = c != null ? c.width : 480f, h = c != null ? c.height : 800f;
		int lvlW = Dungeon.level.width(), lvlH = Dungeon.level.height();

		int x0 = Math.max( 0, (int)( camX / DungeonTilemap.SIZE ) - 1 );
		int y0 = Math.max( 0, (int)( camY / DungeonTilemap.SIZE ) - 1 );
		int x1 = Math.min( lvlW - 1, (int)( (camX + w) / DungeonTilemap.SIZE ) + 1 );
		int y1 = Math.min( lvlH - 1, (int)( (camY + h) / DungeonTilemap.SIZE ) + 1 );

		float wind = Weather.windX;
		float amp = 1.2f + Math.abs( wind ) * 2.2f;
		float pace = PACE * (1f + 0.8f * Math.abs( wind ));

		int n = 0;
		for (int cy = y0; cy <= y1; cy++){
			for (int cx = x0; cx <= x1; cx++){
				int cell = cy * lvlW + cx;
				if (!DungeonTerrainTilemap.swayable( cell )) continue;

				Tile t = tiles.get( cell );
				if (t == null){
					t = make( cell );
					if (t == null) continue;
					tiles.put( cell, t );
				}

				int lean = (int)Math.round( Math.sin( time * pace * 6.2831853f + t.phase ) * amp + wind * 1.5f );
				t.top.x = t.baseX + lean;
				n++;
			}
		}
		swaying = n;
	}

	/** The two strips of one grass cell, on the layer. */
	private static Tile make( int cell ){
		if (host == null) return null;
		Image foot = DungeonTerrainTilemap.strip( cell, SPLIT, DungeonTilemap.SIZE );
		Image top = DungeonTerrainTilemap.strip( cell, 0, SPLIT );
		if (foot == null || top == null) return null;

		PointF p = DungeonTilemap.tileToWorld( cell );
		foot.x = p.x - 1f;
		foot.y = p.y + SPLIT;
		top.x = p.x - 1f;
		top.y = p.y;
		host.add( foot );
		host.add( top );

		Tile t = new Tile();
		t.foot = foot;
		t.top = top;
		t.baseX = p.x - 1f;
		t.phase = FxRandom.Float( 0f, 6.2831853f );
		return t;
	}
}
