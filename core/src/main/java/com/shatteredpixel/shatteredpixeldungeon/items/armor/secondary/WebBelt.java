/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class WebBelt extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 15 + 10 * level() };
	}


	public WebBelt() {
		super(4, 2, 4, 16);
		image = "web_belt";
	}
}
