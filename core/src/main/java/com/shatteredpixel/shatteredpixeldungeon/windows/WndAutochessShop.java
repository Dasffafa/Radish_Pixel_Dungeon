package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessUnitBuff;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RoundedFrame;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Reflection;

/**
 * 自走棋的备战界面：金币、三个可买的单位、羁绊统计、刷新与开战。
 *
 * <p>买完会整窗重建一次，界面数据始终从 {@link AutochessRun} 现取，不做局部刷新。</p>
 */
public class WndAutochessShop extends Window {

	private static final int WIDTH = 150;
	private static final int MARGIN = 5;
	private static final int ROW_HEIGHT = 27;
	private static final int GAP = 3;

	private static final int COLOR_TITLE = 0xFFFF44;
	private static final int COLOR_GOLD = 0xFFD700;
	private static final int COLOR_DIM = 0xAAAAAA;

	public WndAutochessShop(int round) {

		super();

		AutochessRun run = AutochessRun.get();
		if (run == null) {
			hide();
			return;
		}

		RenderedTextBlock title = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "title", round), 9);
		title.hardlight(COLOR_TITLE);
		title.setPos(MARGIN, MARGIN);
		add(title);

		RenderedTextBlock gold = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "gold", run.gold(), run.life(), run.rosterSize()), 8);
		gold.hardlight(COLOR_GOLD);
		gold.setPos(MARGIN, title.bottom() + 2);
		add(gold);

		float pos = gold.bottom() + GAP + 1;

		// 备战位：8 个格子，2 行 4 列，第 i 格就是棋盘上的第 i 个站位
		RenderedTextBlock benchTitle = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "bench", run.rosterSize()), 7);
		benchTitle.hardlight(0x99CCFF);
		benchTitle.setPos(MARGIN, pos);
		add(benchTitle);
		pos = benchTitle.bottom() + 2;
		pos = addBenchGrid(run, pos);

		for (int i = 0; i < 3; i++) {
			pos = addOfferRow(run, i, pos);
		}

		// 羁绊统计
		RenderedTextBlock traits = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "traits",
						traitLine(), alliesLine()), 7);
		traits.maxWidth(WIDTH - MARGIN * 2);
		traits.setPos(MARGIN, pos + 2);
		traits.hardlight(COLOR_DIM);
		add(traits);

		pos = traits.bottom() + GAP + 1;

		RedButton refresh = new RedButton(
				M.L(WndAutochessShop.class, "refresh", AutochessRun.REFRESH_COST), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.refresh()) {
					hide();
					GameScene.show(new WndAutochessShop(current.round()));
				}
			}
		};
		refresh.setRect(MARGIN, pos, (WIDTH - MARGIN * 2 - GAP) / 2f, 18);
		add(refresh);

		RedButton fight = new RedButton(M.L(WndAutochessShop.class, "fight"), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				hide();
				if (current != null) {
					current.startFight();
				}
			}
		};
		fight.setRect(refresh.right() + GAP, pos, (WIDTH - MARGIN * 2 - GAP) / 2f, 18);
		add(fight);

		resize(WIDTH, (int) fight.bottom() + MARGIN);
	}

	private static final float CELL_W = 33f;
	private static final float CELL_H = 22f;
	private static final int CELL_GAP = 2;

	/** 画 4×2 的备战位网格。 */
	private float addBenchGrid(AutochessRun run, float pos) {
		for (int i = 0; i < AutochessRun.MAX_UNITS; i++) {
			int col = i % 4;
			int row = i / 4;
			float cx = MARGIN + col * (CELL_W + CELL_GAP);
			float cy = pos + row * (CELL_H + CELL_GAP);

			RoundedFrame frame = new RoundedFrame(0xCC1A1A1A, 0x55FFFF66);
			frame.setRect(cx, cy, CELL_W, CELL_H);
			add(frame);

			String name = run.benchName(i);
			if (name == null) {
				continue;
			}
			RenderedTextBlock label = PixelScene.renderTextBlock(name, 6);
			label.maxWidth((int) CELL_W - 4);
			label.setPos(cx + 2, cy + 2);
			add(label);

			String stars = stars(run.benchStar(i));
			RenderedTextBlock starLabel = PixelScene.renderTextBlock(stars, 6);
			starLabel.hardlight(0xFFD700);
			starLabel.setPos(cx + 2, cy + CELL_H - 9);
			add(starLabel);
		}
		return pos + 2 * CELL_H + CELL_GAP + 3;
	}

	private static String stars(int star) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < star; i++) {
			sb.append("★");
		}
		return sb.toString();
	}

	private float addOfferRow(AutochessRun run, int index, float pos) {

		AutochessPool.Def def = run.offer(index);

		if (def == null) {
			// 这一格买过了：只画一行「已购买」，占同样的行高，窗口才不会跳
			RenderedTextBlock sold = PixelScene.renderTextBlock(M.L(WndAutochessShop.class, "sold"), 7);
			sold.hardlight(COLOR_DIM);
			sold.setPos(MARGIN, pos + 9);
			add(sold);
			return pos + ROW_HEIGHT;
		}

		RenderedTextBlock name = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "row", unitName(def), priceOf(def), traitName(def.trait)), 8);
		name.maxWidth(WIDTH - MARGIN * 2);
		name.setPos(MARGIN, pos + 1);
		add(name);

		// 买过的格子也占同样高度，只把按钮换成「已购买」，
		// 否则窗口每买一次就变矮、整窗重新居中，按钮会跳位置
		if (def != null) {
			final int idx = index;
			RedButton buy = new RedButton(M.L(WndAutochessShop.class, "buy"), 7) {
				@Override
				protected void onClick() {
					AutochessRun current = AutochessRun.get();
					if (current != null && current.buy(idx)) {
						hide();
						GameScene.show(new WndAutochessShop(current.round()));
					}
				}
			};
			buy.active = run.gold() >= priceOf(def);
			buy.setRect(MARGIN, pos + 12, 60, 14);
			add(buy);
		}

		return pos + ROW_HEIGHT;
	}

	private static int priceOf(AutochessPool.Def def) {
		return def.cost;
	}

	private static String unitName(AutochessPool.Def def) {
		Mob mob = Reflection.newInstance(def.cls);
		return mob == null ? def.cls.getSimpleName() : mob.name();
	}

	private static String traitName(String trait) {
		return M.L(AutochessPool.class, "trait_" + trait);
	}

	private static String traitLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(traitName(AutochessPool.BEAST)).append(" ").append(AutochessRun.countTrait(AutochessPool.BEAST));
		sb.append("  ·  ").append(traitName(AutochessPool.HUMANOID)).append(" ").append(AutochessRun.countTrait(AutochessPool.HUMANOID));
		sb.append("  ·  ").append(traitName(AutochessPool.CONSTRUCT)).append(" ").append(AutochessRun.countTrait(AutochessPool.CONSTRUCT));
		sb.append("  ·  ").append(traitName(AutochessPool.DEMON)).append(" ").append(AutochessRun.countTrait(AutochessPool.DEMON));
		return sb.toString();
	}

	private static String alliesLine() {
		AutochessRun run = AutochessRun.get();
		return M.L(WndAutochessShop.class, "allies", run == null ? 0 : run.rosterSize());
	}
}
