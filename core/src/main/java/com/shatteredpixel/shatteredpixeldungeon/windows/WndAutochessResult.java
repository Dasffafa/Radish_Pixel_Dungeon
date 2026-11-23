package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

/**
 * 自走棋的结算：撑过多少回合、留下多少金币、给个评级，然后可以再来一把。
 */
public class WndAutochessResult extends Window {

	private static final int WIDTH = 140;
	private static final int MARGIN = 5;
	private static final int GAP = 4;

	public WndAutochessResult(int round, int gold) {

		super();
		chrome.hardlight(DiceMageUI.DARK);
		chrome.hardlight(DiceMageUI.DARK);

		RenderedTextBlock title = PixelScene.renderTextBlock(
				M.L(WndAutochessResult.class, "title"), 9);
		title.hardlight(0xFFFF44);
		title.setPos(MARGIN, MARGIN);
		add(title);

		RenderedTextBlock stats = PixelScene.renderTextBlock(
				M.L(WndAutochessResult.class, "stats", round, gold), 8);
		stats.maxWidth(WIDTH - MARGIN * 2);
		stats.setPos(MARGIN, title.bottom() + 3);
		add(stats);

		RenderedTextBlock rank = PixelScene.renderTextBlock(
				M.L(WndAutochessResult.class, "rank", rankFor(round)), 8);
		rank.hardlight(0xFFC040);
		rank.setPos(MARGIN, stats.bottom() + 3);
		add(rank);

		float pos = rank.bottom() + GAP + 2;

		DiceMageUI.DiceButton again = new DiceMageUI.DiceButton(M.L(WndAutochessResult.class, "again"), 8) {
			@Override
			protected void onClick() {
				hide();
				// 从「继续游戏」进来时 selectedClass 可能是空的，
				// 直接重开会在 Dungeon.init() 里 NPE，这里补上
				if (com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass == null) {
					com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass =
							Dungeon.hero != null && Dungeon.hero.heroClass != null
									? Dungeon.hero.heroClass
									: com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses.WARRIOR;
				}
				Dungeon.hero = null;
				ActionIndicator.action = null;
				InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
				Game.switchScene(InterlevelScene.class);
			}
		};
		again.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 18);
		add(again);

		pos = again.bottom() + GAP;

		DiceMageUI.DiceButton quit = new DiceMageUI.DiceButton(M.L(WndAutochessResult.class, "quit"), 8) {
			@Override
			protected void onClick() {
				hide();
				ShatteredPixelDungeon.switchNoFade(TitleScene.class);
			}
		};
		quit.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 18);
		add(quit);

		resize(WIDTH, (int) quit.bottom() + MARGIN);
	}

	private static String rankFor(int round) {
		if (round >= 15) return "S";
		if (round >= 10) return "A";
		if (round >= 6) return "B";
		return "C";
	}
}
