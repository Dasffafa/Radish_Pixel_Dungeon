package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Acidic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Albino;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
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

	public static class Def {
		public final Class<? extends Mob> cls;
		public final int cost;
		public final String trait;

		Def(Class<? extends Mob> cls, int cost, String trait) {
			this.cls = cls;
			this.cost = cost;
			this.trait = trait;
		}
	}

	private static final ArrayList<Def> POOL = new ArrayList<>();

	static {
		// 1 费：早期杂兵
		add(Rat.class, 1, BEAST);
		add(Albino.class, 1, BEAST);
		add(Snake.class, 1, BEAST);
		add(Crab.class, 1, BEAST);
		add(Slime.class, 1, BEAST);
		add(Gnoll.class, 1, HUMANOID);

		// 2 费：二三区
		add(Bat.class, 2, BEAST);
		add(Swarm.class, 2, BEAST);
		add(Acidic.class, 2, BEAST);
		add(Thief.class, 2, HUMANOID);
		add(DM100.class, 2, CONSTRUCT);
		add(Skeleton.class, 2, CONSTRUCT);

		// 3 费：三区
		add(Guard.class, 3, HUMANOID);
		add(Shaman.class, 3, HUMANOID);
		add(Spinner.class, 3, CONSTRUCT);
		add(Ghoul.class, 3, HUMANOID);
		add(Brute.class, 3, HUMANOID);
		add(Golem.class, 3, CONSTRUCT);

		// 4 费：四区狠角色
		add(Monk.class, 4, HUMANOID);
		add(Warlock.class, 4, HUMANOID);
		add(Necromancer.class, 4, HUMANOID);
		add(Succubus.class, 4, DEMON);
		add(Elemental.class, 4, DEMON);
		add(Scorpio.class, 4, DEMON);
		add(RipperDemon.class, 4, DEMON);
	}

	private static void add(Class<? extends Mob> cls, int cost, String trait) {
		POOL.add(new Def(cls, cost, trait));
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

	/** 羁绊的档位阈值：{ 一档人数, 二档人数 }。 */
	public static int[] thresholds(String trait) {
		if (DEMON.equals(trait)) return new int[]{2, 4};
		return new int[]{3, 5};
	}
}
