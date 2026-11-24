package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/** A minimal stand-in for Warped's per-sprite enchant state (no enchant look framework here). */
public final class EnchantState {

	private static final EnchantState SHARED = new EnchantState();

	/** When a stunned sprite's star may be drawn (Looks/Blow). */
	public float stunAt = -1f;

	private EnchantState(){}

	public static EnchantState of( CharSprite s ){
		return SHARED;
	}

	public static EnchantState get( CharSprite s ){
		return SHARED;
	}
}
