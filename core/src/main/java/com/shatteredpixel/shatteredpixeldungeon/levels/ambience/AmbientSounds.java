package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

/** 同时发声数量限制的桩：没有音频，永远算"满"。 */
public final class AmbientSounds {
	private AmbientSounds(){}
	public static boolean full(){ return true; }
}
