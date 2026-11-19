package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

/**
 * 战场平台装饰（云顶风）。
 *
 * <p>为了不挡住棋子（贴图是站在格子的下半部分），平台只画在格子底部：
 * 一条半透明的「地基」+ 1px 边框。另外给整个战场铺一层很淡的网格，
 * 让棋盘看起来有格子感。</p>
 */
public class AutochessArenaOverlay extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	/** 平台在格子里的垂直位置：靠下，正好托住棋子的脚。 */
	private static final int PLATFORM_Y = 9;
	private static final int PLATFORM_H = 6;

	public AutochessArenaOverlay() {
		super();

		// 1) 整个战场的淡网格
		for (int y = ChessBoardLevel.FIELD_MIN_Y; y <= ChessBoardLevel.FIELD_MAX_Y; y++) {
			for (int x = 1; x <= ChessBoardLevel.SIZE - 2; x++) {
				faintGrid(x, y);
			}
		}

		// 1.5) 整个半场铺满「浅地基」，看起来像一整片平台
		for (int y = ChessBoardLevel.FIELD_MIN_Y; y <= ChessBoardLevel.FIELD_MAX_Y; y++) {
			for (int x = 1; x <= ChessBoardLevel.PLAYER_MAX_X; x++) {
				litePlatform(x * CELL,
						y * CELL + PLATFORM_Y, DiceMageUI.BLUE);
			}
			for (int x = ChessBoardLevel.ENEMY_MIN_X; x <= ChessBoardLevel.SIZE - 2; x++) {
				litePlatform(x * CELL,
						y * CELL + PLATFORM_Y, DiceMageUI.RED);
			}
		}

		// 2) 可上阵的格子：半透明地基 + 描边
		for (int i = 0; i < 12; i++) {
			platform(ChessBoardLevel.playerSlot(i), DiceMageUI.BLUE);
			platform(ChessBoardLevel.enemySlot(i), DiceMageUI.RED);
		}

		// 3) 备战席
		for (int i = 0; i < ChessBoardLevel.BENCH_COUNT; i++) {
			platform(ChessBoardLevel.benchCell(i), DiceMageUI.GOLD);
			platform(ChessBoardLevel.enemyBenchCell(i), DiceMageUI.GOLD);
		}
	}

	private void faintGrid(int x, int y) {
		int color = 0x22BFC7D5;
		addBlock(x * CELL, y * CELL, CELL - 1, 1, color);
		addBlock(x * CELL, y * CELL + CELL - 1, CELL - 1, 1, color);
		addBlock(x * CELL, y * CELL, 1, CELL - 1, color);
		addBlock(x * CELL + CELL - 1, y * CELL, 1, CELL - 1, color);
	}

	private void platform(int cell, int color) {
		if (cell < 0) return;
		float x = (cell % ChessBoardLevel.SIZE) * (float) CELL;
		float y = (cell / ChessBoardLevel.SIZE) * (float) CELL + PLATFORM_Y;

		// 半透明地基（在棋子脚下）
		addBlock(x + 1, y, CELL - 2, PLATFORM_H, 0x55000000 | (color & 0xFFFFFF));
		// 边框
		int line = 0xBB000000 | (color & 0xFFFFFF);
		addBlock(x, y, CELL - 1, 1, line);
		addBlock(x, y + PLATFORM_H, CELL - 1, 1, line);
		addBlock(x, y, 1, PLATFORM_H, line);
		addBlock(x + CELL - 1, y, 1, PLATFORM_H, line);
	}

	/** 只画一条很淡的地基，把整个半场铺成平台。 */
	private void litePlatform(float x, float y, int color) {
		addBlock(x + 1, y, CELL - 2, PLATFORM_H, 0x22FFFFFF | (color & 0xFFFFFF));
	}

	private void addBlock(float x, float y, float w, float h, int color) {
		ColorBlock block = new ColorBlock(w, h, color);
		block.x = x;
		block.y = y;
		add(block);
	}
}
