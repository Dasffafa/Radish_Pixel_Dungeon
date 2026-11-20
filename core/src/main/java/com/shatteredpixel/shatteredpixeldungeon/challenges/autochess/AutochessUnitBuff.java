package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 自走棋里「自己的棋子」：把一只怪变成盟友，并记住它的星级与羁绊。
 *
 * <p>羁绊不是简单的加伤加血，每系都尽量做成有操作感的机制：</p>
 * <ul>
 *   <li>兽群 · 围攻：每有一个相邻敌人，伤害 +10%/档（最多 3 个）</li>
 *   <li>虫群 · 剧毒：攻击使目标中毒，毒伤按目标最大生命计算</li>
 *   <li>亡灵 · 吸血：攻击后回复自身生命</li>
 *   <li>构装 · 装甲：每回合自动获得护盾</li>
 *   <li>恶魔 · 斩杀：对残血（&lt;40%）目标伤害大幅提高</li>
 *   <li>人形 · 阵型：每个相邻友军提供增伤</li>
 *   <li>坦克 · 护盾：开战即获得大额护盾，并缓慢回复</li>
 *   <li>刺客 · 突袭：对满血目标打出成倍伤害</li>
 *   <li>射手 · 精准：距离越远伤害越高</li>
 *   <li>战士 · 狂暴：生命越低伤害越高</li>
 *   <li>法师 · 灼烧：攻击点燃目标</li>
 *   <li>辅助 · 庇护：治疗最残的友军并附护盾</li>
 * </ul>
 */
public class AutochessUnitBuff extends AllyBuff {

	private int star = 1;
	private String[] traits = new String[]{AutochessPool.BEAST};
	private AutochessSkill skill;
	/** 这只棋子带的装备。 */
	private final java.util.ArrayList<String> items = new java.util.ArrayList<>();
	/** 当前充能 / 充满需要多少。 */
	private int mana = 0;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	/** 装备列表，出生时由 spawnUnit 传入。 */
	public void setItems(java.util.List<String> equipped) {
		items.clear();
		if (equipped != null) items.addAll(equipped);
		applyItemSetup();
	}

	public java.util.ArrayList<String> items() {
		return items;
	}

	private boolean hasItem(String id) {
		return items.contains(id);
	}

	private boolean hasItemShort(String shortName) {
		for (String id : items) {
			if (AutochessItems.shortName(id).equals(shortName)) return true;
		}
		return false;
	}

	private float itemPower(String shortName) {
		for (String id : items) {
			if (AutochessItems.shortName(id).equals(shortName)) return AutochessItems.power(id);
		}
		return 0f;
	}

	/** 防具类出生时一次性生效。 */
	private void applyItemSetup() {
		target.HT += Math.round(150 * itemPower("RatArmor"));
		target.HT += Math.round(200 * itemPower("AfterGlow"));
		target.HT += Math.round(100 * itemPower("SoulMix"));
		target.HP = target.HT;
	}

