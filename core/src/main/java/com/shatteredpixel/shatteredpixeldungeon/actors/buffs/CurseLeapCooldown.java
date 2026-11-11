package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** 后跃斩冷却，按回合数递减。 */
public class CurseLeapCooldown extends Buff {

	private int turns = 0;

	public void set(int turns) {
		this.turns = turns;
	}

	public int turns() {
		return turns;
	}

	@Override
	public boolean act() {
		turns--;
		if (turns <= 0) {
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.TIME;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(turns);
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", turns);
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("turns", turns);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turns = bundle.getInt("turns");
	}
}
