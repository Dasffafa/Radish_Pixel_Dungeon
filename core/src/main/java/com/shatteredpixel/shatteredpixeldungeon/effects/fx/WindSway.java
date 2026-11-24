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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;

/**
 * The wind in the grass: a little blade of it, standing on a grass cell the hero has seen and
 * leaning this way and that with the breeze.
 *
 * Adapted from Warped's tiles/WindSway — which shifts the tops of its tree, bush and tall-grass
 * *tiles* sideways by whole pixels, and so needs its tilemaps to leave those tiles out of their
 * chunks and draw them through a batch of their own (plus its climate for wind speed and gusts).
 * This repo's tilemaps have no such hook, so this takes the other road: a few loose blades drawn
 * over the grass, swaying in a constant breeze. Nothing about the tile art itself moves.
 *
 * Only over ground the hero has seen (a blade must not lean out of the fog), a blade at a time,
 * a couple of pixels either way at most, on whole pixels so the art stays crisp.
 */
public final class WindSway {

	/** Blades kept on screen, and how often they pick a new bit of grass. */
	private static final int BLADES = 60;
	private static final float RETARGET = 2.5f;
	/** The breeze: cycles a second, and the most it leans either way (whole pixels). */
	private static final float PACE = 1.15f, LEAN = 2f;
	/** Its colour, its alpha, and the blades it may wear. */
	private static final int COLOR = 0x7FA04A;
	private static final float ALPHA = 0.55f;
	private static final int[][] FRAMES = { FxFrames.BLADE_3, FxFrames.BLADE_4, FxFrames.BLADE_5 };

	/** Acceptance counter (Z_Wind selftest): blades standing now. */
	public static int standing, swaySum;

	private static Blade[] blades;
	private static float time, retargetIn;

	private static final class Blade {
		Image img;
		int cell = -1;
		float baseX, baseY, phase;
	}

	private WindSway(){}

	/** A new scene: its blades went with the old one. */
	public static void sceneCreated(){
		blades = null;
		time = 0f;
		retargetIn = 0f;
		standing = 0;
	}

	/** Every frame: lean the blades that stand on grass the hero can see, or find new grass. */
	public static void update( float dt ){
		if (!(Game.scene() instanceof GameScene) || Dungeon.level == null) return;
		if (blades == null) build();
		time += dt;
		retargetIn -= dt;
		//a few blades short (a new scene, a meadow that just came into view): look again soon
		boolean retarget = retargetIn <= 0f || standing < BLADES / 2;
		if (retarget) retargetIn = RETARGET;

		int n = 0, leanSum = 0;
		for (Blade b : blades){
			if (b.cell < 0) continue;
			if (!grassVisible( b.cell )){
				b.img.visible = false;
				if (retarget) b.cell = -1;
				continue;
			}
			b.img.visible = true;
			int lean = (int)Math.round( Math.sin( time * PACE * 6.2831853f + b.phase ) * LEAN );
			b.img.x = b.baseX + lean;
			leanSum += lean;
			b.img.y = b.baseY;
			n++;
		}
		if (retarget){
			for (Blade b : blades) if (b.cell < 0) place( b );
		}
		standing = n;
		swaySum = leanSum;
	}

	private static void build(){
		Group host = GameScene.layer( GameScene.Layer.SURFACE );
		if (host == null){ blades = null; return; }
		blades = new Blade[BLADES];
		for (int i = 0; i < BLADES; i++){
			Blade b = new Blade();
			Image img = new Image( Assets.Effects.FX_LIGHT );
			int[] f = FRAMES[i % FRAMES.length];
			img.frame( new RectF( f[0], f[1], f[0] + f[2], f[1] + f[3] ) );
			img.hardlight( COLOR );
			img.am = ALPHA;
			img.scale.set( 2f );
			img.visible = false;
			host.add( img );
			b.img = img;
			b.phase = FxRandom.Float( 0f, 6.2831853f );
			blades[i] = b;
		}
		placeAll();
	}

	private static void placeAll(){
		for (Blade b : blades) place( b );
	}

	/** Put a blade on a bit of grass the hero can see, at its foot. */
	private static void place( Blade b ){
		int w = Dungeon.level.width(), h = Dungeon.level.height();
		if (standing >= BLADES / 2 && FxRandom.chance( 0.5f )) return;
		for (int tries = 0; tries < 12; tries++){
			int cell = FxRandom.Int( w * h );
			if (!grassVisible( cell )) continue;
			PointF p = DungeonTilemap.tileToWorld( cell );
			int[] f = FRAMES[FxRandom.Int( FRAMES.length )];
			b.cell = cell;
			b.baseX = p.x + 2 + FxRandom.Int( DungeonTilemap.SIZE - 4 );
			b.baseY = p.y + DungeonTilemap.SIZE - f[3] - FxRandom.Int( 3 );
			b.img.x = b.baseX;
			b.img.y = b.baseY;
			b.img.visible = true;
			return;
		}
		b.cell = -1;
		b.img.visible = false;
	}

	private static boolean grassVisible( int cell ){
		if (cell < 0 || cell >= Dungeon.level.length()) return false;
		if (!Dungeon.level.heroFOV[cell] && !Dungeon.level.visited[cell]) return false;
		if (Dungeon.level.solid[cell]) return false;
		int t = Dungeon.level.map[cell];
		return t == Terrain.GRASS || t == Terrain.HIGH_GRASS
				|| t == Terrain.FURROWED_GRASS;
	}
}
