/*
 * Radish Pixel Dungeon
 *
 * 踩水表现：
 *  - 水格里的角色脚下盖一条水线（水面贴在脚上），水线的取样位置随水面一起滚动，读起来是"流动的水"
 *  - 角色入水 / 在水里挪动时，在水面留一圈涟漪（复用引擎自带的 Ripple）
 * 覆盖层放在 mobs 之后的独立 Group 里，所以能盖住人物下半身。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class WaterWade {

	private static final int POOL = 16;
	private static final float ALPHA = 0.86f;
	/** 水线高度（像素）：大个子盖多一点 */
	private static final float BAND_HERO = 7f, BAND_MOB = 5f;
	/** 与水面同速（GameScene 里 5px/s） */
	private static final float SCROLL = 5f;

	private static final List<Image> pool = new ArrayList<>();
	private static final Map<Char,Integer> lastCell = new IdentityHashMap<>();
	private static Group host;
	private static String path;

	/** 调试：上帧画了几条水线、累计生成多少涟漪 */
	public static int drawn, ripples;

	public static void init( Group group ) {
		ripples = 0;
		host = group;
		pool.clear();
		lastCell.clear();
		path = null;
		drawn = 0;
	}

	private static Image patch() {
		if (host == null || pool.size() >= POOL) return null;
		String water = Dungeon.level.waterTex();
		if (water == null) return null;
		path = water;
		Image img = new Image( path );
		img.alpha( ALPHA );
		img.visible = false;
		host.add( img );
		pool.add( img );
		return img;
	}

	public static void update( float time ) {
		if (host == null || Dungeon.level == null) return;
		int width = Dungeon.level.width();
		int used = 0;

		// 水线随水面一起往下滚：取样条带做同样的偏移
		float scroll = ((time * SCROLL) % 32f) / 32f;

		used = draw( Dungeon.hero, used, width, scroll );
		for (Mob mob : Dungeon.level.mobs) {
			used = draw( mob, used, width, scroll );
		}
		for (int i = used; i < pool.size(); i++) {
			pool.get(i).visible = false;
		}
		drawn = used;
	}

	private static int draw( Char ch, int used, int width, float scroll ) {
		if (ch == null || !ch.isAlive() || ch.sprite == null) return used;
		int cell = ch.pos;
		if (cell < 0 || cell >= Dungeon.level.water.length || !Dungeon.level.water[cell]) {
			lastCell.remove( ch );
			return used;
		}
		if (!ch.sprite.visible || ch.sprite.alpha() <= 0.05f) return used;

		// 入水 / 水里挪动：留一圈涟漪
		Integer prev = lastCell.put( ch, cell );
		if (prev != null && prev != cell) {
			// 涟漪留在刚离开的那格：不会被角色自己挡住
			GameScene.ripple( prev );
			ripples++;
		}

		Image img = used < pool.size() ? pool.get( used ) : patch();
		if (img == null) return used;

		String water = Dungeon.level.waterTex();
		if (water != null && !water.equals( path )) {
			img.texture( water );
			path = water;
		}

		float band = (ch instanceof Hero) ? BAND_HERO : BAND_MOB;
		float v0 = scroll;
		float v1 = v0 + band / 32f;
		if (v1 > 1f) {
			v0 = 0f;
			v1 = band / 32f;
		}
		img.frame( new RectF( 0f, v0, 0.5f, v1 ) );

		img.x = (cell % width) * 16f;
		img.y = (cell / width) * 16f + 16f - band - 1f;
		img.visible = true;
		return used + 1;
	}
}
