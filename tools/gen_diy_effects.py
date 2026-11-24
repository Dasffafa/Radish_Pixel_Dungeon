"""由 tools/diy_effects_list.py 生成 items/DiyEffects.java。

运行：python tools/gen_diy_effects.py
"""

import os

from diy_effects_list import EFFECTS

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(
    HERE, "..", "core", "src", "main", "java", "com", "shatteredpixel",
    "shatteredpixeldungeon", "items", "DiyEffects.java")

HEADER = """/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 常驻效果目录（本文件由 tools/gen_diy_effects.py 生成，别手改）
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

/** id ↔ 常驻 buff 类的对照表。 */
public final class DiyEffects {

	private DiyEffects() {
	}

	/** 全部可选的常驻效果 id。 */
	public static final String[] IDS = {
%s
	};

	/** 与 {@link #IDS} 一一对应的 buff 类。 */
	@SuppressWarnings("unchecked")
	public static final Class<? extends Buff>[] CLASSES = new Class[]{
%s
	};

	/** 按 id 找 buff 类，找不到返回 null。 */
	public static Class<? extends Buff> byId(String id) {
		if (id == null) return null;
		for (int i = 0; i < IDS.length; i++) {
			if (IDS[i].equals(id)) return CLASSES[i];
		}
		return null;
	}

	/** 这个 id 是不是常驻效果。 */
	public static boolean isEffect(String id) {
		return byId(id) != null;
	}
}
"""


def main():
    ids = "\n".join('			"%s",' % effect_id for effect_id, _ in EFFECTS)
    classes = "\n".join(
        "			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.%s.class," % cls
        for _, cls in EFFECTS)
    text = HEADER % (ids, classes)
    with open(OUT, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)
    print("wrote %s with %d effects" % (OUT, len(EFFECTS)))


if __name__ == "__main__":
    main()
