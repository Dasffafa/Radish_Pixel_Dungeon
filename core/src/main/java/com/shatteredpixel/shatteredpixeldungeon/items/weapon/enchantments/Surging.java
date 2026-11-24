package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

/**
 * A stand-in for Warped's Surging enchantment: only its SurgeTracker (the stacks the state look
 * counts) is known here; this repo has no Surging glyph.
 */
public class Surging {

	public static class SurgeTracker extends Buff {
		public int stacks;
	}
}
