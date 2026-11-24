/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 药水效果：周期再生
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 治疗药水效果：每 12 回合回 4 点血，装备期间一直生效。 */
public class DiyRegen extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private static final int HEAL = 4;
	private static final float INTERVAL = 12f;

	@Override
	public boolean act() {
		if (target.isAlive() && target.HP < target.HT) {
			target.HP = Math.min(target.HT, target.HP + HEAL);
			if (target.sprite != null) {
				target.sprite.showStatus(CharSprite.POSITIVE, "+" + HEAL);
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
