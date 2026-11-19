package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessBot;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.AutochessIcons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.PointerArea;

import java.util.ArrayList;

/**
 * 对战大厅：列出玩家和所有 AI 的血量/等级/本轮对手，
 * 点某个 AI 可以看它的阵容。
 */
public class WndAutochessLobby extends Window {

	/** 面板宽度：按屏幕宽度自适应。 */
	private final int WIDTH;
	private static final int MARGIN = 5;
	private static final int GAP = 3;
	private static final int ROW = 26;

	public WndAutochessLobby() {
		super();

		int camW = Boolean.getBoolean("arena.compact") ? 150 : (int) PixelScene.uiCamera.width;
		WIDTH = Math.min(200, Math.max(120, camW - 24));

		AutochessRun run = AutochessRun.get();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get(WndAutochessLobby.class, "title", run == null ? 1 : run.round()), 9);
		title.hardlight(0xFFCC66);
		title.setPos(MARGIN, MARGIN);
		add(title);

		float pos = title.bottom() + GAP;

		if (run != null) {
			// 玩家自己
			pos = addRow(pos, Messages.get(WndAutochessLobby.class, "you"),
					run.life(), run.level(), run.gold(), opponentOf(run, "你"), null, false);

			for (AutochessBot bot : run.bots()) {
				if (!bot.alive()) {
					RenderedTextBlock dead = PixelScene.renderTextBlock(
							Messages.get(WndAutochessLobby.class, "dead", bot.name), 7);
					dead.hardlight(0x777777);
					dead.setPos(MARGIN, pos + 2);
					add(dead);
					pos += 14;
					continue;
				}
				pos = addRow(pos, bot.name, bot.hp, bot.level, bot.gold,
						opponentOf(run, bot.name), bot, true);
			}
		}

		RedButton close = new RedButton(Messages.get(WndAutochessLobby.class, "close"), 8) {
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

	private float addRow(float pos, String name, int hp, int level, int gold,
						 String opponent, AutochessBot bot, boolean clickable) {
		CharSprite icon = null;
		if (bot != null) {
			ArrayList<String> units = bot.deployed();
			if (!units.isEmpty()) {
				String cls = units.get(0).split(":")[0];
				icon = AutochessIcons.spriteFor(cls);
			}
		}
		if (icon == null) {
			icon = AutochessIcons.spriteFor("Rat");
		}
		if (icon != null) {
			AutochessIcons.place(icon, MARGIN, pos, 16);
			add(icon);
		}

		String line1 = Messages.get(WndAutochessLobby.class, "line", name, hp, level, gold);
		RenderedTextBlock first = PixelScene.renderTextBlock(line1, 7);
		first.hardlight(bot == null ? 0x66DDFF : 0xFFFFFF);
		first.maxWidth(WIDTH - MARGIN * 2 - 26);
		first.setPos(MARGIN + 20, pos + 1);
		add(first);

		RenderedTextBlock vs = PixelScene.renderTextBlock(opponent, 7);
		vs.hardlight(0xFFCC66);
		vs.maxWidth(WIDTH - MARGIN * 2 - 26);
		vs.setPos(MARGIN + 20, pos + 10);
		add(vs);

		if (clickable && bot != null) {
			final AutochessBot botRef = bot;
			PointerArea area = new PointerArea(0, 0, WIDTH - MARGIN * 2, ROW - 4) {
				@Override
				protected void onClick(com.watabou.input.PointerEvent event) {
					GameScene.show(new WndAutochessOpponent(botRef));
				}
			};
			area.camera = PixelScene.uiCamera;
			area.x = MARGIN;
			area.y = pos;
			add(area);
		}

		return pos + ROW;
	}

	/** 找出某个参与者本轮的对手（含结果）。 */
	private static String opponentOf(AutochessRun run, String who) {
		for (AutochessRun.Match match : run.matches()) {
			if (!who.equals(match.a) && !who.equals(match.b)) continue;
			String other = who.equals(match.a) ? match.b : match.a;
			if (match.winner == null) {
				return Messages.get(WndAutochessLobby.class, "vs", other);
			}
			boolean won = who.equals(match.winner);
			return Messages.get(WndAutochessLobby.class, "vs_result", other, won ? "胜" : "负");
		}
		return Messages.get(WndAutochessLobby.class, "vs_none");
	}
}
