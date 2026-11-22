package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.noosa.audio.Sample;

import java.util.Random;

/** 环境音播放：距离衰减 + 左右声像 + 随机变调。 */
public final class AmbientPlayer {

	public static final float PITCH_SPREAD = 0.08f;

	private static final Random RNG = new Random();
	private static boolean loaded;

	private AmbientPlayer(){}

	public static boolean audible(){
		return SPDSettings.soundFx() && SPDSettings.ambVol() > 0;
	}

	/** 环境音量系数（0..1，平方曲线，和音乐/音效滑块一致） */
	public static float level(){
		float v = SPDSettings.ambVol() / 10f;
		return v * v;
	}

	private static void ensureLoaded(){
		if (loaded) return;
		loaded = true;
		Sample.INSTANCE.load( AmbientSound.assets() );
	}

	/** 播一个环境音；pan = -1 左 … 1 右 */
	public static void play( AmbientSound sound, float volume, float pitch, float pan ){
		volume *= level();
		if (sound == null || volume <= 0.004f || !audible()) return;
		ensureLoaded();
		String take = sound.takes[RNG.nextInt( sound.takes.length )];
		float p = Math.max( -1f, Math.min( 1f, pan ) );
		float left = volume * (p > 0f ? 1f - p : 1f);
		float right = volume * (p < 0f ? 1f + p : 1f);
		try {
			Sample.INSTANCE.play( take, left, right, pitch );
		} catch (Exception e){
			// 缺文件也别崩
		}
	}

	public static float pan( float dx ){
		return Math.max( -1f, Math.min( 1f, dx / 240f ) );
	}

	public static float falloff( float distance ){
		return Math.max( 0f, 1f - distance / 320f );
	}
}
