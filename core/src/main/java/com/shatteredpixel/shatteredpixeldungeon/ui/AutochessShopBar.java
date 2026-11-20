package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessItems;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessItems;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessLobby;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessTraits;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 云顶风的底部商店栏：常驻在屏幕底部，不再用弹窗。
 *
 * <p>三行布局：</p>
 * <pre>
 * 第 N 回合 | 金币 X | LvN | 上阵 a/b      [锁] [卖N]
 * [刷新2] [经验4] [装备] [大厅] [羁绊] [开战]
 * [ 卡 ][ 卡 ][ 卡 ][ 卡 ][ 卡 ]
 * </pre>
 */
public class AutochessShopBar extends Component {

	private static final int MARGIN = 3;
	private static final int GAP = 2;
	private static final float H = 48;

	private static final int COLOR_GOLD = 0xFFFFD700;
	private static final int COLOR_CREAM = 0xFFF1E5B5;
	private static final int COLOR_DIM = 0xFF9A9A9A;

	private static final int[] COST_COLOR = {
			0xFFBBBBBB, 0xFF66DD66, 0xFF66AAFF, 0xFFCC66DD, 0xFFFFCC33
	};

	private final ArrayList<Component> components = new ArrayList<>();
	private final ArrayList<com.watabou.noosa.Visual> visuals = new ArrayList<>();
	private String lastSig = "";

	@Override
	public void update() {
		super.update();
		AutochessRun run = AutochessRun.get();
		if (run == null || PixelScene.uiCamera == null) {
			clearBuilt();
			visible = false;
			return;
		}

		boolean show = run.state() == AutochessRun.State.PREP;
		visible = show;
		if (!show) {
			clearBuilt();
			lastSig = "";
			return;
		}

		width = PixelScene.uiCamera.width;
		height = H;

		String sig = run.round() + "|" + run.gold() + "|" + run.level() + "|" + run.deployed()
				+ "|" + run.shopLocked() + "|" + run.unitVersion + "|" + run.selectedSide
				+ "|" + run.selectedIndex + "|" + run.itemBag().size() + "|" + (int) width;
		if (!sig.equals(lastSig)) {
			lastSig = sig;
			rebuild(run);
		}
	}

	private void clearBuilt() {
		for (Component c : components) c.killAndErase();
		for (com.watabou.noosa.Visual v : visuals) v.killAndErase();
		components.clear();
		visuals.clear();
	}

	private void rebuild(AutochessRun run) {
		clearBuilt();

		float w = width;
		float oy = PixelScene.uiCamera.height - H;

		RoundedFrame bg = new RoundedFrame(0xF009070B, DiceMageUI.GOLD);
		bg.setRect(0, oy, w, H);
		add(bg);
		components.add(bg);

		// ---------------- 第一行：状态 + 锁定 / 出售 ----------------
		String selInfo = "";
		if (run.selectedSide >= 0 && run.selectedIndex >= 0) {
			String cls = run.unitClass(run.selectedSide, run.selectedIndex);
			if (cls != null) {
				selInfo = " | " + AutochessRun.displayName(cls) + " "
						+ run.unitStar(run.selectedSide, run.selectedIndex) + "★";
				java.util.ArrayList<String> eq = run.equippedItems(run.selectedSide, run.selectedIndex);
				if (!eq.isEmpty()) {
					StringBuilder sb = new StringBuilder(" [");
					for (int k = 0; k < eq.size(); k++) {
						if (k > 0) sb.append(",");
						String nm = AutochessItems.name(eq.get(k));
						sb.append(nm.length() > 4 ? nm.substring(0, 4) : nm);
					}
					sb.append("]");
					selInfo += sb;
				}
			}
		}

		boolean narrow = w < 210;
		String infoText = narrow
				? M.L(AutochessShopBar.class, "info_short",
						run.round(), run.gold(), run.level(), run.deployed(), run.maxDeploy())
				: M.L(AutochessShopBar.class, "info",
						run.round(), run.gold(), run.level(), run.deployed(), run.maxDeploy());
		if (narrow && selInfo.length() > 12) selInfo = selInfo.substring(0, 12);
		RenderedTextBlock info = PixelScene.renderTextBlock(infoText + selInfo, narrow ? 5 : 6);
		info.hardlight(COLOR_CREAM);
		info.maxWidth((int) Math.max(40, w - 2 * MARGIN - 46));
		info.setPos(MARGIN, oy + 2);
		add(info);
		components.add(info);

		// 右上角两个小按钮：锁 / 卖
		float smallW = 21;
		float smallH = 9;
		float rx = w - MARGIN - smallW * 2 - GAP;
		buildButton(run.shopLocked()
						? M.L(AutochessShopBar.class, "unlock_short")
						: M.L(AutochessShopBar.class, "lock_short"),
				rx, oy + 1, smallW, smallH, () -> {
					AutochessRun r = AutochessRun.get();
					if (r != null) {
						r.toggleShopLock();
						lastSig = "";
					}
				});
		buildButton(M.L(AutochessShopBar.class, "sell_short",
						run.sellValue(run.selectedSide, run.selectedIndex)),
				rx + smallW + GAP, oy + 1, smallW, smallH, () -> {
					AutochessRun r = AutochessRun.get();
					if (r != null && r.selectedSide >= 0 && r.selectedIndex >= 0) {
						int got = r.sell(r.selectedSide, r.selectedIndex);
						if (got > 0) {
							r.clearSelection();
							lastSig = "";
						}
					}
				});

		// ---------------- 第二行：六个功能按钮 ----------------
		float by = oy + 12;
		float bw = (w - MARGIN * 2 - GAP * 5) / 6f;
		float bh = 10;

		buildButton(M.L(AutochessShopBar.class, "refresh", AutochessRun.REFRESH_COST),
				MARGIN, by, bw, bh, () -> {
					AutochessRun r = AutochessRun.get();
					if (r != null && r.refresh()) lastSig = "";
				});
		buildButton(M.L(AutochessShopBar.class, "xp", AutochessRun.XP_COST),
				MARGIN + (bw + GAP), by, bw, bh, () -> {
					AutochessRun r = AutochessRun.get();
					if (r != null && r.buyXp()) lastSig = "";
				});
		buildButton(M.L(AutochessShopBar.class, "items"),
				MARGIN + (bw + GAP) * 2, by, bw, bh,
				() -> GameScene.show(new WndAutochessItems()));
		buildButton(M.L(AutochessShopBar.class, "lobby"),
				MARGIN + (bw + GAP) * 3, by, bw, bh,
				() -> GameScene.show(new WndAutochessLobby()));
		buildButton(M.L(AutochessShopBar.class, "traits"),
				MARGIN + (bw + GAP) * 4, by, bw, bh,
				() -> GameScene.show(new WndAutochessTraits()));
		buildButton(M.L(AutochessShopBar.class, "fight"),
				MARGIN + (bw + GAP) * 5, by, bw, bh, () -> {
					AutochessRun r = AutochessRun.get();
					if (r != null) r.startFight();
				});

		// ---------------- 第三行：卡牌 ----------------
		float cy = by + bh + GAP;
		float ch = H - (cy - oy) - MARGIN;
		float cw = (w - MARGIN * 2 - GAP * 4) / 5f;

		for (int i = 0; i < AutochessRun.OFFER_COUNT; i++) {
			buildCard(run, i, MARGIN + i * (cw + GAP), cy, cw, ch);
		}
	}

