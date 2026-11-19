package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 战场格子装饰（云顶风）：给每个可上阵的格子描一圈发光边框，
 * 我方蓝色、敌方红色，让人一眼看出「这是站棋子的格子」。
 */
public class AutochessArenaOverlay extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	private static final int THICK = 1;

	private final ArrayList<ColorBlock> blocks = new ArrayList<>();

	public AutochessArenaOverlay() {
		super();
		for (int i = 0; i < 12; i++) {
			addCellOutline(ChessBoardLevel.playerSlot(i), DiceMageUI.BLUE);
			addCellOutline(ChessBoardLevel.enemySlot(i), DiceMageUI.RED);
		}
		// 备战席也描一圈金边（上下两排）
		for (int i = 0; i < ChessBoardLevel.BENCH_COUNT; i++) {
			addCellOutline(ChessBoardLevel.benchCell(i), DiceMageUI.GOLD);
			addCellOutline(ChessBoardLevel.enemyBenchCell(i), DiceMageUI.GOLD);
		}

		// 中线：一条暗金分隔线
		int midY = (ChessBoardLevel.FIELD_MIN_Y + ChessBoardLevel.FIELD_MAX_Y) / 2;
		for (int y = ChessBoardLevel.FIELD_MIN_Y; y <= ChessBoardLevel.FIELD_MAX_Y; y++) {
			addBlock(7 * CELL, y * CELL + 7, CELL - 1, 1, 0x66B59E09);
		}
	}

	private void addCellOutline(int cell, int color) {
		if (cell < 0) return;
		float x = (cell % ChessBoardLevel.SIZE) * (float) CELL;
		float y = (cell / ChessBoardLevel.SIZE) * (float) CELL;

		int line = 0xAA000000 | (color & 0xFFFFFF);
		addBlock(x, y, CELL - 1, THICK, line);
		addBlock(x, y + CELL - 1 - THICK, CELL - 1, THICK, line);
		addBlock(x, y, THICK, CELL - 1, line);
		addBlock(x + CELL - 1 - THICK, y, THICK, CELL - 1, line);

		// 角上的小点，看起来更像「平台」
		addBlock(x, y, 2, 2, line);
		addBlock(x + CELL - 3, y, 2, 2, line);
		addBlock(x, y + CELL - 3, 2, 2, line);
		addBlock(x + CELL - 3, y + CELL - 3, 2, 2, line);
	}

	private void addBlock(float x, float y, float w, float h, int color) {
		ColorBlock block = new ColorBlock(w, h, color);
		block.x = x;
		block.y = y;
		add(block);
		blocks.add(block);
	}
}
