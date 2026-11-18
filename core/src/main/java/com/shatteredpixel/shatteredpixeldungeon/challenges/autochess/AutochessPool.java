package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Acidic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Albino;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Artillerist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.ClusteredSkeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deminion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.DemonLord;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deviloon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.DM175;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Dog;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Drake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.GiantWorm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.GnollZealot;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Goblin;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.GoblinRider;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Gorgon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Grudge;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Jailer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Mayfly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Prisoner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.RoyalGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.ShieldMage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.StoneSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Torturer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 自走棋的单位池：费用、羁绊。
 *
 * <p>费用 = 商店价格 = 强度档位。回合越往后，商店能刷出的高费单位越多。</p>
 */
public class AutochessPool {

	public static final String BEAST     = "beast";      // 兽群：攻速
	public static final String HUMANOID  = "humanoid";   // 人形：攻击力
	public static final String CONSTRUCT = "construct";  // 构装：减伤
	public static final String DEMON     = "demon";      // 恶魔：全能增伤
	public static final String UNDEAD    = "undead";     // 亡灵：持续回血
	public static final String GUARD     = "guard";      // 守卫：开战护盾
	public static final String GIANT     = "giant";      // 巨兽：血量上限
	public static final String SWARM     = "swarm";      // 虫群：攻击附带额外伤害

	public static class Def {
		public final Class<? extends Mob> cls;
		public final int cost;
		/** 一只棋子可以有多个羁绊（云顶同款，一般 2~3 个）。 */
		public final String[] traits;

		Def(Class<? extends Mob> cls, int cost, String[] traits) {
			this.cls = cls;
			this.cost = cost;
			this.traits = traits;
		}

		public boolean hasTrait(String trait) {
			for (String t : traits) {
				if (t.equals(trait)) return true;
			}
			return false;
		}
	}

	private static final ArrayList<Def> POOL = new ArrayList<>();

	static {
		// 1 费：下水道
		add(Rat.class, 1, BEAST);
		add(Albino.class, 1, BEAST);
		add(Snake.class, 1, BEAST);
		add(Crab.class, 1, BEAST, GUARD);
		add(Slime.class, 1, SWARM);
		add(Swarm.class, 1, SWARM);
		add(Gnoll.class, 1, HUMANOID, BEAST);
		add(GiantWorm.class, 1, SWARM, GIANT);
		add(Mayfly.class, 1, SWARM);
		add(Dog.class, 1, BEAST);
		add(Goblin.class, 1, HUMANOID);

		// 2 费：监狱
		add(Bat.class, 2, BEAST);
		add(Thief.class, 2, HUMANOID);
		add(DM100.class, 2, CONSTRUCT, GUARD);
		add(Skeleton.class, 2, UNDEAD, CONSTRUCT);
		add(Guard.class, 2, HUMANOID, GUARD);
		add(Shaman.BlueShaman.class, 2, HUMANOID, DEMON);
		add(Spinner.class, 2, SWARM);
		add(Necromancer.class, 2, HUMANOID, UNDEAD);
		add(GoblinRider.class, 2, HUMANOID, BEAST);
		add(Jailer.class, 2, HUMANOID, GUARD);
		add(Prisoner.class, 2, HUMANOID);
		add(Torturer.class, 2, HUMANOID, GUARD);

		// 3 费：矿洞
		add(Brute.class, 3, HUMANOID, GIANT);
		add(Golem.class, 3, CONSTRUCT, GIANT);
		add(Ghoul.class, 3, UNDEAD);
		add(Grudge.class, 3, UNDEAD);
		add(ClusteredSkeleton.class, 3, UNDEAD, CONSTRUCT);
		add(DM175.class, 3, CONSTRUCT, GUARD);
		add(GnollZealot.class, 3, HUMANOID, BEAST);

		// 4 费：城市
		add(Monk.class, 4, HUMANOID, GUARD);
		add(Warlock.class, 4, HUMANOID, DEMON);
		add(Elemental.FireElemental.class, 4, DEMON, CONSTRUCT);
		add(Drake.class, 4, BEAST, GIANT);
		add(StoneSpirit.class, 4, CONSTRUCT, GIANT);
		add(Artillerist.class, 4, HUMANOID, CONSTRUCT);
		add(RoyalGuard.class, 4, HUMANOID, GUARD);
		add(ShieldMage.class, 4, HUMANOID, GUARD);

		// 5 费：恶魔大厅
		add(Succubus.class, 5, DEMON, HUMANOID);
		add(Scorpio.class, 5, SWARM, GIANT);
		add(Acidic.class, 5, SWARM, GIANT);
		add(Eye.class, 5, DEMON, CONSTRUCT);
		add(RipperDemon.class, 5, DEMON);
		add(Deminion.class, 5, DEMON, GIANT);
		add(Deviloon.class, 5, DEMON, SWARM);
		add(Gorgon.class, 5, CONSTRUCT, DEMON);
		add(DemonLord.class, 5, DEMON, GIANT);
	}
	private static void add(Class<? extends Mob> cls, int cost, String... traits) {
		POOL.add(new Def(cls, cost, traits));
	}

