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
 * 「萝卜自走棋」的总控：金币、等级、商店、备战席、上阵位、开战与结算。
 *
 * <p>云顶那套的三条线都在这里：</p>
 * <ul>
 *   <li>金币可以买棋子、刷新商店、买经验；</li>
 *   <li>等级就是上阵人数上限，初始 3 级（3 个上阵位），每升一级 +1；</li>
 *   <li>备战席（9 格）里的棋子不参战，只有摆到上阵位（战场）上的才打。</li>
 * </ul>
 */
public class AutochessRun extends Buff {

	public enum State {
		PREP,
		FIGHT
	}

	public static final int START_GOLD = 8;
	public static final int START_LIFE = 10;
	public static final int REFRESH_COST = 2;

	/** 备战席格数。 */
	public static final int BENCH_SIZE = 9;
	/** 上阵位格数（战场），实际能上几个由等级决定。 */
	public static final int BOARD_SLOTS = 9;

	public static final int XP_COST = 4;
	public static final int XP_PER_BUY = 4;
	public static final int XP_PER_ROUND = 2;
	public static final int MAX_LEVEL = 9;

	private State state = State.PREP;
	private int round = 1;
	private int gold = START_GOLD;
	private int life = START_LIFE;

	private int level = 3;
	private int xp = 0;

	/** 备战席：不参战。 */
	private final String[] bench = new String[BENCH_SIZE];
	/** 上阵位：只有这里面的棋子参战。 */
	private final String[] board = new String[BOARD_SLOTS];
	/** 每个上阵位在棋盘上的实际格子（可在地图上拖动改变）。 */
	private final int[] boardCell = new int[BOARD_SLOTS];

	/** 备战界面当前选中的格子：side 0 = 备战席，1 = 战场。 */
	public int selectedSide = -1;
	public int selectedIndex = -1;

	private String[] offers = new String[3];
	private int[] prices = new int[3];

	private long lastBuyMs = 0;

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
		run.level = 3;
		run.xp = 0;
		run.state = State.PREP;
		java.util.Arrays.fill(run.bench, null);
		java.util.Arrays.fill(run.board, null);
		for (int i = 0; i < BOARD_SLOTS; i++) {
			run.boardCell[i] = ChessBoardLevel.playerSlot(i);
		}
		run.selectedSide = -1;
		run.selectedIndex = -1;
		run.rollShop();


	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	/* ====================== 等级与经验 ====================== */

	public int level() {
		return level;
	}

	public int xp() {
		return xp;
	}

	/** 升到下一级需要的经验。 */
	public int xpNeed() {
		return xpNeed(level);
	}

	private static int xpNeed(int lvl) {
		return 2 + lvl * 2;
	}

	/** 上阵人数上限 = 等级。 */
	public int maxDeploy() {
		return level;
	}

	public int deployed() {
		int count = 0;
		for (String entry : board) {
			if (entry != null) count++;
		}
		return count;
	}

	/** 花 4 金买 4 点经验，够了就升级。 */
	public boolean buyXp() {
		if (state != State.PREP) {
			return false;
		}
		if (level >= MAX_LEVEL) {
			GLog.w(M.L(AutochessRun.class, "level_max"));
			return false;
		}
		if (gold < XP_COST) {
			GLog.w(M.L(AutochessRun.class, "no_gold"));
			return false;
		}
		gold -= XP_COST;
		xp += XP_PER_BUY;
		while (level < MAX_LEVEL && xp >= xpNeed(level)) {
			xp -= xpNeed(level);
			level++;
			GLog.p(M.L(AutochessRun.class, "level_up", level, maxDeploy()));
		}
		return true;
	}

	/* ====================== 商店 ====================== */

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

	/** 买棋子：进第一个空的备战席，然后尝试三合一。 */
	public boolean buy(int index) {
		if (state != State.PREP) {
			return false;
		}
		long now = System.currentTimeMillis();
		if (now - lastBuyMs < 200) {
			return false;
		}
		lastBuyMs = now;

		AutochessPool.Def def = offer(index);
		if (def == null) {
			return false;
		}
		int slot = firstEmptyBench();
		if (slot == -1) {
			GLog.w(M.L(AutochessRun.class, "bench_full"));
			return false;
		}
		if (gold < prices[index]) {
			GLog.w(M.L(AutochessRun.class, "no_gold"));
			return false;
		}
		gold -= prices[index];
		bench[slot] = def.cls.getSimpleName() + ":1";
		offers[index] = null;
		prices[index] = 0;
		merge(def.cls.getSimpleName(), 1);
		return true;
	}

	private int firstEmptyBench() {
		for (int i = 0; i < bench.length; i++) {
			if (bench[i] == null) return i;
		}
		return -1;
	}

	/* ====================== 棋子存取 ====================== */

	private String[] entryArr(int side) {
		return side == 1 ? board : bench;
	}

	public String unitClass(int side, int index) {
		String[] arr = entryArr(side);
		String[] parts = split(arr, index);
		return parts == null ? null : parts[0];
	}

