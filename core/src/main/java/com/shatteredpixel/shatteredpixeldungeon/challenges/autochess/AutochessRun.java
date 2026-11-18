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
 * 「萝卜自走棋」总控：金币、等级、商店、备战位（棋盘上的格子）与战场。
 *
 * <p>棋子的「位置」就是棋盘上的格子：备战位的 9 个格子在棋盘顶部一条被墙隔开的走廊里，
 * 战场格子在棋盘下半部分。拖拽只是改格子坐标，不需要棋子真的走过去。</p>
 */
public class AutochessRun extends Buff {

	public enum State {
		PREP,
		FIGHT
	}

	public static final int START_GOLD = 8;
	public static final int START_LIFE = 10;
	public static final int REFRESH_COST = 2;

	public static final int BENCH_SIZE = ChessBoardLevel.BENCH_COUNT;
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

	/** 备战位的棋子（不参战）。 */
	private final String[] bench = new String[BENCH_SIZE];
	/** 上阵的棋子。 */
	private final String[] board = new String[BOARD_SLOTS];
	/** 上阵棋子所在格子。 */
	private final int[] boardCell = new int[BOARD_SLOTS];

	/** 地图上点选中的棋子：side 0 = 备战位，1 = 战场。 */
	public int selectedSide = -1;
	public int selectedIndex = -1;

	/** 商店格子数（云顶是 5 个）。 */
	public static final int OFFER_COUNT = 5;
	/** 每回合基础收入。 */
	public static final int BASE_INCOME = 5;

	private String[] offers = new String[OFFER_COUNT];
	private int[] prices = new int[OFFER_COUNT];

	/** 连胜/连败与上回合收入（UI 展示用）。 */
	private int winStreak = 0;
	private int loseStreak = 0;
	private int lastIncome = 0;
	private int lastInterest = 0;
	private int lastStreakBonus = 0;

	/** 本回合敌方阵容（类名），拖拽重排时不再重摇。 */
	private String[] enemyComp = null;
	private int enemyCompRound = -1;

	/** 单位实体：key = side * 100 + index，拖拽时只改坐标不重造。 */
	private final java.util.HashMap<Integer, Mob> unitMobs = new java.util.HashMap<>();

	/** 场上棋子有变动时自增，拖拽组件据此重建输入区。 */
	public int unitVersion = 0;

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
		if (!active() || Dungeon.hero == null) return;
		if (!(Dungeon.level instanceof ChessBoardLevel)) return;
		if (Dungeon.hero.buff(AutochessRun.class) != null) return;

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

	/* ====================== 等级 ====================== */

	public int level() {
		return level;
	}

	public int xp() {
		return xp;
	}

	public int xpNeed() {
		return 2 + level * 2;
	}

	public int maxDeploy() {
		return level;
	}

	public int deployed() {
		int count = 0;
		for (String entry : board) if (entry != null) count++;
		return count;
	}

	public boolean buyXp() {
		if (state != State.PREP) return false;
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
		while (level < MAX_LEVEL && xp >= xpNeed()) {
			xp -= xpNeed();
			level++;
			GLog.p(M.L(AutochessRun.class, "level_up", level, maxDeploy()));
		}
		return true;
	}

	/* ====================== 商店 ====================== */

	public void rollShop() {
		for (int i = 0; i < offers.length; i++) {
			AutochessPool.Def def = AutochessPool.randomForLevel(level);
			offers[i] = def.cls.getSimpleName();
			prices[i] = def.cost;
		}
	}

	public AutochessPool.Def offer(int index) {
		if (index < 0 || index >= offers.length || offers[index] == null) return null;
		return AutochessPool.ofName(offers[index]);
	}

	public int price(int index) {
		return index < 0 || index >= prices.length ? 0 : prices[index];
	}

	public boolean refresh() {
		if (state != State.PREP || gold < REFRESH_COST) return false;
		gold -= REFRESH_COST;
		rollShop();
		return true;
	}

	/** 买棋子：进第一个空的备战位。 */
	public boolean buy(int index) {
		if (state != State.PREP) return false;
		long now = System.currentTimeMillis();
		if (now - lastBuyMs < 200) return false;
		lastBuyMs = now;

		AutochessPool.Def def = offer(index);
		if (def == null) return false;

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
		refreshPreview();
		return true;
	}

	private int firstEmptyBench() {
		for (int i = 0; i < bench.length; i++) if (bench[i] == null) return i;
		return -1;
	}

	/* ====================== 棋子/格子 ====================== */

	private String[] entries(int side) {
		return side == 1 ? board : bench;
	}

	public String unitClass(int side, int index) {
		String[] parts = split(entries(side), index);
		return parts == null ? null : parts[0];
	}

