package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 后跃斩冷却。 */
public class CurseLeapCooldown extends FlavourBuff {

	@Override
	public String icon() {
		return BuffIndicator.TIME;
	}

	@Override
	public String toString() {
		return "leap";
	}
}
