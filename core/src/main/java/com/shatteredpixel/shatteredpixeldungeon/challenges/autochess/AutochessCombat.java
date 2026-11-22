package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.PointF;

import java.util.HashMap;

/**
 * 实时战斗：每只棋子独立的攻速、移速。
 *
 * <p>移动是**真正的连续滑行**：</p>
 * <ul>
 *   <li>起步时只播放跑动动画（{@code sprite.autochessStartRun}），不创建补间</li>
 *   <li>每帧按「像素/秒」把 sprite 朝目标格推进，速度恒定</li>
 *   <li>起步就提交 {@code pos} 并 {@code occupyCell}，保证碰撞与不重叠</li>
 *   <li>到达后切回 idle，开始下一次移动冷却</li>
 * </ul>
 */
public class AutochessCombat {

	private static final class UnitState {
		float atkLeft = 0.25f;
		float atkInterval = 1.1f;

		int targetCell = -1;
		float tx = 0f;
		float ty = 0f;
		float pxPerSec = 48f;
		float retarget = 0f;
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
			boolean ours = self.buff(AutochessUnitBuff.class) != null
					|| self.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;

			// 冻结原版 AI + 保证可见
			try {
				self.state = self.SLEEPING;
				self.paralysed = Math.max(self.paralysed, 1000);
				self.sprite.visible = true;
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
			st.retarget -= delta;

			// ---------- 正在滑行：只推进位置 ----------
			if (st.targetCell >= 0) {
				glide(self, st, delta);
				continue;
			}

			Mob foe = nearestFoe(self, mobs);
			if (foe == null) continue;

			if (Dungeon.level.distance(self.pos, foe.pos) <= 1) {
				if (st.atkLeft <= 0f) {
					st.atkLeft = st.atkInterval;
					attack(self, foe);
				}
			} else if (st.retarget <= 0f) {
				if (!startMove(self, st, foe)) st.retarget = 0.1f;
			}
		}
	}

	/** 每帧推进 sprite 位置；到达后切 idle。 */
	private static void glide(Mob self, UnitState st, float delta) {
		float x = self.sprite.x;
		float y = self.sprite.y;
		float dx = st.tx - x;
		float dy = st.ty - y;
		float dist = (float) Math.sqrt(dx * dx + dy * dy);
		float step = st.pxPerSec * delta;

		if (dist <= step || dist < 0.5f) {
			self.sprite.x = st.tx;
			self.sprite.y = st.ty;
			try {
				self.sprite.idle();
			} catch (Throwable ignored) {
			}
			st.targetCell = -1;
			st.retarget = 0.02f;
		} else {
			self.sprite.x = x + dx / dist * step;
			self.sprite.y = y + dy / dist * step;
		}
		self.sprite.visible = true;
	}

	/** 选一格并开始滑行（立刻提交占用，避免两只棋子挤同一格）。 */
	private static boolean startMove(Mob self, UnitState st, Mob foe) {
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

		try {
			int from = self.pos;
			PointF dest = self.sprite.worldToCamera(bestCell);
			st.targetCell = bestCell;
			st.tx = dest.x;
			st.ty = dest.y;

			self.pos = bestCell;
			Dungeon.level.occupyCell(self);
			self.sprite.autochessStartRun(from, bestCell);
			self.sprite.visible = true;
			return true;
		} catch (Throwable t) {
			st.targetCell = -1;
			return false;
		}
	}

	private static void applySpeed(UnitState st, Mob mob, int star) {
		String cls = mob.getClass().getSimpleName();
		float atk = 1.1f;
		float cellsPerSec = 2.6f;

		if (ASSASSIN.contains(cls)) {
			atk = 0.75f;
			cellsPerSec = 3.6f;
		} else if (TANK.contains(cls)) {
			atk = 1.35f;
			cellsPerSec = 2.0f;
		} else if (RANGED.contains(cls)) {
			atk = 1.00f;
			cellsPerSec = 2.3f;
		}

		float starBonus = 1f + 0.06f * Math.max(0, Math.min(3, star) - 1);
		st.atkInterval = atk / starBonus;
		st.pxPerSec = com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.SIZE
				* cellsPerSec * starBonus;
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
