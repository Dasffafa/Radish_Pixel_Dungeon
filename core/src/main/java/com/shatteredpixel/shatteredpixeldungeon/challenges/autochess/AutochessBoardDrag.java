package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
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

import java.util.ArrayList;

/**
 * 棋盘上的拖拽布阵。
 *
 * <p>只给「自己的棋子精灵」挂输入区：按在棋子上才算拖拽，其它地方的点击照常传给
 * HUD / 商店窗口。之前用一块全屏输入区会把所有点击都吃掉，导致关掉商店后连
 * 顶部状态条都点不动、也就没法再开战。</p>
 */
public class AutochessBoardDrag extends Component {

	private final ArrayList<PointerArea> areas = new ArrayList<>();
	private int builtVersion = -1;

	private CharSprite ghost;
	private int fromCell = -1;
	private boolean dragging = false;
	private boolean dragged = false;

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null || run.state() != AutochessRun.State.PREP) {
			if (run != null && run.state() == AutochessRun.State.FIGHT) {
				clearGhost();
			}
			return;
		}
		if (run.unitVersion != builtVersion) {
			builtVersion = run.unitVersion;
			rebuildAreas();
		}
	}

	/** 给场上每个己方棋子挂一个只覆盖精灵的输入区。 */
	private void rebuildAreas() {
		for (PointerArea area : areas) {
			area.destroy();
		}
		areas.clear();

		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive()) continue;
			if (mob.sprite == null || mob.sprite.parent == null) continue;
			final Mob unit = mob;

			// 敌方单位：只能点开详情，不能拖
			if (mob.alignment != Char.Alignment.ALLY) {
				PointerArea peek = new PointerArea(mob.sprite) {
					@Override
					protected void onClick(PointerEvent event) {
						AutochessPool.Def def = AutochessPool.ofName(unit.getClass().getSimpleName());
						if (def != null) {
							com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
									new com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessInfo(def));
						}
					}
				};
				peek.active = true;
				peek.visible = true;
				add(peek);
				continue;
			}

			PointerArea area = new PointerArea(mob.sprite) {
				@Override
				protected void onPointerDown(PointerEvent event) {
					fromCell = unit.pos;
					dragging = true;
					dragged = false;
					makeGhost(unit);
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
					if (dragging && run != null && fromCell != -1) {
						int toCell = cellAt(event.current.x, event.current.y);
						if (toCell != -1) {
							run.moveUnit(fromCell, toCell);
						}
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
					if (run == null) return;
					int[] at = run.unitAt(unit.pos);
					if (at == null) return;
					// 点一下顺便打开详情
					AutochessPool.Def def = AutochessPool.ofName(unit.getClass().getSimpleName());
					if (def != null) {
						com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
								new com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessInfo(def));
					}
					if (run.selectedSide == at[0] && run.selectedIndex == at[1]) {
						run.selectedSide = -1;
						run.selectedIndex = -1;
					} else {
						run.selectedSide = at[0];
						run.selectedIndex = at[1];
						GLog.i(M.L(AutochessBoardDrag.class, "selected", unit.name()));
					}
				}
			};
			area.active = true;
			area.visible = true;
			add(area);
			areas.add(area);
		}
	}

	private void makeGhost(Mob unit) {
		CharSprite sprite = AutochessIcons.spriteFor(unit.getClass().getSimpleName());
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
	public void destroy() {
		for (PointerArea area : areas) {
			area.destroy();
		}
		areas.clear();
		super.destroy();
	}
}
