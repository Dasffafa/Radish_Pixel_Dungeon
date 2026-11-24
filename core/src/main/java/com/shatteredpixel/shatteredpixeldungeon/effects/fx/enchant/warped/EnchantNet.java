package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.warped;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/**
 * Stand-in for Warped's network layer for enchant visuals: this repo has no co-op, so nothing is
 * ever sent. The kinds and signatures are kept so {@link EnchantFx} reads the same as upstream.
 */
final class EnchantNet {

	static final int PROC = 0, GLYPH = 1, GAIN = 2, WIELD = 3, PIECE = 4, REVEAL = 5, BLOW_END = 6, SWING = 7, SURGE = 8;

	private EnchantNet(){}

	static void send( int kind, int[] ints, int[] cells ){}
	static void send( int kind, int[] ints, int[] cells, int serial ){}

	static void sendProc( EnchantLook l, Char a, Char d, float s, int level, int flags, int tier ){}
	static void sendProc( EnchantLook l, Char a, Char d, float s, int level, int flags, int tier, int serial ){}

	static void sendWard( EnchantLook l, Char wearer, Char attacker, float s, int level, int flags, int moment, int tier ){}
	static void sendWard( EnchantLook l, Char wearer, Char attacker, float s, int level, int flags, int moment, int tier,
			int serial ){}

	static void sendBlowEnded( Char a, Char d ){}
	static void sendBlowEnded( Char a, Char d, int serial ){}

	static void sendSwing( EnchantLook l, Char a, Char d, boolean landed, boolean melee, int level, boolean wayward,
			int dist ){}
	static void sendSwing( EnchantLook l, Char a, Char d, boolean landed, boolean melee, int level, boolean wayward,
			int dist, int serial ){}

	static void sendWield( Char c, EnchantState st ){}
}
