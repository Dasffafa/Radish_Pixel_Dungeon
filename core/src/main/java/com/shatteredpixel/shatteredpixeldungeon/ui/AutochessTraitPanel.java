package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessTraitList;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * 棋盘左侧的羁绊栏（云顶同款）：列出当前已激活的羁绊与人数，
 * 点某一条可以看到这个羁绊都有哪些棋子。
 */
public class AutochessTraitPanel extends Component {

	public static float WIDTH = 66;
	private static final int ROW = 11;
	private static final int MAX_ROWS = 10;

	private static final String[] ORDER = {
			AutochessPool.BEAST, AutochessPool.SWARM, AutochessPool.UNDEAD,
			AutochessPool.CONSTRUCT, AutochessPool.DEMON, AutochessPool.HUMANOID,
			AutochessPool.TANK, AutochessPool.ASSASSIN, AutochessPool.RANGED,
			AutochessPool.FIGHTER, AutochessPool.MAGE, AutochessPool.SUPPORT
	};

	private final ArrayList<Row> rows = new ArrayList<>();
	private int builtVersion = -1;
	private int builtRound = -1;

	public AutochessTraitPanel() {
		super();
		camera = PixelScene.uiCamera;
		setSize(WIDTH, 14);
	}

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null) {
			clearRows();
			return;
		}
		if (run.unitVersion != builtVersion || run.round() != builtRound) {
			builtVersion = run.unitVersion;
			builtRound = run.round();
			rebuild();
		}
	}

	private void rebuild() {
		clearRows();

		RenderedTextBlock title = PixelScene.renderTextBlock("羁绊", 7);
		title.hardlight(0xFFCC66);
		title.setPos(0, 0);
		add(title);

		float pos = 10;

		ArrayList<String> active = new ArrayList<>();
		for (String trait : ORDER) {
			if (AutochessRun.countTrait(trait) > 0) active.add(trait);
		}
		active.sort(Comparator
				.comparingInt((String t) -> AutochessRun.traitTier(t)).reversed()
				.thenComparing(Comparator.comparingInt((String t) -> AutochessRun.countTrait(t)).reversed()));

		int count = 0;
		for (String trait : active) {
			if (count++ >= MAX_ROWS) break;
			Row row = new Row(trait);
			row.setPos(0, pos);
			add(row);
			rows.add(row);
			pos += ROW;
		}

		// 连胜 / 连败单独一行
		AutochessRun run = AutochessRun.get();
		if (run != null) {
			String streak;
			if (run.winStreak() > 0) {
				streak = "连胜 " + run.winStreak() + " · 收入 +" + run.lastIncome();
			} else if (run.loseStreak() > 0) {
				streak = "连败 " + run.loseStreak() + " · 收入 +" + run.lastIncome();
			} else {
				streak = "连胜 0 · 收入 +" + run.lastIncome();
			}
			RoundedFrame box = new RoundedFrame(0xCC1A1A1A, 0xFFCC66);
			box.setRect(0, pos, WIDTH, 10);
			add(box);
			RenderedTextBlock line = PixelScene.renderTextBlock(streak, 6);
			line.hardlight(0xFFCC66);
			line.setPos(2, pos + 2);
			add(line);
			pos += 12;
		}

		if (active.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					"上阵后显示", 6);
			empty.hardlight(0x888888);
			empty.setPos(0, pos);
			add(empty);
			pos += 10;
		}

		setSize(WIDTH, pos);
	}

	private void clearRows() {
		for (Row row : rows) row.destroy();
		rows.clear();
		// 子元素由 Component.destroy 统一清理，这里只清行列表
	}

	/** 一条羁绊：色块 + 名字 + 人数/阈值，点击打开棋子列表。 */
	private static class Row extends Component {

		private final String trait;
		private final RoundedFrame bg;
		private final RenderedTextBlock label;
		private final PointerArea tap;

		Row(String trait) {
			this.trait = trait;

			int tier = AutochessRun.traitTier(trait);
			int color = tier >= 2 ? 0xFF66DDFF : (tier == 1 ? 0xFF66DD66 : 0xFF888888);

			bg = new RoundedFrame(0xCC1A1A1A, color);
			add(bg);

			int[] thresholds = AutochessPool.thresholds(trait);
			int n = AutochessRun.countTrait(trait);
			// 超过第一档后显示下一档的需求
			int goal = n >= thresholds[1] ? thresholds[1] : (n >= thresholds[0] ? thresholds[1] : thresholds[0]);
			String text = Messages.get(AutochessPool.class, "trait_" + trait) + " " + n + "/" + goal;
			label = PixelScene.renderTextBlock(text, 6);
			label.hardlight(color);
			add(label);

			tap = new PointerArea(0, 0, WIDTH, ROW - 1) {
				@Override
				protected void onClick(com.watabou.input.PointerEvent event) {
					GameScene.show(new WndAutochessTraitList(Row.this.trait));
				}
			};
			tap.givePointerPriority();
			add(tap);

			setSize(WIDTH, ROW - 1);
		}

		@Override
		protected void layout() {
			bg.setRect(x, y, WIDTH, ROW - 1);
			label.setPos(x + 2, y + 2);
			label.maxWidth((int) WIDTH - 4);
			tap.x = x;
			tap.y = y;
		}

		void place(float px, float py) {
			this.x = px;
			this.y = py;
			layout();
		}
	}
}
