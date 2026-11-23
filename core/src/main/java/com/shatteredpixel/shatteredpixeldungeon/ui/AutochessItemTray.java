package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessItems;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

import java.util.ArrayList;

/**
 * 装备托盘：常驻在商店栏上方的一排装备图标，可以直接拖到棋子身上。
 *
 * <p>拖起来时图标跟着手指走，松手时落到哪只己方棋子上就装给谁；</p>
 */
public class AutochessItemTray extends Component {

	private static final int CHIP = 14;
	private static final int GAP = 2;
	private static final int MARGIN = 3;

	private final ArrayList<Component> chips = new ArrayList<>();
	private final ArrayList<com.watabou.noosa.Visual> visuals = new ArrayList<>();
	private float lastX = 0f;
	private float lastY = 0f;
	private String lastSig = "";

	private ItemSprite ghost;
	private String dragId = null;

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null || PixelScene.uiCamera == null
				|| run.state() != AutochessRun.State.PREP) {
			clearChips();
			lastSig = "";
			visible = false;
			return;
		}
		visible = true;

		StringBuilder sig = new StringBuilder();
		for (String id : run.itemBag()) sig.append(id).append('|');
		if (!sig.toString().equals(lastSig)) {
			lastSig = sig.toString();
			rebuild(run);
		}

		// 拖动中的图标跟随手指（UI 坐标）
		if (ghost != null) {
			PointF p = PixelScene.uiCamera.screenToCamera((int) lastX, (int) lastY);
			ghost.x = p.x - CHIP / 2f;
			ghost.y = p.y - CHIP / 2f;
		}
	}

	private void clearChips() {
		for (Component c : chips) c.killAndErase();
		for (com.watabou.noosa.Visual v : visuals) v.killAndErase();
		chips.clear();
		visuals.clear();
		if (ghost != null) {
			ghost.killAndErase();
			ghost = null;
		}
		dragId = null;
	}

	private void rebuild(AutochessRun run) {
		clearChips();

		float y = PixelScene.uiCamera.height - 48 - CHIP - 4;
		float x = MARGIN;

		int shown = 0;
		for (final String id : new ArrayList<>(run.itemBag())) {
			if (shown >= 8) break;
			shown++;

			RoundedFrame frame = new RoundedFrame(0xCC09070B, DiceMageUI.GOLD);
			frame.setRect(x, y, CHIP, CHIP);
			add(frame);
			chips.add(frame);

			Item item = AutochessItems.item(id);
			if (item != null) {
				ItemSprite icon = new ItemSprite(item);
				float scale = (CHIP - 2) / 16f;
				icon.scale.set(scale);
				icon.x = x + 1;
				icon.y = y + 1;
				add(icon);
				visuals.add(icon);
			}

			PointerArea area = new PointerArea(0, 0, CHIP, CHIP) {
				@Override
				protected void onPointerDown(PointerEvent event) {
					lastX = event.current.x;
					lastY = event.current.y;
					dragId = id;
					startGhost(id);
				}

				@Override
				protected void onDrag(PointerEvent event) {
					lastX = event.current.x;
					lastY = event.current.y;
				}

				@Override
				protected void onPointerUp(PointerEvent event) {
					dropAt(event);
				}

				@Override
				protected void onClick(PointerEvent event) {
					// 轻点不拖：装给当前选中的棋子
					AutochessRun run = AutochessRun.get();
					if (run == null) return;
					if (run.selectedSide < 0 || run.selectedIndex < 0) {
						GLog.w(M.L(AutochessItemTray.class, "hint_select"));
						return;
					}
					run.equipItem(run.selectedSide, run.selectedIndex, id);
				}
			};
			area.camera = PixelScene.uiCamera;
			area.givePointerPriority();
			area.x = x;
			area.y = y;
			area.width = CHIP;
			area.height = CHIP;
			add(area);
			visuals.add(area);

			x += CHIP + GAP;
		}
	}

	private void startGhost(String id) {
		if (ghost != null) ghost.killAndErase();
		Item item = AutochessItems.item(id);
		if (item == null) return;
		ghost = new ItemSprite(item);
		ghost.scale.set((CHIP - 2) / 16f);
		add(ghost);
	}

	private void dropAt(PointerEvent event) {
		String id = dragId;
		dragId = null;
		if (ghost != null) {
			ghost.killAndErase();
			ghost = null;
		}
		if (id == null) return;

		AutochessRun run = AutochessRun.get();
		if (run == null || com.shatteredpixel.shatteredpixeldungeon.Dungeon.level == null) return;

		PointF p = Camera.main.screenToCamera((int) event.current.x, (int) event.current.y);
		int cell = ((int) (p.y / DungeonTilemap.SIZE)) * com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.width()
				+ (int) (p.x / DungeonTilemap.SIZE);
		if (cell < 0 || cell >= com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.length()) return;
		if (!run.equipItemAtCell(cell, id)) {
			GLog.w(M.L(AutochessItemTray.class, "hint_drop"));
		}
	}
}
