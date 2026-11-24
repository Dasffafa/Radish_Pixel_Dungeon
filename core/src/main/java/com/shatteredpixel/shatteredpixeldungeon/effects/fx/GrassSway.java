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
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

/**
 * The tops of the grass, swinging: a patch of grass leans one or two whole pixels this way and that
 * with the wind, its foot staying put.
 *
 * Warped's `tiles/WindSway` moves the *tiles themselves*: its tilemaps leave the swaying tiles out of
 * their chunks and draw them through a batch of their own, so nothing is drawn twice. This repo's
 * tilemaps are chunked and have no such hook, so this takes the next best road: the **top rows of a
 * grass tile are drawn again**, a little to one side or the other, over the tile that is already
 * there. The grass art is thin and its top rows are mostly air, so the doubled tips read as a sway
 * rather than a ghost - checked by eye, not by proof.
 *
 * Only grass the hero can see, a couple of pixels at most, and only as many tiles as are in view.
 * Nothing about the map, the terrain or the game's state changes.
 */
public final class GrassSway {

	/** Patches of grass kept in view, and how many rows off the top of a tile are re-drawn. */
	private static final int STRIPS = 48, ROWS = 9;
	/** How often the patches pick a new bit of grass, s. */
	private static final float RETARGET = 2f;

	/** Acceptance counter (Z_GrassSway selftest): patches swinging now. */
	public static int swaying, leanSum;

	private static Strip[] strips;
	private static float time, retargetIn;

	private static final class Strip {
		Image img;
		int cell = -1;
		float baseX, phase;
	}

	private GrassSway(){}

	/** A new scene: its grass went with the old one. */
	public static void sceneCreated(){
		strips = null;
		time = 0f;
		retargetIn = 0f;
		swaying = 0;
	}

	/** Every frame: swing the patches, or find new grass when there are too few. */
	public static void update( float dt ){
		if (!(Game.scene() instanceof GameScene) || Dungeon.level == null) return;
		if (strips == null) build();
		if (strips == null) return;

		time += dt;
		retargetIn -= dt;
		boolean retarget = retargetIn <= 0f || swaying < STRIPS / 2;
		if (retarget) retargetIn = RETARGET;

		float wind = Weather.windX;
		float amp = 0.8f + Math.abs( wind ) * 1.8f;

		int n = 0, leans = 0;
		for (Strip s : strips){
			if (s.cell < 0 || s.img == null) continue;
			if (!grass( s.cell )){
				s.img.visible = false;
				if (retarget) s.cell = -1;
				continue;
			}
			int lean = (int)Math.round( Math.sin( time * 1.1f * 6.2831853f + s.phase ) * amp + wind * 1.5f );
			s.img.visible = true;
			s.img.x = s.baseX + lean;
			leans += lean;
			n++;
		}
		if (retarget){
			for (Strip s : strips) if (s.cell < 0) place( s );
		}
		swaying = n;
		leanSum = leans;
	}

	private static void build(){
		Group host = GameScene.layer( GameScene.Layer.FLOOR );
		if (host == null){ strips = null; return; }
		strips = new Strip[STRIPS];
		for (int i = 0; i < strips.length; i++){
			Strip s = new Strip();
			s.phase = FxRandom.Float( 0f, 6.2831853f );
			strips[i] = s;
		}
		for (Strip s : strips) place( s );
	}

	/** Put a patch on a bit of grass the hero can see. */
	private static void place( Strip s ){
		Group host = GameScene.layer( GameScene.Layer.FLOOR );
		if (host == null) return;

		int w = Dungeon.level.width(), h = Dungeon.level.height();
		for (int tries = 0; tries < 16; tries++){
			int cell = FxRandom.Int( w * h );
			if (!grass( cell )) continue;
			Image img = DungeonTerrainTilemap.topOf( cell, ROWS );
			if (img == null) continue;
			if (s.img != null){
				s.img.killAndErase();
				s.img.destroy();
			}
			img.visible = true;
			host.add( img );
			s.img = img;
			s.cell = cell;
			s.baseX = img.x;
			return;
		}
		s.cell = -1;
		if (s.img != null) s.img.visible = false;
	}

	private static boolean grass( int cell ){
		if (cell < 0 || cell >= Dungeon.level.length()) return false;
		if (!Dungeon.level.heroFOV[cell] && !Dungeon.level.visited[cell]) return false;
		if (Dungeon.level.solid[cell]) return false;
		int t = Dungeon.level.map[cell];
		return t == Terrain.GRASS || t == Terrain.HIGH_GRASS || t == Terrain.FURROWED_GRASS;
	}
}
