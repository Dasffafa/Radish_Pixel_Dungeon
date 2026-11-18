package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;

/**
 * 棋子的技能：攒够充能后自动释放。
 *
 * <p>技能越强，需要的充能越高。充能每回合自动增长，受到攻击时也会涨一点。</p>
 */
public class AutochessSkill {

	public final String id;
	public final int mana;

	AutochessSkill(String id, int mana) {
		this.id = id;
		this.mana = mana;
	}

	// ---- 技能定义（id 对应文案 key；mana 即充能需求）----

	public static final AutochessSkill STRIKE = new AutochessSkill("strike", 60);
	public static final AutochessSkill CLEAVE = new AutochessSkill("cleave", 80);
	public static final AutochessSkill HEAL = new AutochessSkill("heal", 70);
	public static final AutochessSkill SHIELD = new AutochessSkill("shield", 55);
	public static final AutochessSkill POISON = new AutochessSkill("poison", 65);
	public static final AutochessSkill FIREBALL = new AutochessSkill("fireball", 85);
	public static final AutochessSkill STUN = new AutochessSkill("stun", 90);
	public static final AutochessSkill RAGE = new AutochessSkill("rage", 70);
	public static final AutochessSkill SNIPE = new AutochessSkill("snipe", 95);

	public String name() {
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
				AutochessSkill.class, "name_" + id);
	}

	public String desc() {
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
				AutochessSkill.class, "desc_" + id);
	}

	/** 释放技能。caster 已经攒满充能。 */
	public void cast(Char caster) {
		Char target = enemyTarget(caster);
		switch (id) {
			case "strike":
				if (target != null) {
					hit(caster, target, Math.round(attack(caster) * 2.5f));
				}
				break;
			case "cleave":
				for (Char ch : enemiesAround(caster, 1)) {
					hit(caster, ch, Math.round(attack(caster) * 1.5f));
				}
				break;
			case "heal":
				healLowest(caster);
				break;
			case "shield":
				shieldAllies(caster);
				break;
			case "poison":
				for (Char ch : enemiesAround(caster, 2)) {
					Buff.affect(ch, Poison.class).set(5f);
				}
				break;
			case "fireball":
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.08f)));
					Buff.affect(target, Burning.class).reignite(target, 3f);
				}
				break;
			case "stun":
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.08f)));
					Buff.affect(target, Paralysis.class, 2f);
				}
				break;
			case "rage":
				// 自身狂怒：由 buff 记录回合数，这里直接回满血附近的效果
				Buff.affect(caster, AutochessRage.class).set(3f);
				break;
			case "snipe":
				Char weakest = weakestEnemy(caster);
				if (weakest != null) {
					hit(caster, weakest, Math.max(1, Math.round(weakest.HT * 0.12f)));
				}
				break;
			default:
				break;
		}
	}

	// ---- 内部工具 ----

	private static int attack(Char caster) {
		return Math.max(1, caster.damageRoll());
	}

	private static void hit(Char caster, Char target, int dmg) {
		target.damage(DamageInfo.physical(dmg, caster));
	}

	private static Char enemyTarget(Char caster) {
		if (Dungeon.level == null) return null;
		Char best = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment == caster.alignment) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) > 4) continue;
			if (best == null || mob.HP < best.HP) best = mob;
		}
		return best;
	}

	private static java.util.ArrayList<Char> enemiesAround(Char caster, int range) {
		java.util.ArrayList<Char> list = new java.util.ArrayList<>();
		if (Dungeon.level == null) return list;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment == caster.alignment) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) <= range) list.add(mob);
		}
		return list;
	}

	private static Char weakestEnemy(Char caster) {
		if (Dungeon.level == null) return null;
		Char best = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment == caster.alignment) continue;
			if (best == null || mob.HP / (float) mob.HT < best.HP / (float) best.HT) best = mob;
		}
		return best;
	}

	private static void healLowest(Char caster) {
		if (Dungeon.level == null) return;
		Char lowest = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment != caster.alignment) continue;
			if (mob.HP >= mob.HT) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) > 6) continue;
			if (lowest == null || mob.HP / (float) mob.HT < lowest.HP / (float) lowest.HT) {
				lowest = mob;
			}
		}
		if (lowest == null) return;
		lowest.HP = Math.min(lowest.HT, lowest.HP + Math.max(1, Math.round(lowest.HT * 0.25f)));
		Buff.affect(lowest, Barrier.class).incShield(Math.max(1, Math.round(lowest.HT * 0.10f)));
	}

	private static void shieldAllies(Char caster) {
		if (Dungeon.level == null) return;
		Buff.affect(caster, Barrier.class).incShield(Math.max(1, Math.round(caster.HT * 0.15f)));
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (mob == caster || !mob.isAlive() || mob.alignment != caster.alignment) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) <= 1) {
				Buff.affect(mob, Barrier.class).incShield(Math.max(1, Math.round(mob.HT * 0.15f)));
			}
		}
	}
}
