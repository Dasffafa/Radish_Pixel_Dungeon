package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

/**
 * A stand-in for Warped's Stunned: only the fact a stun lasts HOLD seconds is read (by
 * effects/fx/combat/Looks, which holds a stunned sprite's star that long).
 */
public class Stunned extends FlavourBuff {

	public static final float HOLD = 1.5f;

}
