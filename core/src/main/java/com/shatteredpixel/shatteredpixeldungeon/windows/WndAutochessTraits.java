package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * 羁绊总览（窄屏用）：列出当前已激活的羁绊，点一条看详细。
 */
public class WndAutochessTraits extends Window {

	private static final int MARGIN = 5;
	private static final int GAP = 2;
	private static final int ROW = 16;

	private static final String[] ORDER = {
			AutochessPool.BEAST, AutochessPool.SWARM, AutochessPool.UNDEAD,
			AutochessPool.CONSTRUCT, AutochessPool.DEMON, AutochessPool.HUMANOID,
			AutochessPool.TANK, AutochessPool.ASSASSIN, AutochessPool.RANGED,
			AutochessPool.FIGHTER, AutochessPool.MAGE, AutochessPool.SUPPORT
	};

	public WndAutochessTraits() {
		super();
		chrome.hardlight(DiceMageUI.DARK);
		chrome.hardlight(DiceMageUI.DARK);

		int width = Math.min(170, (int) PixelScene.uiCamera.width - 24);

		AutochessRun run = AutochessRun.get();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get(WndAutochessTraits.class, "title"), 8);
		title.hardlight(0xFFCC66);
		title.setPos(MARGIN, MARGIN);
		add(title);

		float pos = title.bottom() + GAP;

		ArrayList<String> active = new ArrayList<>();
		if (run != null) {
			for (String trait : ORDER) {
				if (AutochessRun.countTrait(trait) > 0) active.add(trait);
			}
		}
		active.sort(Comparator
				.comparingInt((String t) -> AutochessRun.traitTier(t)).reversed()
				.thenComparing(Comparator.comparingInt((String t) -> AutochessRun.countTrait(t)).reversed()));

		if (active.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					Messages.get(WndAutochessTraits.class, "empty"), 7);
			empty.hardlight(0x888888);
			empty.setPos(MARGIN, pos);
			add(empty);
			pos = empty.bottom() + GAP;
		}

		for (String trait : active) {
			int tier = AutochessRun.traitTier(trait);
			int color = tier >= 2 ? 0x66DDFF : (tier == 1 ? 0x66DD66 : 0x888888);
			int[] thresholds = AutochessPool.thresholds(trait);
			int n = AutochessRun.countTrait(trait);
			int goal = n >= thresholds[1] ? thresholds[1] : (n >= thresholds[0] ? thresholds[1] : thresholds[0]);

			String text = Messages.get(AutochessPool.class, "trait_" + trait) + "  " + n + "/" + goal;
			DiceMageUI.DiceButton btn = new DiceMageUI.DiceButton(text, 7) {
				@Override
				protected void onClick() {
					GameScene.show(new WndAutochessTraitList(trait));
				}
			};
			btn.setRect(MARGIN, pos, width - MARGIN * 2, ROW - 2);
			btn.textColor(color);
			add(btn);
			pos += ROW;
		}

		DiceMageUI.DiceButton close = new DiceMageUI.DiceButton(Messages.get(WndAutochessTraits.class, "close"), 8) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos + GAP, width - MARGIN * 2, 16);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(width, (int) Math.ceil(pos));
	}
}
