package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.AutochessIcons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

/**
 * 棋盘上的拖拽布阵：按住自己的棋子拖到别的格子松手。
 *
 * <p>覆盖整个主镜头，所以屏幕上的任何格子都能落子；合法性（己方半场、上阵人数上限）
 * 由 {@link AutochessRun#moveUnit(int, int)} 判。</p>
 */
public class AutochessBoardDrag extends Component {

	private final PointerArea area;
	private CharSprite ghost;

	private int fromCell = -1;
	private boolean dragging = false;
	private boolean dragged = false;

	public AutochessBoardDrag() {
		super();
		area = new PointerArea(0, 0, 0, 0) {
			@Override
			protected void onPointerDown(PointerEvent event) {
				AutochessRun run = AutochessRun.get();
				if (run == null || run.state() != AutochessRun.State.PREP) return;
				int cell = cellAt(event.current.x, event.current.y);
				if (cell == -1) return;
				int[] unit = run.unitAt(cell);
				if (unit == null) return;
				fromCell = cell;
				dragging = true;
				dragged = false;
				makeGhost(unit[0], unit[1]);
			}

			@Override
			protected void onDrag(PointerEvent event) {
				if (!dragging || ghost == null) return;
				dragged = true;
				PointF p = Camera.main.screenToCamera((int) event.current.x, (int) event.current.y);
				ghost.x = p.x - ghost.width() * 0.5f;
				ghost.y = p.y - ghost.height() * 0.9f;
				PixelScene.align(ghost);
			}

			@Override
			protected void onPointerUp(PointerEvent event) {
				AutochessRun run = AutochessRun.get();
				int toCell = cellAt(event.current.x, event.current.y);
				if (dragging && run != null && fromCell != -1 && toCell != -1) {
					run.moveUnit(fromCell, toCell);
				}
				clearGhost();
				dragging = false;
				fromCell = -1;
			}

			@Override
			protected void onClick(PointerEvent event) {
				if (dragged) {
					dragged = false;
					return;
				}
				AutochessRun run = AutochessRun.get();
				if (run == null || run.state() != AutochessRun.State.PREP) return;
				int cell = cellAt(event.current.x, event.current.y);
				if (cell == -1) return;
				int[] unit = run.unitAt(cell);
				if (unit == null) {
					run.selectedSide = -1;
					run.selectedIndex = -1;
					return;
				}
				if (run.selectedSide == unit[0] && run.selectedIndex == unit[1]) {
					run.selectedSide = -1;
					run.selectedIndex = -1;
				} else {
					run.selectedSide = unit[0];
					run.selectedIndex = unit[1];
					GLog.i(M.L(AutochessBoardDrag.class, "selected", run.unitName(unit[0], unit[1])));
				}
			}
		};
		area.camera = Camera.main;
		// 必须真正「挡住」事件：否则拖拽松手那一下会穿透到商店窗口的按钮上，
		// 表现为「拖一下棋子就买了一次经验 / 刷新」
		area.visible = true;
		area.active = true;
		area.blockLevel = PointerArea.ALWAYS_BLOCK;
		add(area);
	}

	private void makeGhost(int side, int index) {
		AutochessRun run = AutochessRun.get();
		if (run == null) return;
		CharSprite sprite = AutochessIcons.spriteFor(run.unitClass(side, index));
		if (sprite == null) return;
		ghost = sprite;
		ghost.alpha(0.85f);
		add(ghost);
	}

	private void clearGhost() {
		if (ghost != null) {
			ghost.killAndErase();
			ghost = null;
		}
	}

	/** 屏幕坐标 → 棋盘格子。 */
	private static int cellAt(float screenX, float screenY) {
		if (Dungeon.level == null) return -1;
		PointF p = Camera.main.screenToCamera((int) screenX, (int) screenY);
		int cx = (int) (p.x / DungeonTilemap.SIZE);
		int cy = (int) (p.y / DungeonTilemap.SIZE);
		if (cx < 0 || cy < 0 || cx >= Dungeon.level.width() || cy >= Dungeon.level.height()) {
			return -1;
		}
		return cy * Dungeon.level.width() + cx;
	}

	@Override
	public void update() {
		super.update();
		// 让输入区始终盖住当前主镜头
		Camera c = Camera.main;
		if (c != null) {
			area.x = c.scroll.x;
			area.y = c.scroll.y;
			area.width = c.width;
			area.height = c.height;
		}
		AutochessRun run = AutochessRun.get();
		if (run == null || run.state() != AutochessRun.State.PREP) {
			clearGhost();
			dragging = false;
		}
	}
}
