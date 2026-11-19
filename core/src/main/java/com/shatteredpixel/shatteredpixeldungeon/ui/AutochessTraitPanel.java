package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessTraitList;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 左侧羁绊图标列（云顶式）：每个已激活的羁绊一个小方块，颜色代表档位，
 * 上面写羁绊名首字和人数，点一下打开详细效果。
 */
public class AutochessTraitPanel extends Component {

	private static final int CELL = 16;
	private static final int GAP = 2;
	private static final int COLS = 2;
	private static final int MAX = 12;

	private static final String[] ORDER = {
			AutochessPool.BEAST, AutochessPool.SWARM, AutochessPool.UNDEAD,
			AutochessPool.CONSTRUCT, AutochessPool.DEMON, AutochessPool.HUMANOID,
			AutochessPool.TANK, AutochessPool.ASSASSIN, AutochessPool.RANGED,
			AutochessPool.FIGHTER, AutochessPool.MAGE, AutochessPool.SUPPORT
	};

	private final ArrayList<RoundedFrame> frames = new ArrayList<>();
	private final ArrayList<RenderedTextBlock> texts = new ArrayList<>();
	private final ArrayList<com.watabou.noosa.PointerArea> taps = new ArrayList<>();
	private int builtVersion = -1;
	private int builtRound = -1;

	public AutochessTraitPanel() {
		super();
		camera = PixelScene.uiCamera;
		setSize(COLS * (CELL + GAP), MAX / COLS * (CELL + GAP));
	}

	@Override
	public void update() {
		super.update();
		AutochessRun run = AutochessRun.get();
		if (run == null) {
			clearCells();
			return;
		}
		if (run.unitVersion != builtVersion || run.round() != builtRound) {
			builtVersion = run.unitVersion;
			builtRound = run.round();
			rebuild();
		}
	}

	private void clearCells() {
		for (RoundedFrame f : frames) f.killAndErase();
		for (RenderedTextBlock t : texts) t.killAndErase();
		for (com.watabou.noosa.PointerArea p : taps) p.killAndErase();
		frames.clear();
		texts.clear();
		taps.clear();
	}

	private void rebuild() {
		clearCells();
		int i = 0;
		for (String trait : ORDER) {
			if (i >= MAX) break;
			int count = AutochessRun.countTrait(trait);
			if (count <= 0) continue;
			addCell(trait, count, i % COLS, i / COLS);
			i++;
		}
		// 没有任何羁绊时给一点提示，免得左侧一片空白

	}

	// 子元素用绝对坐标，面板自己贴左上角
	private static final float BASE_X = 4f;
	private static final float BASE_Y = 42f;

	private void addCell(final String trait, int count, int col, int row) {
		final float x = BASE_X + col * (CELL + GAP);
		final float y = BASE_Y + row * (CELL + GAP);

		int tier = AutochessRun.traitTier(trait);
		int border = tier >= 2 ? 0xFFFFCC33 : (tier == 1 ? 0xFFBFC7D5 : 0xFF666666);

		RoundedFrame bg = new RoundedFrame(0xEE09070B, border);
		bg.setRect(x, y, CELL, CELL);
		add(bg);

		String name = Messages.get(AutochessPool.class, "trait_" + trait);
		String shortName = name.length() > 1 ? name.substring(0, 1) : name;

		RenderedTextBlock label = PixelScene.renderTextBlock(shortName, 6);
		label.hardlight(tier >= 2 ? 0xFFFFCC33 : 0xFFFFFF);
		label.setPos(x + (CELL - label.width()) / 2f, y + 1);
		add(label);

		RenderedTextBlock num = PixelScene.renderTextBlock("×" + count, 5);
		num.hardlight(0xFF99CCFF);
		num.setPos(x + (CELL - num.width()) / 2f, y + 9);
		add(num);

		com.watabou.noosa.PointerArea tap = new com.watabou.noosa.PointerArea(0, 0, CELL, CELL) {
			@Override
			protected void onClick(com.watabou.input.PointerEvent event) {
				GameScene.show(new WndAutochessTraitList(trait));
			}
		};
		tap.camera = PixelScene.uiCamera;
		tap.givePointerPriority();
		tap.x = x;
		tap.y = y;
		add(tap);

		frames.add(bg);
		texts.add(label);
		texts.add(num);
		taps.add(tap);
	}
}
