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

	/** 敌方棋子也会放技能：攒充能，满了自动释放。 */
	private AutochessSkill skill;
	private int star = 1;
	private int mana = 0;

	public void setup(AutochessSkill skill, int star) {
		this.skill = skill;
		this.star = Math.max(1, Math.min(3, star));
		this.mana = 0;
	}

	@Override
	public boolean act() {
		spend(TICK);
		if (skill != null && target.isAlive()) {
			mana += 12;
			if (mana >= skill.mana) {
				mana = 0;
				skill.cast(target, star, 1f);
			}
		}
		return true;
	}

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public void setItems(ArrayList<String> equipped) {
		items.clear();
		if (equipped != null) items.addAll(equipped);
		target.HT += Math.round(150 * power("RatArmor"));
		target.HT += Math.round(200 * power("AfterGlow"));
		target.HT += Math.round(100 * power("SoulMix"));
		target.HP = target.HT;
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
		mul += 0.25f * power("MoonlightArmor");
		mul += 0.40f * power("KillBoatSword");
		mul += 0.20f * power("SoulMix");
		if (mul != 1f) info.addStackMultModifier(mul, "autochess_foe");
	}

	@Override
	public void modifyIncomingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (defender != target) return;
		float evasion = power("CrabArmor");
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