	private void buildButton(String label, float x, float y, float w, float h, Runnable action) {
		DiceMageUI.DiceButton btn = new DiceMageUI.DiceButton(label, 6) {
			@Override
			protected void onClick() {
				action.run();
			}
		};
		btn.setRect(x, y, w, h);
		add(btn);
		components.add(btn);
	}

	private void buildCard(AutochessRun run, int index, float x, float y, float w, float h) {
		AutochessPool.Def def = run.offer(index);

		RoundedFrame card = new RoundedFrame(0xEE09070B, DiceMageUI.GREY_LINE);
		card.setRect(x, y, w, h);
		add(card);
		components.add(card);

		if (def == null) {
			RenderedTextBlock sold = PixelScene.renderTextBlock(M.L(AutochessShopBar.class, "sold"), 5);
			sold.hardlight(COLOR_DIM);
			sold.setPos(x + (w - sold.width()) / 2f, y + (h - sold.height()) / 2f);
			add(sold);
			components.add(sold);
			return;
		}

		int costColor = COST_COLOR[Math.max(0, Math.min(COST_COLOR.length - 1, def.cost - 1))];

		RoundedFrame border = new RoundedFrame(0x00000000, costColor);
		border.setRect(x, y, w, h);
		add(border);
		components.add(border);

		CharSprite icon = AutochessIcons.spriteFor(def.cls.getSimpleName());
		if (icon != null) {
			AutochessIcons.place(icon, x + (w - 12) / 2f, y + 1, 12);
			add(icon);
			visuals.add(icon);
		}

		String name = AutochessRun.displayName(def.cls.getSimpleName());
		if (name.length() > 3) name = name.substring(0, 3);
		RenderedTextBlock nameText = PixelScene.renderTextBlock(name, 5);
		nameText.hardlight(costColor);
		nameText.setPos(x + (w - nameText.width()) / 2f, y + h - 11);
		add(nameText);
		components.add(nameText);

		RenderedTextBlock price = PixelScene.renderTextBlock(def.cost + "g", 5);
		price.hardlight(COLOR_GOLD);
		price.setPos(x + (w - price.width()) / 2f, y + h - 6);
		add(price);
		components.add(price);

		PointerArea tap = new PointerArea(0, 0, w, h) {
			@Override
			protected void onClick(com.watabou.input.PointerEvent event) {
				AutochessRun r = AutochessRun.get();
				if (r != null && r.buy(index)) {
					lastSig = "";
				}
			}
		};
		tap.camera = PixelScene.uiCamera;
		tap.givePointerPriority();
		tap.x = x;
		tap.y = y;
		tap.width = w;
		tap.height = h;
		add(tap);
		visuals.add(tap);
	}
}