	public int unitStar(int side, int index) {
		String[] parts = split(entries(side), index);
		return parts == null ? 0 : parseInt(parts[1]);
	}

	public String unitName(int side, int index) {
		String cls = unitClass(side, index);
		return cls == null ? null : nameOf(cls);
	}

	/** 某个格子上是哪个棋子，返回 {side, index}，没有返回 null。 */
	public int[] unitAt(int cell) {
		for (int i = 0; i < bench.length; i++) {
			if (bench[i] != null && ChessBoardLevel.benchCell(i) == cell) return new int[]{0, i};
		}
		for (int i = 0; i < board.length; i++) {
			if (board[i] != null && boardCell[i] == cell) return new int[]{1, i};
		}
		return null;
	}

	public int boardIndexAt(int cell) {
		for (int i = 0; i < boardCell.length; i++) {
			if (board[i] != null && boardCell[i] == cell) return i;
		}
		return -1;
	}

	public int boardCellOf(int index) {
		return index >= 0 && index < boardCell.length ? boardCell[index] : -1;
	}

	private static String[] split(String[] arr, int index) {
		if (arr == null || index < 0 || index >= arr.length || arr[index] == null) return null;
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
	 * 把 fromCell 上的棋子拖到 toCell。
	 *
	 * <ul>
	 *   <li>拖到备战位格子：进备战位（占用就交换）；</li>
	 *   <li>拖到己方战场格子：上阵/换位，上阵数受等级限制；</li>
	 *   <li>拖到其它地方：拒绝。</li>
	 * </ul>
	 */
	public boolean moveUnit(int fromCell, int toCell) {
		if (state != State.PREP || fromCell == toCell) return false;
		int[] from = unitAt(fromCell);
		if (from == null) return false;

		int benchIdx = ChessBoardLevel.benchIndexAt(toCell);
		boolean toBoard = ChessBoardLevel.isPlayerField(toCell);

		if (benchIdx < 0 && !toBoard) {
			GLog.w(M.L(AutochessRun.class, "only_player_half"));
			return false;
		}

		int[] target = unitAt(toCell);

		if (toBoard && from[0] == 0) {
			// 备战位 → 战场：数量不能超过等级
			boolean swapping = target != null && target[0] == 0;
			if (!swapping && deployed() >= maxDeploy()) {
				GLog.w(M.L(AutochessRun.class, "deploy_full", maxDeploy(), level));
				return false;
			}
		}

		if (target != null) {
			// 交换：把目标棋子放回源格子
			String[] fromArr = entries(from[0]);
			String[] toArr = entries(target[0]);
			String tmp = fromArr[from[1]];
			fromArr[from[1]] = toArr[target[1]];
			toArr[target[1]] = tmp;
			if (from[0] == 1) boardCell[from[1]] = toCell;
			if (target[0] == 1) boardCell[target[1]] = fromCell;
			syncPositions();
			return true;
		}

		// 空位：直接挪
		String[] fromArr = entries(from[0]);
		String cls = fromArr[from[1]];
		fromArr[from[1]] = null;

		if (toBoard) {
			int slot = firstEmptyBoardSlot();
			if (slot == -1) {
				fromArr[from[1]] = cls;
				return false;
			}
			board[slot] = cls;
			boardCell[slot] = toCell;
		} else {
			bench[benchIdx] = cls;
		}
		// 换了容器（备战位↔战场），实体归属也变了，直接重新铺一次
		refreshPreview();
		return true;
	}

	/** 拖拽后只把已有实体挪到新格子，不重新生成，避免重摇敌方阵容/产生副作用。 */
	private void syncPositions() {
		for (java.util.Map.Entry<Integer, Mob> e : unitMobs.entrySet()) {
			int key = e.getKey();
			Mob mob = e.getValue();
			if (mob == null || !mob.isAlive()) continue;
			int cell;
			if (key >= 100) {
				int index = key - 100;
				String[] parts = split(board, index);
				if (parts == null) continue;
				cell = boardCell[index];
			} else {
				String[] parts = split(bench, key);
				if (parts == null) continue;
				cell = ChessBoardLevel.benchCell(key);
			}
			if (cell < 0 || cell == mob.pos) continue;
			mob.pos = cell;
			unitVersion++;
			Dungeon.level.occupyCell(mob);
			if (mob.sprite != null) {
				mob.sprite.place(cell);
			}
		}
	}

	private int firstEmptyBoardSlot() {
		for (int i = 0; i < board.length; i++) if (board[i] == null) return i;
		return -1;
	}

	/** 卖掉选中棋子，返回金币。 */
	public int sell(int side, int index) {
		if (state != State.PREP) return 0;
		String[] arr = entries(side);
		String[] parts = split(arr, index);
		if (parts == null) return 0;
		int value = sellValue(side, index);
		arr[index] = null;
		gold += value;
		refreshPreview();
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

	/** 三合一：备战位 + 战场一起算，升上来的落在第三个棋子那格。 */
	private void merge(String className, int star) {
		ArrayList<int[]> slots = new ArrayList<>();
		for (int side = 0; side <= 1; side++) {
			String[] arr = entries(side);
			for (int i = 0; i < arr.length; i++) {
				String[] parts = split(arr, i);
				if (parts != null && parts[0].equals(className) && parseInt(parts[1]) == star) {
					slots.add(new int[]{side, i});
				}
			}
		}
		if (slots.size() < 3) return;
		int[] keeper = slots.get(2);
		entries(slots.get(0)[0])[slots.get(0)[1]] = null;
		entries(slots.get(1)[0])[slots.get(1)[1]] = null;
		int newStar = Math.min(3, star + 1);
		entries(keeper[0])[keeper[1]] = className + ":" + newStar;
		GLog.p(M.L(AutochessRun.class, "merge", nameOf(className), newStar));
		if (newStar < 3) merge(className, newStar);
	}

	/* ====================== 预览与战斗 ====================== */

	private void clearUnits() {
		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			boolean unit = mob.buff(AutochessUnitBuff.class) != null
					|| mob.alignment == Char.Alignment.ENEMY;
			if (unit) {
				despawn(mob);
			}
		}
	}

	/**
	 * 清掉一个棋子。
	 *
	 * <p>不能直接 {@code mob.destroy()}：它会给英雄发经验、记击杀统计、掉战利品，
	 * 于是「拖一下棋子就升级」。先把阵营改成中立再 destroy，这些奖励分支就都不走了。</p>
	 */
	private static void despawn(Mob mob) {
		if (mob == null) return;
		if (mob.sprite != null) mob.sprite.killAndErase();
		mob.alignment = Char.Alignment.NEUTRAL;
		mob.destroy();
	}

	/** 把双方按当前摆位重新铺到棋盘上（备战阶段就是预览，不会动）。 */
	public void refreshPreview() {
		if (!(Dungeon.level instanceof ChessBoardLevel)) return;
		clearUnits();

		unitMobs.clear();
		unitVersion++;
		for (int i = 0; i < bench.length; i++) {
			String[] parts = split(bench, i);
			if (parts == null) continue;
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def == null) continue;
			Mob mob = spawnUnit(def, ChessBoardLevel.benchCell(i), true, parseInt(parts[1]));
			if (mob != null) unitMobs.put(i, mob);
		}
		for (int i = 0; i < board.length; i++) {
			String[] parts = split(board, i);
			if (parts == null) continue;
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def == null) continue;
			Mob mob = spawnUnit(def, boardCell[i], true, parseInt(parts[1]));
			if (mob != null) unitMobs.put(100 + i, mob);
		}
		spawnEnemies();
	}

	private void ensureEnemyComp() {
		if (enemyComp != null && enemyCompRound == round) {
			return;
		}
		int count = Math.min(6, 1 + round);
		enemyComp = new String[count];
		for (int i = 0; i < count; i++) {
			enemyComp[i] = AutochessPool.random(round).cls.getSimpleName();
		}
		enemyCompRound = round;
	}

	private void spawnEnemies() {
		ensureEnemyComp();
		int count = enemyComp.length;
		for (int i = 0; i < count; i++) {
			AutochessPool.Def def = AutochessPool.ofName(enemyComp[i]);
			if (def == null) continue;
			Mob mob = spawnUnit(def, ChessBoardLevel.enemySlot(i + (round % 3)), false, 1);
			if (mob != null) {
				float scale = 1f + Math.min(1.5f, 0.10f * (round - 1));
				mob.HT = Math.max(1, Math.round(mob.HT * scale));
				mob.HP = mob.HT;
			}
		}
	}

	public void startFight() {
		if (state != State.PREP) return;
		if (deployed() == 0) {
			GLog.w(M.L(AutochessRun.class, "need_deploy"));
			return;
		}
		state = State.FIGHT;
		refreshPreview();
		GLog.p(M.L(AutochessRun.class, "fight_start", round, Math.min(6, 1 + round)));
	}

	private static Mob spawnUnit(AutochessPool.Def def, int cell, boolean playerSide, int star) {
		if (cell == -1 || Dungeon.level == null) return null;
		Mob mob = Reflection.newInstance(def.cls);
		if (mob == null) return null;
		mob.pos = cell;
		mob.state = mob.HUNTING;
		GameScene.add(mob);
		Dungeon.level.occupyCell(mob);
		if (playerSide) {
			AutochessUnitBuff buff = Buff.affect(mob, AutochessUnitBuff.class);
			buff.setup(def.trait, star);
		} else {
			// 自走棋里的敌人不给英雄经验/升级，玩家是观战方
			mob.maxLvl = 0;
		}
		// 场景刚建时 sprite 可能还没挂上，beckon 会 NPE，等有 sprite 再吼
		if (mob.sprite != null) {
			mob.beckon(playerSide ? findEnemyPos(mob) : 0);
		}
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
		if (state != State.FIGHT || Dungeon.level == null) return;
		if (ChessBoardLevel.countSide(false) > 0) {
			if (ChessBoardLevel.countSide(true) <= 0) loseRound();
			return;
		}
		winRound();
	}

	/** 利息：每 10 金 +1，最多 +5。 */
	private int interest() {
		return Math.min(5, gold / 10);
	}

	/** 连胜/连败奖励：2~3 连 +1，4 连 +2，5 连 +3，6 连以上 +4。 */
	private int streakBonus(int streak) {
		if (streak >= 6) return 4;
		if (streak >= 5) return 3;
		if (streak >= 4) return 2;
		if (streak >= 2) return 1;
		return 0;
	}

	private void winRound() {
		state = State.PREP;
		winStreak++;
		loseStreak = 0;

		lastInterest = interest();
		lastStreakBonus = streakBonus(winStreak);
		lastIncome = BASE_INCOME + lastInterest + lastStreakBonus;
		gold += lastIncome;

		gainRoundXp();
		round++;
		rollShop();
		GLog.p(M.L(AutochessRun.class, "win_income", BASE_INCOME, lastInterest, lastStreakBonus, lastIncome, round));
		refreshPreview();
		showShopNextFrame();
	}

	private void loseRound() {
		state = State.PREP;
		winStreak = 0;
		loseStreak++;

		// 扣血 = 基础 2 + 场上还活着的敌人 + 回合加成（云顶的算法味道）
		int survivors = ChessBoardLevel.countSide(false);
		int damage = 2 + survivors + round / 4;
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
		lastInterest = interest();
		lastStreakBonus = streakBonus(loseStreak);
		lastIncome = BASE_INCOME + lastInterest + lastStreakBonus;
		gold += lastIncome;

		gainRoundXp();
		round++;
		rollShop();
		GLog.w(M.L(AutochessRun.class, "lose", damage, life, round));
		GLog.i(M.L(AutochessRun.class, "lose_income", BASE_INCOME, lastInterest, lastStreakBonus, lastIncome));
		refreshPreview();
		showShopNextFrame();
	}

	private void gainRoundXp() {
		xp += XP_PER_ROUND;
		while (level < MAX_LEVEL && xp >= xpNeed()) {
			xp -= xpNeed();
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

	public int winStreak() {
		return winStreak;
	}

	public int loseStreak() {
		return loseStreak;
	}

	public int lastIncome() {
		return lastIncome;
	}

	public int gold() {
		return gold;
	}

	public int life() {
		return life;
	}

	public int rosterSize() {
		int count = 0;
		for (String e : bench) if (e != null) count++;
		for (String e : board) if (e != null) count++;
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
		bundle.put("ac_win_streak", winStreak);
		bundle.put("ac_lose_streak", loseStreak);
		for (int i = 0; i < bench.length; i++) {
			bundle.put("ac_bench_" + i, bench[i] == null ? "" : bench[i]);
		}
		for (int i = 0; i < board.length; i++) {
			bundle.put("ac_board_" + i, board[i] == null ? "" : board[i]);
			bundle.put("ac_cell_" + i, boardCell[i]);
		}
		StringBuilder comp = new StringBuilder();
		if (enemyComp != null) {
			for (String name : enemyComp) {
				if (comp.length() > 0) comp.append(",");
				comp.append(name);
			}
		}
		bundle.put("ac_enemy_comp", comp.toString());
		bundle.put("ac_enemy_round", enemyCompRound);
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
		winStreak = bundle.getInt("ac_win_streak");
		loseStreak = bundle.getInt("ac_lose_streak");
		for (int i = 0; i < bench.length; i++) {
			String e = bundle.getString("ac_bench_" + i);
			bench[i] = e == null || e.isEmpty() ? null : e;
		}
		for (int i = 0; i < board.length; i++) {
			String e = bundle.getString("ac_board_" + i);
			board[i] = e == null || e.isEmpty() ? null : e;
			boardCell[i] = bundle.contains("ac_cell_" + i)
					? bundle.getInt("ac_cell_" + i)
					: ChessBoardLevel.playerSlot(i);
		}
		String comp = bundle.getString("ac_enemy_comp");
		if (comp != null && !comp.isEmpty()) {
			enemyComp = comp.split(",");
			enemyCompRound = bundle.getInt("ac_enemy_round");
		} else {
			enemyComp = null;
			enemyCompRound = -1;
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
