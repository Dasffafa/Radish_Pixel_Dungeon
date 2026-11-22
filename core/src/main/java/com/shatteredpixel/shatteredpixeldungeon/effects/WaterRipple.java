/*
 * Radish Pixel Dungeon
 *
 * 水面涟漪：程序生成一圈浅色水波，从 0.9 倍放大到 2 倍、同时淡出。
 * 放在 terrain 之上、mobs 之下（GameScene 的 waterRipples 层），所以在水面上、在人物身后。
 * 引擎自带的 Ripple 只有 16px 且从 0 放大，太弱；这里换成更大更慢的一版。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

public class WaterRipple extends Image {

	private static SmartTexture RING;

	private float left, life;
	private float fromScale, toScale;
	private float startAlpha;
	private float delay;

	public static WaterRipple spawn( Group host, float cx, float cy,
	                                 float fromScale, float toScale, float life, float alpha, float delay ) {
		if (host == null || ring() == null) return null;
		WaterRipple r = new WaterRipple( cx, cy, fromScale, toScale, life, alpha, delay );
		host.add( r );
		return r;
	}

	private WaterRipple( float cx, float cy, float fromScale, float toScale, float life, float alpha, float delay ) {
		super( ring() );
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

	private static SmartTexture ring() {
		if (RING != null) return RING;
		try {
			Pixmap p = new Pixmap( 32, 32, Pixmap.Format.RGBA8888 );
			p.setColor( 0f, 0f, 0f, 0f );
			p.fill();
			// 外圈亮、内圈略淡，读起来像水面反光的一圈
			p.setColor( 0.95f, 1f, 0.98f, 1f );
			p.drawCircle( 15, 15, 14 );
			p.drawCircle( 15, 15, 13 );
			p.setColor( 0.86f, 0.98f, 0.92f, 0.8f );
			p.drawCircle( 15, 15, 12 );
			p.setColor( 0.74f, 0.92f, 0.86f, 0.55f );
			p.drawCircle( 15, 15, 13 );
			SmartTexture t = new SmartTexture( p );
			t.filter( Texture.NEAREST, Texture.NEAREST );
			RING = t;
		} catch (Throwable e) {
			RING = null;
		}
		return RING;
	}
}
