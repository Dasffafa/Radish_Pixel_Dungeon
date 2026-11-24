/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 物品效果（药水/卷轴/炸弹/符石）周期触发
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * 物品类效果的周期触发器：把「治疗药水」「魔法地图」这类物品效果每 45 回合自动来一次。
 *
 * <p>一个 buff 里可以同时挂多个物品效果（每件装备都可能带不同条目），各自独立计时。</p>
 */
public class DiyItemPulse extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public static final int INTERVAL = 30;
	private static final int FIRST_DELAY = 8;

	private final LinkedHashSet<String> effects = new LinkedHashSet<>();
	private final LinkedHashMap<String, Integer> timers = new LinkedHashMap<>();

	public static DiyItemPulse ensure() {
		if (DungeonHero() == null) return null;
		return Buff.affect(DungeonHero(), DiyItemPulse.class);
	}

	private static com.shatteredpixel.shatteredpixeldungeon.actors.Char DungeonHero() {
		return com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
	}

	public static void add(String id) {
		DiyItemPulse pulse = ensure();
		if (pulse != null && id != null) pulse.effects.add(id);
	}

	public void remove(String id) {
		effects.remove(id);
		timers.remove(id);
		if (effects.isEmpty()) {
			detach();
		}
	}

	public boolean contains(String id) {
		return effects.contains(id);
	}

	@Override
	public boolean act() {
		for (String id : new ArrayList<>(effects)) {
			// 新加的效果第一次很快触发（FIRST_DELAY 回合），之后每 INTERVAL 回合一次
			int t = timers.containsKey(id) ? timers.get(id) : INTERVAL - FIRST_DELAY;
			t++;
			if (t >= INTERVAL) {
				t = 0;
				Diy.performItemEffect(id);
			}
			timers.put(id, t);
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	private static final String EFFECTS = "effects";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(EFFECTS, effects.toArray(new String[0]));
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		effects.clear();
		timers.clear();
		if (bundle.contains(EFFECTS)) {
			String[] stored = bundle.getStringArray(EFFECTS);
			if (stored != null) {
				for (String id : stored) {
					if (id != null && !id.isEmpty()) effects.add(id);
				}
			}
		}
	}
}
