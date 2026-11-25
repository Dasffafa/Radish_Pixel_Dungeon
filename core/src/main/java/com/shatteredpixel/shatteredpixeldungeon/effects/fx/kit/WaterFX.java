package com.shatteredpixel.shatteredpixeldungeon.effects.fx.kit;

/**
 * A stand-in for a Warped helper the particle kit calls into. Warped's own version reads weather,
 * water and decal state; the kit only needs the calls to exist, so these do nothing here.
 */
public final class WaterFX {

	private WaterFX(){}

	public static boolean onWater( float x, float y ){ return false; }
	public static void drop( float x, float y, float strength, int color ){}
}
