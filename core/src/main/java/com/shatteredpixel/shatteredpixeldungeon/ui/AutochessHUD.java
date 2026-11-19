package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.watabou.noosa.ColorBlock;
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
	public static final float HEIGHT = 20;

	private static final float TURN_INTERVAL = 0.45f;   // 每回合之间的间隔：太短会看不清战斗过程
	private static final int COLOR_NORMAL = 0xF1E5B5;   // DiceMage 奶油色
	private static final int COLOR_FIGHT = 0xE8A24A;

	private RoundedFrame frame;
	private RoundedFrame avatarFrame;
	private CharSprite avatar;
	private String avatarCls = null;
	private ColorBlock hpBg;
	private ColorBlock hpFill;
	private RenderedTextBlock subLabel;
	private RenderedTextBlock fieldLabel;
	private RenderedTextBlock label;
	private com.watabou.noosa.PointerArea tap;
	private String shown = "";

	private static final com.watabou.input.PointerEvent.Type[] NO_TYPES = null;

	private float acc = 0f;
	private int stuckTicks = 0;
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
		frame = new RoundedFrame(0xEE09070B, DiceMageUI.GOLD);
		add(frame);

		avatarFrame = new RoundedFrame(0xCC222222, 0xFFFFCC66);
		add(avatarFrame);

		hpBg = new ColorBlock(14, 2, 0xFF302020);
		add(hpBg);
		hpFill = new ColorBlock(14, 2, 0xFF66DD66);
		add(hpFill);

		subLabel = PixelScene.renderTextBlock(5);
		subLabel.hardlight(0xE8C13A);
		add(subLabel);

		fieldLabel = PixelScene.renderTextBlock(5);
		fieldLabel.hardlight(0x66CCFF);
		add(fieldLabel);

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
				// 商店现在是底部常驻栏，点状态条不再弹窗
			}
		};
		tap.givePointerPriority();
		add(tap);
	}

	@Override
	protected void layout() {
		frame.setRect(x, y, WIDTH, HEIGHT);
		avatarFrame.setRect(x + 2, y + 2, 14, 14);
		hpBg.x = x + 1;
		hpBg.y = y + 17;
		hpFill.x = x + 1;
		hpFill.y = y + 17;
		if (label != null) {
			label.maxWidth((int) Math.max(40, WIDTH - 26));
			label.setPos(x + 18, y + 1);
		}
		if (subLabel != null) subLabel.setPos(x + 18, y + 8);
		if (fieldLabel != null) fieldLabel.setPos(x + 18, y + 14);
		if (tap != null) {
			tap.x = x;
			tap.y = y;
			tap.width = WIDTH;
			tap.height = HEIGHT;
		}
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

	/** 备战阶段的待机呼吸：棋子轻微上下浮动。 */
	private final java.util.HashMap<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob, Float> breathBase =
			new java.util.HashMap<>();
	private final java.util.HashMap<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob, Float> breathPhase =
			new java.util.HashMap<>();
	private float breathTime = 0f;
	/** 回合切换时的状态条脉冲（1 → 0）。 */
	private float roundPulse = 0f;
	private int lastRoundSeen = -1;
	/** 准备阶段倒计时（秒），到 0 自动开战。 */
	private float prepLeft = -1f;
	private static final float PREP_TIME = 30f;
	/** 上一次看到的羁绊档位，用来检测「刚激活」。 */
	private final java.util.HashMap<String, Integer> lastTiers = new java.util.HashMap<>();

	private void idleBreath(AutochessRun run) {
		breathTime += Game.elapsed;
		if (Dungeon.level == null) {
			breathBase.clear();
			breathPhase.clear();
			return;
		}
		boolean prep = run.state() == AutochessRun.State.PREP;
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob
				: Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
			if (!mob.isAlive() || mob.sprite == null) {
				Float b = breathBase.remove(mob);
				breathPhase.remove(mob);
				if (b != null && mob.sprite != null) mob.sprite.y = b;
				continue;
			}
			boolean ours = mob.buff(com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessUnitBuff.class) != null
					|| mob.buff(com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessFoeBuff.class) != null;
			if (!ours || !prep) {
				Float b = breathBase.remove(mob);
				breathPhase.remove(mob);
				if (b != null) mob.sprite.y = b;
				continue;
			}
			Float base = breathBase.get(mob);
			if (base == null) {
				base = mob.sprite.y;
				breathBase.put(mob, base);
				breathPhase.put(mob, (float) (Math.random() * Math.PI * 2));
			}
			Float ph = breathPhase.get(mob);
			mob.sprite.y = base + (float) Math.sin(breathTime * 1.8f + (ph == null ? 0f : ph)) * 1.2f;
		}
	}

	/** 棋盘上不该有掉落物：每帧都清一遍，旧存档里的遗留物也会被扫掉。 */
	private void clearGroundLoot() {
		if (Dungeon.level != null && Dungeon.level.heaps != null && Dungeon.level.heaps.size > 0) {
			Dungeon.level.heaps.clear();
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
		clearGroundLoot();
		idleBreath(run);
		spectate(run);

		// 回合切换：状态条脉冲 + 顶部横幅
		if (run.round() != lastRoundSeen) {
			lastRoundSeen = run.round();
			roundPulse = 1f;
			prepLeft = PREP_TIME;
			banner(M.L(AutochessHUD.class, "round_banner", run.round()), 0xFFD58A);
		}
		// ------- 准备阶段倒计时：到 0 自动开战 / 补位 / 判负 -------
		if (run.state() == AutochessRun.State.PREP) {
			if (prepLeft < 0f) prepLeft = PREP_TIME;
			prepLeft -= Game.elapsed;
			if (prepLeft <= 0f) {
				prepLeft = PREP_TIME;
				run.autoDeployBench();
				if (run.deployed() > 0) {
					banner(M.L(AutochessHUD.class, "auto_fight"), 0xFFD58A);
					run.startFight();
				} else {
					banner(M.L(AutochessHUD.class, "no_units_lose"), 0xFF8844);
					run.forfeitEmpty();
				}
			}
		} else {
			prepLeft = -1f;
		}

		// ------- 羁绊刚激活时提示 -------
		for (String trait : new String[] {
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.BEAST,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.SWARM,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.UNDEAD,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.CONSTRUCT,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.DEMON,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.HUMANOID,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.TANK,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.ASSASSIN,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.RANGED,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.FIGHTER,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.MAGE,
				com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.SUPPORT }) {
			int tier = AutochessRun.traitTier(trait);
			Integer prev = lastTiers.get(trait);
			if (prev != null && tier > prev) {
				banner(M.L(AutochessHUD.class, "trait_activated",
						M.L(com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool.class, "trait_" + trait),
						tier), 0x66DDFF);
			}
			lastTiers.put(trait, tier);
		}
		if (roundPulse > 0f) {
			roundPulse = Math.max(0f, roundPulse - Game.elapsed / 0.9f);
			// 边框从亮金渐变回暗金
			int r = (int) (0xB5 + (0xF5 - 0xB5) * roundPulse);
			int g2 = (int) (0x9E + (0xE8 - 0x9E) * roundPulse);
			int b2 = (int) (0x09 + (0x9A - 0x09) * roundPulse);
			if (frame != null) frame.setLineColor(0xFF000000 | (r << 16) | (g2 << 8) | b2);
		}

		// ------- 云顶式顶部条 -------
		boolean fighting = run.state() == AutochessRun.State.FIGHT;
		String countdown = run.state() == AutochessRun.State.PREP && prepLeft >= 0f
				? " ｜ " + Math.max(0, (int) Math.ceil(prepLeft)) + "s"
				: "";
		String line1 = "第 " + run.round() + " 回合 · "
				+ M.L(AutochessHUD.class, fighting ? "fighting" : "prep") + countdown;
		String line2 = run.gold() + " 金 ｜ Lv" + run.level()
				+ " ｜ " + run.deployed() + "/" + run.maxDeploy();
		String line3 = "经验 " + run.xp() + "/"
				+ (run.level() >= AutochessRun.MAX_LEVEL ? "-" : run.xpNeed());

		if (!line1.equals(shown)) {
			shown = line1;
			label.text(line1);
			subLabel.text(line2);
			fieldLabel.text(line3);
			layout();
		}
		label.hardlight(fighting ? COLOR_FIGHT : COLOR_NORMAL);

		// 生命条（观战者血量）
		float lifeRatio = Math.max(0f, Math.min(1f, run.life() / 100f));
		if (hpFill != null) {
			hpFill.width = 14 * lifeRatio;
			hpFill.color(0xFF000000 | (run.life() > 50 ? 0x66DD66 : (run.life() > 25 ? 0xDDCC44 : 0xDD5555)));
		}

		// 头像：阵容里第一只棋子
		String firstCls = null;
		for (int i = 0; i < 12; i++) {
			String cls = run.unitClass(1, i);
			if (cls != null) { firstCls = cls; break; }
		}
		if (firstCls == null) firstCls = "Rat";
		if (!firstCls.equals(avatarCls)) {
			avatarCls = firstCls;
			if (avatar != null) avatar.killAndErase();
			avatar = AutochessIcons.spriteFor(firstCls);
			if (avatar != null) {
				avatar.scale.set(0.7f);
				add(avatar);
			}
			layout();
		}
		if (avatar != null) {
			avatar.x = x + 3;
			avatar.y = y + 3;
			PixelScene.align(avatar);
		}

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
				// 英雄只是节拍器：ready 就推进；连续几拍不 ready 就强制置为就绪
				if (Dungeon.hero != null && Dungeon.hero.ready) {
					stuckTicks = 0;
					Dungeon.hero.spendAndNext(1f);
				} else if (Dungeon.hero != null) {
					stuckTicks++;
					if (stuckTicks >= 3) {
						stuckTicks = 0;
						Dungeon.hero.ready = true;
						Dungeon.hero.spendAndNext(1f);
					}
				}
				run.tickFight();
				run.checkFight();
			}
		} else {
			acc = 0f;
		}
	}
}
