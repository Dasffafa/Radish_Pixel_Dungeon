package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.warped;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/**
 * Stand-in for Warped's Sting (the one sound an enchantment's proc adds to a blow): this repo's
 * enchantment sounds are planned but not hooked here yet, so it asks for nothing. Kept with the
 * upstream signatures and volume math so {@link EnchantFx} reads the same as upstream.
 */
final class Sting {

	static final float SAME_GAP = 0.12f, ID_GAP = 0.25f, ECHO_VOL = 0.3f, ECHO_PITCH = 0.85f,
			QUIETEST = 0.25f, LOUDEST = 0.6f, PITCH_SPREAD = 0.05f;
	static final float[] TIER_VOL = { 0.7f, 1f, 1.25f, 1.4f };

	private Sting(){}

	static void reset(){}

	static void ask( final EnchantState st, EnchantLook l, Char at, int tier, boolean echo, boolean guest ){}

	static void play( EnchantState st ){}

	public static float volume( EnchantLook l, int tier, boolean echo ){
		if (l == null) return 0f;
		float v = l.vol * TIER_VOL[Math.max( 0, Math.min( TIER_VOL.length - 1, tier ) )];
		if (echo) v *= ECHO_VOL;
		return Math.max( QUIETEST, Math.min( LOUDEST, v ) );
	}
}
