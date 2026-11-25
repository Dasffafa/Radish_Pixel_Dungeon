package com.shatteredpixel.shatteredpixeldungeon.effects.fx.kit;

/**
 * A stand-in for a Warped helper the particle kit calls into. Warped's own version reads weather,
 * water and decal state; the kit only needs the calls to exist, so these do nothing here.
 */
public final class Fx {

	private Fx(){}

	public static float windX(){ return 0f; }
	public static float windY(){ return 0f; }
	public static boolean seen( float x, float y ){ return true; }
}
