package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessResult;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAutochessShop;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * 「萝卜自走棋」的总控：金币、商店、阵容、开战、回合结算。
 *
 * <p>英雄只在场边观战，不参战；玩家做的只有「买谁、凑什么羁绊」。
 * 阵容存在 {@link #roster} 里（类名 + 星级），每回合开打时整场重建，
 * 所以棋子不会因为上一场打死了就永久消失。</p>
 */
public class AutochessRun extends Buff {

	public enum State {
		PREP,
		FIGHT
	}

	public static final int START_GOLD = 8;
	public static final int START_LIFE = 10;
	public static final int REFRESH_COST = 2;

	/** 阵容上限。 */
	public static final int MAX_UNITS = 8;

	private State state = State.PREP;
	private int round = 1;
	private int gold = START_GOLD;
	private int life = START_LIFE;

	/** 阵容：每项是「类名:星级」。 */
	private final ArrayList<String> roster = new ArrayList<>();

	private String[] offers = new String[3];
	private int[] prices = new int[3];

	{
		type = buffType.POSITIVE;
	}

	/* ====================== 生命周期 ====================== */

	public static boolean active() {
		return Dungeon.isChallenged(Challenges.AUTO_BATTLER);
	}

	public static AutochessRun get() {
		Hero hero = Dungeon.hero;
		return hero == null ? null : hero.buff(AutochessRun.class);
	}

	public static void ensureStarted() {
		if (!active() || Dungeon.hero == null) {
			return;
		}
		if (!(Dungeon.level instanceof ChessBoardLevel)) {
			return;
		}
		if (Dungeon.hero.buff(AutochessRun.class) != null) {
			return;
		}
		AutochessRun run = Buff.affect(Dungeon.hero, AutochessRun.class);
		run.round = 1;
		run.gold = START_GOLD;
		run.life = START_LIFE;
		run.state = State.PREP;
		run.roster.clear();
		run.rollShop();

		// 英雄坐在墙里观战，视野只有一小块；把整张棋盘标记为已探索，玩家才看得见对局
		for (int i = 0; i < Dungeon.level.length(); i++) {
			Dungeon.level.mapped[i] = true;
			Dungeon.level.visited[i] = true;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.updateMap();
				Dungeon.observe();
			}
		});
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	/* ====================== 商店与阵容 ====================== */

	public void rollShop() {
		for (int i = 0; i < offers.length; i++) {
			AutochessPool.Def def = AutochessPool.random(round);
			offers[i] = def.cls.getSimpleName();
			prices[i] = def.cost;
		}
	}

	public AutochessPool.Def offer(int index) {
		if (index < 0 || index >= offers.length || offers[index] == null) {
			return null;
		}
		return AutochessPool.ofName(offers[index]);
	}

	public int price(int index) {
		return index < 0 || index >= prices.length ? 0 : prices[index];
	}

	public boolean refresh() {
		if (state != State.PREP || gold < REFRESH_COST) {
			return false;
		}
		gold -= REFRESH_COST;
		rollShop();
		return true;
	}

	/** 买第 index 个位置上的单位，进阵容。 */
	public boolean buy(int index) {
		if (state != State.PREP) {
			return false;
		}
		AutochessPool.Def def = offer(index);
		if (def == null) {
			return false;
		}
		if (roster.size() >= MAX_UNITS) {
			GLog.w(M.L(AutochessRun.class, "full"));
			return false;
		}
		if (gold < prices[index]) {
			GLog.w(M.L(AutochessRun.class, "no_gold"));
			return false;
		}
		gold -= prices[index];
		roster.add(def.cls.getSimpleName() + ":1");
		offers[index] = null;
		prices[index] = 0;

		mergeRoster(def.cls.getSimpleName(), 1);
		return true;
	}

	/** 三合一：阵容里同名同星凑够 3 个就升一星。 */
	private void mergeRoster(String className, int star) {
		ArrayList<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < roster.size(); i++) {
			String[] parts = roster.get(i).split(":");
			if (parts[0].equals(className) && parseInt(parts[1]) == star) {
				indexes.add(i);
			}
		}
		if (indexes.size() < 3) {
			return;
		}
		// 从后往前删，避免下标错位
		for (int i = 2; i >= 0; i--) {
			roster.remove((int) indexes.get(i));
		}
		int newStar = Math.min(3, star + 1);
		roster.add(className + ":" + newStar);
		GLog.p(M.L(AutochessRun.class, "merge", unitName(className), newStar));
		if (newStar < 3) {
			mergeRoster(className, newStar);
		}
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 1;
		}
	}

	private static String unitName(String className) {
		AutochessPool.Def def = AutochessPool.ofName(className);
		if (def == null) {
			return className;
		}
		Mob mob = Reflection.newInstance(def.cls);
		return mob == null ? className : mob.name();
	}

	public int rosterSize() {
		return roster.size();
	}

	/** 阵容里某个羁绊的数量。 */
	public static int countTrait(String trait) {
		AutochessRun run = get();
		if (run == null) {
			return 0;
		}
		int count = 0;
		for (String entry : run.roster) {
			String[] parts = entry.split(":");
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def != null && trait.equals(def.trait)) {
				count++;
			}
		}
		return count;
	}

	public static int traitTier(String trait) {
		int count = countTrait(trait);
		int[] th = AutochessPool.thresholds(trait);
		if (count >= th[1]) return 2;
		if (count >= th[0]) return 1;
		return 0;
	}

	/* ====================== 战斗 ====================== */

	private static final String[] ROSTER_SPLIT = new String[0];

	/** 清空棋盘上所有参战单位（敌我都不留）。 */
	private void clearBoard() {
		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			boolean unit = mob.buff(AutochessUnitBuff.class) != null
					|| mob.alignment == Char.Alignment.ENEMY;
			if (unit) {
				if (mob.sprite != null) {
					mob.sprite.killAndErase();
				}
				mob.destroy();
			}
		}
	}

	public void startFight() {
		if (state != State.PREP) {
			return;
		}
		state = State.FIGHT;

		clearBoard();

		// 己方阵容
		for (int i = 0; i < roster.size(); i++) {
			String[] parts = roster.get(i).split(":");
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def == null) continue;
			spawnUnit(def, ChessBoardLevel.playerSlot(i), true, parseInt(parts[1]));
		}

		// 敌方波次
		int count = Math.min(6, 1 + round);
		for (int i = 0; i < count; i++) {
			AutochessPool.Def def = AutochessPool.random(round);
			Mob mob = spawnUnit(def, ChessBoardLevel.enemySlot(i), false, 1);
			if (mob != null) {
				float scale = 1f + Math.min(1.5f, 0.10f * (round - 1));
				mob.HT = Math.max(1, Math.round(mob.HT * scale));
				mob.HP = mob.HT;
			}
		}

		GLog.p(M.L(AutochessRun.class, "fight_start", round, count));
	}

	private static Mob spawnUnit(AutochessPool.Def def, int cell, boolean playerSide, int star) {
		if (cell == -1 || Dungeon.level == null) {
			return null;
		}
		Mob mob = Reflection.newInstance(def.cls);
		if (mob == null) {
			return null;
		}
		mob.pos = cell;
		mob.state = mob.HUNTING;
		GameScene.add(mob);
		Dungeon.level.occupyCell(mob);

		if (playerSide) {
			AutochessUnitBuff buff = Buff.affect(mob, AutochessUnitBuff.class);
			buff.setup(def.trait, star);
		}
		mob.beckon(playerSide ? findEnemyPos(mob) : 0);
		return mob;
	}

	/** 最近的敌方位置，没有就返回自己的位置。 */
	private static int findEnemyPos(Mob ally) {
		int best = ally.pos;
		int bestDist = Integer.MAX_VALUE;
		for (Mob other : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!other.isAlive() || other.alignment == Char.Alignment.ALLY) continue;
			int d = Dungeon.level.distance(ally.pos, other.pos);
			if (d < bestDist) {
				bestDist = d;
				best = other.pos;
			}
		}
		return best;
	}

	/** 每帧检查战斗是否结束。 */
	public void checkFight() {
		if (state != State.FIGHT || Dungeon.level == null) {
			return;
		}
		if (ChessBoardLevel.countSide(false) > 0) {
			if (ChessBoardLevel.countSide(true) <= 0) {
				loseRound();
			}
			return;
		}
		winRound();
	}

	private void winRound() {
		state = State.PREP;
		int reward = 5 + (round % 3 == 0 ? 3 : 0);
		gold += reward;
		round++;
		rollShop();
		GLog.p(M.L(AutochessRun.class, "win", reward, round));
		showShopNextFrame();
	}

	private void loseRound() {
		state = State.PREP;
		int damage = 2 + round / 4;
		life -= damage;
		if (life <= 0) {
			life = 0;
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndAutochessResult(round, gold));
				}
			});
			return;
		}
		gold += 2;
		round++;
		rollShop();
		GLog.w(M.L(AutochessRun.class, "lose", damage, life, round));
		showShopNextFrame();
	}

	private void showShopNextFrame() {
		final int r = round;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndAutochessShop(r));
			}
		});
	}

	/* ====================== HUD 取值 ====================== */

	public State state() {
		return state;
	}

	public int round() {
		return round;
	}

	public int gold() {
		return gold;
	}

	public int life() {
		return life;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	/* ====================== 存档 ====================== */

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("ac_state", state.name());
		bundle.put("ac_round", round);
		bundle.put("ac_gold", gold);
		bundle.put("ac_life", life);
		StringBuilder sb = new StringBuilder();
		for (String entry : roster) {
			if (sb.length() > 0) sb.append(",");
			sb.append(entry);
		}
		bundle.put("ac_roster", sb.toString());
		for (int i = 0; i < offers.length; i++) {
			bundle.put("ac_offer_" + i, offers[i] == null ? "" : offers[i]);
			bundle.put("ac_price_" + i, prices[i]);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		try {
			state = State.valueOf(bundle.getString("ac_state"));
		} catch (Exception e) {
			state = State.PREP;
		}
		round = bundle.getInt("ac_round");
		gold = bundle.getInt("ac_gold");
		life = bundle.getInt("ac_life");
		roster.clear();
		String saved = bundle.getString("ac_roster");
		if (saved != null && !saved.isEmpty()) {
			for (String entry : saved.split(",")) {
				if (entry.contains(":")) roster.add(entry);
			}
		}
		for (int i = 0; i < offers.length; i++) {
			String name = bundle.getString("ac_offer_" + i);
			offers[i] = name == null || name.isEmpty() ? null : name;
			prices[i] = bundle.getInt("ac_price_" + i);
		}
		if (round < 1) round = 1;
		if (life <= 0) life = START_LIFE;
	}
}
