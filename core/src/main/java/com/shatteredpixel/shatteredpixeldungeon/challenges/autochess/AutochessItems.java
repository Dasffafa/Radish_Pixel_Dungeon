package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;

/**
 * 自走棋装备：每件装备给棋子一个固定增益，最多带 2 件。
 */
public class AutochessItems {

	public static final String SWORD = "sword";
	public static final String STAFF = "staff";
	public static final String ARMOR = "armor";
	public static final String CLOAK = "cloak";
	public static final String FANG = "fang";
	public static final String FLAME = "flame";
	public static final String DRAIN = "drain";
	public static final String GEM = "gem";

	public static final String[] ALL = {
			SWORD, STAFF, ARMOR, CLOAK, FANG, FLAME, DRAIN, GEM
	};

	public static final int MAX_PER_UNIT = 2;

	public static String name(String id) {
		return Messages.get(AutochessItems.class, "name_" + id);
	}

	public static String desc(String id) {
		return Messages.get(AutochessItems.class, "desc_" + id);
	}

	/** 随机一件装备。 */
	public static String random() {
		return ALL[com.watabou.utils.Random.Int(ALL.length)];
	}

	/** 从 "Class:star|item1,item2" 里取出装备列表。 */
	public static ArrayList<String> parse(String entry) {
		ArrayList<String> list = new ArrayList<>();
		if (entry == null) return list;
		int bar = entry.indexOf('|');
		if (bar < 0 || bar + 1 >= entry.length()) return list;
		for (String id : entry.substring(bar + 1).split(",")) {
			if (id != null && !id.isEmpty()) list.add(id);
		}
		return list;
	}

	/** 把装备列表写回条目字符串（不加前缀部分）。 */
	public static String encode(ArrayList<String> items) {
		StringBuilder sb = new StringBuilder();
		for (String id : items) {
			if (sb.length() > 0) sb.append(",");
			sb.append(id);
		}
		return sb.toString();
	}
}
