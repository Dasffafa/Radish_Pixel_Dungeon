/*
 * Radish Pixel Dungeon
 *
 * 水面涟漪：程序生成一圈浅色水波，从 1.0 倍放大到 2.1 倍、同时淡出。
 * 颜色跟着当前楼层的水色走（取水面贴图平均色再提亮），所以岩浆/冰水/神庙水各有各的泡沫色。
 * 放在 terrain 之上、mobs 之下（GameScene 的 waterRipples 层），所以在水面上、在人物身后。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

import java.util.HashMap;
import java.util.Map;

public class WaterRipple extends Image {

	private static final int SIZE = 32;
	private static final Map<Integer,SmartTexture> CACHE = new HashMap<>();

	private float left, life;
	private float fromScale, toScale;
	private float startAlpha;
	private float delay;

	public static WaterRipple spawn( Group host, float cx, float cy,
	                                 float fromScale, float toScale, float life, float alpha, float delay, int color ) {
		if (host == null) return null;
		SmartTexture tex = ring( color );
		if (tex == null) return null;
		WaterRipple r = new WaterRipple( tex, cx, cy, fromScale, toScale, life, alpha, delay );
		host.add( r );
		return r;
	}

	private WaterRipple( SmartTexture tex, float cx, float cy,
	                     float fromScale, float toScale, float life, float alpha, float delay ) {
		super( tex );
		this.fromScale = fromScale;
		this.toScale = toScale;
		this.life = life;
		this.left = life;
		this.startAlpha = alpha;
		this.delay = delay;

		origin.set( width / 2f, height / 2f );
		x = cx - origin.x;
		y = cy - origin.y;
		scale.set( fromScale, fromScale * 0.55f );
		alpha( 0f );
	}

	@Override
	public void update() {
		super.update();

		if (delay > 0f) {
			delay -= Game.elapsed;
			visible = false;
			return;
		}
		visible = true;

		left -= Game.elapsed;
		if (left <= 0f) {
			killAndErase();
			return;
		}

		float p = 1f - left / life;
		float s = fromScale + (toScale - fromScale) * p;
		scale.set( s, s * 0.55f );
		alpha( startAlpha * (1f - p) );
	}

	/** 一个颜色的水波贴图（外亮内淡的三圈） */
	private static SmartTexture ring( int color ) {
		SmartTexture cached = CACHE.get( color );
		if (cached != null) return cached;

		try {
			Pixmap p = new Pixmap( SIZE, SIZE, Pixmap.Format.RGBA8888 );
			p.setColor( 0f, 0f, 0f, 0f );
			p.fill();

			int outer = lighten( color, 0.72f );
			int mid = lighten( color, 0.55f );
			int inner = lighten( color, 0.38f );

			set( p, outer, 1f );
			p.drawCircle( 15, 15, 14 );
			p.drawCircle( 15, 15, 13 );

			set( p, mid, 0.85f );
			p.drawCircle( 15, 15, 12 );

			set( p, inner, 0.6f );
			p.drawCircle( 15, 15, 11 );

			SmartTexture t = new SmartTexture( p );
			t.filter( Texture.NEAREST, Texture.NEAREST );
			CACHE.put( color, t );
			return t;
		} catch (Throwable e) {
			CACHE.put( color, null );
			return null;
		}
	}

	private static void set( Pixmap p, int rgb, float a ) {
		p.setColor( ((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, a );
	}

	private static int lighten( int rgb, float toWhite ) {
		int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
		r = (int)(r + (255 - r) * toWhite);
		g = (int)(g + (255 - g) * toWhite);
		b = (int)(b + (255 - b) * toWhite);
		return (r << 16) | (g << 8) | b;
	}
}
