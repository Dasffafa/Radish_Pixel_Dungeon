/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 拆解 / 合成
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiyCraft;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * DIY 工作台：把任意物品拆成材料，再用材料合成新的物品。
 *
 * <p>物品本身不会被消耗，也没有使用次数限制，开局就带一件，商店里也能买到。
 * 「拆解」可以从背包里挑任意非唯一物品；「合成」打开分类列表，
 * 花材料换一件新东西。</p>
 */
public class DiyWorkbench extends Item {

	/** 工作台自带的精粹仓库：key = kind + "|" + payload。 */
	private final java.util.LinkedHashMap<String, Integer> essences = new java.util.LinkedHashMap<>();

	public static String essenceKey(int kind, String payload) {
		return kind + "|" + (payload == null ? "" : payload);
	}

	public int essenceCount(int kind, String payload) {
		Integer n = essences.get(essenceKey(kind, payload));
		return n == null ? 0 : n;
	}

	public void addEssence(int kind, String payload, int amount) {
		if (amount <= 0) return;
		String key = essenceKey(kind, payload);
		Integer old = essences.get(key);
		essences.put(key, (old == null ? 0 : old) + amount);
	}

	public boolean takeEssence(int kind, String payload, int amount) {
		if (amount <= 0) return true;
		String key = essenceKey(kind, payload);
		Integer old = essences.get(key);
		if (old == null || old < amount) return false;
		if (old == amount) essences.remove(key);
		else essences.put(key, old - amount);
		return true;
	}

	/** kind → payload → 数量（合并供界面显示）。 */
	public java.util.LinkedHashMap<String, Integer> essenceStacks(int kind) {
		java.util.LinkedHashMap<String, Integer> out = new java.util.LinkedHashMap<>();
		String prefix = kind + "|";
		for (java.util.Map.Entry<String, Integer> e : essences.entrySet()) {
			if (!e.getKey().startsWith(prefix)) continue;
			out.put(e.getKey().substring(prefix.length()), e.getValue());
		}
		return out;
	}

	public int essenceTotal(int kind) {
		int total = 0;
		String prefix = kind + "|";
		for (java.util.Map.Entry<String, Integer> e : essences.entrySet()) {
			if (e.getKey().startsWith(prefix)) total += e.getValue();
		}
		return total;
	}

	// ===== 基础材料仓库：kind(0/1/2) → 数量 =====
	private final int[] materials = new int[3];

	public int materialCount(int kind) {
		if (kind < 0 || kind >= materials.length) return 0;
		return materials[kind];
	}

	public void addMaterial(int kind, int amount) {
		if (kind < 0 || kind >= materials.length || amount <= 0) return;
		materials[kind] += amount;
	}

	public boolean takeMaterial(int kind, int amount) {
		if (amount <= 0) return true;
		if (kind < 0 || kind >= materials.length) return false;
		if (materials[kind] < amount) return false;
		materials[kind] -= amount;
		return true;
	}

	private static final String MATERIALS = "diy_materials";

	private static final String ESSENCES = "diy_essences";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		String[] lines = new String[essences.size()];
		int i = 0;
		for (java.util.Map.Entry<String, Integer> e : essences.entrySet()) {
			lines[i++] = e.getKey() + "=" + e.getValue();
		}
		bundle.put(ESSENCES, lines);
		bundle.put(MATERIALS, materials);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		essences.clear();
		if (bundle.contains(MATERIALS)) {
			int[] stored = bundle.getIntArray(MATERIALS);
			if (stored != null) {
				for (int i = 0; i < materials.length && i < stored.length; i++) {
					materials[i] = Math.max(0, stored[i]);
				}
			}
		}
		if (bundle.contains(ESSENCES)) {
			String[] lines = bundle.getStringArray(ESSENCES);
			if (lines != null) {
				for (String line : lines) {
					if (line == null) continue;
					int eq = line.lastIndexOf('=');
					if (eq <= 0) continue;
					try {
						essences.put(line.substring(0, eq), Integer.parseInt(line.substring(eq + 1)));
					} catch (NumberFormatException ignored) {
					}
				}
			}
		}
	}


	public static final String AC_DISMANTLE = "DISMANTLE";
	public static final String AC_CRAFT     = "CRAFT";
	public static final String AC_ESSENCES  = "ESSENCES";

	{
		image = ItemSpriteSheet.DIY_WORKBENCH;
		unique = true;
		keptThoughLostInvent = true;
		defaultAction = AC_DISMANTLE;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_DISMANTLE);
		actions.add(AC_CRAFT);
		actions.add(AC_ESSENCES);
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (AC_DISMANTLE.equals(action) || AC_CRAFT.equals(action) || AC_ESSENCES.equals(action)) {
			return Messages.get(DiyWorkbench.class, "ac_" + action.toLowerCase());
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (AC_DISMANTLE.equals(action)) {
			GameScene.show(new WndBag(Dungeon.hero.belongings.backpack, dismantleSelector()));
		} else if (AC_CRAFT.equals(action)) {
			GameScene.show(new WndDiyCraft());
		} else if (AC_ESSENCES.equals(action)) {
			GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndDiyEssence());
		}
	}

	private static WndBag.ItemSelector dismantleSelector() {
		return new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(DiyWorkbench.class, "prompt_dismantle");
			}

			@Override
			public boolean itemSelectable(Item item) {
				return Diy.canDismantle(item);
			}

			@Override
			public void onSelect(Item item) {
				if (item == null) return;
				final WndBag.ItemSelector selector = this;
				// 先给一份产出清单再确认，免得手滑把好东西拆了
				GameScene.show(new WndOptions("拆解",
						"拆解「" + item.name() + "」会得到：\n" + Diy.analyze(item).describeLines(),
						"拆解", "取消") {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							Diy.dismantle(item);
						}
						GameScene.show(new WndBag(Dungeon.hero.belongings.backpack, selector));
					}
				});
			}
		};
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 250;
	}
}
