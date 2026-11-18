package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 自走棋里「自己的棋子」：把一只怪变成盟友，并记住它的星级与羁绊。
 *
 * <p>星级影响血量与伤害：2 星血量 ×1.8、伤害 ×1.8；3 星血量 ×3、伤害 ×3.5。</p>
 * <p>羁绊加成在造成/受到伤害时现算，数量一变立刻生效，不需要重新挂 Buff。</p>
 */
public class AutochessUnitBuff extends AllyBuff {

	private int star = 1;
	private String trait = AutochessPool.BEAST;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	/** 出生时调用：设好羁绊与星级，并按星级放大属性。 */
	public void setup(String trait, int star) {
		this.trait = trait;
		this.star = Math.max(1, Math.min(3, star));
		applyStarStats();
	}

	/** 升星：血量按比例放大并回满。 */
	public void upgrade() {
		star = Math.min(3, star + 1);
		applyStarStats();
	}

	public int star() {
		return star;
	}

	public String trait() {
		return trait;
	}

	private void applyStarStats() {
		if (!(target instanceof Mob)) {
			return;
		}
		Mob mob = (Mob) target;
		int oldHT = mob.HT;
		float factor = star == 2 ? 1.8f : (star == 3 ? 3f : 1f);
		// 血量倍率以「基础值」为准：升星时按当前档位重算，避免反复相乘
		int base = Math.round(oldHT / starFactor(star - 1));
		mob.HT = Math.max(1, Math.round(base * factor));
		mob.HP = mob.HT;
	}

	private static float starFactor(int star) {
		return star == 2 ? 1.8f : (star == 3 ? 3f : 1f);
	}

	/** 伤害倍率：星级加成 + 羁绊加成。 */
	private float damageMultiplier() {
		float mul = 1f + 0.8f * (star - 1);
		int tier = AutochessRun.traitTier(trait);
		if (AutochessPool.HUMANOID.equals(trait)) {
			mul += 0.20f * tier;
		} else if (AutochessPool.DEMON.equals(trait)) {
			mul += 0.30f * tier;
		}
		return mul;
	}

	@Override
	public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (attacker != target) {
			return;
		}
		float mul = damageMultiplier();
		if (mul != 1f) {
			info.addStackMultModifier(mul, "autochess");
		}
	}

	@Override
	public void modifyIncomingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (defender != target) {
			return;
		}
		if (AutochessPool.CONSTRUCT.equals(trait)) {
			int tier = AutochessRun.traitTier(trait);
			if (tier > 0) {
				info.addStackMultModifier(1f - 0.20f * tier, "autochess_construct");
			}
		}
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			if (target instanceof Mob) {
				((Mob) target).makeIntelligentAlly();
			}
			return true;
		}
		return false;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("ac_star", star);
		bundle.put("ac_trait", trait);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		star = bundle.getInt("ac_star");
		trait = bundle.getString("ac_trait");
		if (star < 1) star = 1;
		if (trait == null) trait = AutochessPool.BEAST;
	}
}
