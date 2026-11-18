package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Callback;

/**
 * 自走棋棋盘：15×15。
 *
 * <pre>
 *  y=1   [观战包间 x1-3][ 备战位 x5-13 共 9 格 ]
 *  y=2   一整排墙，把备战区和战场隔开（棋子靠拖拽进出，不用走路）
 *  y=3+  战场：左半（x 1..6）玩家，右半（x 9..13）敌方，x=7 是中线装饰格
 * </pre>
 */
public class ChessBoardLevel extends DeadEndLevel {

	public static final int SIZE = 15;
	public static final int PLAYER_MAX_X = 6;
	public static final int ENEMY_MIN_X = 9;
	public static final int FIELD_MIN_Y = 3;
	public static final int BENCH_Y = 1;
	public static final int BENCH_MIN_X = 5;
	public static final int BENCH_COUNT = 9;

	public static final int SPECTATOR_X = 2;
	public static final int SPECTATOR_Y = 1;

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	@Override
	public void playLevelMusic() {
		Music.INSTANCE.play(Assets.Music.SEWERS_TENSE, true);
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_MOSS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_MOSS;
	}

	@Override
	protected boolean build() {

		setSize(SIZE, SIZE);

		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				boolean border = (x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1);
				map[y * width() + x] = border ? Terrain.WALL : Terrain.EMPTY;
			}
		}

		// 观战包间：x1..3 在 y=1，右边和下边用墙封死
		map[SPECTATOR_Y * width() + 4] = Terrain.WALL;
		for (int x = 1; x <= 4; x++) {
			map[2 * width() + x] = Terrain.WALL;
		}

		// 备战位：y=1 的 x5..13，铺装饰格让玩家一眼看出这是「备战席」
		for (int i = 0; i < BENCH_COUNT; i++) {
			map[BENCH_Y * width() + BENCH_MIN_X + i] = Terrain.EMPTY_DECO;
		}
		// 备战位下面整排墙，和战场隔开
		for (int x = BENCH_MIN_X; x <= SIZE - 2; x++) {
			map[2 * width() + x] = Terrain.WALL;
		}

		// 中线：战场整列装饰格
		int center = SIZE / 2;
		for (int y = FIELD_MIN_Y; y < SIZE - 1; y++) {
			map[y * width() + center] = Terrain.EMPTY_DECO;
		}

		int entrance = SPECTATOR_Y * width() + SPECTATOR_X;
		map[entrance] = Terrain.ENTRANCE;

		LevelTransition t = LevelTransition.surface(this, entrance);
		t.type = LevelTransition.Type.REGULAR_ENTRANCE;
		transitions.add(t);

		return true;
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.direction == LevelTransition.Direction.SURFACE) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndMessage(M.L(ChessBoardLevel.class, "no_way_up")));
				}
			});
			return false;
		}
		return super.activateTransition(hero, transition);
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}

	@Override
	public int exit() {
		return entrance();
	}

	/** 备战位第 i 格在棋盘上的格子。 */
	public static int benchCell(int index) {
		if (Dungeon.level == null || index < 0 || index >= BENCH_COUNT) return -1;
		return BENCH_Y * Dungeon.level.width() + BENCH_MIN_X + index;
	}

	/** 这个格子是不是备战位，是的话返回下标，否则 -1。 */
	public static int benchIndexAt(int cell) {
		if (Dungeon.level == null) return -1;
		int w = Dungeon.level.width();
		int x = cell % w;
		int y = cell / w;
		if (y != BENCH_Y || x < BENCH_MIN_X || x >= BENCH_MIN_X + BENCH_COUNT) return -1;
		return x - BENCH_MIN_X;
	}

	/** 上阵位默认站位（战场左半场）。 */
	public static int playerSlot(int index) {
		int[][] layout = {
				{5, 8}, {5, 7}, {5, 9}, {4, 8}, {4, 7}, {4, 9}, {3, 8}, {3, 7}, {3, 9},
				{6, 8}, {6, 7}, {6, 9}
		};
		int[] slot = layout[Math.floorMod(index, layout.length)];
		return cellAt(slot[0], slot[1]);
	}

	/** 敌方站位（战场右半场）。 */
	public static int enemySlot(int index) {
		int[][] layout = {
				{12, 8}, {12, 7}, {12, 9}, {12, 6}, {12, 10}, {11, 8}, {11, 7}, {11, 9},
				{11, 6}, {11, 10}, {10, 8}, {10, 7}
		};
		int[] slot = layout[Math.floorMod(index, layout.length)];
		return cellAt(slot[0], slot[1]);
	}

	private static int cellAt(int x, int y) {
		if (Dungeon.level == null) return -1;
		if (x <= 0 || y <= 0 || x >= SIZE - 1 || y >= SIZE - 1) return -1;
		int cell = y * Dungeon.level.width() + x;
		if (!Dungeon.level.passable[cell] || Actor.findChar(cell) != null) {
			for (int r = 1; r <= 4; r++) {
				for (int dy = -r; dy <= r; dy++) {
					for (int dx = -r; dx <= r; dx++) {
						if (Math.max(Math.abs(dx), Math.abs(dy)) != r) continue;
						int nx = x + dx;
						int ny = y + dy;
						if (nx <= 0 || ny <= 0 || nx >= SIZE - 1 || ny >= SIZE - 1) continue;
						int alt = ny * Dungeon.level.width() + nx;
						if (Dungeon.level.passable[alt] && Actor.findChar(alt) == null) {
							return alt;
						}
					}
				}
			}
			return -1;
		}
		return cell;
	}

	/** 这个格子是不是备战位。 */
	public static boolean isBenchCell(int cell) {
		return benchIndexAt(cell) >= 0;
	}

	/** 战场里属于玩家的格子（x ≤ 6 且在中线以下）。 */
	public static boolean isPlayerField(int cell) {
		if (cell < 0 || Dungeon.level == null) return false;
		int w = Dungeon.level.width();
		return cell % w <= PLAYER_MAX_X && cell / w >= FIELD_MIN_Y;
	}

	/** 战场里属于敌方的格子。 */
	public static boolean isEnemyField(int cell) {
		if (cell < 0 || Dungeon.level == null) return false;
		int w = Dungeon.level.width();
		return cell % w >= ENEMY_MIN_X && cell / w >= FIELD_MIN_Y;
	}

	/**
	 * 棋盘上指定阵营的单位数量。
	 *
	 * <p>玩家的备战位棋子虽然也站在棋盘上，但不参战，所以这里要排除掉，
	 * 否则「一个上阵单位都没有」也会被判成还没输。</p>
	 */
	public static int countSide(boolean player) {
		if (Dungeon.level == null) return 0;
		int count = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive()) continue;
			boolean ally = mob.alignment == Char.Alignment.ALLY;
			if (ally != player) continue;
			if (ally && isBenchCell(mob.pos)) continue;
			count++;
		}
		return count;
	}
}
