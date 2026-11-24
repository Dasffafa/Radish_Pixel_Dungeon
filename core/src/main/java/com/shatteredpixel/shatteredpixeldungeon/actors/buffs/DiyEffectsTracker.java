/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 常驻效果记录
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * DIY 贴上去的常驻效果清单。
 *
 * <p>效果本身是挂在英雄身上的 buff，但 buff 有两点靠不住：读档后时长可能没存下来，
 * 或者被驱散类效果清掉。这个 buff 把效果 id 记成字符串列表（一定存得下），
 * 每回合检查一遍，缺了就补回去。</p>
 */
public class DiyEffectsTracker extends Buff {

	/** 我们已经挂上去的效果 id。 */
	private final LinkedHashSet<String> granted = new LinkedHashSet<>();
	/** 挂上去之前本来就在的效果 id（比如喝药拿到的灵视），脱下装备时不能删。 */
	private final LinkedHashSet<String> borrowed = new LinkedHashSet<>();

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public static DiyEffectsTracker get(Char ch) {
		return ch == null ? null : ch.buff(DiyEffectsTracker.class);
	}

	/** 保证英雄身上有同步器（没有就挂一个）。 */
	public static DiyEffectsTracker ensure(Char ch) {
		if (ch == null) return null;
		return Buff.affect(ch, DiyEffectsTracker.class);
	}

	public java.util.ArrayList<String> granted() {
		return new ArrayList<>(granted);
	}

	public boolean isGranted(String id) {
		return granted.contains(id);
	}

	public boolean isBorrowed(String id) {
		return borrowed.contains(id);
	}

	public void markGranted(String id) {
		if (id != null) granted.add(id);
	}

	public void markBorrowed(String id) {
		if (id != null) borrowed.add(id);
	}

	public void forget(String id) {
		granted.remove(id);
		borrowed.remove(id);
	}

	/** 临时型效果的重挂间隔（回合）。 */
	private static final int PULSE_INTERVAL = 20;

	private transient int pulseTimer = 0;

	@Override
	public boolean act() {
		Diy.syncEquippedEffects(this);
		if (++pulseTimer >= PULSE_INTERVAL) {
			pulseTimer = 0;
			Diy.pulseEffects(this);
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	private static final String GRANTED = "granted";
	private static final String BORROWED = "borrowed";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(GRANTED, granted.toArray(new String[0]));
		bundle.put(BORROWED, borrowed.toArray(new String[0]));
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		granted.clear();
		borrowed.clear();
		if (bundle.contains(GRANTED)) {
			String[] stored = bundle.getStringArray(GRANTED);
			if (stored != null) for (String id : stored) if (id != null && !id.isEmpty()) granted.add(id);
		}
		if (bundle.contains(BORROWED)) {
			String[] stored = bundle.getStringArray(BORROWED);
			if (stored != null) for (String id : stored) if (id != null && !id.isEmpty()) borrowed.add(id);
		}
	}
}
