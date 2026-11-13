/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class ShiningGloves extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 25 + 5 * level() };
	}


	public ShiningGloves() {
		super(4, 1, 2, 16);
		image = "shining_gloves";
	}
}
