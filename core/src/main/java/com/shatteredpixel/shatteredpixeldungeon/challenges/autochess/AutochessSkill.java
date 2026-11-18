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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;

/**
 * 棋子的专属技能：每只怪一个，攒够充能后自动释放。
 *
 * <p>技能 id 就是怪物类名（如 Rat、BlueShaman），技能越强充能需求越高。</p>
 */
public class AutochessSkill {

	public final String id;
	public final int mana;

	private AutochessSkill(String id, int mana) {
		this.id = id;
		this.mana = mana;
	}

	/** 按怪物类名和充能需求创建技能。 */
	public static AutochessSkill of(String id, int mana) {
		return new AutochessSkill(id, mana);
	}

	public String name() {
		return Messages.get(AutochessSkill.class, "name_" + id);
	}

	// ------------------------------------------------------------------
	// 释放
	// ------------------------------------------------------------------

	public void cast(Char caster) {
		Char target = lowestEnemy(caster, 4);
		switch (id) {
			// ---- 1 费：下水道 ----
			case "Rat": // 鼠群撕咬
				if (target != null) {
					hit(caster, target, Math.round(atk(caster) * 1.2f));
					poison(target, 2f);
				}
				break;
			case "Albino": // 疯狂啃咬
				if (target != null) {
					for (int i = 0; i < 3; i++) hit(caster, target, Math.round(atk(caster) * 0.6f));
				}
				break;
			case "Snake": // 毒牙
				if (target != null) {
					hit(caster, target, Math.round(atk(caster) * 1.0f));
					poison(target, 4f);
				}
				break;
			case "Crab": // 甲壳硬化
				shield(caster, 0.20f);
				for (Char ch : alliesAround(caster, 1)) shield(ch, 0.20f);
				break;
			case "Slime": // 腐蚀粘液
				for (Char ch : enemiesAround(caster, 1)) poison(ch, 3f);
				break;
			case "Swarm": // 群虫噬咬
				for (Char ch : enemiesAround(caster, 1)) hit(caster, ch, Math.round(atk(caster) * 0.8f));
				break;
			case "Gnoll": // 骨棒
				if (target != null) hit(caster, target, Math.round(atk(caster) * 2.0f));
				break;
			case "GiantWorm": // 掘地突袭
				if (target != null) {
					hit(caster, target, Math.round(atk(caster) * 1.5f));
					stun(target, 1f);
				}
				break;
			case "Mayfly": // 治愈之尘
				for (Char ch : alliesAround(caster, 6)) heal(ch, 0.12f);
				break;
			case "Dog": // 撕咬
				if (target != null) hit(caster, target, Math.round(atk(caster) * 1.5f));
				heal(caster, 0.10f);
				break;
			case "Goblin": // 偷袭
				if (target != null) hit(caster, target, Math.round(atk(caster) * 1.8f));
				break;

			// ---- 2 费：监狱 ----
			case "Bat": // 吸血
				if (target != null) {
					hit(caster, target, Math.round(atk(caster) * 1.2f));
					heal(caster, 0.15f);
				}
				break;
			case "Thief": // 背刺
				Char sneak = lowestEnemy(caster, 8);
				if (sneak != null) hit(caster, sneak, Math.round(atk(caster) * 2.5f));
				break;
			case "DM100": // 酸性射击
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.08f)));
					poison(target, 3f);
				}
				break;
			case "Skeleton": // 骨刺
				for (Char ch : enemiesAround(caster, 1)) hit(caster, ch, Math.round(atk(caster) * 1.0f));
				break;
			case "Guard": // 盾墙
				shield(caster, 0.15f);
				for (Char ch : alliesAround(caster, 1)) shield(ch, 0.15f);
				break;
			case "BlueShaman": // 治疗图腾
				Char weakest = lowestAlly(caster);
				if (weakest != null) heal(weakest, 0.30f);
				break;
			case "Spinner": // 蛛网
				for (Char ch : enemiesAround(caster, 2)) stun(ch, 1f);
				break;
			case "Necromancer": // 亡者归来
				for (Char ch : alliesAround(caster, 8)) heal(ch, 0.15f);
				break;
			case "GoblinRider": // 冲锋
				if (target != null) hit(caster, target, Math.round(atk(caster) * 2.2f));
				break;
			case "Jailer": // 锁链
				if (target != null) stun(target, 2f);
				break;
			case "Prisoner": // 拼死一搏
				Buff.affect(caster, AutochessRage.class).set(3f);
				break;
			case "Torturer": // 严刑拷打
				if (target != null) {
					hit(caster, target, Math.round(atk(caster) * 0.9f));
					poison(target, 4f);
				}
				break;

			// ---- 3 费：矿洞 ----
			case "Brute": // 重砸
				for (Char ch : enemiesAround(caster, 1)) hit(caster, ch, Math.round(atk(caster) * 1.3f));
				break;
			case "Golem": // 岩石皮肤
				for (Char ch : alliesAround(caster, 8)) shield(ch, 0.12f);
				break;
			case "Ghoul": // 啃食
				if (target != null) hit(caster, target, Math.round(atk(caster) * 1.5f));
				heal(caster, 0.15f);
				break;
			case "Grudge": // 怨念缠身
				if (target != null) hit(caster, target, Math.max(1, Math.round(target.HT * 0.12f)));
				break;
			case "ClusteredSkeleton": // 骨堆冲撞
				for (Char ch : enemiesAround(caster, 1)) hit(caster, ch, Math.round(atk(caster) * 1.1f));
				break;
			case "DM175": // 炮击
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.10f)));
					for (Char ch : enemiesAround(target, 1)) {
						hit(caster, ch, Math.max(1, Math.round(ch.HT * 0.05f)));
					}
				}
				break;
			case "GnollZealot": // 狂热献祭
				Buff.affect(caster, AutochessRage.class).set(3f);
				heal(caster, 0.20f);
				break;

			// ---- 4 费：城市 ----
			case "Monk": // 连环拳
				if (target != null) {
					for (int i = 0; i < 3; i++) hit(caster, target, Math.round(atk(caster) * 0.7f));
				}
				break;
			case "Warlock": // 暗影箭
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.12f)));
					burn(target, 3f);
				}
				break;
			case "FireElemental": // 烈焰新星
				for (Char ch : enemiesAround(caster, 2)) {
					hit(caster, ch, Math.max(1, Math.round(ch.HT * 0.08f)));
					burn(ch, 3f);
				}
				break;
			case "Drake": // 龙息
				for (Char ch : enemiesAround(caster, 2)) {
					hit(caster, ch, Math.max(1, Math.round(ch.HT * 0.10f)));
					burn(ch, 3f);
				}
				break;
			case "StoneSpirit": // 大地壁垒
				for (Char ch : alliesAround(caster, 8)) shield(ch, 0.15f);
				break;
			case "Artillerist": // 炮火覆盖
				Char weak = lowestEnemy(caster, 8);
				if (weak != null) hit(caster, weak, Math.max(1, Math.round(weak.HT * 0.15f)));
				break;
			case "RoyalGuard": // 王卫庇护
				shield(caster, 0.20f);
				Char ally = lowestAlly(caster);
				if (ally != null) shield(ally, 0.20f);
				break;
			case "ShieldMage": // 群体护盾
				for (Char ch : alliesAround(caster, 8)) shield(ch, 0.15f);
				break;

			// ---- 5 费：恶魔大厅 ----
			case "Succubus": // 魅惑之吻
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.08f)));
					stun(target, 2f);
				}
				break;
			case "Scorpio": // 毒刺
				if (target != null) {
					hit(caster, target, Math.max(1, Math.round(target.HT * 0.10f)));
					poison(target, 5f);
				}
				break;
			case "Acidic": // 酸液喷射
				for (Char ch : enemiesAround(caster, 2)) poison(ch, 5f);
				break;
			case "Eye": // 邪恶凝视
				if (target != null) stun(target, 3f);
				break;
			case "RipperDemon": // 撕裂
				if (target != null) hit(caster, target, Math.round(atk(caster) * 3.0f));
				break;
			case "Deminion": // 恶魔之力
				Buff.affect(caster, AutochessRage.class).set(3f);
				for (Char ch : enemiesAround(caster, 1)) {
					hit(caster, ch, Math.max(1, Math.round(ch.HT * 0.08f)));
				}
				break;
			case "Deviloon": // 烈焰撕咬
				for (Char ch : enemiesAround(caster, 1)) {
					hit(caster, ch, Math.round(atk(caster) * 1.0f));
					poison(ch, 3f);
				}
				break;
			case "Gorgon": // 石化凝视
				for (Char ch : enemiesAround(caster, 2)) stun(ch, 2f);
				break;
			case "DemonLord": // 末日审判
				for (Char ch : enemiesAround(caster, 8)) {
					hit(caster, ch, Math.max(1, Math.round(ch.HT * 0.15f)));
					burn(ch, 3f);
				}
				break;
			default:
				if (target != null) hit(caster, target, atk(caster));
				break;
		}
	}

	// ------------------------------------------------------------------
	// 工具
	// ------------------------------------------------------------------

	private static int atk(Char caster) {
		return Math.max(1, caster.damageRoll());
	}

	private static void hit(Char caster, Char target, int dmg) {
		if (target == null || !target.isAlive()) return;
		target.damage(DamageInfo.physical(dmg, caster));
	}

	private static void heal(Char ch, float pct) {
		if (ch == null || !ch.isAlive()) return;
		ch.HP = Math.min(ch.HT, ch.HP + Math.max(1, Math.round(ch.HT * pct)));
	}

	private static void shield(Char ch, float pct) {
		if (ch == null || !ch.isAlive()) return;
		Buff.affect(ch, Barrier.class).incShield(Math.max(1, Math.round(ch.HT * pct)));
	}

	private static void poison(Char ch, float turns) {
		if (ch != null && ch.isAlive()) Buff.affect(ch, Poison.class).set(turns);
	}

	private static void burn(Char ch, float turns) {
		if (ch != null && ch.isAlive()) Buff.affect(ch, Burning.class).reignite(ch, turns);
	}

	private static void stun(Char ch, float turns) {
		if (ch != null && ch.isAlive()) Buff.affect(ch, Paralysis.class, turns);
	}

	private static ArrayList<Char> enemiesAround(Char caster, int range) {
		ArrayList<Char> list = new ArrayList<>();
		if (Dungeon.level == null) return list;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment == caster.alignment) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) <= range) list.add(mob);
		}
		return list;
	}

	private static ArrayList<Char> alliesAround(Char caster, int range) {
		ArrayList<Char> list = new ArrayList<>();
		if (Dungeon.level == null) return list;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment != caster.alignment) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) <= range) list.add(mob);
		}
		return list;
	}

	private static Char lowestEnemy(Char caster, int range) {
		if (Dungeon.level == null) return null;
		Char best = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment == caster.alignment) continue;
			if (Dungeon.level.distance(mob.pos, caster.pos) > range) continue;
			if (best == null || mob.HP < best.HP) best = mob;
		}
		return best;
	}

	private static Char lowestAlly(Char caster) {
		if (Dungeon.level == null) return null;
		Char best = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment != caster.alignment || mob.HP >= mob.HT) continue;
			if (best == null || mob.HP / (float) mob.HT < best.HP / (float) best.HT) best = mob;
		}
		return best;
	}
}
