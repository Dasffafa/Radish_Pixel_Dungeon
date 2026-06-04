package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 诗 Buff — 连续攻击相同名字的敌人拥有 66% 伤害加成
 */
public class PoemBuff extends Buff {

	public static final float COMBO_DAMAGE_MULTIPLIER = 1.66f;
	public static final int REQUIRED_COMBO = 3;

	{
		type = buffType.POSITIVE;
	}

	private String lastTargetName = null;
	private int comboCount = 0;

	/**
	 * 在 Hero.attack() 中调用，更新连击并返回是否有加成
	 */
	public boolean checkCombo(Char enemy) {
		String enemyName = enemy.name();
		if (enemyName.equals(lastTargetName)) {
			comboCount++;
		} else {
			lastTargetName = enemyName;
			comboCount = 1;
		}
		return comboCount >= REQUIRED_COMBO;
	}

	public float getDamageMultiplier() {
		return comboCount >= REQUIRED_COMBO ? COMBO_DAMAGE_MULTIPLIER : 1.0f;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", REQUIRED_COMBO, comboCount >= REQUIRED_COMBO ? (int)((COMBO_DAMAGE_MULTIPLIER - 1) * 100) : 0);
	}

	private static final String LAST_TARGET = "last_target";
	private static final String COMBO = "combo";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LAST_TARGET, lastTargetName);
		bundle.put(COMBO, comboCount);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastTargetName = bundle.getString(LAST_TARGET);
		comboCount = bundle.getInt(COMBO);
	}
}
