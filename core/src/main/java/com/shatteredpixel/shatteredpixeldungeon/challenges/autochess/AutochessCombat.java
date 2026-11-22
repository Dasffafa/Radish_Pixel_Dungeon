package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

import java.util.HashMap;

/**
 * 实时战斗：每只棋子有自己的攻速、移速。
 *
 * <p>移动走 SPD 自己的 {@link com.shatteredpixel.shatteredpixeldungeon.actors.Char#move(int)}，
 * 这样格子占用、碰撞、跑动动画都由引擎处理；为了让节奏更接近云顶，
 * GameScene 进场时会把 {@code CharSprite.moveInterval} 调到 0.10 秒（默认 0.2），
 * 移动看起来就接近连续滑行，而不是一跳一跳。</p>
 */
public class AutochessCombat {

	private static final class UnitState {
		float atkLeft = 0.25f;
		float atkInterval = 1.1f;
		float moveLeft = 0f;
		float moveInterval = 0.5f;
	}

	private static final HashMap<Mob, UnitState> states = new HashMap<>();

	private static final java.util.HashSet<String> RANGED = new java.util.HashSet<>(java.util.Arrays.asList(
			"DM100", "BlueShaman", "Necromancer", "Warlock",
			"DM175", "Artillerist", "ShieldMage", "Eye", "Scorpio", "Acidic", "FireElemental"));

	private static final java.util.HashSet<String> ASSASSIN = new java.util.HashSet<>(java.util.Arrays.asList(
			"Goblin", "Thief", "Assassin", "Bat", "Snake", "Spinner"));

	private static final java.util.HashSet<String> TANK = new java.util.HashSet<>(java.util.Arrays.asList(
			"Guard", "Jailer", "Golem", "Crab", "Brute", "Skeleton", "ClusteredSkeleton", "Gnoll"));

	public static void update(float delta) {
		AutochessRun run = AutochessRun.get();
		if (run == null || Dungeon.level == null || run.state() != AutochessRun.State.FIGHT) {
			if (!states.isEmpty()) states.clear();
			return;
		}
		if (delta <= 0f) return;
		delta = Math.min(delta, 0.1f);

		Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		for (Mob m : mobs) present.add(m);

		states.entrySet().removeIf(e -> !present.contains(e.getKey())
				|| e.getKey().sprite == null || !AutochessUtil.alive(e.getKey()));

		for (Mob self : mobs) {
			if (self.sprite == null) continue;
			self.sprite.visible = true;
			boolean ours = self.buff(AutochessUnitBuff.class) != null
					|| self.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;

			// 冻结原版 AI
			try {
				self.state = self.SLEEPING;
				self.paralysed = Math.max(self.paralysed, 1000);
				self.sprite.hideEmo();
			} catch (Throwable ignored) {
			}

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

			int dist = Dungeon.level.distance(self.pos, foe.pos);
			if (dist <= 1) {
				if (st.atkLeft <= 0f) {
					st.atkLeft = st.atkInterval;
					attack(self, foe);
				}
			} else if (st.moveLeft <= 0f) {
				if (stepToward(self, foe)) {
					st.moveLeft = st.moveInterval;
				} else {
					st.moveLeft = 0.15f;
				}
			}
		}
	}

	private static void applySpeed(UnitState st, Mob mob, int star) {
		String cls = mob.getClass().getSimpleName();
		float atk = 1.1f;
		float move = 0.42f;

		if (ASSASSIN.contains(cls)) {
			atk = 0.75f;
			move = 0.26f;
		} else if (TANK.contains(cls)) {
			atk = 1.35f;
			move = 0.60f;
		} else if (RANGED.contains(cls)) {
			atk = 1.00f;
			move = 0.36f;
		}

		float starBonus = 1f + 0.06f * Math.max(0, Math.min(3, star) - 1);
		st.atkInterval = atk / starBonus;
		st.moveInterval = move / starBonus;
		st.atkLeft = st.atkInterval * 0.5f;
	}

	private static Mob nearestFoe(Mob self, Mob[] mobs) {
		Mob best = null;
		int bestD = Integer.MAX_VALUE;
		for (Mob m : mobs) {
			if (m == self || !AutochessUtil.alive(m)) continue;
			if (m.sprite == null) continue;
			if (m.alignment == self.alignment) continue;
			int d = Dungeon.level.distance(self.pos, m.pos);
			if (d < bestD) {
				bestD = d;
				best = m;
			}
		}
		return best;
	}

	/** 朝敌人走一格；走成功返回 true。 */
	private static boolean stepToward(Mob self, Mob foe) {
		int w = Dungeon.level.width();
		int sx = self.pos % w, sy = self.pos / w;

		int bestCell = -1;
		int bestDist = Integer.MAX_VALUE;
		for (int oy = -1; oy <= 1; oy++) {
			for (int ox = -1; ox <= 1; ox++) {
				if (ox == 0 && oy == 0) continue;
				int nx = sx + ox, ny = sy + oy;
				if (nx <= 0 || ny <= 0 || nx >= w - 1 || ny >= Dungeon.level.height() - 1) continue;
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
		if (bestCell < 0) return false;

		// 自己处理移动：动画 + 占用，但不触发 Char.move() 里的 heroFOV 可见性副作用
		try {
			int from = self.pos;
			if (self.sprite != null) self.sprite.move(from, bestCell);
			self.pos = bestCell;
			Dungeon.level.occupyCell(self);
			if (self.sprite != null) {
				self.sprite.visible = true;
				self.sprite.hideEmo();
			}
			return true;
		} catch (Throwable t2) {
			return false;
		}
	}

	private static void attack(Mob self, Mob foe) {
		if (self.sprite != null) self.sprite.flash();

		int hpBefore = foe.HP;
		try {
			self.attack(foe);
		} catch (Throwable ignored) {
		}
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
}
