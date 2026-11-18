package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 地图布阵：在棋盘上点自己的棋子选中，再点己方半场的空格把它挪过去。
 *
 * <p>和云顶一样，位置是直接在地图上摆的，不是窗口里的小格子。</p>
 */
public class AutochessPlacement {

	/** 已经选中的棋子所在格子，-1 表示还没选。 */
	private static int picked = -1;

	public static void begin() {
		picked = -1;
		AutochessRun run = AutochessRun.get();
		if (run == null || run.state() != AutochessRun.State.PREP) {
			GLog.w(M.L(AutochessPlacement.class, "not_prep"));
			return;
		}
		run.refreshPreview();
		select();
	}

	private static void select() {
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null) {
					picked = -1;
					return;
				}
				AutochessRun run = AutochessRun.get();
				if (run == null || run.state() != AutochessRun.State.PREP) {
					picked = -1;
					return;
				}
				if (picked == -1) {
					int index = run.boardIndexAt(cell);
					if (index < 0) {
						GLog.w(M.L(AutochessPlacement.class, "pick_unit"));
						select();
						return;
					}
					picked = cell;
					GLog.i(M.L(AutochessPlacement.class, "pick_target"));
					select();
				} else {
					if (run.moveOnMap(picked, cell)) {
						GLog.p(M.L(AutochessPlacement.class, "moved"));
					}
					picked = -1;
					select();
				}
			}

			@Override
			public String prompt() {
				return picked == -1
						? M.L(AutochessPlacement.class, "prompt_pick")
						: M.L(AutochessPlacement.class, "prompt_place");
			}
		});
	}
}