	/** 池子里全部棋子定义（共享池初始化/兜底用）。 */
	public static java.util.List<Def> all() {
		return new ArrayList<>(POOL);
	}

	public static Def of(Class<?> cls) {
		for (Def def : POOL) {
			if (def.cls == cls) {
				return def;
			}
		}
		return null;
	}

	public static Def ofName(String className) {
		for (Def def : POOL) {
			if (def.cls.getSimpleName().equals(className)) {
				return def;
			}
		}
		return null;
	}

	/** 按回合限制最高费用，越早越只能刷到便宜的。 */
	private static int costCap(int round) {
		if (round <= 2) return 1;
		if (round <= 4) return 2;
		if (round <= 7) return 3;
		return 4;
	}

	/** 随机一个当前回合能刷到的单位。 */
	public static Def random(int round) {
		int cap = costCap(round);
		ArrayList<Def> candidates = new ArrayList<>();
		for (Def def : POOL) {
			if (def.cost <= cap) {
				candidates.add(def);
			}
		}
		if (candidates.isEmpty()) {
			return POOL.get(0);
		}
		return candidates.get(Random.Int(candidates.size()));
	}

	/**
	 * 云顶式商店概率：按等级给不同费用单位的权重。
	 * 等级越高，高费单位出现得越多（3~9 级）。
	 */
	public static Def randomForLevel(int level) {
		// 1~5 费权重，按等级（3~9）
		float[][] table = {
				{65, 25, 10, 0, 0},   // 3 级
				{50, 35, 15, 0, 0},   // 4 级
				{40, 30, 25, 5, 0},   // 5 级
				{30, 35, 25, 10, 0},  // 6 级
				{20, 30, 30, 15, 5},  // 7 级
				{15, 25, 30, 20, 10}, // 8 级
				{10, 20, 25, 25, 20}  // 9 级
		};
		float[] weights = table[Math.max(0, Math.min(6, level - 3))];
		float total = 0;
		for (int cost = 1; cost <= 5; cost++) total += weights[cost - 1];
		float roll = Random.Float(total);
		float acc = 0;
		int cost = 5;
		for (int i = 0; i < 5; i++) {
			acc += weights[i];
			if (roll <= acc) { cost = i + 1; break; }
		}
		ArrayList<Def> pool = new ArrayList<>();
		for (Def def : POOL) {
			if (def.cost == cost) pool.add(def);
		}
		if (pool.isEmpty()) pool = POOL;
		return pool.get(Random.Int(pool.size()));
	}
	/** 1~5 费在某个等级的出现概率（百分比，和为 100）。 */
	public static int[] oddsForLevel(int level) {
		float[][] table = {
				{65, 25, 10, 0, 0},   // 3 级
				{50, 35, 15, 0, 0},   // 4 级
				{40, 30, 25, 5, 0},   // 5 级
				{30, 35, 25, 10, 0},  // 6 级
				{20, 30, 30, 15, 5},  // 7 级
				{15, 25, 30, 20, 10}, // 8 级
				{10, 20, 25, 25, 20}  // 9 级
		};
		float[] w = table[Math.max(0, Math.min(6, level - 3))];
		int[] pct = new int[5];
		float total = 0;
		for (float v : w) total += v;
		for (int i = 0; i < 5; i++) {
			pct[i] = Math.round(w[i] / total * 100f);
		}
		return pct;
	}

	/** 羁绊的档位阈值：{ 一档人数, 二档人数 }。 */
	public static int[] thresholds(String trait) {
		if (DEMON.equals(trait) || UNDEAD.equals(trait)) return new int[]{2, 4};
		return new int[]{3, 5};
	}
}
