/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 合成分类
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/**
 * DIY 合成：先选一个大类，再进具体物品列表。
 */
public class WndDiyCraft extends Window {

	private static final int WIDTH_P = 132;
	private static final int WIDTH_L = 152;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;

	public WndDiyCraft() {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock("DIY 合成", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(MARGIN, pos);
		title.maxWidth(width - MARGIN * 2);
		add(title);
		pos = title.bottom() + MARGIN;

		RenderedTextBlock materials = PixelScene.renderTextBlock(6);
		materials.text(Diy.materialsLine(), (int)(width - MARGIN * 2));
		materials.setPos(MARGIN, pos);
		add(materials);
		pos = materials.bottom() + 2;

		RenderedTextBlock components = PixelScene.renderTextBlock(6);
		components.text(Diy.componentsLine(), (int)(width - MARGIN * 2));
		components.setPos(MARGIN, pos);
		add(components);
		pos = components.bottom() + 2 * MARGIN;

		Diy.Group[] groups = Diy.Group.values();

		float buttonWidth = (width - MARGIN * 3) / 2f;
		int col = 0;
		float rowY = pos;
		float bottom = pos;

		for (Diy.Group group : groups) {
			final Diy.Group target = group;

			RedButton button = new RedButton(group.label) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show(new WndDiyCraftList(target));
				}
			};
			button.setRect(MARGIN + col * (buttonWidth + MARGIN), rowY, buttonWidth, BUTTON_HEIGHT);
			add(button);
			bottom = rowY + BUTTON_HEIGHT;

			col++;
			if (col >= 2) {
				col = 0;
				rowY += BUTTON_HEIGHT + MARGIN;
			}
		}

		resize(width, (int)(bottom + MARGIN));
	}
}