	public int unitStar(int side, int index) {
		String[] parts = split(entryArr(side), index);
		return parts == null ? 0 : parseInt(parts[1]);
	}

	public String unitName(int side, int index) {
		String cls = unitClass(side, index);
		return cls == null ? null : nameOf(cls);
	}

	private static String[] split(String[] arr, int index) {
		if (arr == null || index < 0 || index >= arr.length || arr[index] == null) {
			return null;
		}
		String[] parts = arr[index].split(":");
		return parts.length == 2 ? parts : null;
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 1;
		}
	}

	private static String nameOf(String className) {
		AutochessPool.Def def = AutochessPool.ofName(className);
		if (def == null) return className;
		Mob mob = Reflection.newInstance(def.cls);
		return mob == null ? className : mob.name();
	}

	/**
	 * 拖拽落子：从 (fromSide, fromIndex) 拖到 (toSide, toIndex)。
	 *
	 * <p>规则和云顶一致：两边交换；放到战场但已经上满就不能再上，
	 * 除非是同一侧互换（换完数量不变）。</p>
	 */
	public boolean drag(int fromSide, int fromIndex, int toSide, int toIndex) {
		if (state != State.PREP || fromSide == toSide && fromIndex == toIndex) {
			return false;
		}
		String[] from = entryArr(fromSide);
		String[] to = entryArr(toSide);
		if (fromIndex < 0 || fromIndex >= from.length || toIndex < 0 || toIndex >= to.length) {
			return false;
		}
		if (from[fromIndex] == null) {
			return false;
		}

		boolean movingToBoard = toSide == 1 && fromSide != 1;
		boolean targetEmpty = to[toIndex] == null;

		if (movingToBoard && targetEmpty && deployed() >= maxDeploy()) {
			GLog.w(M.L(AutochessRun.class, "deploy_full", maxDeploy(), level));
			return false;
		}

		String tmp = from[fromIndex];
		from[fromIndex] = to[toIndex];
		to[toIndex] = tmp;
		return true;
	}

	/** 卖棋子，返回返还金币。 */
	public int sell(int side, int index) {
		if (state != State.PREP) {
			return 0;
		}
		String[] arr = entryArr(side);
		String[] parts = split(arr, index);
		if (parts == null) {
			return 0;
		}
		int value = sellValue(side, index);
		arr[index] = null;
		gold += value;
		return value;
	}

	public int sellValue(int side, int index) {
		String cls = unitClass(side, index);
		if (cls == null) return 0;
		AutochessPool.Def def = AutochessPool.ofName(cls);
		if (def == null) return 0;
		int star = unitStar(side, index);
		int mult = star >= 3 ? 9 : (star == 2 ? 3 : 1);
		return def.cost * mult;
	}

	/* ====================== 三合一 ====================== */

	/** 在备战席 + 战场里找同名同星，凑够 3 个就升星；升上来的落在第三个棋子那格。 */
	private void merge(String className, int star) {
		ArrayList<int[]> slots = new ArrayList<>();
		for (int side = 0; side <= 1; side++) {
			String[] arr = entryArr(side);
			for (int i = 0; i < arr.length; i++) {
				String[] parts = split(arr, i);
				if (parts != null && parts[0].equals(className) && parseInt(parts[1]) == star) {
					slots.add(new int[]{side, i});
				}
			}
		}
		if (slots.size() < 3) {
			return;
		}
		int[] keeper = slots.get(2);
		entryArr(slots.get(0)[0])[slots.get(0)[1]] = null;
		entryArr(slots.get(1)[0])[slots.get(1)[1]] = null;
		int newStar = Math.min(3, star + 1);
		entryArr(keeper[0])[keeper[1]] = className + ":" + newStar;
		GLog.p(M.L(AutochessRun.class, "merge", nameOf(className), newStar));
		if (newStar < 3) {
			merge(className, newStar);
		}
	}

	/* ====================== 战斗 ====================== */

	private void clearBoardUnits() {
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

	/** 上阵位 i 所在的棋盘格子。 */
	public int boardCellOf(int index) {
		return index >= 0 && index < boardCell.length ? boardCell[index] : -1;
	}

	/** 棋盘格子对应的上阵位下标，没有返回 -1。 */
	public int boardIndexAt(int cell) {
		for (int i = 0; i < boardCell.length; i++) {
			if (board[i] != null && boardCell[i] == cell) return i;
		}
		return -1;
	}

	/** 备战阶段把双方摆到棋盘上预览（不推进回合，所以它们不会动）。 */
	public void refreshPreview() {
		if (!(Dungeon.level instanceof ChessBoardLevel)) {
			return;
		}
		clearBoardUnits();
		for (int i = 0; i < board.length; i++) {
			String[] parts = split(board, i);
			if (parts == null) continue;
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def == null) continue;
			spawnUnit(def, boardCell[i], true, parseInt(parts[1]));
		}
		for (int i = 0; i < enemyPreviewCount(); i++) {
			AutochessPool.Def def = AutochessPool.random(round);
			Mob mob = spawnUnit(def, ChessBoardLevel.enemySlot(i + (round % 3)), false, 1);
			if (mob != null) {
				float scale = 1f + Math.min(1.5f, 0.10f * (round - 1));
				mob.HT = Math.max(1, Math.round(mob.HT * scale));
				mob.HP = mob.HT;
			}
		}
	}

	private int enemyPreviewCount() {
		return Math.min(6, 1 + round);
	}

	/** 在地图上把一个己方棋子挪到另一个格子。 */
	public boolean moveOnMap(int fromCell, int toCell) {
		if (state != State.PREP) return false;
		int index = boardIndexAt(fromCell);
		if (index < 0) return false;
		if (!ChessBoardLevel.isPlayerHalf(toCell)) {
			GLog.w(M.L(AutochessRun.class, "only_player_half"));
			return false;
		}
		int other = boardIndexAt(toCell);
		if (other >= 0) {
			boardCell[other] = boardCell[index];
		}
		boardCell[index] = toCell;
		refreshPreview();
		return true;
	}

	/** 备战席棋子直接上阵到空的上阵位。 */
	public boolean deploy(int benchIndex) {
		if (state != State.PREP || benchIndex < 0 || benchIndex >= bench.length || bench[benchIndex] == null) {
			return false;
		}
		if (deployed() >= maxDeploy()) {
			GLog.w(M.L(AutochessRun.class, "deploy_full", maxDeploy(), level));
			return false;
		}
		for (int i = 0; i < board.length; i++) {
			if (board[i] == null) {
				board[i] = bench[benchIndex];
				bench[benchIndex] = null;
				boardCell[i] = ChessBoardLevel.playerSlot(i);
				refreshPreview();
				return true;
			}
		}
		return false;
	}

	/** 把一个上阵棋子撤回备战席。 */
	public boolean retreat(int boardIndex) {
		if (state != State.PREP || boardIndex < 0 || boardIndex >= board.length || board[boardIndex] == null) {
			return false;
		}
		for (int i = 0; i < bench.length; i++) {
			if (bench[i] == null) {
				bench[i] = board[boardIndex];
				board[boardIndex] = null;
				refreshPreview();
				return true;
			}
		}
		GLog.w(M.L(AutochessRun.class, "bench_full"));
		return false;
	}

	public void startFight() {
		if (state != State.PREP) {
			return;
		}
		state = State.FIGHT;
		// 备战阶段的预览单位就是这一波的实际阵容，直接开打，不重新摆
		refreshPreview();
		GLog.p(M.L(AutochessRun.class, "fight_start", round, enemyPreviewCount()));
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
		gainRoundXp();
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
		gainRoundXp();
		round++;
		rollShop();
		GLog.w(M.L(AutochessRun.class, "lose", damage, life, round));
		showShopNextFrame();
	}

	/** 每回合白给一点经验，和云顶一样。 */
	private void gainRoundXp() {
		xp += XP_PER_ROUND;
		while (level < MAX_LEVEL && xp >= xpNeed(level)) {
			xp -= xpNeed(level);
			level++;
			GLog.p(M.L(AutochessRun.class, "level_up", level, maxDeploy()));
		}
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

	/* ====================== 羁绊 ====================== */

	/** 羁绊只算「上阵」的棋子。 */
	public static int countTrait(String trait) {
		AutochessRun run = get();
		if (run == null) return 0;
		int count = 0;
		for (String entry : run.board) {
			if (entry == null) continue;
			String[] parts = entry.split(":");
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def != null && trait.equals(def.trait)) count++;
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

	/* ====================== 状态 ====================== */

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

	/** 备战席 + 战场的棋子总数（HUD 用）。 */
	public int rosterSize() {
		int count = 0;
		for (String entry : bench) if (entry != null) count++;
		for (String entry : board) if (entry != null) count++;
		return count;
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
		bundle.put("ac_level", level);
		bundle.put("ac_xp", xp);
		for (int i = 0; i < bench.length; i++) {
			bundle.put("ac_bench_" + i, bench[i] == null ? "" : bench[i]);
		}
		for (int i = 0; i < board.length; i++) {
			bundle.put("ac_board_" + i, board[i] == null ? "" : board[i]);
		}
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
		level = bundle.getInt("ac_level");
		xp = bundle.getInt("ac_xp");
		for (int i = 0; i < bench.length; i++) {
			String e = bundle.getString("ac_bench_" + i);
			bench[i] = e == null || e.isEmpty() ? null : e;
		}
		for (int i = 0; i < board.length; i++) {
			String e = bundle.getString("ac_board_" + i);
			board[i] = e == null || e.isEmpty() ? null : e;
		}
		for (int i = 0; i < offers.length; i++) {
			String name = bundle.getString("ac_offer_" + i);
			offers[i] = name == null || name.isEmpty() ? null : name;
			prices[i] = bundle.getInt("ac_price_" + i);
		}
		if (round < 1) round = 1;
		if (level < 1) level = 3;
		if (life <= 0) life = START_LIFE;
	}
}
