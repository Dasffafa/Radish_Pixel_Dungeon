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
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

/**
 * 三分钟到点后的结算：清了几波、宰了几只、给个评级，然后一键再来一把。
 */
public class WndArenaResult extends Window {

	private static final int WIDGET_WIDTH = 140;
	private static final int MARGIN = 5;
	private static final int GAP = 4;
	private static final int BUTTON_HEIGHT = 18;

	private static final int COLOR_TITLE = 0xFFFF44;

	public WndArenaResult(int wave, int kills, int totalTurns) {

		super();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				M.L(WndArenaResult.class, "title"), 9);
		title.hardlight(COLOR_TITLE);
		title.setPos(MARGIN, MARGIN);
		add(title);

		RenderedTextBlock stats = PixelScene.renderTextBlock(
				M.L(WndArenaResult.class, "stats", totalTurns / 60, wave, kills), 8);
		stats.maxWidth(WIDGET_WIDTH - MARGIN * 2);
		stats.setPos(MARGIN, title.bottom() + 3);
		add(stats);

		RenderedTextBlock rank = PixelScene.renderTextBlock(
				M.L(WndArenaResult.class, "rank", rankFor(wave)), 8);
		rank.maxWidth(WIDGET_WIDTH - MARGIN * 2);
		rank.hardlight(0xFFC040);
		rank.setPos(MARGIN, stats.bottom() + 3);
		add(rank);

		float pos = rank.bottom() + GAP + 2;

		RedButton again = new RedButton(M.L(WndArenaResult.class, "again"), 8) {
			@Override
			protected void onClick() {
				restart();
			}
		};
		again.setRect(MARGIN, pos, WIDGET_WIDTH - MARGIN * 2, BUTTON_HEIGHT);
		add(again);

		pos = again.bottom() + GAP;

		RedButton quit = new RedButton(M.L(WndArenaResult.class, "quit"), 8) {
			@Override
			protected void onClick() {
				hide();
				ShatteredPixelDungeon.switchNoFade(TitleScene.class);
			}
		};
		quit.setRect(MARGIN, pos, WIDGET_WIDTH - MARGIN * 2, BUTTON_HEIGHT);
		add(quit);

		resize(WIDGET_WIDTH, (int) quit.bottom() + MARGIN);
	}

	private static String rankFor(int wave) {
		if (wave >= 12) return "S";
		if (wave >= 9) return "A";
		if (wave >= 6) return "B";
		return "C";
	}

	/** 重开一把：和选人界面开局走同一条路（hero 置空 → DESCEND 会重新 Dungeon.init）。 */
	private void restart() {
		hide();
		Dungeon.hero = null;
		ActionIndicator.action = null;
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		Game.switchScene(InterlevelScene.class);
	}
}
