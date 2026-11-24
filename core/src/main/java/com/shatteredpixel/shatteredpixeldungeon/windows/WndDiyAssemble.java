/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 组装窗口（1 等级 / 2 附魔 / 3 效果 / 4 属性）
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyComponent;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyMaterial;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.LinkedHashSet;

/**
 * DIY 组装的第二步，四个板块：
 * <ol>
 *   <li>等级：强化精华，每份 +1 级</li>
 *   <li>附魔：全游戏附魔/铭文，多选</li>
 *   <li>效果：装备/药水/卷轴/法杖/投掷物/护甲等来源的效果，装备后生效、脱下消失</li>
 *   <li>属性：武器伤害/攻击距离/攻击速度，护甲减伤</li>
 * </ol>
 */
public class WndDiyAssemble extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;
	private static final int SMALL_HEIGHT = 16;
	private static final int ROW_GAP = 1;

	/** 组装状态，重建窗口时原样带过去。 */
	public static final class State {
		public final Diy.Group group;
		public final Class<? extends Item> base;
		public int levels;
		public final LinkedHashSet<String> enchants = new LinkedHashSet<>();
		public final LinkedHashSet<String> passives = new LinkedHashSet<>();
		public int attackBonus;
		public int armorBonus;
		public int reachBonus;
		public int speedBonus;
		public int countBonus;
		public int energyBonus;
		public int chargeBonus;
		/** 上次离开时的滚动位置（重建/从选择器返回时恢复）。 */
		public float scrollY;

		public State(Diy.Group group, Class<? extends Item> base) {
			this.group = group;
			this.base = base;
		}
	}

	private final State state;
	private final Item sample;
	private ScrollPane scrollPane;

	public WndDiyAssemble(Diy.Group group, Class<? extends Item> baseClass) {
		this(new State(group, baseClass));
	}

	public WndDiyAssemble(State state) {
		super();
		this.state = state;
		this.sample = safeInstance(state.base);
		build();
	}

	private static Item safeInstance(Class<? extends Item> cls) {
		try {
			Item item = Reflection.newInstance(cls);
			if (item != null) item.identify(false);
			return item;
		} catch (Throwable t) {
			return null;
		}
	}

	private void rebuild() {
		if (scrollPane != null) {
			state.scrollY = scrollPane.scrollY();
		}
		hide();
		GameScene.show(new WndDiyAssemble(state));
	}

	private void build() {
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		final float innerWidth = width - MARGIN * 2;

		if (sample == null) {
			if (Dungeon.hero != null) {
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("这件底材没法组装。");
			}
			hide();
			return;
		}

		int levelAvailable = Diy.componentCount(DiyComponent.LEVEL, "");
		if (state.levels > levelAvailable) state.levels = levelAvailable;

		Diy.Price price = Diy.priceOf(sample, state.levels, state.enchants, state.passives,
				state.attackBonus, state.armorBonus, state.reachBonus, state.speedBonus,
				state.countBonus, state.energyBonus, state.chargeBonus);

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock("组装 · " + sample.name(), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(MARGIN, pos);
		title.maxWidth((int) innerWidth);
		add(title);
		pos = title.bottom() + MARGIN;

		RenderedTextBlock info = PixelScene.renderTextBlock(6);
		info.text("成品：" + Diy.assemblePreview(sample, state.levels, state.enchants,
						state.passives, state.attackBonus, state.armorBonus,
						state.reachBonus, state.speedBonus,
						state.countBonus, state.energyBonus, state.chargeBonus)
				+ "\n底材：" + Diy.materialName(Diy.kindOf(sample)) + " ×" + Diy.costOf(sample)
				+ "（有 " + Diy.materialCount(Diy.kindOf(sample)) + "）", (int) innerWidth);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + 2 * MARGIN;

		Component content = new Component();
		float y = 0;

		int section = 1;
		boolean upgradable = sample.isUpgradable();
		if (!upgradable) {
			state.levels = 0;
		}

		// ================= 等级（只有可强化物品才有这一栏） =================
		if (upgradable) {
			y = addSectionTitle(content, y, innerWidth, (section++) + ". 等级");
			y = addText(content, y, innerWidth,
					"强化精华 " + levelAvailable + " 份，每份 +1 级；当前 +" + state.levels);
			y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
					new Runnable[]{
							() -> { if (state.levels > 0) { state.levels--; rebuild(); } },
							() -> { if (state.levels < levelAvailable) { state.levels++; rebuild(); } },
							() -> {
								if (levelAvailable > state.levels) {
									state.levels = Math.min(levelAvailable, state.levels + 5);
									rebuild();
								}
							},
							() -> { if (state.levels != 0) { state.levels = 0; rebuild(); } },
					});
			y += 3;
		}

		// ================= 2. 附魔 =================
		java.util.ArrayList<String> enchantCatalog = Diy.enchantCatalog(sample);
		if (!enchantCatalog.isEmpty()) {
			y = addSectionTitle(content, y, innerWidth, (section++) + ". 附魔");
			y = addText(content, y, innerWidth, "全部 " + enchantCatalog.size() + " 种，已选 "
					+ state.enchants.size() + "；每项消耗 1 个对应附魔精粹（不能用材料代替）");
			RedButton pick = new RedButton("选择附魔", 8) {
				@Override
				protected void onClick() {
					if (scrollPane != null) state.scrollY = scrollPane.scrollY();
					hide();
					GameScene.show(new WndDiyPick(state, true));
				}
			};
			pick.setRect(0, y, innerWidth, BUTTON_HEIGHT);
			content.add(pick);
			y += BUTTON_HEIGHT + 3;
		}

		// ================= 3. 效果 =================
		java.util.ArrayList<String> effects = Diy.passiveCatalog(sample);
		if (!effects.isEmpty() && Diy.supportsPassives(sample)) {
			y = addSectionTitle(content, y, innerWidth, (section++) + ". 效果");
			y = addText(content, y, innerWidth, "装备/护甲/投掷物/法杖/卷轴/符石/消耗品/药水 + 附魔铭文 共 "
					+ effects.size() + " 种，已选 " + state.passives.size()
					+ "；装备后生效、脱下消失。每项消耗 1 个对应精粹（不能用材料代替）");
			RedButton pick = new RedButton("选择效果", 8) {
				@Override
				protected void onClick() {
					if (scrollPane != null) state.scrollY = scrollPane.scrollY();
					hide();
					GameScene.show(new WndDiyPick(state, false));
				}
			};
			pick.setRect(0, y, innerWidth, BUTTON_HEIGHT);
			content.add(pick);
			y += BUTTON_HEIGHT + 3;
		}

		// ================= 4. 属性 =================
		y = addSectionTitle(content, y, innerWidth, (section++) + ". 属性");
		if (sample instanceof Weapon) {
			y = addText(content, y, innerWidth, "伤害 +" + state.attackBonus
					+ "（每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.STAT_METAL_COST + "）");
			y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
					statActions(true, false, false));
			y += 3;
			y = addText(content, y, innerWidth, "攻击距离 +" + state.reachBonus
					+ "（每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.REACH_METAL_COST + "）");
			y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
					statActions(false, true, false));
			y += 3;
			y = addText(content, y, innerWidth, "攻击速度 +" + state.speedBonus
					+ "（每点延迟 -0.05，每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.SPEED_METAL_COST + "）");
			y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
					statActions(false, false, true));
		} else if (sample instanceof Armor) {
			y = addText(content, y, innerWidth, "减伤 +" + state.armorBonus
					+ "（每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.STAT_METAL_COST + "）");
			y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
					new Runnable[]{
							() -> { if (state.armorBonus > 0) { state.armorBonus--; rebuild(); } },
							() -> { state.armorBonus++; rebuild(); },
							() -> { state.armorBonus += 5; rebuild(); },
							() -> { if (state.armorBonus != 0) { state.armorBonus = 0; rebuild(); } },
					});
		} else if (sample instanceof com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon) {
			// 投掷武器之类：只有伤害
			y = addText(content, y, innerWidth, "伤害 +" + state.attackBonus
					+ "（每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.STAT_METAL_COST + "）");
			y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
					statActions(true, false, false));
		} else if (!upgradable) {
			// 消耗品专属属性：数量 / 饱食度 / 充能
			if (sample.stackable) {
				y = addText(content, y, innerWidth, "数量 +" + state.countBonus
						+ "（每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.COUNT_METAL_COST + "）");
				y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
						bonusActions(0));
				y += 3;
			}
			if (sample instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food) {
				y = addText(content, y, innerWidth, "饱食度 +" + state.energyBonus + "（每点 +50 能量，"
						+ Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.ENERGY_METAL_COST + "）");
				y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
						bonusActions(1));
				y += 3;
			}
			if (sample instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand) {
				y = addText(content, y, innerWidth, "充能 +" + state.chargeBonus
						+ "（每点 " + Diy.materialName(DiyMaterial.METAL) + " ×" + Diy.CHARGE_METAL_COST + "）");
				y = addButtonsRow(content, y, innerWidth, new String[]{ "－1", "＋1", "＋5", "清零" },
						bonusActions(2));
				y += 3;
			}
			if (!sample.stackable) {
				y = addText(content, y, innerWidth, "这类物品没有可加的属性。");
			}
		} else {
			y = addText(content, y, innerWidth, "这类物品没有可加的属性。");
		}

		content.setSize(innerWidth, y);

		ScrollPane scroll = new ScrollPane(content);
		scrollPane = scroll;
		add(scroll);

		float confirmHeight = BUTTON_HEIGHT;

		RenderedTextBlock priceLine = PixelScene.renderTextBlock(6);
		priceLine.text("需要：" + price.summary(), (int) innerWidth);
		priceLine.hardlight(price.affordable ? 0xFFFF44 : 0xFF6666);
		float priceHeight = priceLine.height();

		// 手机版：整个窗口（标题 + 滚动区 + 价格 + 按钮）不能超出 UI 视口
		float maxTotal = PixelScene.uiCamera.height * 0.80f;
		float maxVisible = maxTotal - pos - MARGIN * 4 - priceHeight - confirmHeight;
		float visibleHeight = Math.max(40f, Math.min(y, maxVisible));
		scroll.setRect(MARGIN, pos, innerWidth, visibleHeight);
		if (state.scrollY > 0) {
			scroll.scrollTo(0, state.scrollY);
		}
		float priceY = pos + visibleHeight + MARGIN;
		priceLine.setPos(MARGIN, priceY);
		add(priceLine);

		RedButton confirm = new RedButton(price.affordable ? "开始组装" : "材料不够", 8) {
			@Override
			protected void onClick() {
				hide();
				Item out = Diy.craft(state.base, state.levels, state.enchants, state.passives,
						state.attackBonus, state.armorBonus, state.reachBonus, state.speedBonus,
						state.countBonus, state.energyBonus, state.chargeBonus);
				if (out != null && state.group != null) {
					GameScene.show(new WndDiyCraftList(state.group));
				} else {
					GameScene.show(new WndDiyAssemble(state));
				}
			}
		};
		confirm.setRect(MARGIN, priceLine.bottom() + MARGIN, innerWidth, confirmHeight);
		confirm.enable(price.affordable);
		add(confirm);

		resize(width, (int) Math.min(confirm.bottom() + MARGIN, PixelScene.uiCamera.height - MARGIN));
	}

	private Runnable[] bonusActions(final int which) {
		return new Runnable[]{
				() -> {
					if (which == 0) { if (state.countBonus > 0) { state.countBonus--; rebuild(); } }
					else if (which == 1) { if (state.energyBonus > 0) { state.energyBonus--; rebuild(); } }
					else { if (state.chargeBonus > 0) { state.chargeBonus--; rebuild(); } }
				},
				() -> {
					if (which == 0) state.countBonus++;
					else if (which == 1) state.energyBonus++;
					else state.chargeBonus++;
					rebuild();
				},
				() -> {
					if (which == 0) state.countBonus += 5;
					else if (which == 1) state.energyBonus += 5;
					else state.chargeBonus += 5;
					rebuild();
				},
				() -> {
					if (which == 0) state.countBonus = 0;
					else if (which == 1) state.energyBonus = 0;
					else state.chargeBonus = 0;
					rebuild();
				},
		};
	}

	private Runnable[] statActions(final boolean attack, final boolean reach, final boolean speed) {
		return new Runnable[]{
				() -> {
					if (attack) {
						if (state.attackBonus > 0) { state.attackBonus--; rebuild(); }
					} else if (reach) {
						if (state.reachBonus > 0) { state.reachBonus--; rebuild(); }
					} else if (speed) {
						if (state.speedBonus > 0) { state.speedBonus--; rebuild(); }
					}
				},
				() -> {
					if (attack) state.attackBonus++;
					else if (reach) state.reachBonus++;
					else state.speedBonus++;
					rebuild();
				},
				() -> {
					if (attack) state.attackBonus += 5;
					else if (reach) state.reachBonus += 5;
					else state.speedBonus += 5;
					rebuild();
				},
				() -> {
					if (attack) state.attackBonus = 0;
					else if (reach) state.reachBonus = 0;
					else state.speedBonus = 0;
					rebuild();
				},
		};
	}

	private float addSectionTitle(Component content, float y, float width, String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 8);
		block.hardlight(0xFFFF44);
		block.setPos(0, y);
		block.maxWidth((int) width);
		content.add(block);
		return y + block.height() + 2;
	}

	private float addButtonsRow(Component content, float y, float width, String[] labels, Runnable[] actions) {
		float buttonWidth = (width - MARGIN * (labels.length - 1)) / labels.length;
		for (int i = 0; i < labels.length; i++) {
			final Runnable action = actions[i];
			RedButton button = new RedButton(labels[i], 7) {
				@Override
				protected void onClick() {
					action.run();
				}
			};
			button.setRect(i * (buttonWidth + MARGIN), y, buttonWidth, SMALL_HEIGHT);
			content.add(button);
		}
		return y + SMALL_HEIGHT + ROW_GAP;
	}

	private float addText(Component content, float y, float width, String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.setPos(0, y);
		block.maxWidth((int) width);
		content.add(block);
		return y + block.height() + 2;
	}

	/** 供界面之外的地方（测试/复用）取状态。 */
	public State state() {
		return state;
	}
}
