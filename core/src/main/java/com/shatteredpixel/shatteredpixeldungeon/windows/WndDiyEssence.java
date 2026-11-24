/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 精粹仓库查看窗口
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyComponent;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyMaterial;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查看工作台仓库里的精粹：强化精华 / 附魔精粹 / 效果精粹（按来源分组）。
 */
public class WndDiyEssence extends Window {

	private static final int WIDTH_P = 150;
	private static final int WIDTH_L = 172;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;

	public WndDiyEssence() {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		final float innerWidth = width - MARGIN * 2;

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock("工作台仓库", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(MARGIN, pos);
		title.maxWidth((int) innerWidth);
		add(title);
		pos = title.bottom() + MARGIN;

		int levels = Diy.componentTotal(DiyComponent.LEVEL);
		int enchants = Diy.componentTotal(DiyComponent.ENCHANT);
		int passives = Diy.componentTotal(DiyComponent.PASSIVE);

		RenderedTextBlock summary = PixelScene.renderTextBlock(6);
		summary.text("强化 " + levels + " 份　附魔 " + enchants + " 份　效果 " + passives + " 份", (int) innerWidth);
		summary.setPos(MARGIN, pos);
		add(summary);
		pos = summary.bottom() + MARGIN;

		RenderedTextBlock materials = PixelScene.renderTextBlock(6);
		materials.text("铁件 " + Diy.materialCount(DiyMaterial.METAL)
				+ "　魔尘 " + Diy.materialCount(DiyMaterial.ARCANE)
				+ "　杂料 " + Diy.materialCount(DiyMaterial.MISC), (int) innerWidth);
		materials.setPos(MARGIN, pos);
		add(materials);
		pos = materials.bottom() + MARGIN;

		Component content = new Component();
		float y = 0;

		// ---- 基础材料 ----
		y = addHeader(content, y, innerWidth, "基础材料");
		y = addRow(content, y, innerWidth, Diy.materialName(DiyMaterial.METAL),
				Diy.materialCount(DiyMaterial.METAL));
		y = addRow(content, y, innerWidth, Diy.materialName(DiyMaterial.ARCANE),
				Diy.materialCount(DiyMaterial.ARCANE));
		y = addRow(content, y, innerWidth, Diy.materialName(DiyMaterial.MISC),
				Diy.materialCount(DiyMaterial.MISC));
		y += 2;

		// ---- 强化精华 ----
		y = addHeader(content, y, innerWidth, "强化精华");
		y = addRow(content, y, innerWidth, "强化精华", levels);
		y += 2;

		// ---- 附魔精粹 ----
		LinkedHashMap<String, Integer> enchantStacks = Diy.componentStacks(DiyComponent.ENCHANT);
		y = addHeader(content, y, innerWidth,
				"附魔精粹（" + enchantStacks.size() + " 种 / 共 " + enchants + " 份）");
		if (enchantStacks.isEmpty()) {
			y = addNote(content, y, innerWidth, "还没有附魔精粹，拆解带附魔/铭文的装备可以获得。");
		} else {
			ArrayList<String> sorted = new ArrayList<>(enchantStacks.keySet());
			sorted.sort((a, b) -> Diy.enchantName(a).compareTo(Diy.enchantName(b)));
			for (String cls : sorted) {
				y = addRow(content, y, innerWidth, Diy.enchantName(cls) + "精粹", enchantStacks.get(cls));
			}
		}
		y += 2;

		// ---- 效果精粹（按来源分组）----
		LinkedHashMap<String, Integer> passiveStacks = Diy.componentStacks(DiyComponent.PASSIVE);
		y = addHeader(content, y, innerWidth,
				"效果精粹（" + passiveStacks.size() + " 种 / 共 " + passives + " 份）");
		if (passiveStacks.isEmpty()) {
			y = addNote(content, y, innerWidth, "还没有效果精粹，拆解对应类别的物品可以获得。");
		} else {
			for (String source : Diy.EFFECT_SOURCES) {
				ArrayList<String> ids = new ArrayList<>();
				for (String id : passiveStacks.keySet()) {
					if (source.equals(Diy.effectSource(id))) ids.add(id);
				}
				if (ids.isEmpty()) continue;
				ids.sort((a, b) -> Diy.effectName(a).compareTo(Diy.effectName(b)));
				y = addSubHeader(content, y, innerWidth, "— " + source + " —");
				for (String id : ids) {
					y = addRow(content, y, innerWidth, Diy.effectName(id) + "精粹", passiveStacks.get(id));
				}
			}
		}

		content.setSize(innerWidth, y);

		ScrollPane scroll = new ScrollPane(content);
		add(scroll);

		float visibleHeight = Math.min(y, Math.max(60f,
				PixelScene.uiCamera.height * 0.55f - pos - BUTTON_HEIGHT - 3 * MARGIN));
		scroll.setRect(MARGIN, pos, innerWidth, visibleHeight);

		RedButton close = new RedButton("关闭", 8) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos + visibleHeight + MARGIN, innerWidth, BUTTON_HEIGHT);
		add(close);

		resize(width, (int) (pos + visibleHeight + MARGIN + BUTTON_HEIGHT + MARGIN));
	}

	private float addHeader(Component content, float y, float width, String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 8);
		block.hardlight(0xFFFF44);
		block.setPos(0, y);
		block.maxWidth((int) width);
		content.add(block);
		return y + block.height() + 2;
	}

	private float addSubHeader(Component content, float y, float width, String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 7);
		block.hardlight(0xBBBBBB);
		block.setPos(0, y + 1);
		block.maxWidth((int) width);
		content.add(block);
		return y + block.height() + 2;
	}

	private float addRow(Component content, float y, float width, String name, int count) {
		RenderedTextBlock block = PixelScene.renderTextBlock(name + "　×" + count, 7);
		block.setPos(0, y);
		block.maxWidth((int) width);
		content.add(block);
		return y + block.height() + 2;
	}

	private float addNote(Component content, float y, float width, String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.hardlight(0x888888);
		block.setPos(0, y);
		block.maxWidth((int) width);
		content.add(block);
		return y + block.height() + 2;
	}
}
