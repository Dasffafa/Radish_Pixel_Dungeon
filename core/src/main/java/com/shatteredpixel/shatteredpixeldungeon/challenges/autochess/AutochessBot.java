package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 自走棋里的 AI 对手。
 *
 * <p>它不操作棋盘，只维护「血量 / 等级 / 金币 / 阵容」这几项，每回合简单补强，
 * 玩家打的那一轮直接把它的阵容铺到敌方半场。</p>
 */
public class AutochessBot {

	public final String name;
	public int hp = 100;
	public int level = 1;
	public int gold = 8;

	/** 阵容：「类名:星级」。 */
	public final ArrayList<String> roster = new ArrayList<>();

	public AutochessBot(String name) {
		this.name = name;
	}

	public boolean alive() {
		return hp > 0;
	}

	public void damage(int amount) {
		hp = Math.max(0, hp - amount);
	}

	/** 每回合变强一点：拿钱、升级、从共享池里补棋子。 */
	public void advance(AutochessSharedPool pool, int round) {
		gold += 5;

		// 和玩家一样靠回合经验升级：前几回合升得快，后面放缓
		int targetLevel = Math.min(10, 1 + (round + 1) / 2);
		while (level < targetLevel) {
			level++;
		}

		// 补棋子（只在池子里还有货的时候）
		int cap = Math.min(12, level + 2);
		int guard = 0;
		while (roster.size() < cap && gold >= 1 && guard++ < 20) {
			String cls = pool.draw(level);
			if (cls == null) break;
			if (!pool.take(cls)) break;
			gold -= 1;
			roster.add(cls + ":1");
		}

		mergeAll();
	}

	/** 三合一：同名同星凑 3 个升 1 星。 */
	private void mergeAll() {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int star = 1; star <= 2 && !changed; star++) {
				for (String cls : rosterClasses()) {
					int count = 0;
					for (String e : roster) {
						if (e.equals(cls + ":" + star)) count++;
					}
					if (count >= 3) {
						for (int i = 0; i < 3; i++) roster.remove(cls + ":" + star);
						roster.add(cls + ":" + (star + 1));
						changed = true;
						break;
					}
				}
			}
		}
	}

	private ArrayList<String> rosterClasses() {
		ArrayList<String> classes = new ArrayList<>();
		for (String e : roster) {
			String cls = e.split(":")[0];
			if (!classes.contains(cls)) classes.add(cls);
		}
		return classes;
	}

	/** 上阵单位：取阵容前 level 个。 */
	public ArrayList<String> deployed() {
		ArrayList<String> list = new ArrayList<>();
		for (int i = 0; i < Math.min(level + 2, roster.size()); i++) {
			list.add(roster.get(i));
		}
		return list;
	}

	/** 阵容战力（给 UI / 强度估算用）。 */
	public int power() {
		int sum = 0;
		for (String e : deployed()) {
			String[] parts = e.split(":");
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			int star = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
			sum += (def == null ? 1 : def.cost) * star;
		}
		return sum;
	}

	public String display() {
		return name + " " + hp + "血";
	}
}
