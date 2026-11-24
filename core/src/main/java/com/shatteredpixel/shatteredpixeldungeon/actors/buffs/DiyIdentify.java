/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 卷轴效果：自动鉴定
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 鉴定卷轴效果：每 10 回合把背包（含卷轴筒等子袋子）里没鉴定的东西鉴定了。 */
public class DiyIdentify extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private static final float INTERVAL = 10f;

	/** 触发次数（只用于测试/诊断，不进存档）。 */
	public transient int activations = 0;

	@Override
	public boolean act() {
		activations++;
		if (Dungeon.hero != null && Dungeon.hero.belongings != null
				&& Dungeon.hero.belongings.backpack != null) {
			int count = identifyIn(Dungeon.hero.belongings.backpack);
			if (count > 0) {
				GLog.i("自动鉴定：%d 件物品", count);
			}
		}
		spend(INTERVAL);
		return true;
	}

	/** 递归鉴定（袋子里的东西也要管）。 */
	private static int identifyIn(Bag bag) {
		int count = 0;
		for (Item item : bag.items) {
			if (item == null) continue;
			if (item instanceof Bag) {
				count += identifyIn((Bag) item);
			} else if (!item.isIdentified()) {
				item.identify();
				count++;
			}
		}
		return count;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}
}
