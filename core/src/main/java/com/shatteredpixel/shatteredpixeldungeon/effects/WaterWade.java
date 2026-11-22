/*
 * Radish Pixel Dungeon
 *
 * 踩水表现：在水格里的角色脚下覆盖一条水线（水面贴在脚上），并随水面轻微起伏。
 * 覆盖层放在 mobs 之后的独立 Group 里，所以能盖住人物下半身。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

import java.util.ArrayList;
import java.util.List;

public class WaterWade {

	private static final int POOL = 16;
	private static final float BAND_H = 7f;
	private static final float BAND_TOP = 17f;
	private static final float ALPHA = 0.86f;

	private static final List<Image> pool = new ArrayList<>();
	private static Group host;
	private static String path;

	/** 调试：上帧画了几条水线 */
	public static int drawn;

	public static void init( Group group ) {
		host = group;
		pool.clear();
		path = null;
	}

	private static Image patch() {
		if (host == null) return null;
		if (pool.size() >= POOL) return null;
		String water = Dungeon.level.waterTex();
		if (water == null) return null;
		if (path == null || !path.equals( water )) path = water;
		Image img = new Image( path );
		img.frame( 0, (int)BAND_TOP, 16, (int)BAND_H );
		img.alpha( ALPHA );
		img.visible = false;
		host.add( img );
		pool.add( img );
		return img;
	}

	public static void update( float time ) {
		if (host == null || Dungeon.level == null) return;
		int used = 0;
		int width = Dungeon.level.width();

		used = draw( Dungeon.hero, used, width, time );
		for (Mob mob : Dungeon.level.mobs) {
			used = draw( mob, used, width, time );
		}
		drawn = used;
		for (int i = used; i < pool.size(); i++) {
			pool.get(i).visible = false;
		}
	}

	private static int draw( Char ch, int used, int width, float time ) {
		if (ch == null || !ch.isAlive() || ch.sprite == null) return used;
		int cell = ch.pos;
		if (cell < 0 || cell >= Dungeon.level.water.length || !Dungeon.level.water[cell]) return used;
		if (!ch.sprite.visible || ch.sprite.alpha( ) <= 0.05f) return used;

		int x = used < pool.size() ? used : -1;
		Image img = null;
		if (x >= 0) {
			img = pool.get( x );
			String water = Dungeon.level.waterTex();
			if (water != null && water != path) {
				img.texture( water );
				img.frame( 0, (int)BAND_TOP, 16, (int)BAND_H );
				path = water;
			}
		} else {
			img = patch();
			if (img == null) return used;
		}

		img.x = (cell % width) * 16f;
		img.y = (cell / width) * 16f + 9f + (float)Math.sin( time * 3f + cell * 0.7f ) * 0.4f;
		img.visible = true;
		return used + 1;
	}
}
