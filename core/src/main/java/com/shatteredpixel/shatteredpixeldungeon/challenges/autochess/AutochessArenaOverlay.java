package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

/**
 * 棋盘外的装饰（云顶风）：
 * <ul>
 *   <li>战场外圈一条暖金色描边（云顶的镶边感）</li>
 *   <li>两条备战席也镶金边</li>
 *   <li>棋盘外围铺一层很暗的蓝网格，替代纯黑背景</li>
 * </ul>
 * 全部画在棋子之下、地砖之上，不会挡住单位。
 */
public class AutochessArenaOverlay extends Component {

	private static final int CELL = DungeonTilemap.SIZE;

	public AutochessArenaOverlay() {
		super();

		// ---------- 外围暗色网格（棋盘四周）----------
		int boardW = ChessBoardLevel.SIZE * CELL;
		int boardH = ChessBoardLevel.HEIGHT * CELL;
		int margin = 96;

		int gridColor = 0x14A8C8E8;     // 很淡的冷蓝
		for (int gx = -margin; gx <= boardW + margin; gx += CELL) {
			ColorBlock line = new ColorBlock(1, boardH + margin * 2, gridColor);
			line.x = gx;
			line.y = -margin;
			add(line);
		}
		for (int gy = -margin; gy <= boardH + margin; gy += CELL) {
			ColorBlock line = new ColorBlock(boardW + margin * 2, 1, gridColor);
			line.x = -margin;
			line.y = gy;
			add(line);
		}

		// ---------- 战场金边 ----------
		float fx = 1 * (float) CELL;
		float fy = ChessBoardLevel.FIELD_MIN_Y * (float) CELL;
		float fw = (ChessBoardLevel.SIZE - 2) * (float) CELL;
		float fh = (ChessBoardLevel.FIELD_MAX_Y - ChessBoardLevel.FIELD_MIN_Y + 1) * (float) CELL;
		goldFrame(fx, fy, fw, fh);

		// ---------- 两条备战席金边 ----------
		float bx = ChessBoardLevel.BENCH_MIN_X * (float) CELL;
		float by = ChessBoardLevel.BENCH_Y * (float) CELL;
		goldFrame(bx, by, ChessBoardLevel.BENCH_COUNT * (float) CELL, CELL);

		float ebx = ChessBoardLevel.ENEMY_BENCH_MIN_X * (float) CELL;
		float eby = ChessBoardLevel.ENEMY_BENCH_Y * (float) CELL;
		goldFrame(ebx, eby, ChessBoardLevel.ENEMY_BENCH_COUNT * (float) CELL, CELL);
	}

	private void goldFrame(float x, float y, float w, float h) {
		int outer = 0xCCB59E09;   // 暗金
		int inner = 0x66F1E5B5;   // 淡金高光

		addBlock(x, y, w, 1, outer);
		addBlock(x, y + h - 1, w, 1, outer);
		addBlock(x, y, 1, h, outer);
		addBlock(x + w - 1, y, 1, h, outer);

		// 内侧一条更淡的金线，做出「双线镶边」的质感
		addBlock(x + 1, y + 1, w - 2, 1, inner);
		addBlock(x + 1, y + h - 2, w - 2, 1, inner);
	}

	private void addBlock(float x, float y, float w, float h, int color) {
		ColorBlock block = new ColorBlock(w, h, color);
		block.x = x;
		block.y = y;
		add(block);
	}
}
