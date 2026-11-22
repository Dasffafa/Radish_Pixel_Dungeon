/*
 * Radish Pixel Dungeon
 *
 * 水面逐帧动画：把 environment/waterN_cycle.png（8 帧横向条）切成 8 张 32x32 纹理，
 * 按时间轮换给 SkinnedBlock，保留原有的滚动偏移。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.SkinnedBlock;

import java.util.HashMap;
import java.util.Map;

public class WaterCycle {

	private static final int FRAME_SIZE = 32;
	private static final int FRAME_COUNT = 8;
	private static final float FRAME_TIME = 0.18f;

	private static final Map<String, SmartTexture[]> cache = new HashMap<>();
	private static final Map<String, Integer> current = new HashMap<>();

	/** 每帧调用：把水面纹理切成 32x32 的单帧，按时间切换。失败时静默保留原贴图。 */
	public static void tick( SkinnedBlock water, String path, float time ) {
		if (water == null || path == null) return;
		SmartTexture[] frames = frames( path );
		if (frames == null) return;
		int idx = (int)(time / FRAME_TIME) % FRAME_COUNT;
		Integer prev = current.get( path );
		if (prev == null || prev != idx) {
			water.texture = frames[idx];
			water.offsetTo( water.offsetX(), water.offsetY() );
			current.put( path, idx );
		}
	}

	private static SmartTexture[] frames( String path ) {
		if (cache.containsKey( path )) return cache.get( path );
		String cycle = path.replace( ".png", "_cycle.png" );
		SmartTexture[] out = null;
		try {
			Pixmap sheet = new Pixmap( Gdx.files.internal( cycle ) );
			out = new SmartTexture[FRAME_COUNT];
			for (int i = 0; i < FRAME_COUNT; i++) {
				Pixmap f = new Pixmap( FRAME_SIZE, FRAME_SIZE, Pixmap.Format.RGBA8888 );
				f.drawPixmap( sheet, 0, 0, i * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE );
				SmartTexture t = new SmartTexture( f );
				t.wrap( Texture.REPEAT, Texture.REPEAT );
				t.filter( Texture.NEAREST, Texture.NEAREST );
				out[i] = t;
				// SmartTexture 保留 Pixmap 引用、首次 bind 时才上传，这里不能 dispose
			}
			// sheet 同样交给 GC，避免上传前被释放
		} catch (Throwable e) {
			out = null;
		}
		cache.put( path, out );
		return out;
	}

	public static void reset() {
		current.clear();
	}
}
