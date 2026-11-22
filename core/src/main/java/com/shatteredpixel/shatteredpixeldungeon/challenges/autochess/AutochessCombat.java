package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

import java.util.HashMap;

/**
 * 实时战斗：每只棋子有自己的攻击速度和移动速度。
 *
 * <p>不再按「回合」推进，而是每帧用 delta 时间驱动：</p>
 * <ul>
 *   <li>移动冷却到 0 → 朝最近的敌人挪一格（sprite 平滑走过去）</li>
 *   <li>攻击冷却到 0 且贴身 → 出手（带闪光与伤害数字）</li>
 * </ul>
 *
 * <p>速度按棋子的定位给：刺客快、坦克慢、远程标准。</p>
 */
public class AutochessCombat {

	/** 每只棋子的战斗计时。 */
	private static final class UnitState {
		float atkLeft = 0.2f;
		float moveLeft = 0f;
		float atkInterval = 1.1f;
		float moveInterval = 0.5f;
		boolean inited = false;
	}

	private static final HashMap<Mob, UnitState> states = new HashMap<>();

	private static final java.util.HashSet<String> RANGED = new java.util.HashSet<>(java.util.Arrays.asList(
			"DM100", "BlueShaman", "Necromancer", "Warlock",
			"DM175", "Artillerist", "ShieldMage", "Eye", "Scorpio", "Acidic", "FireElemental"));

	private static final java.util.HashSet<String> ASSASSIN = new java.util.HashSet<>(java.util.Arrays.asList(
			"Goblin", "Thief", "Assassin", "Bat", "Snake", "Spinner"));

	private static final java.util.HashSet<String> TANK = new java.util.HashSet<>(java.util.Arrays.asList(
			"Guard", "Jailer", "Golem", "Crab", "Brute", "Skeleton", "ClusteredSkeleton", "Gnoll"));

	/** 由 HUD 每帧调用，delta 为帧间隔。 */
	public static void update(float delta) {
		AutochessRun run = AutochessRun.get();
		if (run == null || Dungeon.level == null || run.state() != AutochessRun.State.FIGHT) {
			if (!states.isEmpty()) states.clear();
			return;
		}

		Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		for (Mob m : mobs) present.add(m);

		states.entrySet().removeIf(e -> !present.contains(e.getKey())
				|| e.getKey().sprite == null || !AutochessUtil.alive(e.getKey()));

		for (Mob self : mobs) {
			if (self.sprite == null || !self.sprite.visible) continue;
			boolean ours = self.buff(AutochessUnitBuff.class) != null
					|| self.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;

			UnitState st = states.get(self);
			if (st == null) {
				st = new UnitState();
				applySpeed(st, self, run.starOf(self));
				states.put(self, st);
			}

			st.atkLeft -= delta;
			st.moveLeft -= delta;

			Mob foe = nearestFoe(self, mobs);
			if (foe == null) continue;

			if (Dungeon.level.distance(self.pos, foe.pos) <= 1) {
				if (st.atkLeft <= 0f) {
					st.atkLeft = st.atkInterval;
					attack(self, foe);
				}
			} else if (st.moveLeft <= 0f) {
				st.moveLeft = st.moveInterval;
				stepToward(self, foe);
			}
		}
	}

	/** 按棋子定位给速度（数值单位：秒）。 */
	private static void applySpeed(UnitState st, Mob mob, int star) {
		String cls = mob.getClass().getSimpleName();
		float atk = 1.1f;
		float move = 0.50f;

		if (ASSASSIN.contains(cls)) {
			atk = 0.75f;
			move = 0.35f;
		} else if (TANK.contains(cls)) {
			atk = 1.35f;
			move = 0.65f;
		} else if (RANGED.contains(cls)) {
			atk = 1.00f;
			move = 0.55f;
		}

		// 星级越高越快一点
		float starBonus = 1f + 0.06f * Math.max(0, Math.min(3, star) - 1);
		st.atkInterval = atk / starBonus;
		st.moveInterval = move / starBonus;
		st.atkLeft = st.atkInterval * 0.5f;
		st.moveLeft = 0f;
	}

	private static Mob nearestFoe(Mob self, Mob[] mobs) {
		Mob best = null;
		int bestD = Integer.MAX_VALUE;
		for (Mob m : mobs) {
			if (m == self || !AutochessUtil.alive(m)) continue;
			if (m.sprite == null || !m.sprite.visible) continue;
			if (m.alignment == self.alignment) continue;
			int d = Dungeon.level.distance(self.pos, m.pos);
			if (d < bestD) {
				bestD = d;
				best = m;
			}
		}
		return best;
	}

	private static void attack(Mob self, Mob foe) {
		// 出手动画（朝向敌人），让战斗看起来更"活"
		if (self.sprite != null) {
			try {
				self.sprite.attack(foe.pos);
			} catch (Throwable ignored) {
			}
		}
		int hpBefore = foe.HP;
		try {
			self.attack(foe);
		} catch (Throwable ignored) {
		}
		// 被完全格挡/闪避时至少削 2% 最大生命，保证血条会动
		if (foe.HP >= hpBefore) {
			int chip = Math.max(1, Math.round(foe.HT * 0.02f));
			foe.HP = Math.max(0, foe.HP - chip);
			if (foe.sprite != null) {
				foe.sprite.showStatus(
						com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEGATIVE,
						"-" + chip);
			}
		}
		if (foe.sprite != null) foe.sprite.flash();
		if (foe.HP <= 0) {
			foe.HP = 0;
			try {
				foe.die(self);
			} catch (Throwable ignored) {
			}
		}
	}

	private static void stepToward(Mob self, Mob foe) {
		int w = Dungeon.level.width();
		int sx = self.pos % w, sy = self.pos / w;
		int fx = foe.pos % w, fy = foe.pos / w;

		int bestCell = -1;
		int bestDist = Integer.MAX_VALUE;
		for (int oy = -1; oy <= 1; oy++) {
			for (int ox = -1; ox <= 1; ox++) {
				if (ox == 0 && oy == 0) continue;
				int nx = sx + ox, ny = sy + oy;
				if (nx <= 0 || ny <= 0 || nx >= Dungeon.level.width() - 1 || ny >= Dungeon.level.height() - 1) continue;
				int np = ny * w + nx;
				if (!Dungeon.level.passable[np]) continue;
				if (Actor.findChar(np) != null) continue;
				int d = Dungeon.level.distance(np, foe.pos);
				if (d < bestDist) {
					bestDist = d;
					bestCell = np;
				}
			}
		}
		if (bestCell < 0) return;

		int from = self.pos;
		self.pos = bestCell;
		if (self.sprite != null) {
			self.sprite.move(from, bestCell);
			self.sprite.hideEmo();
		}
	}
}
