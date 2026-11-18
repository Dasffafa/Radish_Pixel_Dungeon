package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessBot;
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
 * 自走棋商店：买棋子、买经验、刷新、开战、卖出地图上选中的棋子。
 *
 * <p>摆位和拖拽都在地图上做，这个界面只负责花钱。</p>
 */
public class WndAutochessShop extends Window {

	private static final int WIDTH = 158;
	private static final int MARGIN = 5;
	private static final int OFFER_ROW = 30;
	private static final int GAP = 3;

	private static final int COLOR_TITLE = 0xFFFF44;
	private static final int COLOR_GOLD = 0xFFD700;
	private static final int COLOR_DIM = 0xAAAAAA;
	private static final int COLOR_SECTION = 0x99CCFF;

	private final AutochessRun run;

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

		// 经验条（云顶同款）
		float barW = WIDTH - MARGIN * 2;
		ColorBlock barBg = new ColorBlock(barW, 3, 0xFF2A2A2A);
		barBg.x = MARGIN;
		barBg.y = info.bottom() + 1;
		add(barBg);
		float ratio = run.level() >= AutochessRun.MAX_LEVEL ? 1f
				: Math.max(0f, Math.min(1f, run.xp() / (float) Math.max(1, run.xpNeed())));
		ColorBlock barFill = new ColorBlock(barW * ratio, 3, 0xFF66DDFF);
		barFill.x = MARGIN;
		barFill.y = barBg.y;
		add(barFill);

