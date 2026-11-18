package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.AutochessIcons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RoundedFrame;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

/**
 * 自走棋备战界面：
 * 战场（上阵位，数量受等级限制）+ 备战席（不参战）、买棋子、买经验、刷新、拖拽换位、卖出、开战。
 */
public class WndAutochessShop extends Window {

	private static final int WIDTH = 162;
	private static final int MARGIN = 5;
	private static final int CELL_W = 30;
	private static final int CELL_H = 21;
	private static final int CELL_GAP = 2;
	private static final int OFFER_ROW = 28;
	private static final int GAP = 3;
	private static final int COLS = 3;

	private static final int COLOR_TITLE = 0xFFFF44;
	private static final int COLOR_GOLD = 0xFFD700;
	private static final int COLOR_DIM = 0xAAAAAA;
	private static final int COLOR_SECTION = 0x99CCFF;
	private static final int FRAME_LINE = 0x55FFFF66;
	private static final int FRAME_LINE_SELECTED = 0xFFFFAA00;
	private static final int FRAME_LINE_BOARD = 0x6644FF88;

	private final AutochessRun run;

	/** [side][index] 的格子左上角（本地坐标），side 0 = 备战席，1 = 战场。 */
	private final float[][] cellX = new float[2][9];
	private final float[][] cellY = new float[2][9];

	private CharSprite dragGhost;
	private int dragSide = -1;
	private int dragIndex = -1;
	private boolean dragging = false;
	private boolean dragged = false;

