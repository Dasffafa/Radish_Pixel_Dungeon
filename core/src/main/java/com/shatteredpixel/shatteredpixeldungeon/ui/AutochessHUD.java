package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

/**
 * 自走棋顶部状态条，同时兼任「自动回合」的节拍器。
 *
 * <p>这游戏是回合制，没人行动就不走回合。战斗阶段由这里每 0.34 秒替英雄空过一回合，
 * 顺带让英雄朝最近的敌人开火——这样双方 AI 才能真的打起来。</p>
 */
public class AutochessHUD extends Component {

	public static final float WIDTH = 168;
	public static final float HEIGHT = 16;

	private static final float TURN_INTERVAL = 0.16f;
	private static final int COLOR_NORMAL = 0xFFFFCC;
	private static final int COLOR_FIGHT = 0xFF9060;

	private RoundedFrame frame;
	private RenderedTextBlock label;
	private com.watabou.noosa.PointerArea tap;
	private String shown = "";

	private static final com.watabou.input.PointerEvent.Type[] NO_TYPES = null;

	private float acc = 0f;
	private boolean revealed = false;

	public AutochessHUD() {
		super();
		setSize(WIDTH, HEIGHT);
	}

	@Override
	protected void createChildren() {
		frame = new RoundedFrame(0xCC1A1A1A, 0xFFFF66);
		add(frame);

		label = PixelScene.renderTextBlock(7);
		label.maxWidth((int) WIDTH - 8);
		label.hardlight(COLOR_NORMAL);
		add(label);

		// 点顶部状态条随时打开备战界面（关掉商店后不用等下一回合）
		tap = new com.watabou.noosa.PointerArea(0, 0, WIDTH, HEIGHT) {
			@Override
			protected void onClick(com.watabou.input.PointerEvent event) {
				AutochessRun run = AutochessRun.get();
				if (run != null && run.state() == AutochessRun.State.PREP) {
					com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
							new com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessShop(run.round()));
				}
			}
		};
		tap.givePointerPriority();
		add(tap);
	}

	@Override
	protected void layout() {
		frame.setRect(x, y, WIDTH, HEIGHT);
		if (tap != null) {
			tap.x = x;
			tap.y = y;
			tap.width = WIDTH;
			tap.height = HEIGHT;
		}
		centerLabel();
	}

	/** 把整张棋盘标记为已探索并刷新地图与迷雾，否则玩家看到的是一片黑。 */
	private void revealBoard() {
		if (Dungeon.level == null) {
			return;
		}
		for (int i = 0; i < Dungeon.level.length(); i++) {
			Dungeon.level.mapped[i] = true;
			Dungeon.level.visited[i] = true;
		}
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap();
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateFog();
		Dungeon.observe();
	}

	/** 把 heroFOV 全置真并刷新迷雾与棋子可见性。 */
	private void keepFullVision() {
		if (Dungeon.level == null || Dungeon.level.heroFOV == null
				|| Dungeon.level.heroFOV.length != Dungeon.level.length()) {
			return;
		}
		boolean changed = false;
		for (int i = 0; i < Dungeon.level.heroFOV.length; i++) {
			if (!Dungeon.level.heroFOV[i]) {
				Dungeon.level.heroFOV[i] = true;
				changed = true;
			}
		}
		if (!changed) {
			return;
		}
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateFog();
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob :
				Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
			if (mob.sprite != null) {
				mob.sprite.visible = true;
			}
		}
		if (Dungeon.hero != null && Dungeon.hero.sprite != null) {
			Dungeon.hero.sprite.visible = true;
		}
	}

	private void centerLabel() {
		if (label == null) return;
		label.setPos(x + (WIDTH - label.width()) / 2f, y + (HEIGHT - label.height()) / 2f);
		PixelScene.align(label);
	}

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null) {
			return;
		}

		// 英雄坐在封闭包间里，视野只有一小块；场景建好后的第一帧把整张棋盘点亮
		if (!revealed) {
			revealed = true;
			revealBoard();
			// 场景建好之后再铺预览，避免在 create 阶段 spawn 出没有 sprite 的怪
			if (run.state() == AutochessRun.State.PREP) {
				run.refreshPreview();
			}




		}
		// 之后每帧继续把视野撑满：怪物和迷雾都按 heroFOV 判断可见性，
		// 不这么做的话棋子会在包间外面「隐身」，只看得到日志说谁死了
		keepFullVision();

		String text = M.L(AutochessHUD.class, "format",
				run.round(), run.gold(), run.life(), run.level(),
				run.deployed(), run.maxDeploy(),
				M.L(AutochessHUD.class, run.state() == AutochessRun.State.FIGHT ? "fighting" : "prep"));
		if (!text.equals(shown)) {
			shown = text;
			label.text(text);
			centerLabel();
		}
		label.hardlight(run.state() == AutochessRun.State.FIGHT ? COLOR_FIGHT : COLOR_NORMAL);

		if (run.state() == AutochessRun.State.FIGHT) {
			acc += Game.elapsed;
			if (acc >= TURN_INTERVAL) {
				acc = 0f;
				// 英雄只观战：这里只负责把回合推进器踩下去，让双方 AI 自己打
				if (Dungeon.hero != null && Dungeon.hero.ready) {
					Dungeon.hero.spendAndNext(1f);
				}
			}
			run.checkFight();
		} else {
			acc = 0f;
		}
	}
}
