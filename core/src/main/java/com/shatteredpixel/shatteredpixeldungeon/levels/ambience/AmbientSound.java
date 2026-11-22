package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

/** Warped 的环境音枚举（只保留小动物用到的几个），本仓库没有音频管线。 */
public enum AmbientSound {
	FROG(0.6f), SPLASH(0.5f), SKITTER(0.4f), RAT(0.5f), FLY(0.5f), LAVA(1.0f);

	/** 这段声音的长度，秒 */
	public final float length;

	AmbientSound( float length ){
		this.length = length;
	}
}
