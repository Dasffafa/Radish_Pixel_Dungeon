package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessItems;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RoundedFrame;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;

import java.util.ArrayList;

/**
 * 装备背包（云顶风图标网格）：点一件装备，装到当前选中的棋子上。
 */
public class WndAutochessItems extends Window {

	private static final int MARGIN = 4;
	private static final int GAP = 3;

	private final int WIDTH;
	private final int CELL_W;
	private final int COLUMNS;

	public WndAutochessItems() {
		super();

		chrome.hardlight(DiceMageUI.DARK);

		int camW = PixelScene.uiCamera == null ? 160 : (int) PixelScene.uiCamera.width;
		WIDTH = Math.min(176, Math.max(120, camW - 20));
		COLUMNS = WIDTH < 150 ? 4 : 5;
		CELL_W = (WIDTH - MARGIN * 2 - GAP * (COLUMNS - 1)) / COLUMNS;

		AutochessRun run = AutochessRun.get();
		if (run == null) {
			hide();
			return;
		}

		RenderedTextBlock title = PixelScene.renderTextBlock(
				M.L(WndAutochessItems.class, "title"), 8);
		title.hardlight(DiceMageUI.GOLD);
		title.setPos(MARGIN, MARGIN);
		add(title);

		RenderedTextBlock hint = PixelScene.renderTextBlock(
				M.L(WndAutochessItems.class, "hint"), 5);
		hint.hardlight(DiceMageUI.CREAM);
		hint.maxWidth(WIDTH - MARGIN * 2);
		hint.setPos(MARGIN, title.bottom() + 2);
		add(hint);

		float pos = hint.bottom() + 3;

		ArrayList<String> bag = new ArrayList<>(run.itemBag());
		if (bag.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					M.L(WndAutochessItems.class, "empty"), 6);
			empty.hardlight(DiceMageUI.GREY_LINE);
			empty.setPos(MARGIN, pos);
			add(empty);
			pos = empty.bottom() + 2;
		}

		for (int i = 0; i < bag.size(); i++) {
			final String id = bag.get(i);
			int col = i % COLUMNS;
			int row = i / COLUMNS;
			float x = MARGIN + col * (CELL_W + GAP);
			float y = pos + row * (CELL_W + 12);

			RoundedFrame cell = new RoundedFrame(0xEE09070B, DiceMageUI.GREY_LINE);
			cell.setRect(x, y, CELL_W, CELL_W + 11);
			add(cell);

			Item item = AutochessItems.item(id);
			if (item != null) {
				ItemSprite icon = new ItemSprite(item);
				float scale = Math.min(1f, (CELL_W - 2) / 16f);
				icon.scale.set(scale);
				icon.x = x + (CELL_W - 16 * scale) / 2f;
				icon.y = y + 1;
				add(icon);
			}

			String nm = AutochessItems.name(id);
			if (nm.length() > 4) nm = nm.substring(0, 4);
			RenderedTextBlock name = PixelScene.renderTextBlock(nm, 5);
			name.hardlight(DiceMageUI.CREAM);
			name.setPos(x + (CELL_W - name.width()) / 2f, y + CELL_W + 1);
			add(name);

			PointerArea tap = new PointerArea(0, 0, CELL_W, CELL_W + 11) {
				@Override
				protected void onClick(com.watabou.input.PointerEvent event) {
					AutochessRun current = AutochessRun.get();
					if (current == null) return;
					if (current.selectedSide < 0 || current.selectedIndex < 0) {
						com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
								M.L(WndAutochessItems.class, "no_selection"));
						return;
					}
					if (current.equipItem(current.selectedSide, current.selectedIndex, id)) {
						hide();
						com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
								new WndAutochessItems());
					}
				}
			};
			tap.camera = PixelScene.uiCamera;
			tap.givePointerPriority();
			tap.x = x;
			tap.y = y;
			tap.width = CELL_W;
			tap.height = CELL_W + 11;
			add(tap);
		}

		int rows = (bag.size() + COLUMNS - 1) / COLUMNS;
		if (rows > 0) pos += rows * (CELL_W + 12) + 1;

		DiceMageUI.DiceButton close = new DiceMageUI.DiceButton(
				M.L(WndAutochessItems.class, "close"), 7) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 14);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(WIDTH, (int) Math.ceil(pos));
	}
}
