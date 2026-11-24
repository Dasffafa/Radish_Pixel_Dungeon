/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 合成列表
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
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

/**
 * DIY 合成：某个大类的可选物品列表，每行写着要花多少材料。
 * 材料不够的行是灰的，点不动。
 */
public class WndDiyCraftList extends Window {

	private static final int WIDTH_P = 132;
	private static final int WIDTH_L = 152;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;

	private final Diy.Group group;

	public WndDiyCraftList(Diy.Group group) {
		super();

		this.group = group;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock("合成 · " + group.label, 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(MARGIN, pos);
		title.maxWidth(width - MARGIN * 2);
		add(title);
		pos = title.bottom() + MARGIN;

		RenderedTextBlock materials = PixelScene.renderTextBlock(6);
		materials.text(Diy.materialsLine(), width - MARGIN * 2);
		materials.setPos(MARGIN, pos);
		add(materials);
		pos = materials.bottom() + 2 * MARGIN;

		ArrayList<Item> samples = Diy.craftableSamples(group);

		Component content = new Component();
		float y = 0;
		for (Item sample : samples) {
			final Class<? extends Item> cls = sample.getClass();
			final int kind = Diy.kindOf(sample);
			final int cost = Diy.costOf(sample);

			RedButton button = new RedButton(sample.name() + "   " + Diy.materialName(kind) + "×" + cost, 7) {
				@Override
				protected void onClick() {
					openAssemble(cls);
				}
			};
			button.multiline = true;
			button.setRect(0, y, width - MARGIN * 2, BUTTON_HEIGHT);
			button.enable(Diy.canAfford(kind, cost));
			content.add(button);

			y += BUTTON_HEIGHT + 1;
		}

		if (samples.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock("这里没有能合成的东西。", 6);
			empty.setPos(0, 0);
			empty.maxWidth(width - MARGIN * 2);
			content.add(empty);
			y = empty.height() + MARGIN;
		}

		content.setSize(width - MARGIN * 2, y);

		ScrollPane scroll = new ScrollPane(content);
		add(scroll);

		float visibleHeight = Math.min(y,
				Math.max(48f, PixelScene.uiCamera.height * 0.72f - pos));
		scroll.setRect(MARGIN, pos, width - MARGIN * 2, visibleHeight);

		resize(width, (int)(pos + visibleHeight + MARGIN));
	}

	/** 选好底材就进组装窗口，自己挑强化/附魔/特性。 */
	private void openAssemble(final Class<? extends Item> cls) {
		hide();
		GameScene.show(new WndDiyAssemble(group, cls));
	}
}
