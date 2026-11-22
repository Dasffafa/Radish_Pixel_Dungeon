package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

/** 环境音播放器桩：本仓库没有搬音频系统，全部静默。 */
public final class AmbientPlayer {

	public static final float PITCH_SPREAD = 0.08f;

	private AmbientPlayer(){}

	public static boolean audible(){ return false; }

	public static void play( AmbientSound sound, float volume, float pitch, float pan ){}

	public static float pan( float dx ){ return 0f; }

	public static float falloff( float distance ){ return 0f; }
}
