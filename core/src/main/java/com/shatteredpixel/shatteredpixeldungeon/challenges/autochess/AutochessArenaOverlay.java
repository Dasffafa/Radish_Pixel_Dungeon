package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

/**
 * 战场装饰（尽量克制）。
 *
 * <p>只画一条中线 + 双方半场的极淡边界，不再给每个格子加彩色框——
 * 之前那些红黄蓝方框会和棋子穿模，看起来很乱。敌我区分交给地砖颜色。</p>
 */
public class AutochessArenaOverlay extends Component {

	private static final int CELL = DungeonTilemap.SIZE;

	public AutochessArenaOverlay() {
		super();

		// 中线：一条暗金细线，标出「中线左边是我方」
		int centerX = (ChessBoardLevel.PLAYER_MAX_X + 1) * CELL;
		for (int y = ChessBoardLevel.FIELD_MIN_Y; y <= ChessBoardLevel.FIELD_MAX_Y; y++) {
			ColorBlock dash = new ColorBlock(1, CELL - 6, 0x55B59E09);
			dash.x = centerX;
			dash.y = y * (float) CELL + 3;
			add(dash);
		}
	}
}
