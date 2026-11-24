/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 法杖效果：法杖充能
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 「法杖充能」效果：每 12 回合给背包里一把没满的法杖补 1 点充能。 */
public class DiyWandCharge extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private static final float INTERVAL = 12f;

	/** 触发次数（测试/诊断用）。 */
	public transient int activations = 0;

	@Override
	public boolean act() {
		activations++;
		if (Dungeon.hero != null && Dungeon.hero.belongings != null
				&& Dungeon.hero.belongings.backpack != null) {
			for (Item item : Dungeon.hero.belongings.backpack.items) {
				if (item instanceof Wand) {
					Wand wand = (Wand) item;
					if (wand.curCharges < wand.maxCharges) {
						wand.curCharges = Math.min(wand.curCharges + 1, wand.maxCharges);
						wand.updateQuickslot();
						GLog.i("法杖充能：%s +1", wand.name());
						break;
					}
				}
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
