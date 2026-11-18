package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 自走棋里「自己的棋子」：把一只怪变成盟友，并记住它的星级与羁绊。
 *
 * <p>星级：2 星血量 ×1.8、伤害 ×1.8；3 星血量 ×3、伤害 ×3.5。</p>
 * <p>羁绊效果都写在这个类的钩子里，按场上同羁绊人数实时判定档位：</p>
 * <ul>
 *   <li>兽群：伤害 +10%/档</li>
 *   <li>人形：伤害 +20%/档</li>
 *   <li>构装：受到伤害 -20%/档</li>
 *   <li>恶魔：伤害 +30%/档（2/4 两档）</li>
 *   <li>亡灵：每回合回复 1% 最大生命/档</li>
 *   <li>守卫：开战护盾 15% 最大生命/档（2/4 两档）</li>
 *   <li>巨兽：最大生命 +15%/档</li>
 *   <li>虫群：攻击附带 2 点/档 额外伤害</li>
 * </ul>
 */
public class AutochessUnitBuff extends AllyBuff {

	private int star = 1;
	private String trait = AutochessPool.BEAST;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	/** 出生时调用：设好羁绊与星级，并按星级放大属性、结算一次性羁绊。 */
	public void setup(String trait, int star) {
		this.trait = trait;
		this.star = Math.max(1, Math.min(3, star));
		applyStarStats();
		applyTraitSetup();
	}

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
		if (!(target instanceof Mob)) return;
		Mob mob = (Mob) target;
		int oldHT = mob.HT;
		float factor = starFactor(star);
		int base = Math.round(oldHT / starFactor(star - 1));
		mob.HT = Math.max(1, Math.round(base * factor));
		mob.HP = mob.HT;
	}

	private static float starFactor(int star) {
		return star == 2 ? 1.8f : (star == 3 ? 3f : 1f);
	}

	/** 巨兽加血、守卫给盾——这类一次性效果在出生时结算。 */
	private void applyTraitSetup() {
		int tier = AutochessRun.traitTier(trait);
		if (tier <= 0) return;
		if (AutochessPool.GIANT.equals(trait)) {
			target.HT += Math.round(target.HT * 0.15f * tier);
			target.HP = target.HT;
		} else if (AutochessPool.GUARD.equals(trait)) {
			Buff.affect(target, Barrier.class).setShield(Math.round(target.HT * 0.15f * tier));
		}
	}

	/** 伤害倍率：星级 + 攻击型羁绊。 */
	private float damageMultiplier() {
		float mul = 1f + 0.8f * (star - 1);
		int tier = AutochessRun.traitTier(trait);
		if (AutochessPool.HUMANOID.equals(trait)) {
			mul += 0.20f * tier;
		} else if (AutochessPool.DEMON.equals(trait)) {
			mul += 0.30f * tier;
		} else if (AutochessPool.BEAST.equals(trait)) {
			mul += 0.10f * tier;
		}
		return mul;
	}

	@Override
	public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (attacker != target) return;
		float mul = damageMultiplier();
		if (mul != 1f) {
			info.addStackMultModifier(mul, "autochess");
		}
		if (AutochessPool.SWARM.equals(trait)) {
			int tier = AutochessRun.traitTier(trait);
			if (tier > 0) {
				info.addFlatModifier(2 * tier, "autochess_swarm");
			}
		}
	}

	@Override
	public void modifyIncomingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (defender != target) return;
		if (AutochessPool.CONSTRUCT.equals(trait)) {
			int tier = AutochessRun.traitTier(trait);
			if (tier > 0) {
				info.addStackMultModifier(1f - 0.20f * tier, "autochess_construct");
			}
		}
	}

	@Override
	public boolean act() {
		if (AutochessPool.UNDEAD.equals(trait)) {
			int tier = AutochessRun.traitTier(trait);
			if (tier > 0 && target.HP < target.HT) {
				target.HP = Math.min(target.HT, target.HP + Math.max(1, Math.round(target.HT * 0.01f * tier)));
			}
		}
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
