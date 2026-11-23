package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 棋子池：每个棋子有固定份数，玩家和 AI 都从这里抽。
 *
 * <p>抽走就少一份，卖回就多一份——这就是云顶「D 牌」手感的核心：
 * 别人拿走的牌，你这边就抽不到了。</p>
 */
public class AutochessSharedPool {

	/** 每种费用的总份数（参考云顶，池子越贵越少）。 */
	private static final int[] COPIES = {22, 18, 15, 12, 9};

	private final HashMap<String, Integer> remaining = new HashMap<>();

	public AutochessSharedPool() {
		for (AutochessPool.Def def : AutochessPool.all()) {
			int idx = Math.max(0, Math.min(COPIES.length - 1, def.cost - 1));
			remaining.put(def.cls.getSimpleName(), COPIES[idx]);
		}
	}

	public int count(String className) {
		Integer n = remaining.get(className);
		return n == null ? 0 : n;
	}

	/** 从池子里拿走一张，成功返回 true。 */
	public boolean take(String className) {
		int n = count(className);
		if (n <= 0) return false;
		remaining.put(className, n - 1);
		return true;
	}

	/** 卖回池子。 */
	public void give(String className) {
		remaining.put(className, count(className) + 1);
	}

	/**
	 * 按等级概率抽一张还在池子里的牌。
	 *
	 * @return 类名，池子空了返回 null
	 */
	public String draw(int level) {
		for (int tries = 0; tries < 60; tries++) {
			AutochessPool.Def def = AutochessPool.randomForLevel(level);
			if (def != null && count(def.cls.getSimpleName()) > 0) {
				return def.cls.getSimpleName();
			}
		}
		// 兜底：随便找一张还有货的
		for (AutochessPool.Def def : AutochessPool.all()) {
			if (count(def.cls.getSimpleName()) > 0) {
				return def.cls.getSimpleName();
			}
		}
		return null;
	}

	/** 池子里某费用的剩余总数（UI 展示用）。 */
	public int remainingOfCost(int cost) {
		int sum = 0;
		for (AutochessPool.Def def : AutochessPool.all()) {
			if (def.cost == cost) sum += count(def.cls.getSimpleName());
		}
		return sum;
	}

	/** 存档恢复用：直接设置某张牌的剩余份数。 */
	public void setCount(String className, int n) {
		remaining.put(className, Math.max(0, n));
	}

	public ArrayList<String> names() {
		return new ArrayList<>(remaining.keySet());
	}
}
