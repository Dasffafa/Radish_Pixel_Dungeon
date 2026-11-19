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
	/** 大家初始 100 血，扣血随回合递增。 */
	public static final int START_LIFE = 100;
	public static final int REFRESH_COST = 2;

	public static final int BENCH_SIZE = ChessBoardLevel.BENCH_COUNT;
	public static final int BOARD_SLOTS = 12;

	public static final int XP_COST = 4;
	public static final int XP_PER_BUY = 4;
	public static final int XP_PER_ROUND = 2;
	public static final int MAX_LEVEL = 10;

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
	private int lastDamage = 0;


	/** 单位实体：key = side * 100 + index，拖拽时只改坐标不重造。 */
	private final java.util.HashMap<Integer, Mob> unitMobs = new java.util.HashMap<>();
	/** 本轮的所有对局（玩家用「你」表示）。 */
	public static class Match {
		public final String a;
		public final String b;
		public String winner = null;

		public Match(String a, String b) {
			this.a = a;
			this.b = b;
		}
	}

	private final java.util.ArrayList<Match> matches = new java.util.ArrayList<>();
	/** 商店锁定：锁住后跨回合不刷新。 */
	private boolean shopLocked = false;

	public boolean shopLocked() {
		return shopLocked;
	}

	public void toggleShopLock() {
		shopLocked = !shopLocked;
	}

	/** 未装备的装备背包。 */
	private final java.util.ArrayList<String> itemBag = new java.util.ArrayList<>();

	/** 把装备装到某个棋盘格子上的棋子（拖拽用）。 */
	public boolean equipItemAtCell(int cell, String itemId) {
		int[] at = unitAt(cell);
		if (at == null) return false;
		return equipItem(at[0], at[1], itemId);
	}

	/** 取某个格子的棋子条目（含装备）。 */
	public String entryAt(int side, int index) {
		String[] arr = entries(side);
		if (arr == null || index < 0 || index >= arr.length) return null;
		return arr[index];
	}

	public java.util.ArrayList<String> itemBag() {
		return itemBag;
	}

	public java.util.ArrayList<Match> matches() {
		return matches;
	}

	/** 升星后待播放的特效（"类名:星级"），预览重建完再放。 */
	private String fxPending = null;
	/** 每只棋子在场上的星级（敌我都有），单位头顶展示用。 */
	private final java.util.HashMap<Mob, Integer> unitStars = new java.util.HashMap<>();
	/** 对面备战席上的展示棋子（不参战）。 */
	private final java.util.ArrayList<Mob> benchVisuals = new java.util.ArrayList<>();

	/** 场上棋子有变动时自增，拖拽组件据此重建输入区。 */
	public int unitVersion = 0;

	private long lastBuyMs = 0;

	/** 共享棋子池：玩家和 AI 都从这里抽牌。 */
	private AutochessSharedPool pool;
	/** 其他 AI 对手。 */
	private final java.util.ArrayList<AutochessBot> bots = new java.util.ArrayList<>();
	/** 本轮要打的对手。 */
	private AutochessBot opponent;

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
		run.level = 1;
		run.xp = 0;
		run.state = State.PREP;
		java.util.Arrays.fill(run.bench, null);
		java.util.Arrays.fill(run.board, null);
		for (int i = 0; i < BOARD_SLOTS; i++) {
			run.boardCell[i] = ChessBoardLevel.playerSlot(i);
		}
		run.selectedSide = -1;
		run.selectedIndex = -1;
		run.pool = new AutochessSharedPool();
		run.bots.clear();
		String[] names = {"老王", "阿呆", "包子", "萝卜头", "土豆"};
		for (String n : names) {
			run.bots.add(new AutochessBot(n));
		}
		run.opponent = null;
		// 开局先给每个 AI 发一手牌，否则第一轮对手是空场
		for (AutochessBot bot : run.bots) {
			bot.advance(run.pool, 0);
		}
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

	/** 云顶式升级经验表：1→2 要 2，2→3 要 2，3→4 要 6，之后 10/20/36/48/76。 */
	public int xpNeed() {
		switch (level) {
			case 1: return 2;
			case 2: return 2;
			case 3: return 6;
			case 4: return 10;
			case 5: return 20;
			case 6: return 36;
			case 7: return 48;
			case 8: return 76;
			case 9: return 84;
			default: return 999;
		}
	}

	/** 上阵人数：1 级就能上 3 个，每升 1 级 +1，10 级 12 个。 */
	public int maxDeploy() {
		return level + 2;
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
		boolean leveled = false;
		while (level < MAX_LEVEL && xp >= xpNeed()) {
			xp -= xpNeed();
			level++;
			leveled = true;
			GLog.p(M.L(AutochessRun.class, "level_up", level, maxDeploy()));
		}
		if (!leveled) {
			GLog.i(M.L(AutochessRun.class, "xp_gain", XP_PER_BUY, xp, xpNeed()));
		}
		return true;
	}

	/* ====================== 商店 ====================== */

	public void rollShop() {
		for (int i = 0; i < offers.length; i++) {
			String cls = pool == null ? null : pool.draw(level);
			if (cls == null) {
				offers[i] = null;
				prices[i] = 0;
				continue;
			}
			AutochessPool.Def def = AutochessPool.ofName(cls);
			offers[i] = cls;
			prices[i] = def == null ? 1 : def.cost;
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
		String cls = def.cls.getSimpleName();
		if (pool != null && !pool.take(cls)) {
			GLog.w(M.L(AutochessRun.class, "pool_empty"));
			return false;
		}
		gold -= prices[index];
		bench[slot] = cls + ":1";
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
		// 战斗中不生成备战席棋子：它们不参战，留在场上只会干扰回合推进
		if (state != State.FIGHT) {
		for (int i = 0; i < bench.length; i++) {
			if (bench[i] != null && ChessBoardLevel.benchCell(i) == cell) return new int[]{0, i};
		}
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
		try {
			Mob mob = Reflection.newInstance(def.cls);
			if (mob != null) return mob.name();
		} catch (Exception ignored) {
		}
		return className;
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
		// 自走棋里棋子不掉落物（没人捡，只会堆一地）
		mob.clearLoot();
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
		if (pool != null) {
			pool.give(parts[0]);
		}
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
		fxPending = className + ":" + newStar;
		GLog.p(M.L(AutochessRun.class, "merge", nameOf(className), newStar));
		if (newStar < 3) merge(className, newStar);
	}

	/* ====================== 预览与战斗 ====================== */

	/** 升星特效：星光 + 闪光 + 音效，3 星额外一圈金光。 */
	private void playMergeFx(String tag) {
		if (Dungeon.level == null) return;
		String[] parts = tag.split(":");
		if (parts.length < 2) return;
		String cls = parts[0];
		int star = parseInt(parts[1]);

		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.sprite == null) continue;
			if (!mob.getClass().getSimpleName().equals(cls)) continue;
			if (starOf(mob) != star) continue;

			mob.sprite.flash();
			mob.sprite.centerEmitter().burst(
					com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
							com.shatteredpixel.shatteredpixeldungeon.effects.Speck.STAR),
					star >= 3 ? 24 : 12);
			if (star >= 3) {
				mob.sprite.centerEmitter().burst(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
								com.shatteredpixel.shatteredpixeldungeon.effects.Speck.LIGHT), 12);
				mob.sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE, "★★★");
			} else {
				mob.sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE, "★★");
			}
			com.watabou.noosa.audio.Sample.INSTANCE.play(
					com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.LEVELUP);
			break;
		}
	}

	private void clearUnits() {

		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			boolean unit = mob.buff(AutochessUnitBuff.class) != null
					|| mob.alignment == Char.Alignment.ENEMY;
			if (unit) {
				despawn(mob);
			}
		}
		// 对面备战席的展示棋子也一起清掉（它们同样不该留在战场上）
		clearBenchVisuals();
	}

	/**
	 * 清掉一个棋子。
	 *
	 * <p>不能直接 {@code mob.destroy()}：它会给英雄发经验、记击杀统计、掉战利品，
	 * 于是「拖一下棋子就升级」。先把阵营改成中立再 destroy，这些奖励分支就都不走了。</p>
	 */
	private static void despawn(Mob mob) {
		AutochessRun current = get();
		if (current != null) {
			current.unitStars.remove(mob);
		}
		if (mob == null) return;
		if (mob.sprite != null) mob.sprite.killAndErase();
		mob.alignment = Char.Alignment.NEUTRAL;
		mob.destroy();
	}

	/** 把双方按当前摆位重新铺到棋盘上（备战阶段就是预览，不会动）。 */
	public void refreshPreview() {
		// 清掉棋盘上遗留的掉落物（旧存档/怪物死亡都可能留下）
		if (Dungeon.level != null && Dungeon.level.heaps != null) {
			Dungeon.level.heaps.clear();
		}
		if (!(Dungeon.level instanceof ChessBoardLevel)) return;
		clearUnits();

		unitMobs.clear();
		unitVersion++;
		// 战斗中不生成备战席棋子：它们不参战，留在场上只会干扰回合推进
		if (state != State.FIGHT) {
		for (int i = 0; i < bench.length; i++) {
			String[] parts = split(bench, i);
			if (parts == null) continue;
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def == null) continue;
			Mob mob = spawnUnit(def, ChessBoardLevel.benchCell(i), true, parseInt(parts[1]));
			if (mob != null) unitMobs.put(i, mob);
			AutochessUnitBuff ib = mob == null ? null : mob.buff(AutochessUnitBuff.class);
			if (ib != null) ib.setItems(AutochessItems.parse(bench[i]));
		}
		}

		for (int i = 0; i < board.length; i++) {
			String[] parts = split(board, i);
			if (parts == null) continue;
			AutochessPool.Def def = AutochessPool.ofName(parts[0]);
			if (def == null) continue;
			Mob mob = spawnUnit(def, boardCell[i], true, parseInt(parts[1]));
			if (mob != null) unitMobs.put(100 + i, mob);
			AutochessUnitBuff ib2 = mob == null ? null : mob.buff(AutochessUnitBuff.class);
			if (ib2 != null) ib2.setItems(AutochessItems.parse(board[i]));
		}
		spawnEnemies();
		if (state == State.FIGHT) {
			// 战斗中对面备战席也不留实体：它们不参战，留着会干扰回合
			clearBenchVisuals();
		} else {
			spawnOpponentBench();
		}

		// 备战阶段就把本轮配对算出来，对战大厅才能显示「即将对战谁」
		if (state == State.PREP) {
			buildMatches();
		}

		if (fxPending != null) {
			playMergeFx(fxPending);
			fxPending = null;
		}
	}

	/** 把对手的备战席也铺出来，纯展示、不参战。 */
	private void spawnOpponentBench() {
		clearBenchVisuals();
		if (opponent == null || Dungeon.level == null) return;
		for (int i = 0; i < opponent.roster.size() && i < ChessBoardLevel.ENEMY_BENCH_COUNT; i++) {
			AutochessPool.Def def = AutochessPool.ofName(AutochessBot.clsOf(opponent.roster.get(i)));
			if (def == null) continue;
			int star = AutochessBot.starOf(opponent.roster.get(i));
			Mob mob = spawnUnit(def, ChessBoardLevel.enemyBenchCell(i), false, star);
			if (mob != null) {
				mob.alignment = Char.Alignment.NEUTRAL;
				mob.state = mob.SLEEPING;
				benchVisuals.add(mob);
			}
		}
	}

	private void clearBenchVisuals() {
		for (Mob mob : benchVisuals) {
			if (mob != null && mob.isAlive()) despawn(mob);
		}
		benchVisuals.clear();
	}


	/** 敌方半场铺上「本轮对手」的阵容；没有对手就退化成随机波次。 */
	private void spawnEnemies() {
		if (opponent == null || !opponent.alive()) {
			pickOpponent();
		}
		if (opponent == null) {
			int count = Math.min(6, 1 + round);
			for (int i = 0; i < count; i++) {
				AutochessPool.Def def = AutochessPool.randomForLevel(round + 2);
				if (def == null) continue;
				spawnUnit(def, ChessBoardLevel.enemySlot(i), false, 1);
			}
			return;
		}
		java.util.ArrayList<String> units = opponent.deployed();
		if (units.isEmpty()) {
			// 对手还没成型（极端情况），先拿随机波次顶一下，别让玩家白赢
			int count = Math.min(6, 1 + round);
			for (int i = 0; i < count; i++) {
				AutochessPool.Def def = AutochessPool.randomForLevel(round + 2);
				if (def == null) continue;
				spawnUnit(def, ChessBoardLevel.enemySlot(i), false, 1);
			}
			return;
		}
		for (int i = 0; i < units.size(); i++) {
			AutochessPool.Def def = AutochessPool.ofName(AutochessBot.clsOf(units.get(i)));
			if (def == null) continue;
			int star = AutochessBot.starOf(units.get(i));
			Mob foeMob = spawnUnit(def, ChessBoardLevel.enemySlot(i + (round % 3)), false, star);
			giveBotItems(foeMob, i);
		}
	}

	/** 敌方棋子随机带 1~2 件装备（不然 AI 永远没装备）。 */
	/** 把 AI 攒下的装备按顺序发给它的棋子（每个 2 件）。 */
	private void giveBotItems(Mob mob, int unitIndex) {
		if (mob == null) return;
		AutochessFoeBuff fb = mob.buff(AutochessFoeBuff.class);
		if (fb == null) return;
		if (opponent == null || opponent.items.isEmpty()) {
			giveEnemyItems(mob);
			return;
		}
		java.util.ArrayList<String> eq = new java.util.ArrayList<>();
		int start = unitIndex * 2;
		for (int k = 0; k < 2 && start + k < opponent.items.size(); k++) {
			eq.add(opponent.items.get(start + k));
		}
		if (!eq.isEmpty()) fb.setItems(eq);
	}

	private static void giveEnemyItems(Mob mob) {
		if (mob == null || Math.random() > 0.55) return;
		AutochessFoeBuff fb = mob.buff(AutochessFoeBuff.class);
		if (fb == null) return;
		int n = 1 + (int) (Math.random() * 2);
		java.util.ArrayList<String> eq = new java.util.ArrayList<>();
		for (int k = 0; k < n; k++) {
			eq.add(AutochessItems.ALL[(int) (Math.random() * AutochessItems.ALL.length)]);
		}
		fb.setItems(eq);
	}

	/** 轮换着挑一个还活着的 AI 当对手。 */
	private void pickOpponent() {
		java.util.ArrayList<AutochessBot> alive = new java.util.ArrayList<>();
		for (AutochessBot bot : bots) {
			if (bot.alive()) alive.add(bot);
		}
		if (alive.isEmpty()) {
			opponent = null;
			return;
		}
		opponent = alive.get(round % alive.size());
	}

	public void startFight() {
		if (state != State.PREP) return;
		if (deployed() == 0) {
			GLog.w(M.L(AutochessRun.class, "need_deploy"));
			return;
		}
		state = State.FIGHT;
		fightTurns = 0;
		buildMatches();
		refreshPreview();
		int enemyCount = (opponent != null && !opponent.deployed().isEmpty())
				? opponent.deployed().size()
				: Math.min(6, 1 + round);
		GLog.p(M.L(AutochessRun.class, "fight_start", round, enemyCount));
	}

	/** 某只场上棋子的星级；没记录就当作 1 星。 */
	public int starOf(Char mob) {
		Integer star = unitStars.get(mob);
		return star == null ? 1 : star;
	}

	private static Mob spawnUnit(AutochessPool.Def def, int cell, boolean playerSide, int star) {
		if (cell == -1 || Dungeon.level == null) return null;
		Mob mob;
		try {
			mob = Reflection.newInstance(def.cls);
		} catch (Exception e) {
			return null;
		}
		if (mob == null) return null;
		// 自走棋里放不下的单位（Boss / 不能移动）换成普通杂兵兜底
		if (mob.properties().contains(Char.Property.BOSS)
				|| mob.properties().contains(Char.Property.IMMOVABLE)) {
			mob = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
		}
		mob.pos = cell;
		// 自走棋里棋子不掉落物（没人捡，只会堆一地）
		mob.clearLoot();
		AutochessRun current = get();
		if (current != null) {
			current.unitStars.put(mob, Math.max(1, star));
		}
		mob.state = mob.HUNTING;
		GameScene.add(mob);
		Dungeon.level.occupyCell(mob);
		if (playerSide) {
			AutochessUnitBuff buff = Buff.affect(mob, AutochessUnitBuff.class);
			buff.setup(def.traits, star, def.skill);

		} else {

			// 自走棋里的敌人不给英雄经验/升级，玩家是观战方
			mob.maxLvl = 0;
			// 敌方也要能放技能、吃装备加成
			AutochessFoeBuff foe = Buff.affect(mob, AutochessFoeBuff.class);
			foe.setup(def.skill, star);
		}
		// 场景刚建时 sprite 可能还没挂上，beckon 会 NPE；
		// 另外备战阶段不 beckon，否则每个棋子头上都会冒一个黄色感叹号
		AutochessRun run = get();
		if (mob.sprite != null && run != null && run.state == State.FIGHT) {
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

	/** 本场战斗已经推进了多少回合（防止双方够不到对方时永远打不完）。 */
	private int fightTurns = 0;

	/** 每回合推进计数（用于开战宽限）。 */
	public void tickFight() {
		if (state != State.FIGHT) return;
		fightTurns++;
		// 远程单位每隔几拍主动开火：无视挡在前面的友军
		if (fightTurns % 3 == 0) rangedAssist();
	}

	/** 这些棋子按远程处理（能越过友军直接打对面）。 */
	private static final java.util.HashSet<String> RANGED = new java.util.HashSet<>(java.util.Arrays.asList(
			"DM100", "BlueShaman", "Necromancer", "Warlock",
			"DM175", "Artillerist", "ShieldMage", "Eye", "Scorpio", "Acidic", "FireElemental"));

	private void rangedAssist() {
		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive()) continue;
			if (!RANGED.contains(mob.getClass().getSimpleName())) continue;
			Mob enemy = null;
			for (Mob other : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (!other.isAlive() || other.alignment == mob.alignment) continue;
				int d = Dungeon.level.distance(mob.pos, other.pos);
				if (d > 5) continue;
				if (enemy == null || d < Dungeon.level.distance(mob.pos, enemy.pos)) enemy = other;
			}
			if (enemy == null) continue;
			// 只帮忙挑目标，真正出手交给它自己的 AI（保留攻速与攻击动画节奏）
			mob.retarget(enemy.pos);
		}
	}

	public void checkFight() {
		if (state != State.FIGHT || Dungeon.level == null) return;
		// 开局头几拍不判负：单位刚生成、刚唤醒，避免「点了开战立刻失败」
		if (fightTurns < 3) return;
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
		damageOpponent();
		dropItem();
		advanceBots();
		round++;
		if (allBotsDead()) {
			showVictory();
			return;
		}
		rollShop();
		com.shatteredpixel.shatteredpixeldungeon.ui.AutochessHUD.banner("胜利！", 0x66FF66);
		GLog.p(M.L(AutochessRun.class, "win_income", BASE_INCOME, lastInterest, lastStreakBonus, lastIncome, round));
		refreshPreview();
		showShopNextFrame();
	}

	/** 赢了：对手按「2 + 场上存活单位」掉血。 */
	private void damageOpponent() {
		if (opponent == null || !opponent.alive()) return;
		int survivors = ChessBoardLevel.countSide(true);
		int dmg = stageDamage() + survivors;
		opponent.damage(dmg);
		for (Match match : matches) {
			if ("你".equals(match.a) && match.winner == null) match.winner = "你";
		}
		resolveAiMatches();
		GLog.p(M.L(AutochessRun.class, "bot_hurt", opponent.name, dmg, opponent.hp));
	}

	/** 每回合所有 AI 一起变强，并让它们互相消耗一点血。 */
	/** 每回合结束给一件装备（背包上限 9）。 */
	private void dropItem() {
		if (itemBag.size() >= 9) return;
		String item = AutochessItems.random();
		itemBag.add(item);
		GLog.p(M.L(AutochessRun.class, "item_drop", AutochessItems.name(item)));
		com.shatteredpixel.shatteredpixeldungeon.ui.AutochessHUD.banner("掉落装备：" + AutochessItems.name(item), 0xFFD766);
	}

	/** 把背包里的装备装到指定格子的棋子上。 */
	public boolean equipItem(int side, int index, String itemId) {
		String[] arr = entries(side);
		if (arr == null || index < 0 || index >= arr.length) return false;
		String entry = arr[index];
		if (entry == null) return false;
		java.util.ArrayList<String> items = AutochessItems.parse(entry);
		if (items.size() >= AutochessItems.MAX_PER_UNIT) {
			GLog.w(M.L(AutochessRun.class, "item_full"));
			return false;
		}
		String base = entry;
		int bar = base.indexOf('|');
		if (bar >= 0) base = base.substring(0, bar);
		items.add(itemId);
		arr[index] = base + "|" + AutochessItems.encode(items);
		itemBag.remove(itemId);
		GLog.p(M.L(AutochessRun.class, "item_equip", AutochessItems.name(itemId), nameOf(base.split(":")[0])));
		refreshPreview();
		return true;
	}

	/** 阶段基础伤害：2 起步，每 5 回合 +1，最高 8（云顶同一套思路）。 */
	public int stageDamage() {
		return Math.min(8, 2 + round / 5);
	}

	/** 上一轮自己掉了多少血（UI 用）。 */
	public int lastDamage() {
		return lastDamage;
	}

	/** 本轮配对：玩家打本轮对手，其余 AI 两两配对。 */
	private void buildMatches() {
		matches.clear();
		if (opponent != null && opponent.alive()) {
			matches.add(new Match("你", opponent.name));
		}
		java.util.ArrayList<AutochessBot> rest = new java.util.ArrayList<>();
		for (AutochessBot bot : bots) {
			if (bot.alive() && bot != opponent) rest.add(bot);
		}
		java.util.Collections.shuffle(rest, new java.util.Random(round * 7919L));
		for (int i = 0; i + 1 < rest.size(); i += 2) {
			matches.add(new Match(rest.get(i).name, rest.get(i + 1).name));
		}
		if (rest.size() % 2 == 1) {
			matches.add(new Match(rest.get(rest.size() - 1).name, "轮空"));
		}
	}

	/** 结算 AI 之间的对局（玩家那场由棋盘战斗决定）。 */
	private void resolveAiMatches() {
		for (Match match : matches) {
			if (match.winner != null) continue;
			if ("你".equals(match.a) || "你".equals(match.b)) continue;
			AutochessBot a = botByName(match.a);
			AutochessBot b = botByName(match.b);
			if (a == null || b == null) continue;
			int pa = a.power() + (int) (Math.random() * 6);
			int pb = b.power() + (int) (Math.random() * 6);
			AutochessBot loser = pa >= pb ? b : a;
			AutochessBot winner = loser == a ? b : a;
			loser.damage(stageDamage() + 2);
			match.winner = winner.name;
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i(
					M.L(AutochessRun.class, "ai_battle", winner.name, loser.name, loser.hp));
		}
	}

	private AutochessBot botByName(String name) {
		for (AutochessBot bot : bots) {
			if (bot.name.equals(name)) return bot;
		}
		return null;
	}

	private void advanceBots() {
		if (pool == null) return;
		for (AutochessBot bot : bots) {
			if (!bot.alive()) continue;
			bot.advance(pool, round);
			// 除本轮的对手外，其它 AI 互殴：随机掉 1~3 血
			if (bot != opponent && Math.random() < 0.5) {
				bot.damage(1 + (int) (Math.random() * 3));
			}
		}
	}

	private boolean allBotsDead() {
		for (AutochessBot bot : bots) {
			if (bot.alive()) return false;
		}
		return true;
	}

	private void showVictory() {
		state = State.PREP;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndAutochessResult(round, gold));
			}
		});
	}

	private void loseRound() {
		state = State.PREP;
		winStreak = 0;
		loseStreak++;

		// 云顶式扣血：阶段基础（随回合涨）+ 场上还活着的敌人
		int survivors = ChessBoardLevel.countSide(false);
		int damage = stageDamage() + survivors;
		lastDamage = damage;
		life -= damage;
		for (Match match : matches) {
			if ("你".equals(match.a) && match.winner == null) match.winner = match.b;
		}
		resolveAiMatches();
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
		advanceBots();
		round++;
		rollShop();
		com.shatteredpixel.shatteredpixeldungeon.ui.AutochessHUD.banner("战败 -" + damage, 0xFF6666);
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
			if (def != null && def.hasTrait(trait)) count++;
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

	/** 当前对手（可能为 null）。 */
	public AutochessBot opponent() {
		return opponent;
	}

	public java.util.ArrayList<AutochessBot> bots() {
		return bots;
	}

	public int aliveBots() {
		int n = 0;
		for (AutochessBot bot : bots) if (bot.alive()) n++;
		return n;
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
		// 战斗中不生成备战席棋子：它们不参战，留在场上只会干扰回合推进
		if (state != State.FIGHT) {
		for (int i = 0; i < bench.length; i++) {
			bundle.put("ac_bench_" + i, bench[i] == null ? "" : bench[i]);
		}
		}

		for (int i = 0; i < board.length; i++) {
			bundle.put("ac_board_" + i, board[i] == null ? "" : board[i]);
			bundle.put("ac_cell_" + i, boardCell[i]);
		}

		for (int i = 0; i < offers.length; i++) {
			bundle.put("ac_offer_" + i, offers[i] == null ? "" : offers[i]);
			bundle.put("ac_price_" + i, prices[i]);
		}

		// AI 对手：血量/等级/金币/阵容
		bundle.put("ac_bot_count", bots.size());
		for (int bi = 0; bi < bots.size(); bi++) {
			AutochessBot bot = bots.get(bi);
			StringBuilder sb = new StringBuilder();
			sb.append(bot.name).append("|").append(bot.hp).append("|")
					.append(bot.level).append("|").append(bot.gold);
			for (String entry : bot.roster) sb.append(";").append(entry);
			bundle.put("ac_bot_" + bi, sb.toString());
			bundle.put("ac_bot_items_" + bi, AutochessItems.encode(bot.items));
		}
		if (opponent != null) bundle.put("ac_opponent", opponent.name);
		bundle.put("ac_items", AutochessItems.encode(itemBag));
		bundle.put("ac_locked", shopLocked);
		if (pool != null) {
			for (AutochessPool.Def def : AutochessPool.all()) {
				bundle.put("ac_pool_" + def.cls.getSimpleName(), pool.count(def.cls.getSimpleName()));
			}
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
		// 战斗中不生成备战席棋子：它们不参战，留在场上只会干扰回合推进
		if (state != State.FIGHT) {
		for (int i = 0; i < bench.length; i++) {
			String e = bundle.getString("ac_bench_" + i);
			bench[i] = e == null || e.isEmpty() ? null : e;
		}
		}

		for (int i = 0; i < board.length; i++) {
			String e = bundle.getString("ac_board_" + i);
			board[i] = e == null || e.isEmpty() ? null : e;
			boardCell[i] = bundle.contains("ac_cell_" + i)
					? bundle.getInt("ac_cell_" + i)
					: ChessBoardLevel.playerSlot(i);
		}

		for (int i = 0; i < offers.length; i++) {
			String name = bundle.getString("ac_offer_" + i);
			offers[i] = name == null || name.isEmpty() ? null : name;
			prices[i] = bundle.getInt("ac_price_" + i);
		}
		// 共享池
		pool = new AutochessSharedPool();
		for (AutochessPool.Def def : AutochessPool.all()) {
			String cls = def.cls.getSimpleName();
			if (bundle.contains("ac_pool_" + cls)) {
				pool.setCount(cls, bundle.getInt("ac_pool_" + cls));
			}
		}
		// AI 对手
		bots.clear();
		int botCount = bundle.getInt("ac_bot_count");
		for (int bi = 0; bi < botCount; bi++) {
			String saved = bundle.getString("ac_bot_" + bi);
			if (saved == null || saved.isEmpty()) continue;
			String[] parts = saved.split("\\|");
			if (parts.length < 4) continue;
			AutochessBot bot = new AutochessBot(parts[0]);
			bot.hp = parseInt(parts[1]);
			bot.level = parseInt(parts[2]);
			bot.gold = parseInt(parts[3]);
			int semi = saved.indexOf(";");
			if (semi >= 0) {
				for (String entry : saved.substring(semi + 1).split(";")) {
					if (!entry.isEmpty()) bot.roster.add(entry);
				}
			}
			String its = bundle.getString("ac_bot_items_" + bi);
			if (its != null && !its.isEmpty()) {
				bot.items.addAll(AutochessItems.parse("|" + its));
			}
			bots.add(bot);
		}
		opponent = null;
		String oppName = bundle.getString("ac_opponent");
		if (oppName != null) {
			for (AutochessBot bot : bots) {
				if (bot.name.equals(oppName)) opponent = bot;
			}
		}
		// 老存档没有 AI 数据：补建 5 个，别让大厅和对手空着
		if (bots.isEmpty()) {
			String[] defaultNames = {"老王", "阿呆", "包子", "萝卜头", "土豆"};
			for (String n : defaultNames) bots.add(new AutochessBot(n));
			for (AutochessBot bot : bots) bot.advance(pool, 0);
		}
		// 装备背包
		itemBag.clear();
		String savedItems = bundle.getString("ac_items");
		if (savedItems != null && !savedItems.isEmpty()) {
			for (String id : savedItems.split(",")) {
				if (!id.isEmpty()) itemBag.add(id);
			}
		}
		shopLocked = bundle.getBoolean("ac_locked");
		if (round < 1) round = 1;
		if (level < 1) level = 1;
		if (life <= 0) life = START_LIFE;
	}
}
