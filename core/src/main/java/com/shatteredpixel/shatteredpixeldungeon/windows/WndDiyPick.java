/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 附魔/特性选择器（全开放列表）
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyComponent;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyMaterial;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * 从游戏里的**全部**附魔 / 铭文 / 特性里挑，想选多少选多少。
 *
 * <p>有对应精粹的项会显示「精粹×1」，按精粹抵扣；没有的显示材料价
 * （附魔 4 魔尘、特性 6 铁件），只要材料够就能贴上去。</p>
 */
public class WndDiyPick extends Window {

	private static final int WIDTH_P = 150;
	private static final int WIDTH_L = 172;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;
	private static final int ROW_HEIGHT = 17;
	private static final int ROW_GAP = 1;

	private final WndDiyAssemble.State state;
	private final boolean enchantMode;
	private final Item sample;

	public WndDiyPick(WndDiyAssemble.State state, boolean enchantMode) {
		super();
		this.state = state;
		this.enchantMode = enchantMode;
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
		hide();
		GameScene.show(new WndDiyPick(state, enchantMode));
	}

	private void back() {
		hide();
		GameScene.show(new WndDiyAssemble(state));
	}

	private void build() {
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		final float innerWidth = width - MARGIN * 2;

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock(enchantMode ? "选择附魔" : "选择效果", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(MARGIN, pos);
		title.maxWidth((int) innerWidth);
		add(title);
		pos = title.bottom() + MARGIN;

		ArrayList<String> catalog = enchantMode
				? Diy.enchantCatalog(sample)
				: Diy.passiveCatalog(sample);
		LinkedHashSet<String> selected = enchantMode ? state.enchants : state.passives;

		RenderedTextBlock info = PixelScene.renderTextBlock(6);
		info.text("已选 " + selected.size() + " / " + catalog.size()
				+ (enchantMode
				? "　　魔尘 " + Diy.materialCount(DiyMaterial.ARCANE) + "，附魔精粹 " + Diy.componentTotal(DiyComponent.ENCHANT)
				: "　　铁件 " + Diy.materialCount(DiyMaterial.METAL) + "，效果精粹 " + Diy.componentTotal(DiyComponent.PASSIVE)),
				(int) innerWidth);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + MARGIN;

		// 全选 / 清空
		float half = (innerWidth - MARGIN) / 2f;
		RedButton selectAll = new RedButton("全选", 6) {
			@Override
			protected void onClick() {
				selected.clear();
				selected.addAll(catalog);
				rebuild();
			}
		};
		selectAll.setRect(MARGIN, pos, half, BUTTON_HEIGHT);
		add(selectAll);

		RedButton clear = new RedButton("清空", 6) {
			@Override
			protected void onClick() {
				if (!selected.isEmpty()) {
					selected.clear();
					rebuild();
				}
			}
		};
		clear.setRect(MARGIN + half + MARGIN, pos, half, BUTTON_HEIGHT);
		add(clear);
		pos += BUTTON_HEIGHT + MARGIN;

		Component content = new Component();
		float y = 0;
		String lastSource = null;
		for (final String entry : catalog) {
			String source = enchantMode ? null : Diy.effectSource(entry);
			if (source != null && !source.equals(lastSource)) {
				RenderedTextBlock header = PixelScene.renderTextBlock("— " + source + " —", 7);
				header.hardlight(0xFFFF44);
				header.setPos(0, y + 2);
				header.maxWidth((int) innerWidth);
				content.add(header);
				y = header.bottom() + 2;
				lastSource = source;
			}
			boolean isSelected = selected.contains(entry);
			boolean enchantEntry = enchantMode || Diy.isEnchantEffect(entry);
			String name = enchantEntry ? Diy.enchantName(Diy.enchantEffectClass(entry) != null
					? Diy.enchantEffectClass(entry) : entry) : Diy.effectName(entry);
			boolean owned = enchantEntry
					? Diy.componentCount(DiyComponent.ENCHANT, Diy.enchantEffectClass(entry) != null
							? Diy.enchantEffectClass(entry) : entry) > 0
					: Diy.componentCount(DiyComponent.PASSIVE, entry) > 0;
			String price = owned ? "精粹×1" : "精粹×1（缺）";
			String label = (isSelected ? "【已选】" : "") + name + "　" + price;

			RedButton row = new RedButton(label, 7) {
				@Override
				protected void onClick() {
					if (!selected.remove(entry)) {
						selected.add(entry);
					}
					rebuild();
				}
			};
			row.multiline = true;
			row.setRect(0, y, innerWidth, ROW_HEIGHT);
			content.add(row);
			y += ROW_HEIGHT + ROW_GAP;
		}
		content.setSize(innerWidth, y);

		ScrollPane scroll = new ScrollPane(content);
		add(scroll);

		float doneHeight = BUTTON_HEIGHT;
		float visibleHeight = Math.min(y, Math.max(60f,
				PixelScene.uiCamera.height * 0.55f - pos - doneHeight - 3 * MARGIN));
		scroll.setRect(MARGIN, pos, innerWidth, visibleHeight);

		RedButton done = new RedButton("完成", 8) {
			@Override
			protected void onClick() {
				back();
			}
		};
		done.setRect(MARGIN, pos + visibleHeight + MARGIN, innerWidth, doneHeight);
		add(done);

		resize(width, (int)(pos + visibleHeight + MARGIN + doneHeight + MARGIN));
	}
}
