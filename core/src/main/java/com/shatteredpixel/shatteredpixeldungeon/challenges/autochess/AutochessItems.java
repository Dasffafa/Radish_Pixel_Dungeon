package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Reflection;

/**
 * 自走棋装备：直接用本体的装备（短剑、锁甲、戒指、法杖…），
 * 每只棋子最多带 2 件，两件同名可以合成一件强化版（id 后面加 "+"）。
 */
public class AutochessItems {

	/** 备选装备（全部是萝卜地牢本体里的物品）。 */
	public static final String[] ALL = {
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.RatArmor",
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.CrabArmor",
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.PrismArmor",
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.EnergyArmor",
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.MoonlightArmor",
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterGlow",
			"com.shatteredpixel.shatteredpixeldungeon.items.armor.SoulMix",
			"com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.KillBoatSword"
	};

	public static final int MAX_PER_UNIT = 2;

	/** 是不是合成后的强化版。 */
	public static boolean isUpgraded(String id) {
		return id != null && id.endsWith("+");
	}

	/** 去掉强化标记。 */
	public static String base(String id) {
		return isUpgraded(id) ? id.substring(0, id.length() - 1) : id;
	}

	/** 强化倍率。 */
	public static float power(String id) {
		return isUpgraded(id) ? 1.7f : 1f;
	}

	public static String name(String id) {
		Item item = item(id);
		String base = item == null ? base(id) : item.name();
		return isUpgraded(id) ? base + "+" : base;
	}

	public static String desc(String id) {
		String key = shortName(id);
		String text = com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
				AutochessItems.class, "desc_" + key);
		return isUpgraded(id) ? text + "（强化 1.7 倍）" : text;
	}

	/** 类名最后一段，用于文案 key 和效果判断。 */
	public static String shortName(String id) {
		String base = base(id);
		int dot = base.lastIndexOf('.');
		return dot < 0 ? base : base.substring(dot + 1);
	}

	public static Item item(String id) {
		try {
			return (Item) Reflection.newInstance(Class.forName(base(id)));
		} catch (Exception e) {
			return null;
		}
	}

	/** 合成配方：两件不同装备 → 一件更强的（云顶式配方）。 */
	public static final String[][] RECIPES = {
			{"RatArmor", "KillBoatSword", "SoulMix"},
			{"RatArmor", "CrabArmor", "AfterGlow"},
			{"PrismArmor", "EnergyArmor", "MoonlightArmor"},
			{"MoonlightArmor", "AfterGlow", "SoulMix"}
	};

	/** 短名 → 完整类名。 */
	public static String fullName(String shortName) {
		for (String id : ALL) {
			if (AutochessItems.shortName(id).equals(shortName)) return id;
		}
		return "com.shatteredpixel.shatteredpixeldungeon.items.armor." + shortName;
	}

	public static String random() {
		return ALL[com.watabou.utils.Random.Int(ALL.length)];
	}

	/** 从 "Class:star|item1,item2" 里取出装备列表。 */
	public static java.util.ArrayList<String> parse(String entry) {
		java.util.ArrayList<String> list = new java.util.ArrayList<>();
		if (entry == null) return list;
		int bar = entry.indexOf('|');
		if (bar < 0 || bar + 1 >= entry.length()) return list;
		for (String id : entry.substring(bar + 1).split(",")) {
			if (id != null && !id.isEmpty()) list.add(id);
		}
		return list;
	}

	public static String encode(java.util.List<String> items) {
		StringBuilder sb = new StringBuilder();
		for (String id : items) {
			if (sb.length() > 0) sb.append(",");
			sb.append(id);
		}
		return sb.toString();
	}
}
