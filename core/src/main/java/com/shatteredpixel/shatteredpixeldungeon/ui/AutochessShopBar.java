package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessLobby;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessTraits;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 云顶风的底部商店栏：常驻在屏幕底部，不再用弹窗。
 *
 * <p>上面一行状态（回合/金币/等级/上阵/倒计时），中间一排小按钮，
 * 下面 5 张卡，点卡直接买。备战阶段常显，战斗时隐藏。</p>
 */
public class AutochessShopBar extends Component {

	private static final int MARGIN = 3;
	private static final int GAP = 2;
	private static final float H = 52;

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
	protected void createChildren() {
		super.createChildren();
	}

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

		// 贴底铺满（子元素用绝对坐标，位置在 rebuild 里算）
		width = PixelScene.uiCamera.width;
		height = H;

		String sig = run.round() + "|" + run.gold() + "|" + run.level() + "|" + run.deployed()
				+ "|" + run.shopLocked() + "|" + run.unitVersion + "|" + run.selectedIndex
				+ "|" + run.itemBag().size();
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

		// ---------- 状态行 ----------
		RenderedTextBlock info = PixelScene.renderTextBlock(
				M.L(AutochessShopBar.class, "info",
						run.round(), run.gold(), run.level(), run.deployed(), run.maxDeploy()), 6);
		info.hardlight(COLOR_CREAM);
		info.setPos(MARGIN, oy + 2);
		add(info);
		components.add(info);

		// ---------- 按钮行 ----------
		float by = oy + 11;
		float bw = (w - MARGIN * 2 - GAP * 5) / 6f;
		float bh = 11;

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
		buildButton(run.shopLocked() ? M.L(AutochessShopBar.class, "unlock") : M.L(AutochessShopBar.class, "lock"),
				MARGIN + (bw + GAP) * 2, by, bw, bh, () -> {
					AutochessRun r = AutochessRun.get();
					if (r != null) {
						r.toggleShopLock();
						lastSig = "";
					}
				});
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

		// ---------- 卡牌行 ----------
		float cy = oy + 23;
		float ch = H - 23 - MARGIN;
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
		nameText.setPos(x + (w - nameText.width()) / 2f, y + h - 13);
		add(nameText);
		components.add(nameText);

		RenderedTextBlock price = PixelScene.renderTextBlock(def.cost + "g", 5);
		price.hardlight(COLOR_GOLD);
		price.setPos(x + (w - price.width()) / 2f, y + h - 7);
		add(price);
		components.add(price);

		com.watabou.noosa.PointerArea tap = new com.watabou.noosa.PointerArea(0, 0, w, h) {
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
