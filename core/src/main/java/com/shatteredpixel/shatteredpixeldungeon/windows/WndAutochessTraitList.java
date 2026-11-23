package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.AutochessIcons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

import java.util.List;

/**
 * 某个羁绊的棋子一览：点开羁绊栏里的条目就能看到该羁绊有哪些怪，
 * 点怪可以继续查看详情。
 */
public class WndAutochessTraitList extends Window {

	private static final int MARGIN = 5;
	private static final int GAP = 3;

	/** 以下按屏幕宽度自适应 */
	private final int WIDTH;
	private final int COL_W;
	private final int ROW_H;
	private final int COLUMNS;
	private final int ICON;

	public WndAutochessTraitList(String trait) {
		super();
		chrome.hardlight(DiceMageUI.DARK);
		chrome.hardlight(DiceMageUI.DARK);

		WIDTH = Math.min(180, Math.max(120, (int) PixelScene.uiCamera.width - 24));
		COLUMNS = WIDTH < 150 ? 2 : 3;
		COL_W = (WIDTH - MARGIN * 2) / COLUMNS;
		ROW_H = WIDTH < 150 ? 26 : 34;
		ICON = WIDTH < 150 ? 12 : 16;

		int[] thresholds = AutochessPool.thresholds(trait);
		String titleText = Messages.get(AutochessPool.class, "trait_" + trait);
		RenderedTextBlock title = PixelScene.renderTextBlock(titleText, 9);
		title.hardlight(0x66DDFF);
		title.setPos(MARGIN, MARGIN);
		add(title);

		float pos = title.bottom() + GAP;

		RenderedTextBlock info = PixelScene.renderTextBlock(
				Messages.get(WndAutochessTraitList.class, "info", thresholds[0], thresholds[1]), 7);
		info.hardlight(0xAAAAAA);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + 1;

		RenderedTextBlock effect = PixelScene.renderTextBlock(AutochessPool.effectFor(trait), 7);
		effect.maxWidth(WIDTH - MARGIN * 2);
		effect.hardlight(0x66DDFF);
		effect.setPos(MARGIN, pos);
		add(effect);
		pos = effect.bottom() + GAP * 2;

		List<AutochessPool.Def> units = AutochessPool.unitsWithTrait(trait);

		int col = 0;
		for (AutochessPool.Def def : units) {
			float x = MARGIN + col * COL_W;
			float y = pos;

			CharSprite icon = AutochessIcons.spriteFor(def.cls.getSimpleName());
			if (icon != null) {
				AutochessIcons.place(icon, x, y, ICON);
				add(icon);
			}

			RenderedTextBlock name = PixelScene.renderTextBlock(nameOf(def), 6);
			name.maxWidth(COL_W - 4);
			name.hardlight(costColor(def.cost));
			name.setPos(x + ICON + 2, y + 1);
			add(name);

			RenderedTextBlock cost = PixelScene.renderTextBlock(def.cost + "费", 6);
			cost.hardlight(0x999999);
			cost.setPos(x + ICON + 2, y + (ICON < 14 ? 7 : 9));
			add(cost);

			com.watabou.noosa.PointerArea tap = new com.watabou.noosa.PointerArea(0, 0, COL_W - 2, 16) {
				@Override
				protected void onClick(com.watabou.input.PointerEvent event) {
					com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
							new WndAutochessInfo(def));
				}
			};
			tap.x = x;
			tap.y = y;
			add(tap);

			col++;
			if (col >= COLUMNS) {
				col = 0;
				pos += ROW_H;
			}
		}
		if (col > 0) pos += ROW_H;

		DiceMageUI.DiceButton close = new DiceMageUI.DiceButton(Messages.get(WndAutochessTraitList.class, "close"), 8) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos + GAP, WIDTH - MARGIN * 2, 16);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(WIDTH, (int) Math.ceil(pos));
	}

	private static String nameOf(AutochessPool.Def def) {
		try {
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob =
					com.watabou.utils.Reflection.newInstance(def.cls);
			if (mob != null) return mob.name();
		} catch (Exception ignored) {
		}
		return def.cls.getSimpleName();
	}

	private static int costColor(int cost) {
		switch (cost) {
			case 1: return 0xCCCCCC;
			case 2: return 0x66DD66;
			case 3: return 0x66AAFF;
			case 4: return 0xCC77FF;
			default: return 0xFFAA33;
		}
	}
}
