package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 「狂怒」技能留下的临时增伤状态：持续 3 回合，伤害 +60%。 */
public class AutochessRage extends Buff {

	private float left = 3f;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public void set(float turns) {
		left = turns;
	}

	public float left() {
		return left;
	}

	@Override
	public boolean act() {
		left -= 1f;
		if (left <= 0) {
			detach();
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}
}
