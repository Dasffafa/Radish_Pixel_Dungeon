package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessItems;
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
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

/**
 * 自走棋商店（云顶风：底部横条）。
 *
 * <p>上面一条状态 + 一排操作按钮，中间 5 张横排卡牌，下面装备栏和卖出。
 * 卡片直接点「购买」，装备点一下装到当前选中的棋子上。</p>
 */
public class WndAutochessShop extends Window {

	private static final int MARGIN = 3;
	private static final int GAP = 2;
	private static final int CARD_H = 54;

	private static final int COLOR_GOLD = 0xFFD700;
	private static final int COLOR_DIM = 0xAAAAAA;

	/** 费用对应的卡牌边框色（云顶那种一费白、五费金）。 */
	private static final int[] COST_COLOR = {
			0xBBBBBB, 0x66DD66, 0x66AAFF, 0xCC66DD, 0xFFCC33
	};

	private final int WIDTH;
	private final int CARD_W;
	private final AutochessRun run;

	private int lastSelSide = -2;
	private int lastSelIndex = -2;

	public WndAutochessShop(int round) {
		super();

		int camW = PixelScene.uiCamera == null ? 158 : (int) PixelScene.uiCamera.width;
		if (Boolean.getBoolean("arena.compact")) camW = 150;
		WIDTH = Math.min(camW - 6, 340);
		CARD_W = (WIDTH - MARGIN * 2 - GAP * 4) / 5;

		run = AutochessRun.get();
		if (run == null) {
			hide();
			return;
		}
		lastSelSide = run.selectedSide;
		lastSelIndex = run.selectedIndex;

		float y = MARGIN;

		// ---------- 状态条 ----------
		RenderedTextBlock header = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "hud",
						run.round(), run.gold(), run.life(), run.level(),
						run.deployed(), run.maxDeploy()), 7);
		header.hardlight(COLOR_GOLD);
		header.maxWidth(WIDTH - MARGIN * 2);
		header.setPos(MARGIN, y);
		add(header);
		y = header.bottom() + 1;

		// 经验条
		float barW = WIDTH - MARGIN * 2;
		ColorBlock barBg = new ColorBlock(barW, 2, 0xFF2A2A2A);
		barBg.x = MARGIN;
		barBg.y = y;
		add(barBg);
		float ratio = run.level() >= AutochessRun.MAX_LEVEL ? 1f
				: Math.max(0f, Math.min(1f, run.xp() / (float) Math.max(1, run.xpNeed())));
		ColorBlock barFill = new ColorBlock(barW * ratio, 2, 0xFF66DDFF);
		barFill.x = MARGIN;
		barFill.y = barBg.y;
		add(barFill);
		y = barBg.y + 4;

		// ---------- 操作按钮（一排 6 个）----------
		float btnW = (WIDTH - MARGIN * 2 - GAP * 5) / 6f;
		float bx = MARGIN;
		float bh = 13;

		RedButton refresh = new RedButton("刷新 " + AutochessRun.REFRESH_COST, 6) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.refresh()) rebuild();
			}
		};
		refresh.setRect(bx, y, btnW, bh);
		add(refresh);
		bx += btnW + GAP;

		RedButton xp = new RedButton("经验 " + AutochessRun.XP_COST, 6) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.buyXp()) rebuild();
			}
		};
		xp.setRect(bx, y, btnW, bh);
		add(xp);
		bx += btnW + GAP;

		RedButton lock = new RedButton(run.shopLocked() ? "开锁" : "锁定", 6) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current == null) return;
				current.toggleShopLock();
				rebuild();
			}
		};
		lock.setRect(bx, y, btnW, bh);
		add(lock);
		bx += btnW + GAP;

		RedButton lobby = new RedButton("大厅", 6) {
			@Override
			protected void onClick() {
				GameScene.show(new WndAutochessLobby());
			}
		};
		lobby.setRect(bx, y, btnW, bh);
		add(lobby);
		bx += btnW + GAP;

		RedButton traitsBtn = new RedButton("羁绊", 6) {
			@Override
			protected void onClick() {
				GameScene.show(new WndAutochessTraits());
			}
		};
		traitsBtn.setRect(bx, y, btnW, bh);
		add(traitsBtn);
		bx += btnW + GAP;

		RedButton fight = new RedButton("开战", 7) {
			@Override
			protected void onClick() {
				hide();
				AutochessRun current = AutochessRun.get();
				if (current != null) current.startFight();
			}
		};
		fight.setRect(bx, y, btnW, bh);
		add(fight);

		y += bh + GAP;

		// ---------- 5 张卡（横排）----------
		for (int i = 0; i < AutochessRun.OFFER_COUNT; i++) {
			addCard(i, MARGIN + i * (CARD_W + GAP), y);
		}
		y += CARD_H + GAP;

		// ---------- 装备栏 ----------
		java.util.ArrayList<String> bag = new java.util.ArrayList<>(run.itemBag());
		if (!bag.isEmpty()) {
			float chipW = Math.min(46, (WIDTH - MARGIN * 2 - GAP * 5) / 6f);
			float cx = MARGIN;
			int shown = 0;
			for (String id : bag) {
				if (shown >= 6) break;
				RedButton chip = new RedButton(AutochessItems.name(id), 6) {
					@Override
					protected void onClick() {
						AutochessRun current = AutochessRun.get();
						if (current == null) return;
						if (current.selectedSide < 0 || current.selectedIndex < 0) {
							com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
									M.L(WndAutochessShop.class, "no_selection"));
							return;
						}
						if (current.equipItem(current.selectedSide, current.selectedIndex, id)) {
							rebuild();
						}
					}
				};
				chip.setRect(cx, y, chipW, 11);
				add(chip);
				cx += chipW + GAP;
				shown++;
			}
			y += 11 + GAP;
		}

		// ---------- 底部：卖出 / 提示 ----------
		if (run.selectedSide >= 0 && run.selectedIndex >= 0) {
			final int side = run.selectedSide;
			final int index = run.selectedIndex;
			RedButton sell = new RedButton("卖出（+" + run.sellValue(side, index) + " 金）", 6) {
				@Override
				protected void onClick() {
					AutochessRun current = AutochessRun.get();
					if (current == null) return;
					int got = current.sell(side, index);
					if (got > 0) {
						current.selectedSide = -1;
						current.selectedIndex = -1;
						com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i("卖出获得 " + got + " 金");
						rebuild();
					}
				}
			};
			sell.setRect(MARGIN, y, (WIDTH - MARGIN * 2 - GAP) / 2f, 12);
			add(sell);

			RenderedTextBlock hint = PixelScene.renderTextBlock(
					M.L(WndAutochessShop.class, "select_hint"), 6);
			hint.hardlight(COLOR_DIM);
			hint.maxWidth((int) ((WIDTH - MARGIN * 2 - GAP) / 2f));
			hint.setPos(MARGIN + (WIDTH - MARGIN * 2 - GAP) / 2f + GAP, y + 1);
			add(hint);
			y += 12 + GAP;
		} else {
			RenderedTextBlock hint = PixelScene.renderTextBlock(
					M.L(WndAutochessShop.class, "select_hint"), 6);
			hint.hardlight(COLOR_DIM);
			hint.maxWidth(WIDTH - MARGIN * 2);
			hint.setPos(MARGIN, y);
			add(hint);
			y += hint.height() + GAP;
		}

		resize(WIDTH, (int) Math.ceil(y + MARGIN));
	}

	/** 一张卡：边框按费用着色，上面图标、名字、羁绊，下面是购买按钮。 */
	private void addCard(int index, float x, float y) {
		AutochessPool.Def def = run.offer(index);

		RoundedFrame card = new RoundedFrame(0xCC1E1E1E, 0xFF444444);
		card.setRect(x, y, CARD_W, CARD_H);
		add(card);

		if (def == null) {
			RenderedTextBlock sold = PixelScene.renderTextBlock("已购买", 6);
			sold.hardlight(COLOR_DIM);
			sold.setPos(x + (CARD_W - sold.width()) / 2f, y + CARD_H / 2f - 3);
			add(sold);
			return;
		}

		int costColor = COST_COLOR[Math.max(0, Math.min(COST_COLOR.length - 1, def.cost - 1))];
		RoundedFrame border = new RoundedFrame(0x00000000, costColor);
		border.setRect(x, y, CARD_W, CARD_H);
		add(border);

		CharSprite icon = AutochessIcons.spriteFor(def.cls.getSimpleName());
		if (icon != null) {
			AutochessIcons.place(icon, x + (CARD_W - 18) / 2f, y + 2, 18);
			add(icon);
		}

		String name = AutochessRun.displayName(def.cls.getSimpleName());
		if (name.length() > 4) name = name.substring(0, 4);   // 卡片窄，名字截到 4 个字
		RenderedTextBlock nameText = PixelScene.renderTextBlock(name, 5);
		nameText.hardlight(costColor);
		nameText.maxWidth(CARD_W);
		nameText.setPos(x + (CARD_W - nameText.width()) / 2f, y + 22);
		add(nameText);

		// 卡片窄，只显示第一个羁绊
		String traitStr = def.traits.length > 0
				? com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
						AutochessPool.class, "trait_" + def.traits[0])
				: "";
		RenderedTextBlock traitText = PixelScene.renderTextBlock(traitStr, 5);
		traitText.hardlight(COLOR_DIM);
		traitText.maxWidth(CARD_W - 2);
		traitText.setPos(x + (CARD_W - traitText.width()) / 2f, y + 31);
		add(traitText);

		RedButton buy = new RedButton(def.cost + " 金", 6) {
			@Override
			protected void onClick() {
				AutochessRun current = AutochessRun.get();
				if (current != null && current.buy(index)) rebuild();
			}
		};
		buy.setRect(x + 1, y + CARD_H - 12, CARD_W - 2, 11);
		add(buy);
	}

	@Override
	public void update() {
		super.update();
		// 窗口内部布局本身就是横条，位置保持居中；不再改 camera，避免把窗口推出屏幕
		AutochessRun current = AutochessRun.get();
		if (current != null
				&& (current.selectedSide != lastSelSide || current.selectedIndex != lastSelIndex)) {
			lastSelSide = current.selectedSide;
			lastSelIndex = current.selectedIndex;
			rebuild();
		}
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
}
