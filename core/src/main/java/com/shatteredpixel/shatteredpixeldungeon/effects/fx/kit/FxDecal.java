package com.shatteredpixel.shatteredpixeldungeon.effects.fx.kit;

/**
 * A stand-in for a Warped helper the particle kit calls into. Warped's own version reads weather,
 * water and decal state; the kit only needs the calls to exist, so these do nothing here.
 */
public final class FxDecal {

	private FxDecal(){}

	public static void splat( float x, float y, int color ){}
	public static void wet( float x, float y ){}
}
