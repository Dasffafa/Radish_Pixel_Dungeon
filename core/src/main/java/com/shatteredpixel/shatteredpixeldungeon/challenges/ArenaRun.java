package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.ArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndArenaResult;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndArenaReward;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.Locale;

/**
 * 「三分钟一把」的总导演：计时、出怪、清波、三选一、结算。
 *
 * <p>挂一个 Buff 在英雄身上而不是单独写 Actor，是因为 Buff 天然跟着英雄走：
 * 读档、死亡、换层都会跟着一起清理，不用额外维护生命周期。</p>
 *
 * <p>计时按「回合」走：1 回合约等于 1 秒的观感，180 回合差不多就是三分钟一把。
 * 用回合而不是真实时间，是因为这游戏是回合制，真实计时会让玩家在读弹窗时被白扣时间。</p>
 */
public class ArenaRun extends Buff {

	/** 一局总时长（回合计），180 回合 ≈ 3 分钟。 */
	public static final int TOTAL_TURNS = 180;

	public enum State {
		/** 刚挂上，还没有出第一波。 */
		WAITING,
		/** 战斗中：计时、清波判定。 */
		FIGHTING,
		/** 本波清空，等玩家三选一。 */
		REWARD,
		/** 已选完，下一回合开始刷下一波。 */
		NEXT_WAVE,
		/** 时间到，已出结算。 */
		FINISHED
	}

	private State state = State.WAITING;
	private int wave = 0;
	private int kills = 0;
	private int timeLeft = TOTAL_TURNS;
	private int stall = 0;
	private ArenaUpgrade pending = null;
	/** 自动开火冷却（回合）与连射强化层数。 */
	private int fireCd = 1;
	private int rapid = 0;

	{
		type = buffType.POSITIVE;
	}

	/* ====================== 生命周期 ====================== */

	public static boolean active() {
		return Dungeon.isChallenged(Challenges.THREE_MINUTE);
	}

	public static ArenaRun get() {
		Hero hero = Dungeon.hero;
		return hero == null ? null : hero.buff(ArenaRun.class);
	}

	public static ArenaEmpower empower() {
		Hero hero = Dungeon.hero;
		return hero == null ? null : hero.buff(ArenaEmpower.class);
	}

	/**
	 * GameScene 每次建场都会调一次：只认「竞技场 + 挑战开启」，
	 * 挂上导演与强化 Buff 后自己接管一切。
	 */
	public static void ensureStarted() {
		if (!active() || Dungeon.hero == null) {
			return;
		}
		if (!(Dungeon.level instanceof ArenaLevel)) {
			return;
		}
		if (Dungeon.hero.buff(ArenaRun.class) != null) {
			return;
		}
		Buff.affect(Dungeon.hero, ArenaRun.class);
		Buff.affect(Dungeon.hero, ArenaEmpower.class);

		// 竞技场开局给 1.5 倍血量并回满：一层英雄只有 12 血，
		// 不加这一下，第一波就能把玩家按死，完全谈不上解压
		Hero hero = Dungeon.hero;
		int bonusHp = Math.round(hero.HT * 0.5f);
		hero.HT += bonusHp;
		hero.HP = hero.HT;
	}

	@Override
	public boolean act() {

		if (!(Dungeon.level instanceof ArenaLevel) || Dungeon.hero == null || !Dungeon.hero.isAlive()) {
			detach();
			return true;
		}

		switch (state) {
			case WAITING:
				startWave(1);
				break;

			case NEXT_WAVE:
				if (pending != null) {
					pending.apply(Dungeon.hero);
					pending = null;
				}
				startWave(wave + 1);
				break;

			case FIGHTING:
				timeLeft--;
				tickAutoFire();
				if (timeLeft <= 0) {
					timeLeft = 0;
					finish();
					break;
				}
				if (hostilesAlive() == 0) {
					waveCleared();
					break;
				}
				if (++stall >= 60) {
					// 兜底：怪被地形卡住时每 60 回合重新吼一次，逼它们找路
					stall = 0;
					beckonAll();
				}
				break;

			default:
				break;
		}

		spend(TICK);
		return true;
	}

	/** 射击间隔：基础 2 回合，每层「连射」-1，最低 1。 */
	public static int fireInterval() {
		ArenaRun run = get();
		int rapid = run == null ? 0 : run.rapid;
		return Math.max(1, 2 - rapid);
	}

	/** 每回合尝试自动开火；打不出来就下回合再试，免得空转。 */
	private void tickAutoFire() {
		if (--fireCd > 0) {
			return;
		}
		if (ArenaAutoFire.fire(Dungeon.hero)) {
			fireCd = fireInterval();
		} else {
			fireCd = 1;
		}
	}

	/** 连射强化：射击间隔 -1（最低 1）。 */
	public static void addRapid() {
		ArenaRun run = get();
		if (run != null) {
			run.rapid++;
		}
	}

	private void startWave(int newWave) {
		wave = newWave;
		stall = 0;
		state = State.FIGHTING;
		ArenaMobs.spawnWave(newWave);
		GLog.p(M.L(ArenaRun.class, "wave_start", newWave, ArenaMobs.count(newWave)));
	}

