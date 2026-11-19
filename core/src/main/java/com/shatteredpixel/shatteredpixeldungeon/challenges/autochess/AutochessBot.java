package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 自走棋里的 AI 对手。
 *
 * <p>它不操作棋盘，只维护「血量 / 等级 / 金币 / 阵容 / 装备」这几项，
 * 每个回合按一套简单的运营逻辑变强：攒利息、按曲线升级、围绕主流羁绊补棋、
 * 阵容满了卖最弱的换更强的、凑三连升星、定期捡装备。</p>
 */
public class AutochessBot {

	public final String name;
	public int hp = 100;
	public int level = 1;
	public int gold = 8;

	/** 阵容：「类名:星级」。 */
	public final ArrayList<String> roster = new ArrayList<>();

	/** 收集到的装备（上场时按顺序发给棋子）。 */
	public final ArrayList<String> items = new ArrayList<>();

	public AutochessBot(String name) {
		this.name = name;
	}

	public boolean alive() {
		return hp > 0;
	}

	public void damage(int amount) {
		hp = Math.max(0, hp - amount);
	}

	// ------------------------------------------------------------------
	// 每回合运营
	// ------------------------------------------------------------------

	/** 每回合变强一点：收入 → 升级 → 留利息 → 补棋/换棋 → 三连 → 捡装备。 */
	public void advance(AutochessSharedPool pool, int round) {

		// 1) 收入：基础 5 + 利息（每 10 金 +1，上限 5）
		gold += 5 + Math.min(5, gold / 10);

		// 2) 升级：跟着回合走，钱够才买（每回合最多一次）
		int targetLevel = Math.min(10, 1 + round / 2);
		if (level < targetLevel && gold >= 8) {
			gold -= 4;
			level++;
		}

		// 3) 留底：前两回合把钱花光，之后开始攒利息
		int reserve = round <= 2 ? 0 : Math.min(20, (gold / 10) * 10);

		int cap = Math.min(12, level + 2);

		// 4) 补棋：从池子里挑「最合阵容」的一个，直到钱花到底线
		int guard = 0;
		while (roster.size() < cap && gold > reserve && gold >= 1 && guard++ < 10) {
			String pick = pickBestCandidate(pool);
			if (pick == null) break;
			if (!pool.take(pick)) break;
			gold -= costOf(pick);
			roster.add(pick + ":1");
		}

		// 5) 阵容满了：如果抽到比队里最弱更好的，就卖掉换掉
		if (roster.size() >= cap && gold > reserve + 2) {
			String pick = pickBestCandidate(pool);
			if (pick != null) {
				int worst = worstIndex();
				if (worst >= 0 && scoreClass(pick) > scoreClass(clsOf(roster.get(worst)))) {
					String sold = roster.remove(worst);
					pool.give(clsOf(sold));
					if (pool.take(pick)) {
						gold = Math.max(0, gold - costOf(pick));
						roster.add(pick + ":1");
					}
				}
			}
		}

		// 6) 三连升星
		mergeAll();

		// 7) 装备：每回合 40% 概率捡一件，最多 8 件
		if (Math.random() < 0.4f && items.size() < 8) {
			items.add(AutochessItems.ALL[(int) (Math.random() * AutochessItems.ALL.length)]);
		}
	}

	/** 从池子里抽几个候选，挑评分最高的（模拟「刷新看牌」）。 */
	private String pickBestCandidate(AutochessSharedPool pool) {
		String best = null;
		int bestScore = Integer.MIN_VALUE;
		for (int i = 0; i < 6; i++) {
			String cls = pool.draw(level);
			if (cls == null) continue;
			int score = scoreClass(cls);
			if (score > bestScore) {
				bestScore = score;
				best = cls;
			}
		}
		return best;
	}

	/** 一张牌对本阵容的价值：费用、主流羁绊、是否凑对/凑三连。 */
	private int scoreClass(String cls) {
		AutochessPool.Def def = AutochessPool.ofName(cls);
		if (def == null) return 0;

		int score = def.cost * 2;

		String dominant = dominantTrait();
		if (dominant != null && def.hasTrait(dominant)) score += 3;

		int sameClass = 0;
		for (String e : roster) {
			if (clsOf(e).equals(cls)) sameClass++;
		}
		score += sameClass;                       // 同名单卡多，更容易三连
		if (sameClass == 2) score += 4;           // 差一个就三连

		return score;
	}

