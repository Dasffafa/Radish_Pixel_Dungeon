package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 敌方棋子身上的装备效果（我方用的是 AutochessUnitBuff）。
 *
 * <p>AI 的装备只结算能对敌人生效的部分：攻击力、生命、减伤。</p>
 */
public class AutochessFoeBuff extends Buff {

	private final ArrayList<String> items = new ArrayList<>();

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public void setItems(ArrayList<String> equipped) {
		items.clear();
		if (equipped != null) items.addAll(equipped);
		if (has("MailArmor")) {
			target.HT += Math.round(150 * power("MailArmor"));
			target.HP = target.HT;
		}
		if (has("PlateArmor")) {
			target.HT += Math.round(250 * power("PlateArmor"));
			target.HP = target.HT;
		}
		if (has("RingOfMight")) {
			target.HT += Math.round(100 * power("RingOfMight"));
			target.HP = target.HT;
		}
	}

	private boolean has(String shortName) {
		for (String id : items) {
			if (AutochessItems.shortName(id).equals(shortName)) return true;
		}
		return false;
	}

	private float power(String shortName) {
		for (String id : items) {
			if (AutochessItems.shortName(id).equals(shortName)) return AutochessItems.power(id);
		}
		return 0f;
	}

	@Override
	public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (attacker != target) return;
		float mul = 1f;
		mul += 0.25f * power("Sword");
		mul += 0.40f * power("BattleAxe");
		mul += 0.15f * power("RingOfMight");
		if (mul != 1f) info.addStackMultModifier(mul, "autochess_foe");
	}

	@Override
	public void modifyIncomingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (defender != target) return;
		float evasion = power("RingOfEvasion");
		if (evasion > 0) {
			info.addStackMultModifier(1f - 0.10f * evasion, "autochess_foe_evasion");
		}
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("items", AutochessItems.encode(items));
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		items.clear();
		String saved = bundle.getString("items");
		if (saved != null) {
			for (String id : saved.split(",")) {
				if (!id.isEmpty()) items.add(id);
			}
		}
	}
}
