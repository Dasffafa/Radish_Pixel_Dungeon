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
 * 自走棋的备战界面：金币、备战位（8 格，可拖拽换位、点选卖出）、三个可买的单位、开战。
 *
 * <p>备战位第 i 格就是棋盘上的第 i 个站位，所以拖拽换格 = 调整上阵站位。
 * 拖拽的实现是每个格子挂一个 {@link PointerArea}：拖动时把该格棋子的精灵当「幽灵」
 * 跟着手指走，松手时看落在哪一格，交换两格内容并重建窗口。</p>
 */
public class WndAutochessShop extends Window {

	private static final int WIDTH = 156;
	private static final int MARGIN = 5;
	private static final int CELL_W = 34;
	private static final int CELL_H = 24;
	private static final int CELL_GAP = 2;
	private static final int OFFER_ROW = 30;
	private static final int GAP = 3;

	private static final int COLOR_TITLE = 0xFFFF44;
	private static final int COLOR_GOLD = 0xFFD700;
	private static final int COLOR_DIM = 0xAAAAAA;
	private static final int COLOR_BENCH_TITLE = 0x99CCFF;
	private static final int FRAME_LINE = 0x55FFFF66;
	private static final int FRAME_LINE_SELECTED = 0xFFFFAA00;

	private final AutochessRun run;

	private final float[] cellX = new float[AutochessRun.MAX_UNITS];
	private final float[] cellY = new float[AutochessRun.MAX_UNITS];


