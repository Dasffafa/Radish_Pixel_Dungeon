package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.warped;

/**
 * Stand-in for Warped's DrainFx (a lifesteal's beads drawn home along a lifeline): this repo's
 * drain enchantment has no bead art of its own, so the DRAIN family draws nothing. Kept so
 * {@link EnchantFx}'s dispatch reads the same as upstream.
 */
final class DrainFx {

	private DrainFx(){}

	static void play( EnchantFx.Beat b ){}

	static void finisher( EnchantFx.Beat b ){}

	static void gather( float x, float y, EnchantLook l, float seconds ){}

	static void finale( float x, float y, EnchantLook l, float scale ){}

	static float home( EnchantFx.Beat b, Object twist ){
		return 0f;
	}
}