		RenderedTextBlock odds = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "odds_title") + " " + oddsLine(run.level()), 7);
		odds.hardlight(COLOR_GOLD);
		odds.maxWidth(WIDTH - MARGIN * 2);
		odds.setPos(MARGIN, barBg.y + 6);
		add(odds);

		float pos = odds.bottom() + 1;

		// 本轮对手 / 剩余 AI：点一下看对方阵容
		AutochessBot opp = run.opponent();
		RedButton vs = new RedButton(M.L(WndAutochessShop.class, "matchup",
				opp == null ? "-" : (opp.name + "（" + opp.hp + "血）"),
				run.aliveBots(), Math.max(1, run.bots().size())), 7) {
			@Override
			protected void onClick() {
				AutochessBot current = AutochessRun.get() == null ? null : AutochessRun.get().opponent();
				if (current != null) {
					com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
							new WndAutochessOpponent(current));
				}
			}
		};
		vs.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 14);
		add(vs);
		pos = vs.bottom() + 2;

		// 对战大厅：看所有 AI 的本轮对局
		RedButton lobby = new RedButton(M.L(WndAutochessShop.class, "lobby"), 7) {
			@Override
			protected void onClick() {
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
						new WndAutochessLobby());
			}
		};
		lobby.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 14);
		add(lobby);
		pos = lobby.bottom() + GAP;

		RenderedTextBlock legend = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "legend"), 7);
		legend.maxWidth(WIDTH - MARGIN * 2);
		legend.hardlight(COLOR_SECTION);
		legend.setPos(MARGIN, pos);
		add(legend);
		pos = legend.bottom() + GAP;

		pos = addSelectionRow(pos);

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

		for (int i = 0; i < AutochessRun.OFFER_COUNT; i++) {
			pos = addOfferRow(i, pos);
		}

		// 装备背包
		RenderedTextBlock eqTitle = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "items"), 7);
		eqTitle.hardlight(COLOR_SECTION);
		eqTitle.setPos(MARGIN, pos);
		add(eqTitle);
		pos = eqTitle.bottom() + 1;

		float halfEq = (WIDTH - MARGIN * 2 - GAP) / 2f;
		int eqIndex = 0;
		for (String itemId : new java.util.ArrayList<>(run.itemBag())) {
			final String id = itemId;
			float x = MARGIN + (eqIndex % 2) * (halfEq + GAP);
			float y = pos + (eqIndex / 2) * 15;

			RoundedFrame box = new RoundedFrame(0xCC333333, 0xFFCC66);
			box.setRect(x, y, halfEq, 13);
			add(box);

			RenderedTextBlock label = PixelScene.renderTextBlock(
					com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessItems.name(id), 7);
			label.maxWidth((int) halfEq - 4);
			label.setPos(x + 2, y + 2);
			add(label);

			com.watabou.noosa.PointerArea area = new com.watabou.noosa.PointerArea(0, 0, halfEq, 13) {
				private float downX, downY;
				private boolean dragged;

				@Override
				protected void onPointerDown(com.watabou.input.PointerEvent event) {
					downX = event.current.x;
					downY = event.current.y;
					dragged = false;
				}

				@Override
				protected void onDrag(com.watabou.input.PointerEvent event) {
					if (Math.abs(event.current.x - downX) > 8 || Math.abs(event.current.y - downY) > 8) {
						dragged = true;
					}
				}

				@Override
				protected void onPointerUp(com.watabou.input.PointerEvent event) {
					AutochessRun current = AutochessRun.get();
					if (current == null) return;
					if (dragged) {
						int cell = cellAt(event.current.x, event.current.y);
						if (cell >= 0 && current.equipItemAtCell(cell, id)) {
							rebuild();
							return;
						}
					}
					if (current.selectedSide < 0 || current.selectedIndex < 0) {
						com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(M.L(WndAutochessShop.class, "no_selection"));
						return;
					}
					if (current.equipItem(current.selectedSide, current.selectedIndex, id)) {
						rebuild();
					}
				}
			};
			area.camera = PixelScene.uiCamera;
			area.x = x;
			area.y = y;
			add(area);
			eqIndex++;
		}
		if (eqIndex > 0) {
			pos += ((eqIndex + 1) / 2) * 15 + GAP;
		} else {
			RenderedTextBlock noEq = PixelScene.renderTextBlock(
					M.L(WndAutochessShop.class, "no_items"), 7);
			noEq.hardlight(COLOR_SECTION);
			noEq.setPos(MARGIN, pos);
			add(noEq);
			pos = noEq.bottom() + GAP;
		}
		RenderedTextBlock economy = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "economy",
						run.winStreak(), run.loseStreak(), run.lastIncome()), 7);
		economy.hardlight(COLOR_GOLD);
		economy.setPos(MARGIN, pos + 1);
		add(economy);
		pos = economy.bottom() + 1;

		RenderedTextBlock traits = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "traits", traitLine()), 7);
		traits.maxWidth(WIDTH - MARGIN * 2);
		traits.hardlight(COLOR_DIM);
		traits.setPos(MARGIN, pos + 1);
		add(traits);

		pos = traits.bottom() + GAP;

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

		resize(WIDTH, (int) fight.bottom() + MARGIN);
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

	/** 有选中棋子就显示「卖出」，没选中就提示怎么操作。 */
	private float addSelectionRow(float pos) {
		int side = run.selectedSide;
		int index = run.selectedIndex;

		if (side < 0 || run.unitClass(side, index) == null) {
			RenderedTextBlock hint = PixelScene.renderTextBlock(
					M.L(WndAutochessShop.class, "select_hint"), 7);
			hint.maxWidth(WIDTH - MARGIN * 2);
			hint.hardlight(COLOR_DIM);
			hint.setPos(MARGIN, pos);
			add(hint);
			return hint.bottom() + GAP;
		}

		RedButton sell = new RedButton(
				M.L(WndAutochessShop.class, "sell", run.unitName(side, index),
						stars(run.unitStar(side, index)), run.sellValue(side, index)), 8) {
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
		sell.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 16);
		add(sell);
		return sell.bottom() + GAP;
	}

	private float addOfferRow(int index, float pos) {

		AutochessPool.Def def = run.offer(index);

		if (def == null) {
			RenderedTextBlock sold = PixelScene.renderTextBlock(M.L(WndAutochessShop.class, "sold"), 7);
			sold.hardlight(COLOR_DIM);
			sold.setPos(MARGIN, pos + 8);
			add(sold);
			return pos + OFFER_ROW;
		}

		// 费用底色卡片
		RoundedFrame card = new RoundedFrame(costFill(def.cost), costLine(def.cost));
		card.setRect(MARGIN - 2, pos - 1, WIDTH - MARGIN * 2 + 4, OFFER_ROW - 3);
		add(card);

		// 详情按钮
		RedButton infoBtn = new RedButton("i", 8) {
			@Override
			protected void onClick() {
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
						new WndAutochessInfo(def));
			}
		};
		infoBtn.setRect(WIDTH - MARGIN - 14, pos + 1, 14, 12);
		add(infoBtn);

		CharSprite icon = AutochessIcons.spriteFor(def);
		if (icon != null) {
			AutochessIcons.place(icon, MARGIN, pos + 1, 15);
			add(icon);
		}

		RenderedTextBlock name = PixelScene.renderTextBlock(
				M.L(WndAutochessShop.class, "row", nameOf(def), def.cost, traitsOf(def), skillOf(def)), 8);
		name.hardlight(costColor(def.cost));
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
		try {
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob =
					com.watabou.utils.Reflection.newInstance(def.cls);
			if (mob != null) return mob.name();
		} catch (Exception ignored) {
			// 有些棋子类不能直接实例化，退回类名，别让商店崩掉
		}
		return def.cls.getSimpleName();
	}

	/** 费用卡片的底色（深色，保证文字看得清）。 */
	private static int costFill(int cost) {
		switch (cost) {
			case 1: return 0xCC353535;
			case 2: return 0xCC123012;
			case 3: return 0xCC102A44;
			case 4: return 0xCC2A1244;
			default: return 0xCC442200;
		}
	}

	/** 费用卡片的描边色。 */
	private static int costLine(int cost) {
		switch (cost) {
			case 1: return 0xFFC8C8C8;
			case 2: return 0xFF66DD66;
			case 3: return 0xFF66AAFF;
			case 4: return 0xFFCC77FF;
			default: return 0xFFFFAA33;
		}
	}

	/** 不同费用用不同颜色：1 灰、2 绿、3 蓝、4 紫、5 橙。 */
	private static int costColor(int cost) {
		switch (cost) {
			case 1: return 0xBBBBBB;
			case 2: return 0x66DD66;
			case 3: return 0x66AAFF;
			case 4: return 0xCC77FF;
			default: return 0xFFAA33;
		}
	}

	/** 「1费65% 2费25% ...」这种概率行。 */
	private static String oddsLine(int level) {
		int[] pct = AutochessPool.oddsForLevel(level);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pct.length; i++) {
			if (i > 0) sb.append(" · ");
			sb.append(i + 1).append("费").append(pct[i]).append("%");
		}
		return sb.toString();
	}

	/** 「重击(60)」这种技能展示。 */
	private static String skillOf(AutochessPool.Def def) {
		return def.skill == null ? "-" : def.skill.name() + "(" + def.skill.mana + ")";
	}

	/** 「兽群·虫群」这种多羁绊展示。 */
	private static String traitsOf(AutochessPool.Def def) {
		StringBuilder sb = new StringBuilder();
		for (String t : def.traits) {
			if (sb.length() > 0) sb.append("·");
			sb.append(traitName(t));
		}
		return sb.toString();
	}

	/** 屏幕坐标 → 棋盘格子。 */
	private static int cellAt(float screenX, float screenY) {
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.level == null) return -1;
		com.watabou.utils.PointF p = com.watabou.noosa.Camera.main.screenToCamera((int) screenX, (int) screenY);
		int cx = (int) (p.x / com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.SIZE);
		int cy = (int) (p.y / com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.SIZE);
		if (cx < 0 || cy < 0
				|| cx >= com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.width()
				|| cy >= com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.height()) return -1;
		return cy * com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.width() + cx;
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
		for (String trait : new String[]{
				AutochessPool.BEAST, AutochessPool.HUMANOID, AutochessPool.CONSTRUCT,
				AutochessPool.DEMON, AutochessPool.UNDEAD, AutochessPool.SWARM,
				AutochessPool.TANK, AutochessPool.ASSASSIN,
				AutochessPool.RANGED, AutochessPool.FIGHTER,
				AutochessPool.MAGE, AutochessPool.SUPPORT}) {
			if (AutochessRun.countTrait(trait) > 0) {
				appendTrait(sb, trait);
			}
		}
		if (sb.length() == 0) {
			sb.append(M.L(WndAutochessShop.class, "no_trait"));
		}
		return sb.toString();
	}

	/** 「兽群 4 (3/5)」这种写法：数字是当前人数，括号里是两档阈值。 */
	private static void appendTrait(StringBuilder sb, String trait) {
		int count = AutochessRun.countTrait(trait);
		int[] th = AutochessPool.thresholds(trait);
		if (sb.length() > 0) sb.append("  ·  ");
		sb.append(traitName(trait)).append(" ").append(count)
				.append("(").append(th[0]).append("/").append(th[1]).append(")");
	}
}