	private CharSprite dragGhost;
	private int dragFrom = -1;
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
				M.L(WndAutochessShop.class, "gold", run.gold(), run.life(), run.rosterSize()), 8);
		gold.hardlight(COLOR_GOLD);
		gold.setPos(MARGIN, title.bottom() + 2);
		add(gold);

		float pos = gold.bottom() + GAP + 1;

		RenderedTextBlock benchTitle = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "bench", run.rosterSize()), 7);
		benchTitle.hardlight(COLOR_BENCH_TITLE);
		benchTitle.setPos(MARGIN, pos);
		add(benchTitle);
		pos = benchTitle.bottom() + 2;

		pos = addBenchGrid(pos);
		pos = addSellRow(pos);

		for (int i = 0; i < 3; i++) {
			pos = addOfferRow(i, pos);
		}

		RenderedTextBlock traits = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "traits", traitLine(), alliesLine()), 7);
		traits.maxWidth(WIDTH - MARGIN * 2);
		traits.setPos(MARGIN, pos + 2);
		traits.hardlight(COLOR_DIM);
		add(traits);

		pos = traits.bottom() + GAP + 1;

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
		refresh.setRect(MARGIN, pos, (WIDTH - MARGIN * 2 - GAP) / 2f, 18);
		add(refresh);

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
		fight.setRect(refresh.right() + GAP, pos, (WIDTH - MARGIN * 2 - GAP) / 2f, 18);
		add(fight);

		resize(WIDTH, (int) fight.bottom() + MARGIN);
	}

	/**
	 * 重建备战界面。
	 *
	 * <p>必须延到下一帧再弹新窗口：如果在这一帧里 hide + show，
	 * 当前这次点击的事件还会被派发给新窗口，落点正好压在「购买」上就会
	 * 一路连锁买下去（实测过，一次点击能买空半个商店）。</p>
	 */

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

	/* ====================== 备战位 ====================== */

	private float addBenchGrid(float pos) {

		for (int i = 0; i < AutochessRun.MAX_UNITS; i++) {
			final int slot = i;
			int col = i % 4;
			int row = i / 4;
			float cx = MARGIN + col * (CELL_W + CELL_GAP);
			float cy = pos + row * (CELL_H + CELL_GAP);
			cellX[i] = cx;
			cellY[i] = cy;

			boolean selected = run.selectedSlot == slot && run.benchClass(slot) != null;
			RoundedFrame frame = new RoundedFrame(0xCC1A1A1A,
					selected ? FRAME_LINE_SELECTED : FRAME_LINE);
			frame.setRect(cx, cy, CELL_W, CELL_H);
			add(frame);

			String cls = run.benchClass(slot);
			if (cls != null) {
				CharSprite icon = AutochessIcons.spriteFor(cls);
				if (icon != null) {
					AutochessIcons.place(icon, cx + 2, cy + 4, 14);
					add(icon);
				}

				RenderedTextBlock name = PixelScene.renderTextBlock(run.benchName(slot), 6);
				name.maxWidth(15);
				name.setPos(cx + 18, cy + 2);
				add(name);

				RenderedTextBlock stars = PixelScene.renderTextBlock(stars(run.benchStar(slot)), 6);
				stars.hardlight(0xFFD700);
				stars.setPos(cx + 18, cy + 13);
				add(stars);
			}

			PointerArea area = new PointerArea(cx, cy, CELL_W, CELL_H) {
				@Override
				protected void onPointerDown(PointerEvent event) {
					dragFrom = slot;
					dragging = false;
					dragged = false;
				}

				@Override
				protected void onDrag(PointerEvent event) {
					if (run.benchClass(slot) == null) {
						return;
					}
					if (!dragging) {
						startGhost(slot);
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
					// 拖完那一下不算点选
					if (dragged) {
						dragged = false;
						return;
					}
					if (run.benchClass(slot) == null) {
						return;
					}
					run.selectedSlot = (run.selectedSlot == slot) ? -1 : slot;
					rebuild();
				}
			};
			add(area);
		}

		return pos + 2 * CELL_H + CELL_GAP + 3;
	}

	private void startGhost(int slot) {
		CharSprite sprite = AutochessIcons.spriteFor(run.benchClass(slot));
		if (sprite == null) {
			return;
		}
		dragGhost = sprite;
		dragGhost.alpha(0.8f);
		dragGhost.scale.set(1.2f);
		add(dragGhost);
	}

	private void moveGhost(PointerEvent event) {
		if (dragGhost == null) {
			return;
		}
		PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
		dragGhost.x = p.x - dragGhost.width() * 0.6f;
		dragGhost.y = p.y - dragGhost.height() * 0.9f;
		PixelScene.align(dragGhost);
	}

	private void dropGhost(PointerEvent event) {
		PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
		int target = cellAt(p.x, p.y);

		if (dragGhost != null) {
			dragGhost.killAndErase();
			dragGhost = null;
		}
		if (target >= 0 && target != dragFrom) {
			run.swapBench(dragFrom, target);
			run.selectedSlot = target;
			rebuild();
		}
		dragFrom = -1;
	}

	private int cellAt(float x, float y) {
		for (int i = 0; i < AutochessRun.MAX_UNITS; i++) {
			if (x >= cellX[i] && x <= cellX[i] + CELL_W
					&& y >= cellY[i] && y <= cellY[i] + CELL_H) {
				return i;
			}
		}
		return -1;
	}

	/* ====================== 卖出 ====================== */

	private float addSellRow(float pos) {

		int slot = run.selectedSlot;
		if (slot < 0 || run.benchClass(slot) == null) {
			RenderedTextBlock hint = PixelScene.renderTextBlock(
					M.L(WndAutochessShop.class, "select_hint"), 7);
			hint.maxWidth(WIDTH - MARGIN * 2);
			hint.hardlight(COLOR_DIM);
			hint.setPos(MARGIN, pos);
			add(hint);
			return hint.bottom() + GAP;
		}

		int value = run.sellValue(slot);
		RedButton sell = new RedButton(
				M.L(WndAutochessShop.class, "sell", run.benchName(slot), stars(run.benchStar(slot)), value), 8) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current == null) {
					return;
				}
				int s = current.selectedSlot;
				if (s >= 0 && current.sell(s) > 0) {
					current.selectedSlot = -1;
					rebuild();
				}
			}
		};
		sell.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 16);
		add(sell);
		return sell.bottom() + GAP;
	}

	/* ====================== 商店 ====================== */

	private float addOfferRow(int index, float pos) {

		AutochessPool.Def def = run.offer(index);

		if (def == null) {
			RenderedTextBlock sold = PixelScene.renderTextBlock(M.L(WndAutochessShop.class, "sold"), 7);
			sold.hardlight(COLOR_DIM);
			sold.setPos(MARGIN, pos + 9);
			add(sold);
			return pos + OFFER_ROW;
		}

		CharSprite icon = AutochessIcons.spriteFor(def);
		if (icon != null) {
			AutochessIcons.place(icon, MARGIN, pos + 1, 16);
			add(icon);
		}

		RenderedTextBlock name = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "row", unitName(def), def.cost, traitName(def.trait)), 8);
		name.maxWidth(WIDTH - MARGIN * 2 - 20);
		name.setPos(MARGIN + 19, pos + 1);
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
		buy.setRect(MARGIN + 19, pos + 14, 60, 14);
		add(buy);

		return pos + OFFER_ROW;
	}

	private static String unitName(AutochessPool.Def def) {
		com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob =
				com.watabou.utils.Reflection.newInstance(def.cls);
		return mob == null ? def.cls.getSimpleName() : mob.name();
	}

	private static String traitName(String trait) {
		return M.L(AutochessPool.class, "trait_" + trait);
	}

	private static String stars(int star) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < star; i++) {
			sb.append("★");
		}
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

	private static String alliesLine() {
		AutochessRun current = AutochessRun.get();
		return M.L(WndAutochessShop.class, "allies", current == null ? 0 : current.rosterSize());
	}
}
