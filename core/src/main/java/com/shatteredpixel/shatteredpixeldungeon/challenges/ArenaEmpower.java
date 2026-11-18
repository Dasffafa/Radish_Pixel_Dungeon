package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 「三分钟一把」里英雄身上的永久强化（本局有效）。
 *
 * <p>所有数值都是「可叠加的加成量」而不是最终值：每选一次强化，就往对应字段上再加一笔。
 * 好处是读档只要存这些数字，不需要重放整条强化链。</p>
 */
public class ArenaEmpower extends Buff {

	/** 开局自带的伤害加成：1 + 2.0 = ×3，够爽但还不至于一刀秒一切。 */
	public static final float START_DAMAGE = 2.0f;
	public static final float START_LIFESTEAL = 0.10f;
	/** 开局自带的减伤：受到伤害 ×0.6，配合血量加成让新手不至于第一波就倒。 */
	public static final float START_DAMAGE_TAKEN = 0.6f;

	/** 伤害倍率加成（叠加乘区里是 1 + damage）。 */
	public float damage = START_DAMAGE;
	/** 造成伤害的多少比例回血。 */
	public float lifesteal = START_LIFESTEAL;
	/** 受到伤害的倍率（越低越抗揍）。 */
	public float damageTaken = START_DAMAGE_TAKEN;
	/** 攻击速度加成，最终折算成攻击间隔缩短。 */
	public float attackSpeed = 0f;
	/** 击杀回复最大生命的百分比。 */
	public int killHealPct = 0;
	/** 击杀时是否对周围 2 格敌人溅射。 */
	public boolean cleave = false;
	/** 对残血目标是否双倍伤害。 */
	public boolean execute = false;

	{
		type = buffType.POSITIVE;
	}

	@Override
	public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (attacker != target || !(defender instanceof Mob)) {
			return;
		}
		info.addStackMultModifier(1f + damage, "arena");
		if (execute && defender.HP < defender.HT * 0.35f) {
			info.addStackMultModifier(2f, "arena_execute");
		}
	}

	@Override
	public void modifyIncomingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (defender != target || attacker == null || !(attacker instanceof Mob)) {
			return;
		}
		if (damageTaken < 1f) {
			info.addStackMultModifier(damageTaken, "arena_tough");
		}
	}

	/** 攻击间隔系数：attackSpeed 越大，间隔越短，上限 1.5（约原来 2.5 倍速）。 */
	public static float attackDelayFactor(Char ch) {
		ArenaEmpower empower = ch == null ? null : ch.buff(ArenaEmpower.class);
		if (empower == null) {
			return 1f;
		}
		return 1f / (1f + Math.min(1.5f, empower.attackSpeed));
	}

	/** HUD 用：当前伤害倍率的展示值，例如 3.0 → "×3.0"。 */
	public float shownDamageMultiplier() {
		return 1f + damage;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("arena_damage", damage);
		bundle.put("arena_lifesteal", lifesteal);
		bundle.put("arena_damage_taken", damageTaken);
		bundle.put("arena_attack_speed", attackSpeed);
		bundle.put("arena_kill_heal", killHealPct);
		bundle.put("arena_cleave", cleave);
		bundle.put("arena_execute", execute);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains("arena_damage")) damage = bundle.getFloat("arena_damage");
		if (bundle.contains("arena_lifesteal")) lifesteal = bundle.getFloat("arena_lifesteal");
		if (bundle.contains("arena_damage_taken")) damageTaken = bundle.getFloat("arena_damage_taken");
		if (bundle.contains("arena_attack_speed")) attackSpeed = bundle.getFloat("arena_attack_speed");
		if (bundle.contains("arena_kill_heal")) killHealPct = bundle.getInt("arena_kill_heal");
		if (bundle.contains("arena_cleave")) cleave = bundle.getBoolean("arena_cleave");
		if (bundle.contains("arena_execute")) execute = bundle.getBoolean("arena_execute");
	}
}
