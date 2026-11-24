/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 卷轴效果：危险预知（周期自动搜索）
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 「危险预知」效果：自动搜索周围。
 *
 * <p>搜索半径由原版 {@link Foresight} 控制，所以这里会一直维持一个 Foresight，
 * 再每 15 回合自动搜一次（等价于反复点搜索按钮）。</p>
 */
public class DiySearch extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private static final float TICK_INTERVAL = 1f;
	private static final int SEARCH_EVERY = 15;

	/** 触发次数（测试/诊断用）。 */
	public transient int activations = 0;

	private int cooldown = 0;

	@Override
	public boolean act() {
		if (Dungeon.hero != null) {
			if (Dungeon.hero.buff(Foresight.class) == null) {
				Buff.affect(Dungeon.hero, Foresight.class, 9999f);
			}
			if (cooldown <= 0) {
				Dungeon.hero.search(false);
				cooldown = SEARCH_EVERY;
				activations++;
			} else {
				cooldown--;
			}
		}
		spend(TICK_INTERVAL);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}
}
