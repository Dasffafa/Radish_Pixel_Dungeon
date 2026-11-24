/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 附魔/效果选择器
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * 从全部附魔 / 铭文 / 效果里挑，想选多少选多少。
 *
 * <p>点一行就地切换选中状态，不重建窗口，所以滚动位置不会跳；
 * 「全选」只选已经有对应精粹的条目，避免选完发现材料不够。</p>
 */
public class WndDiyPick extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;
	private static final int ROW_HEIGHT = 17;
	private static final int ROW_GAP = 1;

	private final WndDiyAssemble.State state;
	private final boolean enchantMode;
	private final Item sample;
	private final ArrayList<String> catalog;
	private final LinkedHashSet<String> selected;
	private final LinkedHashMap<String, RedButton> rows = new LinkedHashMap<>();
	private RenderedTextBlock info;
	private int windowWidth = WIDTH_P;

	public WndDiyPick(WndDiyAssemble.State state, boolean enchantMode) {
		super();
		this.state = state;
		this.enchantMode = enchantMode;
		this.sample = safeInstance(state.base);
		this.catalog = enchantMode ? Diy.enchantCatalog(sample) : Diy.passiveCatalog(sample);
		this.selected = enchantMode ? state.enchants : state.passives;
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

	private void back() {
		hide();
		GameScene.show(new WndDiyAssemble(state));
	}

	private static String enchantClassOf(String entry) {
		return Diy.enchantEffectClass(entry) != null ? Diy.enchantEffectClass(entry) : entry;
	}

	private boolean isEnchantEntry(String entry) {
		return enchantMode || Diy.isEnchantEffect(entry);
	}

	private String entryName(String entry) {
		return isEnchantEntry(entry) ? Diy.enchantName(enchantClassOf(entry)) : Diy.effectName(entry);
	}

	private boolean owned(String entry) {
		String cls = enchantClassOf(entry);
		return isEnchantEntry(entry)
				? Diy.componentCount(DiyComponent.ENCHANT, cls) > 0
				: Diy.componentCount(DiyComponent.PASSIVE, entry) > 0;
	}

	private String rowLabel(String entry) {
		String price = owned(entry) ? "精粹×1" : "精粹×1（缺）";
		return (selected.contains(entry) ? "【已选】" : "") + entryName(entry) + "　" + price;
	}

	private void refreshRow(String entry) {
		RedButton row = rows.get(entry);
		if (row != null) {
			row.text(rowLabel(entry));
		}
	}

	private void refreshInfo() {
		if (info == null) return;
		String tail = enchantMode
				? "　魔尘 " + Diy.materialCount(DiyMaterial.ARCANE) + "，附魔精粹 " + Diy.componentTotal(DiyComponent.ENCHANT)
				: "　铁件 " + Diy.materialCount(DiyMaterial.METAL) + "，效果精粹 " + Diy.componentTotal(DiyComponent.PASSIVE);
		info.text("已选 " + selected.size() + " / " + catalog.size() + tail, (int)(windowWidth - MARGIN * 2));
	}

	private void build() {
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		windowWidth = width;
		final float innerWidth = width - MARGIN * 2;

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock(enchantMode ? "选择附魔" : "选择效果", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(MARGIN, pos);
		title.maxWidth((int) innerWidth);
		add(title);
		pos = title.bottom() + MARGIN;

		info = PixelScene.renderTextBlock(6);
		info.text("已选 " + selected.size() + " / " + catalog.size(), (int) innerWidth);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + MARGIN;

		// 全选（只选已有精粹的）/ 清空
		float half = (innerWidth - MARGIN) / 2f;
		RedButton selectAll = new RedButton("全选已有", 6) {
			@Override
			protected void onClick() {
				for (String entry : catalog) {
					if (owned(entry)) {
						selected.add(entry);
						refreshRow(entry);
					}
				}
				refreshInfo();
			}
		};
		selectAll.setRect(MARGIN, pos, half, BUTTON_HEIGHT);
		add(selectAll);

		RedButton clear = new RedButton("清空", 6) {
			@Override
			protected void onClick() {
				for (String entry : new ArrayList<>(selected)) {
					selected.remove(entry);
					refreshRow(entry);
				}
				selected.clear();
				refreshInfo();
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

			RedButton row = new RedButton(rowLabel(entry), 7) {
				@Override
				protected void onClick() {
					// 就地切换，不重建窗口，滚动位置不会丢
					if (!selected.remove(entry)) {
						selected.add(entry);
					}
					refreshRow(entry);
					refreshInfo();
				}
			};
			row.multiline = true;
			row.setRect(0, y, innerWidth, ROW_HEIGHT);
			content.add(row);
			rows.put(entry, row);
			y += ROW_HEIGHT + ROW_GAP;
		}
		content.setSize(innerWidth, y);

		ScrollPane scroll = new ScrollPane(content);
		add(scroll);

		float maxTotal = PixelScene.uiCamera.height * 0.72f;
		float maxVisible = maxTotal - pos - MARGIN * 4 - BUTTON_HEIGHT;
		float visibleHeight = Math.max(40f, Math.min(y, maxVisible));
		scroll.setRect(MARGIN, pos, innerWidth, visibleHeight);

		RedButton done = new RedButton("完成", 8) {
			@Override
			protected void onClick() {
				back();
			}
		};
		done.setRect(MARGIN, pos + visibleHeight + MARGIN, innerWidth, BUTTON_HEIGHT);
		add(done);

		resize(width, (int) Math.min(pos + visibleHeight + MARGIN + BUTTON_HEIGHT + MARGIN,
				PixelScene.uiCamera.height - MARGIN));
	}
}