	private void waveCleared() {
		state = State.REWARD;
		// 清波小回血，让玩家有余力撑到三选一
		Hero hero = Dungeon.hero;
		hero.HP = Math.min(hero.HT, hero.HP + Math.max(1, hero.HT / 20));
		GLog.p(M.L(ArenaRun.class, "wave_clear", wave));

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndArenaReward(wave, ArenaUpgrade.roll()));
			}
		});
	}

	/** 三选一窗口点了之后回调（渲染线程）。真正的刷怪留到下一个游戏回合。 */
	public void choose(ArenaUpgrade upgrade) {
		if (state != State.REWARD) {
			return;
		}
		pending = upgrade;
		state = State.NEXT_WAVE;
		GLog.p(M.L(ArenaRun.class, "picked", upgrade.title()));
	}

	private void finish() {
		state = State.FINISHED;
		final int clearedWave = wave;
		final int totalKills = kills;
		GLog.p(M.L(ArenaRun.class, "finish"));

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndArenaResult(clearedWave, totalKills, TOTAL_TURNS));
			}
		});
	}

	/* ====================== 供 UI / 强化使用 ====================== */

	public static void addTime(int seconds) {
		ArenaRun run = get();
		if (run != null && run.state != State.FINISHED) {
			run.timeLeft += seconds;
		}
	}

	public static int hudSeconds() {
		ArenaRun run = get();
		return run == null ? TOTAL_TURNS : Math.max(0, run.timeLeft);
	}

	public static String hudTime() {
		int seconds = hudSeconds();
		return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
	}

	public static int hudWave() {
		ArenaRun run = get();
		return run == null ? 0 : run.wave;
	}

	public static int hudKills() {
		ArenaRun run = get();
		return run == null ? 0 : run.kills;
	}

	public static boolean isFighting() {
		ArenaRun run = get();
		return run != null && run.state == State.FIGHTING;
	}

	/* ====================== 战斗事件 ====================== */

	/** 溅射时防止「击杀 → 溅射 → 击杀」无限套娃。 */
	private static boolean cleaving = false;

	@SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
	public static void onFinalDamage(CharFinalDamageEvent event) {

		if (!active()) {
			return;
		}
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) {
			return;
		}
		ArenaEmpower empower = empower();
		if (empower == null) {
			return;
		}

		Char attacker = event.getAttacker();
		Char target = event.getTarget();
		int dealt = event.getDamage();

		// 吸血：只要这一下是自己打的、真的掉了血，就按比例回
		if (attacker == hero && dealt > 0 && empower.lifesteal > 0f) {
			int heal = Math.max(1, Math.round(dealt * empower.lifesteal));
			hero.HP = Math.min(hero.HT, hero.HP + heal);
		}

		// 击杀判定：事件发出时 HP 可能已经 <= 0，但 die() 还没走
		if (target instanceof Mob && event.getHpAfter() <= 0) {
			onMobKilled((Mob) target, empower, hero);
		}
	}

	private static void onMobKilled(Mob mob, ArenaEmpower empower, Hero hero) {
		ArenaRun run = get();
		if (run == null || run.state != State.FIGHTING) {
			return;
		}
		run.kills++;

		if (empower.killHealPct > 0) {
			int heal = Math.max(1, Math.round(hero.HT * (empower.killHealPct / 100f)));
			hero.HP = Math.min(hero.HT, hero.HP + heal);
		}

		if (empower.cleave) {
			cleave(mob, hero);
		}
	}

	private static void cleave(Mob dead, Hero hero) {
		if (cleaving || Dungeon.level == null) {
			return;
		}
		cleaving = true;
		try {
			for (Mob other : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (other == dead || !other.isAlive()) {
					continue;
				}
				if (Dungeon.level.distance(other.pos, dead.pos) > 2) {
					continue;
				}
				int dmg = Math.max(1, Math.round(hero.damageRoll() * 0.5f));
				other.damage(DamageInfo.physical(dmg, hero));
			}
		} finally {
			cleaving = false;
		}
	}

	private static int hostilesAlive() {
		if (Dungeon.level == null) {
			return 0;
		}
		int count = 0;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob.isAlive() && mob.alignment == Char.Alignment.ENEMY) {
				count++;
			}
		}
		return count;
	}

	private static void beckonAll() {
		if (Dungeon.level == null || Dungeon.hero == null) {
			return;
		}
		for (Mob mob : Dungeon.level.mobs) {
			if (mob.isAlive() && mob.alignment == Char.Alignment.ENEMY) {
				mob.beckon(Dungeon.hero.pos);
			}
		}
	}

	/* ====================== 存档 ====================== */

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("arena_state", state.name());
		bundle.put("arena_wave", wave);
		bundle.put("arena_kills", kills);
		bundle.put("arena_time", timeLeft);
		bundle.put("arena_stall", stall);
		bundle.put("arena_fire_cd", fireCd);
		bundle.put("arena_rapid", rapid);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		try {
			state = State.valueOf(bundle.getString("arena_state"));
		} catch (Exception e) {
			state = State.WAITING;
		}
		wave = bundle.getInt("arena_wave");
		kills = bundle.getInt("arena_kills");
		timeLeft = bundle.getInt("arena_time");
		stall = bundle.getInt("arena_stall");
		fireCd = bundle.getInt("arena_fire_cd");
		rapid = bundle.getInt("arena_rapid");
		if (timeLeft <= 0) {
			timeLeft = 1;
		}
	}
}
