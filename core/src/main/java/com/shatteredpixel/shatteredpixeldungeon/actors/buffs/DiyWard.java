/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 符石效果：周期性护盾
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 「护盾符文」效果：每 25 回合补到 8 点护盾。 */
public class DiyWard extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private static final float INTERVAL = 25f;
	private static final int SHIELD = 8;

	/** 触发次数（测试/诊断用）。 */
	public transient int activations = 0;

	@Override
	public boolean act() {
		activations++;
		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Barrier barrier = Dungeon.hero.buff(Barrier.class);
			if (barrier == null || barrier.shielding() < SHIELD) {
				barrier = Buff.affect(Dungeon.hero, Barrier.class);
				barrier.setShield(SHIELD);
				GLog.i("护盾符文：护盾补到 %d", SHIELD);
			}
		}
		spend(INTERVAL);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}
}
