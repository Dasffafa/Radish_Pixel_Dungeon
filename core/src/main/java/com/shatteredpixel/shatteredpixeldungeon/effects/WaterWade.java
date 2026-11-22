/*
 * Radish Pixel Dungeon
 *
 * 踩水表现：
 *  - 水格里的角色脚下盖一条水线（水面贴在脚上），取样位置随水面一起滚动，读起来是"流动的水"
 *  - 水线按水流方向轻微倾斜（1px 级别），不是一根死平线
 *  - 浅水/深水分档：岸边最浅、往里逐渐变厚变实
 *  - 角色入水时溅起水花，入水/在水里挪动时在离开的格子放两圈水波（颜色随水色）
 * 覆盖层放在 mobs 之后的独立 Group 里，所以能盖住人物下半身。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class WaterWade {

	private static final int POOL = 16;
	private static final float ALPHA = 0.86f, ALPHA_MID = 0.78f, ALPHA_SHORE = 0.62f;
	/** 水线高度（像素）：大个子盖多一点 */
	private static final float BAND_HERO = 7f, BAND_MOB = 5f;
	/** 浅水/深水的水线厚度与倾斜 */
	private static final float DEEP = 1f, MID = 0.78f, SHORE = 0.5f;
	private static final float SKEW = 1.1f;
	/** 与水面同速（GameScene 里 5px/s） */
	private static final float SCROLL = 5f;

	private static final List<Waterline> pool = new ArrayList<>();
	private static final Map<Char,Integer> lastCell = new IdentityHashMap<>();
	private static Group host, rippleHost;
	private static String path;
	private static int liquidColor = 0xFFDDF7EA;
	private static String colorPath;

	/** 每个水格到岸的距离（1 = 岸边），按层缓存 */
	private static int[] depth;
	private static Level depthLevel;

	/** 调试：上帧画了几条水线、累计生成多少涟漪 */
	public static int drawn, ripples;

	public static void init( Group group, Group rippleLayer ) {
		ripples = 0;
		host = group;
		rippleHost = rippleLayer;
		pool.clear();
		lastCell.clear();
		path = null;
		drawn = 0;
		liquidColor = 0xFFDDF7EA;
		colorPath = null;
		depth = null;
		depthLevel = null;
	}

	/** 供调试/验收：在指定格子放一圈水波 */
	public static void spawnAt( int cell ) {
		spawnRipples( cell, Dungeon.level.width(), false );
	}

	/** 供调试/验收：某格的水深（1 = 岸边） */
	public static int depthAt( int cell ) {
		ensureDepth();
		if (cell < 0 || cell >= depth.length) return 1;
		return Math.max( 1, depth[cell] );
	}

	private static Waterline patch() {
		if (host == null || pool.size() >= POOL) return null;
		String water = Dungeon.level.waterTex();
		if (water == null) return null;
		path = water;
		Waterline img = new Waterline( water );
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

		used = draw( Dungeon.hero, used, width, scroll, time );
		for (Mob mob : Dungeon.level.mobs) {
			used = draw( mob, used, width, scroll, time );
		}
		for (int i = used; i < pool.size(); i++) {
			pool.get(i).visible = false;
		}
		drawn = used;
	}

	private static void spawnRipples( int cell, int width, boolean step ) {
		if (rippleHost == null) return;
		float cx = (cell % width) * 16f + 8f;
		float cy = (cell / width) * 16f + 8f;
		int color = liquidColor( Dungeon.level.waterTex() );

		WaterRipple.spawn( rippleHost, cx, cy, 1.0f, 2.1f, 0.85f, 0.85f, 0f, color );
		WaterRipple.spawn( rippleHost, cx, cy, 0.6f, 1.3f, 0.6f, 0.55f, 0.12f, color );
		ripples++;

		if (step) {
			Splash.at( cell, color, 2 );
		}
	}

	private static int draw( Char ch, int used, int width, float scroll, float time ) {
		if (ch == null || !ch.isAlive() || ch.sprite == null) return used;
		int cell = ch.pos;
		if (cell < 0 || cell >= Dungeon.level.water.length || !Dungeon.level.water[cell]) {
			lastCell.remove( ch );
			return used;
		}
		if (!ch.sprite.visible || ch.sprite.alpha() <= 0.05f) return used;

		Integer prev = lastCell.put( ch, cell );
		if (prev == null) {
			// 入水：水花 + 一圈水波
			Splash.at( cell, liquidColor( Dungeon.level.waterTex() ), 6 );
			spawnRipples( cell, width, false );
		} else if (prev != cell) {
			// 水里挪动：涟漪留在刚离开的那格，身后带两点水花
			spawnRipples( prev, width, true );
		}

		Waterline img = used < pool.size() ? pool.get( used ) : patch();
		if (img == null) return used;

		String water = Dungeon.level.waterTex();
		if (water != null && !water.equals( path )) {
			img.texture( water );
			path = water;
		}

		int dep = depthAt( cell );
		float scale = dep >= 3 ? DEEP : (dep == 2 ? MID : SHORE);
		float alpha = dep >= 3 ? ALPHA : (dep == 2 ? ALPHA_MID : ALPHA_SHORE);
		float band = ((ch instanceof Hero) ? BAND_HERO : BAND_MOB) * scale;

		float v0 = scroll;
		float v1 = v0 + band / 32f;
		if (v1 > 1f) {
			v0 = 0f;
			v1 = band / 32f;
		}
		img.frame( new RectF( 0f, v0, 0.5f, v1 ) );
		img.alpha( alpha );

		// 顺着水流把水线拉出一点斜度（随时间轻微摆动）
		img.setSkew( SKEW * (0.6f + 0.4f * (float)Math.sin( time * 0.9f + cell * 0.35f )) );

		img.x = (cell % width) * 16f;
		img.y = (cell / width) * 16f + 16f - band - 1f;
		img.visible = true;
		return used + 1;
	}

	/** 水线：上下边沿错开 skew 像素，画出来是一条斜的水面线 */
	private static class Waterline extends Image {

		private float skew;

		Waterline( Object tx ) {
			super( tx );
		}

		void setSkew( float skew ) {
			this.skew = skew;
			updateVertices();
		}

		@Override
		protected void updateVertices() {
			vertices[0] = 0;              vertices[1] = 0;
			vertices[4] = width;          vertices[5] = -skew;
			vertices[8] = width;          vertices[9] = height - skew;
			vertices[12] = 0;             vertices[13] = height;
			dirty = true;
		}
	}

	private static void ensureDepth() {
		Level lvl = Dungeon.level;
		if (lvl == depthLevel && depth != null) return;
		depthLevel = lvl;

		int w = lvl.width(), h = lvl.height(), n = lvl.map.length;
		int[] d = new int[n];
		ArrayDeque<Integer> q = new ArrayDeque<>();

		// 1 = 与陆地相邻的水格（岸边）
		for (int i = 0; i < n; i++) {
			if (!lvl.water[i]) continue;
			int x = i % w, y = i / w;
			boolean border = false;
			if (x == 0 || y == 0 || x == w - 1 || y == h - 1) border = true;
			else {
				if (!lvl.water[i - 1] || !lvl.water[i + 1] || !lvl.water[i - w] || !lvl.water[i + w]) border = true;
			}
			if (border) {
				d[i] = 1;
				q.add( i );
			}
		}
		// 往里逐层加 1（最多算到 4，够用了）
		while (!q.isEmpty()) {
			int c = q.poll();
			if (d[c] >= 4) continue;
			int x = c % w, y = c / w;
			int[] nb = { c - 1, c + 1, c - w, c + w };
			for (int k = 0; k < 4; k++) {
				int t = nb[k];
				if (t < 0 || t >= n) continue;
				if (k < 2 && (t % w != x)) continue;
				if (!lvl.water[t] || d[t] != 0) continue;
				d[t] = d[c] + 1;
				q.add( t );
			}
		}
		depth = d;
	}

	/** 取水面贴图的平均色提亮成泡沫色；同一张贴图只算一次 */
	private static int liquidColor( String water ) {
		if (water == null) return liquidColor;
		if (water.equals( colorPath )) return liquidColor;
		try {
			Pixmap p = new Pixmap( Gdx.files.internal( water ) );
			long r = 0, g = 0, b = 0, cnt = 0;
			for (int y = 0; y < p.getHeight(); y++) {
				for (int x = 0; x < p.getWidth(); x++) {
					int c = p.getPixel( x, y );
					if ((c & 0xFF) < 16) continue;
					r += (c >> 24) & 0xFF;
					g += (c >> 16) & 0xFF;
					b += (c >> 8) & 0xFF;
					cnt++;
				}
			}
			if (cnt > 0) {
				int rr = (int)(r / cnt), gg = (int)(g / cnt), bb = (int)(b / cnt);
				rr = (int)(rr + (255 - rr) * 0.62f);
				gg = (int)(gg + (255 - gg) * 0.62f);
				bb = (int)(bb + (255 - bb) * 0.62f);
				liquidColor = (rr << 16) | (gg << 8) | bb;
			}
		} catch (Throwable e) {
			// 保留上一次的颜色
		}
		colorPath = water;
		return liquidColor;
	}
}
