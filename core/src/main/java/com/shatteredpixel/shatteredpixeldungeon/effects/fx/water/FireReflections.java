package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.gltextures.TextureCache;

import java.util.ArrayList;
import java.util.List;

/**
 * 火焰在水面上的倒影：火格附近的水面上画一条随波动轻微晃动的暖色光带（叠加混合）。
 * 最多同时 4 条；没有火的关卡完全不产生开销。
 */
public final class FireReflections {

	private FireReflections(){}

	private static final int MAX = 4;
	private static final float SIZE = DungeonTilemap.SIZE;
	private static final float ALPHA_MIN = 0.18f, ALPHA_MAX = 0.42f;

	private static Group host;
	private static final List<Image> pool = new ArrayList<>();
	private static final List<int[]> fires = new ArrayList<>();
	private static float lookIn;
	public static int shown;

	public static void sceneCreated(){
		host = null;
		pool.clear();
		fires.clear();
		lookIn = 0f;
		shown = 0;
	}

	public static void update( float dt ){
		Level level = Dungeon.level;
		if (level == null || level.blobs == null) return;

		// 0.3 秒刷新一次火格
		if ((lookIn -= dt) <= 0f){
			lookIn = 0.3f;
			fires.clear();
			for (com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob b : level.blobs.values()){
				if (!(b instanceof com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire)) continue;
				for (int i = 0; i < b.cur.length; i++){
					if (b.cur[i] > 0) fires.add( new int[]{ i, b.cur[i] } );
				}
			}
		}
		if (fires.isEmpty()){
			for (Image im : pool) im.visible = false;
			shown = 0;
			return;
		}

		if (host == null){
			host = GameScene.waterSheen();
			if (host == null) return;
		}

		int w = level.width();
		int used = 0;
		float t = Game.timeTotal;
		for (int[] f : fires){
			if (used >= MAX) break;
			int cell = f[0];
			if (!level.heroFOV[cell]) continue;
			// 附近（下/左/右 3 格内）找一格水
			int water = waterNear( level, cell );
			if (water < 0) continue;

			Image im;
			if (used < pool.size()){
				im = pool.get( used );
			} else {
				im = new Additive( TextureCache.get( Assets.Effects.WATER_SHEEN_B ) );
				im.visible = false;
				host.add( im );
				pool.add( im );
			}
			float wx = (water % w) * SIZE, wy = (water / w) * SIZE;
			im.x = wx + 2f + (float)Math.sin( t * 2.1f + used ) * 1.2f;
			im.y = wy + 2f;
			im.scale.set( 12f / 64f, 14f / 64f );
			im.visible = true;
			im.hardlight( 0xFFB050 );
			im.am = ALPHA_MIN + (ALPHA_MAX - ALPHA_MIN) * (0.5f + 0.5f * (float)Math.sin( t * 3.7f + used * 1.7f ));
			used++;
		}
		for (int i = used; i < pool.size(); i++) pool.get( i ).visible = false;
		shown = used;
	}

	/** 叠加混合的倒影片 */
	private static final class Additive extends Image {
		Additive( Object tex ){ super( tex ); }
		@Override
		public void draw(){
			com.watabou.glwrap.Blending.setLightMode();
			super.draw();
			com.watabou.glwrap.Blending.setNormalMode();
		}
	}

	private static int waterNear( Level level, int cell ){
		int w = level.width();
		int[] d = { cell + w, cell + 2 * w, cell + 3 * w, cell - 1, cell + 1,
				cell + w - 1, cell + w + 1, cell + 2 * w - 1, cell + 2 * w + 1 };
		for (int c : d){
			if (c < 0 || c >= level.map.length) continue;
			if (level.water[c]) return c;
		}
		return -1;
	}
}