	public void setup(String[] traits, int star, AutochessSkill skill) {
		this.traits = traits == null || traits.length == 0
				? new String[]{AutochessPool.BEAST} : traits;
		this.skill = skill;
		this.mana = 0;
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

	public String[] traits() {
		return traits;
	}

	private boolean hasTrait(String trait) {
		for (String t : traits) {
			if (t.equals(trait)) return true;
		}
		return false;
	}

	private int tier(String trait) {
		return AutochessRun.traitTier(trait);
	}

	private static float starFactor(int star) {
		return star == 2 ? 1.8f : (star == 3 ? 3f : 1f);
	}

	private void applyStarStats() {
		if (!(target instanceof Mob)) return;
		Mob mob = (Mob) target;
		float factor = starFactor(star);
		int base = Math.round(mob.HT / starFactor(star - 1));
		mob.HT = Math.max(1, Math.round(base * factor));
		mob.HP = mob.HT;
	}

	/** 开战一次性效果：坦克的护盾。 */
	private void applyTraitSetup() {
		if (hasTrait(AutochessPool.TANK)) {
			int tier = tier(AutochessPool.TANK);
			if (tier > 0) {
				Buff.affect(target, Barrier.class).setShield(Math.round(target.HT * 0.20f * tier));
			}
		}
	}

	// ------------------------------------------------------------------
	// 输出侧
	// ------------------------------------------------------------------

	@Override
	public void modifyOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (attacker != target || defender == null) return;

		float mul = 1f + 0.8f * (star - 1);		// 远程出手伤害略降，避免后排还没接战就把前排融化
		if (Dungeon.level != null && Dungeon.level.distance(target.pos, defender.pos) > 1) {
			mul *= 0.78f;
		}

		// 兽群 · 围攻：越被围伤害越高
		if (hasTrait(AutochessPool.BEAST)) {
			int t = tier(AutochessPool.BEAST);
			if (t > 0) mul += 0.10f * t * Math.min(3, adjacentEnemies(defender));
		}

		// 人形 · 阵型：身边友军越多伤害越高
		if (hasTrait(AutochessPool.HUMANOID)) {
			int t = tier(AutochessPool.HUMANOID);
			if (t > 0) mul += 0.08f * t * Math.min(3, adjacentAllies());
		}

		// 战士 · 狂暴：血越少越猛
		if (hasTrait(AutochessPool.FIGHTER)) {
			int t = tier(AutochessPool.FIGHTER);
			if (t > 0) {
				float missing = 1f - target.HP / (float) Math.max(1, target.HT);
				mul += 0.15f * t * missing;
			}
		}

		// 射手 · 精准：站得远打得疼
		if (hasTrait(AutochessPool.RANGED)) {
			int t = tier(AutochessPool.RANGED);
			if (t > 0 && Dungeon.level != null
					&& Dungeon.level.distance(target.pos, defender.pos) >= 3) {
				mul += 0.25f * t;
			}
		}

		// 恶魔 · 斩杀：对残血目标重击
		if (hasTrait(AutochessPool.DEMON)) {
			int t = tier(AutochessPool.DEMON);
			if (t > 0 && defender.HP < defender.HT * 0.4f) mul += 0.5f * t;
		}

		// 刺客 · 突袭：对满血目标成倍伤害
		mul += 0.25f * itemPower("MoonlightArmor");
		mul += 0.40f * itemPower("KillBoatSword");
		mul += 0.20f * itemPower("SoulMix");
		// 「狂怒」技能生效期间额外增伤
		if (target.buff(AutochessRage.class) != null) {
			mul += 0.6f;
		}
		if (hasTrait(AutochessPool.ASSASSIN)) {
			int t = tier(AutochessPool.ASSASSIN);
			if (t > 0 && defender.HP >= defender.HT) mul *= (1f + t);
		}

		if (mul != 1f) {
			info.addStackMultModifier(mul, "autochess");
		}

		// 虫群 · 剧毒

		if (hasTrait(AutochessPool.SWARM)) {
			int t = tier(AutochessPool.SWARM);
			if (t > 0 && defender instanceof Mob) {
				Buff.affect(defender, Poison.class).set(3f);
			}
		}

		// 法师 · 灼烧
		if (hasTrait(AutochessPool.MAGE)) {
			int t = tier(AutochessPool.MAGE);
			if (t > 0 && defender instanceof Mob) {
				Buff.affect(defender, Burning.class).reignite(defender, 3f);
			}
		}
	}

	@Override
	public void modifyIncomingAttackDamage(Char attacker, Char defender, DamageInfo info) {
		if (defender != target) return;
		if (hasTrait(AutochessPool.CONSTRUCT)) {
			int t = tier(AutochessPool.CONSTRUCT);
			if (t > 0) info.addStackMultModifier(1f - 0.15f * t, "autochess_construct");
		}
		float evasion = itemPower("CrabArmor");
		if (evasion > 0) {
			info.addStackMultModifier(1f - 0.10f * evasion, "autochess_evasion");
		}
		// 被打也会涨一点充能
		chargeMana(4);
	}

	// ------------------------------------------------------------------
	// 每回合
	// ------------------------------------------------------------------

