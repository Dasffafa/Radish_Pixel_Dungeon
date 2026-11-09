package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/** 杀戮欲望：每次击杀叠加一层，提升伤害，闲置一段时间后清空。 */
public class Bloodlust extends Buff {

	private static final int MAX_STACKS = 3;

	private int stacks = 0;
	private int idle = 0;

	public void addStack() {
		stacks = Math.min(MAX_STACKS, stacks + 1);
		idle = 0;
	}

	public int stacks() {
		return stacks;
	}

	@Override
	public boolean act() {
		idle++;
		if (idle >= 15) {
			stacks = 0;
		}
		if (stacks == 0) {
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}
}