package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
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
		// 起手充能错开，别让一整队在同一拍同时爆技能
		this.mana = -20 - (int) (Math.random() * 30);
	}

	public boolean act() {
		// 战斗中先走位/出手，但不提前 return，否则下面的回蓝/技能不会执行
		AutochessRun runNow = AutochessRun.get();
		if (runNow != null && runNow.state() == AutochessRun.State.FIGHT) {
			AutochessUtil.combatStep((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target);
		}
		spend(TICK);

		// 每回合稳定充能，和玩家方完全一致（不再依赖「附近有敌人」）
		if (AutochessUtil.alive(target)) {
			mana += 10;
			if (skill != null && mana >= skill.mana) {
				mana = 0;
				try {
					skill.cast(target, star, 1f);
				} catch (Throwable ignored) {
				}
			}
		}
		return true;
	}

	/** 附近（8 格）有敌人才充能，避免开场空放技能。 */
	private boolean hasFoeNearby() {
		if (Dungeon.level == null) return false;
		for (Char ch : Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
			if (!AutochessUtil.alive(ch) || ch.alignment == target.alignment) continue;
			if (Dungeon.level.distance(ch.pos, target.pos) <= 8) return true;
		}
		return false;
	}

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	/** HUD 直接驱动战斗时用：每拍给点法力并尝试放技能。 */
	public void combatManaTick() {
		mana += 6;
		if (skill != null && mana >= skill.mana) {
			mana = 0;
			try {
				skill.cast(target, star, 1f);
			} catch (Throwable ignored) {
			}
		}
	}

	/** 法力进度（0~1），UI 用。 */
	public float manaRatio() {
		float need = skill == null ? 100f : skill.mana;
		return Math.min(1f, Math.max(0f, mana) / need);
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
		if (Dungeon.level != null && Dungeon.level.distance(target.pos, defender.pos) > 1) {
			mul *= 0.78f;
		}
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