	@Override
	public boolean act() {
		AutochessRun runNow = AutochessRun.get();
		if (runNow != null && runNow.state() == AutochessRun.State.FIGHT) {
			if (AutochessUtil.combatStep((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target)) {
				spend(TICK);
				return true;
			}
		}
		int undead = hasTrait(AutochessPool.UNDEAD) ? tier(AutochessPool.UNDEAD) : 0;
		if (undead > 0 && target.HP < target.HT) {
			// 亡灵 · 吸血：持续从造成的伤害里回血，这里做成每回合稳定回复
			target.HP = Math.min(target.HT, target.HP + Math.max(1, Math.round(target.HT * 0.02f * undead)));
		}

		int construct = hasTrait(AutochessPool.CONSTRUCT) ? tier(AutochessPool.CONSTRUCT) : 0;
		int tank = hasTrait(AutochessPool.TANK) ? tier(AutochessPool.TANK) : 0;
		if (construct + tank > 0) {
			// 构装 · 装甲 / 坦克 · 护盾：每回合补护盾
			int shield = Math.round(target.HT * 0.03f * (construct + tank));
			Barrier barrier = Buff.affect(target, Barrier.class);
			if (barrier.shielding() < target.HT * 0.3f) {
				barrier.incShield(Math.max(1, shield));
			}
		}

		int support = hasTrait(AutochessPool.SUPPORT) ? tier(AutochessPool.SUPPORT) : 0;
		if (support > 0) {
			healLowestAlly(support);
		}

		// 迅捷之戒：充能更快
		chargeMana(hasItemShort("EnergyArmor") ? 15 : 10);

		spend(TICK);
		return true;
	}

	/** 攒充能，充满就放技能。 */
	private void chargeMana(int amount) {
		if (skill == null) return;
		mana += amount;
		if (mana >= skill.mana) {
			mana = 0;
			skill.cast(target, star, 1f + 0.3f * itemPower("PrismArmor"));
		}
	}

	public AutochessSkill skill() {
		return skill;
	}

	public float manaRatio() {
		return skill == null ? 0f : Math.min(1f, mana / (float) skill.mana);
	}

	/** 辅助 · 庇护：治疗范围内血量比例最低的友军，并给一点护盾。 */
	private void healLowestAlly(int tier) {
		if (Dungeon.level == null) return;
		Char lowest = null;
		for (Char ch : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (ch == target || ch.alignment != target.alignment) continue;
			if (!AutochessUtil.alive(ch) || ch.HP >= ch.HT) continue;
			if (Dungeon.level.distance(ch.pos, target.pos) > 6) continue;
			if (lowest == null || ch.HP / (float) ch.HT < lowest.HP / (float) lowest.HT) {
				lowest = ch;
			}
		}
		if (lowest == null) return;
		int heal = Math.max(1, Math.round(lowest.HT * 0.03f * tier));
		lowest.HP = Math.min(lowest.HT, lowest.HP + heal);
		Buff.affect(lowest, Barrier.class).incShield(Math.max(1, Math.round(lowest.HT * 0.05f * tier)));
	}

	/** 目标身边有几个敌人。 */
	private int adjacentEnemies(Char target) {
		if (Dungeon.level == null) return 0;
		int count = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!AutochessUtil.alive(mob) || mob.alignment == target.alignment) continue;
			if (Dungeon.level.distance(mob.pos, target.pos) == 1) count++;
		}
		return count;
	}

	/** 自己身边有几个友军。 */
	private int adjacentAllies() {
		if (Dungeon.level == null) return 0;
		int count = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (mob == target || !AutochessUtil.alive(mob) || mob.alignment != target.alignment) continue;
			if (Dungeon.level.distance(mob.pos, target.pos) == 1) count++;
		}
		return count;
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
		bundle.put("ac_trait", traits.length > 0 ? traits[0] : AutochessPool.BEAST);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		star = Math.max(1, bundle.getInt("ac_star"));
		String saved = bundle.getString("ac_trait");
		traits = saved == null ? new String[]{AutochessPool.BEAST} : new String[]{saved};
	}
}
