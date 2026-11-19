package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.Random;

/**
 * 棋盘外的「舞台」背景（云顶风）。
 *
 * <p>棋盘外原来是纯黑，现在铺一层深蓝夜色 + 星点，并在棋盘四周加一圈冷色微光，
 * 像云顶那种被灯光照着的舞台。所有内容都只画在棋盘之外，不会压暗棋盘。</p>
 */
public class AutochessArenaOverlay extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	private static final int MARGIN = 320;

	public AutochessArenaOverlay() {
		super();

		int bw = ChessBoardLevel.SIZE * CELL;
		int bh = ChessBoardLevel.HEIGHT * CELL;

		int night = 0xE00A0D14;      // 深蓝夜色（略带透明）
		int glow = 0x1E4A6FA8;       // 靠棋盘那圈的冷色微光

		// 1) 四个方向铺夜色（不覆盖棋盘）
		addBand(-MARGIN, -MARGIN, MARGIN, bh + MARGIN * 2, night);             // 左
		addBand(bw, -MARGIN, MARGIN, bh + MARGIN * 2, night);                  // 右
		addBand(0, -MARGIN, bw, MARGIN, night);                                // 上
		addBand(0, bh, bw, MARGIN, night);                                     // 下

		// 2) 紧贴棋盘的一圈冷光（只有 2px，避免出现方块状的层次）
		addBand(-2, -2, 2, bh + 4, glow);
		addBand(bw, -2, 2, bh + 4, glow);
		addBand(0, -2, bw, 2, glow);
		addBand(0, bh, bw, 2, glow);

		// 3) 星点（固定种子，只在棋盘外）
		Random rnd = new Random(20261120L);
		for (int i = 0; i < 260; i++) {
			float sx = rnd.nextInt(bw + MARGIN * 2) - MARGIN;
			float sy = rnd.nextInt(bh + MARGIN * 2) - MARGIN;
			if (sx >= -2 && sx <= bw + 2 && sy >= -2 && sy <= bh + 2) continue;

			int v = 70 + rnd.nextInt(120);
			int a = 50 + rnd.nextInt(110);
			ColorBlock star = new ColorBlock(1, 1, (a << 24) | (v << 16) | (v << 8) | v);
			star.x = Math.round(sx);
			star.y = Math.round(sy);
			add(star);

			if (rnd.nextInt(7) == 0) {
				ColorBlock cross = new ColorBlock(3, 1, (a / 2 << 24) | (v << 16) | (v << 8) | v);
				cross.x = star.x - 1;
				cross.y = star.y;
				add(cross);
			}
		}
	}

	private void addBand(float x, float y, float w, float h, int color) {
		ColorBlock block = new ColorBlock(w, h, color);
		block.x = x;
		block.y = y;
		add(block);
	}
}