	public WndAutochessShop(int round) {

		super();

		run = AutochessRun.get();
		if (run == null) {
			hide();
			return;
		}

		RenderedTextBlock title = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "title", round), 9);
		title.hardlight(COLOR_TITLE);
		title.setPos(MARGIN, MARGIN);
		add(title);

		RenderedTextBlock gold = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "gold", run.gold(), run.life()), 8);
		gold.hardlight(COLOR_GOLD);
		gold.setPos(MARGIN, title.bottom() + 2);
		add(gold);

		RenderedTextBlock info = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "info",
						run.level(), run.deployed(), run.maxDeploy(), run.xp(), run.xpNeed()), 7);
		info.hardlight(COLOR_SECTION);
		info.setPos(MARGIN, gold.bottom() + 1);
		add(info);

		float pos = info.bottom() + GAP;

		pos = addGrid(0, "bench_title", M.L(WndAutochessShop.class, "bench_title", benchCount()), pos);
		pos = addSellRow(pos);
		pos = addActions(pos);

		for (int i = 0; i < 3; i++) {
			pos = addOfferRow(i, pos);
		}

		RenderedTextBlock traits = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "traits", traitLine()), 7);
		traits.maxWidth(WIDTH - MARGIN * 2);
		traits.setPos(MARGIN, pos + 1);
		traits.hardlight(COLOR_DIM);
		add(traits);

		resize(WIDTH, (int) traits.bottom() + MARGIN);
	}

	private int benchCount() {
		int count = 0;
		for (int i = 0; i < 9; i++) {
			if (run.unitClass(0, i) != null) count++;
		}
		return count;
	}

	private void rebuild() {
		final AutochessRun current = AutochessRun.get();
		if (current == null) {
			hide();
			return;
		}
		final int round = current.round();
		hide();
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndAutochessShop(round));
			}
		});
	}

	/* ====================== 棋盘格 ====================== */

	private float addGrid(int side, String labelKey, String labelText, float pos) {

		RenderedTextBlock label = PixelScene.renderTextBlock(labelText, 7);
		label.hardlight(COLOR_SECTION);
		label.setPos(MARGIN, pos);
		add(label);
		pos = label.bottom() + 1;

		for (int i = 0; i < 9; i++) {
			final int slot = i;
			final int theSide = side;
			int col = i % COLS;
			int row = i / COLS;
			float cx = MARGIN + col * (CELL_W + CELL_GAP);
			float cy = pos + row * (CELL_H + CELL_GAP);
			cellX[side][i] = cx;
			cellY[side][i] = cy;

			boolean selected = run.selectedSide == side && run.selectedIndex == i
					&& run.unitClass(side, i) != null;
			RoundedFrame frame = new RoundedFrame(0xCC1A1A1A,
					selected ? FRAME_LINE_SELECTED : (side == 1 ? FRAME_LINE_BOARD : FRAME_LINE));
			frame.setRect(cx, cy, CELL_W, CELL_H);
			add(frame);

			String cls = run.unitClass(side, i);
			if (cls != null) {
				CharSprite icon = AutochessIcons.spriteFor(cls);
				if (icon != null) {
					AutochessIcons.place(icon, cx + 2, cy + 4, 12);
					add(icon);
				}
				RenderedTextBlock name = PixelScene.renderTextBlock(run.unitName(side, i), 6);
				name.maxWidth(14);
				name.setPos(cx + 15, cy + 2);
				add(name);

				RenderedTextBlock stars = PixelScene.renderTextBlock(stars(run.unitStar(side, i)), 6);
				stars.hardlight(0xFFD700);
				stars.setPos(cx + 15, cy + 12);
				add(stars);
			}

			PointerArea area = new PointerArea(cx, cy, CELL_W, CELL_H) {
				@Override
				protected void onPointerDown(PointerEvent event) {
					dragSide = theSide;
					dragIndex = slot;
					dragging = false;
					dragged = false;
				}

				@Override
				protected void onDrag(PointerEvent event) {
					if (run.unitClass(theSide, slot) == null) {
						return;
					}
					if (!dragging) {
						startGhost(theSide, slot);
						dragging = true;
						dragged = true;
					}
					moveGhost(event);
				}

				@Override
				protected void onPointerUp(PointerEvent event) {
					if (dragging) {
						dropGhost(event);
						dragging = false;
					}
				}

				@Override
				protected void onClick(PointerEvent event) {
					if (dragged) {
						dragged = false;
						return;
					}
					if (run.unitClass(theSide, slot) == null) {
						return;
					}
					if (run.selectedSide == theSide && run.selectedIndex == slot) {
						run.selectedSide = -1;
						run.selectedIndex = -1;
					} else {
						run.selectedSide = theSide;
						run.selectedIndex = slot;
					}
					rebuild();
				}
			};
			add(area);
		}

		return pos + 3 * CELL_H + 2 * CELL_GAP + GAP;
	}

	private void startGhost(int side, int index) {
		CharSprite sprite = AutochessIcons.spriteFor(run.unitClass(side, index));
		if (sprite == null) return;
		dragGhost = sprite;
		dragGhost.alpha(0.8f);
		dragGhost.scale.set(1.1f);
		add(dragGhost);
	}

	private void moveGhost(PointerEvent event) {
		if (dragGhost == null) return;
		PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
		dragGhost.x = p.x - dragGhost.width() * 0.55f;
		dragGhost.y = p.y - dragGhost.height() * 0.85f;
		PixelScene.align(dragGhost);
	}

	private void dropGhost(PointerEvent event) {
		PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
		if (dragGhost != null) {
			dragGhost.killAndErase();
			dragGhost = null;
		}
		int[] target = cellAt(p.x, p.y);
		if (target != null && run.drag(dragSide, dragIndex, target[0], target[1])) {
			run.selectedSide = target[0];
			run.selectedIndex = target[1];
			rebuild();
		}
		dragSide = -1;
		dragIndex = -1;
	}

	private int[] cellAt(float x, float y) {
		for (int side = 0; side <= 1; side++) {
			for (int i = 0; i < 9; i++) {
				float cx = cellX[side][i];
				float cy = cellY[side][i];
				if (x >= cx && x <= cx + CELL_W && y >= cy && y <= cy + CELL_H) {
					return new int[]{side, i};
				}
			}
		}
		return null;
	}

	/* ====================== 卖出 ====================== */

	private float addSellRow(float pos) {
		int side = run.selectedSide;
		int index = run.selectedIndex;
		if (side == 0 && run.unitClass(side, index) != null) {
			// 备战席上的棋子：可以上阵，也可以卖掉
			float halfW = (WIDTH - MARGIN * 2 - GAP) / 2f;
			RedButton deploy = new RedButton(M.L(WndAutochessShop.class, "deploy"), 8) {
				@Override
				protected void onClick() {
					AutochessRun current = AutochessRun.get();
					if (current != null && current.deploy(current.selectedIndex)) {
						current.selectedSide = -1;
						current.selectedIndex = -1;
						rebuild();
					}
				}
			};
			deploy.setRect(MARGIN, pos, halfW, 16);
			add(deploy);

			RedButton sellBench = new RedButton(M.L(WndAutochessShop.class, "sell_short",
					run.sellValue(0, index)), 8) {
				@Override
				protected void onClick() {
					AutochessRun current = AutochessRun.get();
					if (current == null) return;
					if (current.sell(current.selectedSide, current.selectedIndex) > 0) {
						current.selectedSide = -1;
						current.selectedIndex = -1;
						rebuild();
					}
				}
			};
			sellBench.setRect(deploy.right() + GAP, pos, halfW, 16);
			add(sellBench);
			return deploy.bottom() + GAP;
		}
		if (side < 0 || run.unitClass(side, index) == null) {
			RenderedTextBlock hint = PixelScene.renderTextBlock(
					M.L(WndAutochessShop.class, "select_hint"), 7);
			hint.maxWidth(WIDTH - MARGIN * 2);
			hint.hardlight(COLOR_DIM);
			hint.setPos(MARGIN, pos);
			add(hint);
			return hint.bottom() + GAP;
		}
		int value = run.sellValue(side, index);
		RedButton sell = new RedButton(
				M.L(WndAutochessShop.class, "sell", run.unitName(side, index),
						stars(run.unitStar(side, index)), value), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current == null) return;
				int s = current.selectedSide;
				int i = current.selectedIndex;
				if (s >= 0 && current.sell(s, i) > 0) {
					current.selectedSide = -1;
					current.selectedIndex = -1;
					rebuild();
				}
			}
		};
		sell.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 15);
		add(sell);
		return sell.bottom() + GAP;
	}

	/* ====================== 操作按钮 ====================== */

	private float addActions(float pos) {

		float halfW = (WIDTH - MARGIN * 2 - GAP) / 2f;

		RedButton xpBtn = new RedButton(M.L(WndAutochessShop.class, "buy_xp", AutochessRun.XP_COST), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.buyXp()) {
					rebuild();
				}
			}
		};
		xpBtn.active = run.level() < AutochessRun.MAX_LEVEL && run.gold() >= AutochessRun.XP_COST;
		xpBtn.setRect(MARGIN, pos, halfW, 17);
		add(xpBtn);

		RedButton refresh = new RedButton(
				M.L(WndAutochessShop.class, "refresh", AutochessRun.REFRESH_COST), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.refresh()) {
					rebuild();
				}
			}
		};
		refresh.active = run.gold() >= AutochessRun.REFRESH_COST;
		refresh.setRect(xpBtn.right() + GAP, pos, halfW, 17);
		add(refresh);

		pos = xpBtn.bottom() + GAP;

		RedButton place = new RedButton(M.L(WndAutochessShop.class, "place_on_map"), 8) {
			@Override
			protected void onClick() {
				hide();
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPlacement.begin();
			}
		};
		place.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 16);
		add(place);
		pos = place.bottom() + GAP;

		RedButton fight = new RedButton(M.L(WndAutochessShop.class, "fight"), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				hide();
				if (current != null) {
					current.startFight();
				}
			}
		};
		fight.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 18);
		add(fight);

		return fight.bottom() + GAP;
	}

	/* ====================== 商店 ====================== */

	private float addOfferRow(int index, float pos) {

		AutochessPool.Def def = run.offer(index);

		if (def == null) {
			RenderedTextBlock sold = PixelScene.renderTextBlock(M.L(WndAutochessShop.class, "sold"), 7);
			sold.hardlight(COLOR_DIM);
			sold.setPos(MARGIN, pos + 8);
			add(sold);
			return pos + OFFER_ROW;
		}

		CharSprite icon = AutochessIcons.spriteFor(def);
		if (icon != null) {
			AutochessIcons.place(icon, MARGIN, pos + 1, 15);
			add(icon);
		}

		RenderedTextBlock name = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "row", nameOf(def), def.cost, traitName(def.trait)), 8);
		name.maxWidth(WIDTH - MARGIN * 2 - 20);
		name.setPos(MARGIN + 18, pos + 1);
		add(name);

		final int idx = index;
		RedButton buy = new RedButton(M.L(WndAutochessShop.class, "buy"), 7) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.buy(idx)) {
					rebuild();
				}
			}
		};
		buy.active = run.gold() >= def.cost;
		buy.setRect(MARGIN + 18, pos + 13, 58, 13);
		add(buy);

		return pos + OFFER_ROW;
	}

	private static String nameOf(AutochessPool.Def def) {
		com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob =
				com.watabou.utils.Reflection.newInstance(def.cls);
		return mob == null ? def.cls.getSimpleName() : mob.name();
	}

	private static String traitName(String trait) {
		return M.L(AutochessPool.class, "trait_" + trait);
	}

	private static String stars(int star) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < star; i++) sb.append("★");
		return sb.toString();
	}

	private static String traitLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(traitName(AutochessPool.BEAST)).append(" ").append(AutochessRun.countTrait(AutochessPool.BEAST));
		sb.append("  ·  ").append(traitName(AutochessPool.HUMANOID)).append(" ").append(AutochessRun.countTrait(AutochessPool.HUMANOID));
		sb.append("  ·  ").append(traitName(AutochessPool.CONSTRUCT)).append(" ").append(AutochessRun.countTrait(AutochessPool.CONSTRUCT));
		sb.append("  ·  ").append(traitName(AutochessPool.DEMON)).append(" ").append(AutochessRun.countTrait(AutochessPool.DEMON));
		return sb.toString();
	}
}
