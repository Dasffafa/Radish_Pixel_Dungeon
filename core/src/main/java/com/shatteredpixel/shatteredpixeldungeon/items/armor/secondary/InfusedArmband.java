/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class InfusedArmband extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 2 + level() };
	}


	public InfusedArmband() {
		super(2, 0, 2, 11);
		image = "infused_armband";
	}
}
