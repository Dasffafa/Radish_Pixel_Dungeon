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

	public static float WIDTH = 168;
	public static final float HEIGHT = 16;

	private static final float TURN_INTERVAL = 0.45f;   // 每回合之间的间隔：太短会看不清战斗过程
	private static final int COLOR_NORMAL = 0xFFFFCC;
	private static final int COLOR_FIGHT = 0xFF9060;

	private RoundedFrame frame;
	private RenderedTextBlock label;
	private com.watabou.noosa.PointerArea tap;
	private String shown = "";

	private static final com.watabou.input.PointerEvent.Type[] NO_TYPES = null;

	private float acc = 0f;
	private boolean revealed = false;

	/** 屏幕中上方的短提示（胜利/战败/掉落等）。 */
	private static AutochessHUD active = null;
	private RenderedTextBlock bannerLabel;
	private String bannerText = null;
	private float bannerTimer = 0f;
	private int bannerColor = 0xFFFFFF;

	/** 弹一条提示，1.6 秒后淡出。 */
	public static void banner(String text, int color) {
		if (active == null) return;
		active.bannerText = text;
		active.bannerColor = color;
		active.bannerTimer = 1.6f;
	}

	public AutochessHUD() {
		super();
		active = this;
		float camWidth = PixelScene.uiCamera == null ? WIDTH : PixelScene.uiCamera.width;
		WIDTH = Math.min(168f, Math.max(110f, camWidth - 10f));
		setSize(WIDTH, HEIGHT);
	}

	@Override
	public void destroy() {
		if (active == this) active = null;
		super.destroy();
	}

	@Override
	protected void createChildren() {
		frame = new RoundedFrame(0xCC1A1A1A, 0xFFFF66);
		add(frame);

		label = PixelScene.renderTextBlock(7);
		label.maxWidth((int) WIDTH - 8);
		label.hardlight(COLOR_NORMAL);
		add(label);

		bannerLabel = PixelScene.renderTextBlock("", 9);
		bannerLabel.hardlight(0xFFFFFF);
		bannerLabel.visible = false;
		add(bannerLabel);

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
	/**
	 * 观战者模式：英雄待在包间里不动、不饿、不掉血；
	 * 备战阶段让场上所有棋子休眠，避免玩家一动它们就自己打起来。
	 */
	private void spectate(AutochessRun run) {
		if (Dungeon.hero == null || Dungeon.level == null) return;

		int booth = com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel.spectatorCell();
		if (Dungeon.hero.pos != booth) {
			Dungeon.hero.pos = booth;
			if (Dungeon.hero.sprite != null) Dungeon.hero.sprite.place(booth);
			Dungeon.hero.sprite.idle();
		}
		Dungeon.hero.HP = Dungeon.hero.HT;
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(
				Dungeon.hero,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.class);

		// 战斗中：把盯着观战者的敌人改成盯己方棋子，否则它们会卡在包间外永远打不完
		if (run.state() == AutochessRun.State.FIGHT) {
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob
					: Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
				if (!mob.isAlive() || mob.targetPos() != booth) continue;
				com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob best = null;
				for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob other
						: Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
					if (!other.isAlive()) continue;
					if (other.alignment != com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY) continue;
					if (best == null || Dungeon.level.distance(mob.pos, other.pos)
							< Dungeon.level.distance(mob.pos, best.pos)) {
						best = other;
					}
				}
				mob.retarget(best != null ? best.pos : -1);
			}
		}

		// 清掉棋子头顶的情绪气泡（"?"/"!"），它们会让画面很乱
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob
				: Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
			if (!mob.isAlive()) continue;
			if (run.state() == AutochessRun.State.PREP) mob.state = mob.SLEEPING;
			if (mob.sprite != null) mob.sprite.hideEmo();
		}
	}

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
		spectate(run);

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

		// 顶部提示条（胜利/战败……）
		if (bannerTimer > 0f && bannerText != null) {
			bannerTimer -= Game.elapsed;
			if (bannerTimer <= 0f) {
				bannerLabel.visible = false;
			} else {
				if (!bannerText.equals(bannerLabel.text())) {
					bannerLabel.text(bannerText);
				}
				bannerLabel.hardlight(bannerColor);
				bannerLabel.visible = true;
				bannerLabel.setPos(x + (width() - bannerLabel.width()) / 2f, y + height() + 3);
				bannerLabel.alpha(Math.min(1f, bannerTimer / 0.5f));
			}
		}

		if (run.state() == AutochessRun.State.FIGHT) {
			acc += Game.elapsed;
			if (acc >= TURN_INTERVAL) {
				acc = 0f;
				// 英雄只观战：这里只负责把回合推进器踩下去，让双方 AI 自己打
				if (Dungeon.hero != null && Dungeon.hero.ready) {
					Dungeon.hero.spendAndNext(1f);
				}
			}
			run.tickFight();
			run.checkFight();
		} else {
			acc = 0f;
		}
	}
}
