package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** 黑闪蓄势：命中但未触发黑闪时累积，提升下一次黑闪概率。 */
public class BlackFlashCharge extends Buff {

	public static final int MAX_STACKS = 50;

	private int stacks = 0;
	private boolean attacked = false;

	public int stacks() {
		return stacks;
	}

	public void add(int amount) {
		stacks = Math.min(MAX_STACKS, stacks + amount);
		attacked = true;
	}

	public void reset() {
		stacks = 0;
	}

	@Override
	public boolean act() {
		//上回合没有攻击就清零
		if (!attacked) {
			stacks = 0;
		}
		attacked = false;

		if (stacks <= 0) {
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.MONK_ENERGY;
	}

	@Override
	public String iconTextDisplay() {
		return "+" + stacks + "%";
	}

	@Override
	public void tintIcon(com.watabou.noosa.Image icon) {
		icon.hardlight(0.25f, 0.25f, 0.25f);
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", stacks);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("stacks", stacks);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stacks = bundle.getInt("stacks");
	}
}
