package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessBot;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 右侧对手列表（云顶式）：每个 AI 一行，头像 + 名字 + 血条，点一下看它的阵容。
 */
public class AutochessOpponentList extends Component {

	private static final int WIDTH = 46;
	private static final int ROW = 22;
	private static final int MAX_ROWS = 6;

	private final ArrayList<Component> built = new ArrayList<>();
	private String signature = "";
	private int builtRound = -1;

	public AutochessOpponentList() {
		super();
		camera = PixelScene.uiCamera;
	}

	@Override
	public void update() {
		super.update();
		AutochessRun run = AutochessRun.get();
		if (run == null) {
			clearRows();
			return;
		}
		String sig = signatureOf(run);
		if (!sig.equals(signature) || run.round() != builtRound) {
			signature = sig;
			builtRound = run.round();
			rebuild(run);
		}
	}

	private static String signatureOf(AutochessRun run) {
		StringBuilder sb = new StringBuilder();
		for (AutochessBot bot : run.bots()) {
			sb.append(bot.name).append(bot.hp).append(bot.level).append(bot.alive() ? 1 : 0).append(';');
		}
		return sb.toString();
	}

	private void clearRows() {
		for (Component g : built) g.killAndErase();
		built.clear();
	}

	private void rebuild(AutochessRun run) {
		// 子元素用绝对坐标，这里直接把整列摆到右上角
		float ox = PixelScene.uiCamera == null ? 2f : Math.max(2f, PixelScene.uiCamera.width - 48f);
		float oy = 40f;
		clearRows();
		int i = 0;
		for (AutochessBot bot : run.bots()) {
			if (i >= MAX_ROWS) break;
			buildRow(bot, ox, oy + i * ROW);
			i++;
		}
	}

	private void buildRow(final AutochessBot bot, float x0, float y) {
		Component row = new Component();
		row.camera = PixelScene.uiCamera;

		RoundedFrame bg = new RoundedFrame(bot.alive() ? 0xCC1A1A1A : 0x88202020, 0xFF666666);
		bg.setRect(x0, y, WIDTH, ROW - 2);
		row.add(bg);

		// 头像：它阵容里第一个棋子
		String first = bot.roster.isEmpty() ? null : bot.roster.get(0);
		if (first != null) {
			CharSprite icon = AutochessIcons.spriteFor(AutochessBot.clsOf(first));
			if (icon != null) {
				AutochessIcons.place(icon, x0 + 2, y + 3, 12);
				if (!bot.alive()) icon.alpha(0.4f);
				row.add(icon);
			}
		}

		RenderedTextBlock name = PixelScene.renderTextBlock(bot.name, 5);
		name.hardlight(bot.alive() ? 0xFFFFFF : 0x888888);
		name.maxWidth(WIDTH - 17);
		name.setPos(x0 + 16, y + 1);
		row.add(name);

		// 血条
		float barW = WIDTH - 18;
		ColorBlock barBg = new ColorBlock(barW, 2, 0xFF303030);
		barBg.x = x0 + 16;
		barBg.y = y + 8;
		row.add(barBg);
		ColorBlock barFill = new ColorBlock(barW * Math.max(0f, Math.min(1f, bot.hp / 100f)), 2,
				bot.hp > 50 ? 0xFF66DD66 : (bot.hp > 25 ? 0xFFDDCC44 : 0xFFDD5555));
		barFill.x = x0 + 16;
		barFill.y = barBg.y;
		row.add(barFill);

		RenderedTextBlock info = PixelScene.renderTextBlock(bot.hp + " ｜ Lv" + bot.level, 5);
		info.hardlight(0xAAAAAA);
		info.setPos(x0 + 16, y + 11);
		row.add(info);

		com.watabou.noosa.PointerArea tap = new com.watabou.noosa.PointerArea(0, 0, WIDTH, ROW - 2) {
			@Override
			protected void onClick(com.watabou.input.PointerEvent event) {
				if (bot.alive()) {
					GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessOpponent(bot));
				}
			}
		};
		tap.camera = PixelScene.uiCamera;
		tap.givePointerPriority();
		tap.x = x0;
		tap.y = y;
		tap.height = ROW - 2;
		row.add(tap);

		add(row);
		built.add(row);
	}
}
