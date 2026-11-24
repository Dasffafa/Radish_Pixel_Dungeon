/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;
import com.watabou.gltextures.SmartTexture;
import com.watabou.utils.PathFinder;

public class DungeonTerrainTilemap extends DungeonTilemap {

	static DungeonTerrainTilemap instance;

	public DungeonTerrainTilemap(){
		super(Dungeon.level.tilesTex());

		map( Dungeon.level.map, Dungeon.level.width() );

		instance = this;
	}

	/** Whether this cell's art is one the grass sway draws for itself (see GrassSwayLayer). */
	public static boolean swayable( int cell ){
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) return false;
		int t = Dungeon.level.map[cell];
		return t == Terrain.GRASS || t == Terrain.HIGH_GRASS || t == Terrain.FURROWED_GRASS;
	}

	/** The tile over `cell`, cut to rows `from`..`to` of its 16, as a picture (null if not drawn). */
	public static Image strip( int cell, int from, int to ){
		if (instance == null || instance.tileset == null) return null;
		//the grass's own visual (getTileVisual would say -1 for it now: the sway draws it)
		int tile = Dungeon.level.map[cell];
		int visual = DungeonTileSheet.directVisuals.get( tile, -1 );
		if (visual >= 0){
			visual = DungeonTileSheet.getVisualWithAlts( visual, cell );
		} else {
			visual = instance.visualFor( cell, tile, false );
		}
		if (visual < 0) return null;
		RectF f = instance.tileset.get( visual );
		if (f == null) return null;
		SmartTexture tex = instance.texture;
		//round, never truncate: a 15.999 px frame would leave a one-pixel seam at every tile edge
		int px = Math.round( f.left * tex.width );
		int py = Math.round( f.top * tex.height ) + from;
		//one pixel of overlap to the right: separate quads leave a hairline seam at tile edges
		//where the chunk's single quad set did not
		int pw = Math.round( f.width() * tex.width ) + 2;
		int ph = Math.max( 1, to - from );
		return new Image( tex, px, py, pw, ph );
	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat) {
		//the grass sways: it is left out of the chunk and drawn by GrassSwayLayer instead
		if (!flat && swayable( pos )) return -1;
		return visualFor( pos, tile, flat );
	}

	/** What the cell's art is, grass or not (the sway asks this for the grass it draws itself). */
	protected int visualFor( int pos, int tile, boolean flat ){
		int visual = DungeonTileSheet.directVisuals.get(tile, -1);
		if (visual != -1) return DungeonTileSheet.getVisualWithAlts(visual, pos);

		if (tile == Terrain.WATER) {
			// 水格用拼接变体：边缘那圈"没铺满"的水就是这些图块画出来的，
			// 不画的话每格水面都是满覆盖、池子变成方方正正的色块。
			return DungeonTileSheet.stitchWaterTile(
					map[pos + PathFinder.CIRCLE4[0]],
					map[pos + PathFinder.CIRCLE4[1]],
					map[pos + PathFinder.CIRCLE4[2]],
					map[pos + PathFinder.CIRCLE4[3]]
			);

		} else if (tile == Terrain.CHASM) {
			return DungeonTileSheet.stitchChasmTile( pos > mapWidth ? map[pos - mapWidth] : -1);
		}

		if (!flat) {
			if ((DungeonTileSheet.doorTile(tile))) {
				return DungeonTileSheet.getRaisedDoorTile(tile, map[pos - mapWidth]);
			} else if (DungeonTileSheet.wallStitcheable(tile)){
				return DungeonTileSheet.getRaisedWallTile(
						tile,
						pos,
						(pos+1) % mapWidth != 0 ?   map[pos + 1] : -1,
						pos + mapWidth < size ?     map[pos + mapWidth] : -1,
						pos % mapWidth != 0 ?       map[pos - 1] : -1
						);
			} else if (tile == Terrain.STATUE) {
				return DungeonTileSheet.RAISED_STATUE;
			} else if (tile == Terrain.STATUE_SP) {
				return DungeonTileSheet.RAISED_STATUE_SP;
			} else if (tile == Terrain.MINE_CRYSTAL) {
				return DungeonTileSheet.getVisualWithAlts(
						DungeonTileSheet.RAISED_MINE_CRYSTAL,
						pos);
			} else if (tile == Terrain.MINE_BOULDER) {
				return DungeonTileSheet.getVisualWithAlts(
						DungeonTileSheet.RAISED_MINE_BOULDER,
						pos);
			} else if (tile == Terrain.ALCHEMY) {
				return DungeonTileSheet.RAISED_ALCHEMY_POT;
			} else if (tile == Terrain.BARRICADE) {
				return DungeonTileSheet.RAISED_BARRICADE;
			} else if (tile == Terrain.HIGH_GRASS) {
				return DungeonTileSheet.getVisualWithAlts(
						DungeonTileSheet.RAISED_HIGH_GRASS,
						pos);
			} else if (tile == Terrain.FURROWED_GRASS) {
				return DungeonTileSheet.getVisualWithAlts(
						DungeonTileSheet.RAISED_FURROWED_GRASS,
						pos);
			} else {
				return DungeonTileSheet.NULL_TILE;
			}
		} else {
			return DungeonTileSheet.getVisualWithAlts(
					DungeonTileSheet.directFlatVisuals.get(tile),
					pos);
		}

	}

	public static Image tile(int pos, int tile ) {
		Image img = new Image( instance.texture );
		img.frame( instance.tileset.get( instance.getTileVisual( pos, tile, true ) ) );
		return img;
	}

	@Override
	protected boolean needsRender(int pos) {
		return super.needsRender(pos) && data[pos] != DungeonTileSheet.WATER;
	}
}