	/** 队里人数最多的羁绊（并列时随便取一个）。 */
	private String dominantTrait() {
		String[] traits = {
				AutochessPool.BEAST, AutochessPool.SWARM, AutochessPool.UNDEAD,
				AutochessPool.CONSTRUCT, AutochessPool.DEMON, AutochessPool.HUMANOID,
				AutochessPool.TANK, AutochessPool.ASSASSIN, AutochessPool.RANGED,
				AutochessPool.FIGHTER, AutochessPool.MAGE, AutochessPool.SUPPORT
		};
		String best = null;
		int bestCount = 0;
		for (String trait : traits) {
			int count = 0;
			for (String e : roster) {
				AutochessPool.Def def = AutochessPool.ofName(clsOf(e));
				if (def != null && def.hasTrait(trait)) count++;
			}
			if (count > bestCount) {
				bestCount = count;
				best = trait;
			}
		}
		return bestCount >= 2 ? best : null;
	}

	private int worstIndex() {
		int worst = -1;
		int worstScore = Integer.MAX_VALUE;
		for (int i = 0; i < roster.size(); i++) {
			int score = scoreClass(clsOf(roster.get(i)));
			if (score < worstScore) {
				worstScore = score;
				worst = i;
			}
		}
		return worst;
	}

	private static int costOf(String cls) {
		AutochessPool.Def def = AutochessPool.ofName(cls);
		return def == null ? 1 : Math.max(1, def.cost);
	}

	// ------------------------------------------------------------------
	// 三连
	// ------------------------------------------------------------------

	private void mergeAll() {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int star = 1; star <= 2 && !changed; star++) {
				for (String cls : rosterClasses()) {
					int count = 0;
					for (String e : roster) {
						if (clsOf(e).equals(cls) && starOf(e) == star) count++;
					}
					if (count >= 3) {
						int removed = 0;
						for (int i = roster.size() - 1; i >= 0 && removed < 3; i--) {
							if (clsOf(roster.get(i)).equals(cls) && starOf(roster.get(i)) == star) {
								roster.remove(i);
								removed++;
							}
						}
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
			String cls = clsOf(e);
			if (!classes.contains(cls)) classes.add(cls);
		}
		return classes;
	}

	// ------------------------------------------------------------------
	// 上场
	// ------------------------------------------------------------------

	/** 上阵单位：按前后排排序后取前 level+2 个。 */
	public ArrayList<String> deployed() {
		ArrayList<String> sorted = new ArrayList<>(roster);
		sorted.sort((a, b) -> frontScore(b) - frontScore(a));
		ArrayList<String> list = new ArrayList<>();
		for (int i = 0; i < Math.min(level + 2, sorted.size()); i++) {
			list.add(sorted.get(i));
		}
		return list;
	}

	/** 越靠前越适合站前排。 */
	private static int frontScore(String entry) {
		AutochessPool.Def def = AutochessPool.ofName(clsOf(entry));
		if (def == null) return 0;
		int score = 0;
		if (def.hasTrait(AutochessPool.TANK)) score += 3;
		if (def.hasTrait(AutochessPool.FIGHTER)) score += 2;
		if (def.hasTrait(AutochessPool.ASSASSIN)) score += 1;
		if (def.hasTrait(AutochessPool.RANGED)) score -= 2;
		if (def.hasTrait(AutochessPool.MAGE)) score -= 1;
		return score;
	}

	/** 阵容战力（给 UI / 强度估算用）。 */
	public int power() {
		int sum = 0;
		for (String e : deployed()) {
			AutochessPool.Def def = AutochessPool.ofName(clsOf(e));
			sum += (def == null ? 1 : def.cost) * starOf(e);
		}
		return sum;
	}

	public String display() {
		return name + " " + hp + Strings.HEALTH;
	}

	/** 「类名:星级」里的类名。 */
	public static String clsOf(String entry) {
		if (entry == null) return "";
		int colon = entry.indexOf(':');
		return colon < 0 ? entry : entry.substring(0, colon);
	}

	/** 「类名:星级」里的星级。 */
	public static int starOf(String entry) {
		if (entry == null) return 1;
		int colon = entry.indexOf(':');
		if (colon < 0 || colon + 1 >= entry.length()) return 1;
		int end = colon + 1;
		while (end < entry.length() && Character.isDigit(entry.charAt(end))) end++;
		try {
			return Math.max(1, Integer.parseInt(entry.substring(colon + 1, end)));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/** 仅用于本地文字：「血量」两字。 */
	private static final class Strings {
		static final String HEALTH = "\u8840";
	}
}
