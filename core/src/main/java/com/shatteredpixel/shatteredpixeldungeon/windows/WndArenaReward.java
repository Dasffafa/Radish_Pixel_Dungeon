package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.challenges.ArenaRun;
import com.shatteredpixel.shatteredpixeldungeon.challenges.ArenaUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

/**
 * 清完一波之后的三选一强化窗。
 *
 * <p>故意做成「先看名字和说明，再点按钮选」，而不是三个按钮直接带说明：
 * 手机上窄，按钮里塞不下两行说明，选错了没得反悔。</p>
 */
public class WndArenaReward extends Window {

	private static final int WIDGET_WIDTH = 140;
	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 16;
	private static final int GAP = 3;

	private static final int COLOR_TITLE = 0xFFFF44;
	private static final int COLOR_NAME = 0xFFC040;

	public WndArenaReward(int wave, ArenaUpgrade[] options) {

		super();

		Hero hero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
		int kills = ArenaRun.hudKills();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				M.L(WndArenaReward.class, "title", wave), 9);
		title.hardlight(COLOR_TITLE);
		title.setPos(MARGIN, MARGIN);
		add(title);

		RenderedTextBlock subtitle = PixelScene.renderTextBlock(
				M.L(WndArenaReward.class, "subtitle"), 7);
		subtitle.maxWidth(WIDGET_WIDTH - MARGIN * 2);
		subtitle.setPos(MARGIN, title.bottom() + 2);
		add(subtitle);

		float pos = subtitle.bottom() + GAP + 2;
		boolean first = true;

		for (final ArenaUpgrade upgrade : options) {

			if (!first) {
				// 分隔线，省得三块内容糊成一片
				pos += GAP;
			}
			first = false;

			RenderedTextBlock name = PixelScene.renderTextBlock(upgrade.title(), 8);
			name.hardlight(COLOR_NAME);
			name.setPos(MARGIN + 1, pos);
			add(name);

			RenderedTextBlock desc = PixelScene.renderTextBlock(upgrade.desc(), 7);
			desc.maxWidth(WIDGET_WIDTH - MARGIN * 2 - 2);
			desc.setPos(MARGIN + 1, name.bottom() + 1);
			add(desc);

			RedButton button = new RedButton(M.L(WndArenaReward.class, "choose"), 8) {
				@Override
				protected void onClick() {
					ArenaRun run = ArenaRun.get();
					if (run != null) {
						run.choose(upgrade);
					}
					hide();
				}
			};
			button.setRect(MARGIN, desc.bottom() + 2, WIDGET_WIDTH - MARGIN * 2, BUTTON_HEIGHT);
			add(button);

			pos = button.bottom();
		}

		// 底部留一行当前状态，顺便提醒还剩多少血
		RenderedTextBlock footer = PixelScene.renderTextBlock(
				M.L(WndArenaReward.class, "footer", kills,
						hero == null ? 0 : hero.HP, hero == null ? 0 : hero.HT), 7);
		footer.maxWidth(WIDGET_WIDTH - MARGIN * 2);
		footer.alpha(0.7f);
		footer.setPos(MARGIN, pos + 4);
		add(footer);

		resize(WIDGET_WIDTH, (int) footer.bottom() + MARGIN);
	}
}
