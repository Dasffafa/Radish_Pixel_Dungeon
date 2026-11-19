package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

/**
 * 选中 / 可放置高亮（云顶风）。
 *
 * <p>备战阶段：12 个上阵格有淡淡的呼吸金边；当前选中的棋子脚下是更亮的青色框。</p>
 */
public class AutochessSelectionHighlight extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	private static final int SLOTS = 12;

	private final ColorBlock[][] slotLines = new ColorBlock[SLOTS][4];
	private final ColorBlock[] selLines = new ColorBlock[4];
	private float time = 0f;

	public AutochessSelectionHighlight() {
		super();

		for (int i = 0; i < SLOTS; i++) {
			int cell = ChessBoardLevel.playerSlot(i);
			if (cell < 0) continue;
			float x = (cell % ChessBoardLevel.SIZE) * (float) CELL;
			float y = (cell / ChessBoardLevel.SIZE) * (float) CELL;
			slotLines[i] = frame(x + 2, y + 2, CELL - 5, 0x00FFFFFF);
		}
		selLines[0] = new ColorBlock(CELL - 3, 1, 0xFFFFFFFF);
		selLines[1] = new ColorBlock(CELL - 3, 1, 0xFFFFFFFF);
		selLines[2] = new ColorBlock(1, CELL - 3, 0xFFFFFFFF);
		selLines[3] = new ColorBlock(1, CELL - 3, 0xFFFFFFFF);
		for (ColorBlock c : selLines) {
			c.visible = false;
			add(c);
		}
	}

	private ColorBlock[] frame(float x, float y, float size, int color) {
		ColorBlock[] lines = new ColorBlock[4];
		lines[0] = new ColorBlock(size, 1, color);
		lines[0].x = x;
		lines[0].y = y;
		lines[1] = new ColorBlock(size, 1, color);
		lines[1].x = x;
		lines[1].y = y + size;
		lines[2] = new ColorBlock(1, size, color);
		lines[2].x = x;
		lines[2].y = y;
		lines[3] = new ColorBlock(1, size, color);
		lines[3].x = x + size;
		lines[3].y = y;
		for (ColorBlock c : lines) {
			c.visible = false;
			add(c);
		}
		return lines;
	}

	@Override
	public void update() {
		super.update();
		time += Game.elapsed;

		AutochessRun run = AutochessRun.get();
		boolean prep = run != null && run.state() == AutochessRun.State.PREP;
		if (!prep || Dungeon.level == null) {
			for (ColorBlock[] rows : slotLines) {
				if (rows == null) continue;
				for (ColorBlock c : rows) c.visible = false;
			}
			for (ColorBlock c : selLines) c.visible = false;
			return;
		}

		// 上阵格：呼吸金边
		float pulse = 0.35f + 0.25f * (float) Math.sin(time * 2.2f);
		for (ColorBlock[] rows : slotLines) {
			if (rows == null) continue;
			for (ColorBlock c : rows) {
				c.visible = true;
				c.alpha(pulse * 0.6f);
			}
		}

		// 选中的棋子：亮青色框 + 呼吸
		ColorBlock[] lines = selLines;
		boolean has = run.selectedSide == 1 && run.selectedIndex >= 0
				&& run.unitClass(1, run.selectedIndex) != null;
		if (!has) {
			for (ColorBlock c : lines) c.visible = false;
			return;
		}
		int cell = run.boardCellOf(run.selectedIndex);
		if (cell < 0) {
			for (ColorBlock c : lines) c.visible = false;
			return;
		}
		float x = (cell % ChessBoardLevel.SIZE) * (float) CELL + 1;
		float y = (cell / ChessBoardLevel.SIZE) * (float) CELL + 1;
		float size = CELL - 2;
		float a = 0.55f + 0.35f * (float) Math.sin(time * 5f);

		lines[0].x = x;
		lines[0].y = y;
		lines[1].x = x;
		lines[1].y = y + size - 1;
		lines[2].x = x;
		lines[2].y = y;
		lines[3].x = x + size - 1;
		lines[3].y = y;
		for (ColorBlock c : lines) {
			c.visible = true;
			c.alpha(a);
			c.color(0xFF66DDFF);
		}
	}
}
