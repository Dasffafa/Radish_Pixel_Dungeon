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
 * 自走棋棋盘：15×15 空场。
 *
 * <p>左半边（x 2..6）是玩家布阵区，右半边（x 9..13）是敌方出怪区；
 * 左上角掏了一个 {@link #SPECTATOR_X}..{@link #SPECTATOR_Y} 的小隔间给英雄观战，
 * 四面是墙，敌人既看不见也走不进来。</p>
 */
public class ChessBoardLevel extends DeadEndLevel {

	public static final int SIZE = 15;
	public static final int PLAYER_MIN_X = 2;
	public static final int PLAYER_MAX_X = 6;
	public static final int ENEMY_MIN_X = 9;

	public static final int SPECTATOR_X = 7;
	public static final int SPECTATOR_Y = 3;

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

		int center = SIZE / 2;
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				boolean border = (x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1);
				map[y * width() + x] = border ? Terrain.WALL : Terrain.EMPTY;
			}
		}

		// 观战隔间：正中偏上的一间小包间，四面封死，镜头跟着英雄正好框住棋盘
		for (int x = SPECTATOR_X - 2; x <= SPECTATOR_X + 2; x++) {
			map[(SPECTATOR_Y - 1) * width() + x] = Terrain.WALL;
			map[(SPECTATOR_Y + 1) * width() + x] = Terrain.WALL;
		}
		map[SPECTATOR_Y * width() + SPECTATOR_X - 2] = Terrain.WALL;
		map[SPECTATOR_Y * width() + SPECTATOR_X + 2] = Terrain.WALL;

		// 中线：整列装饰格，把玩家半场和敌方半场明确分开（可以走，只是视觉界限）
		for (int y = 1; y < SIZE - 1; y++) {
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

	/** 玩家布阵的第 index 个站位（从后往前排成两列）。 */
	public static int playerSlot(int index) {
		int[][] layout = {
				{6, 7}, {6, 6}, {6, 8}, {5, 7}, {5, 6}, {5, 8}, {4, 7}, {4, 6}, {4, 8}
		};
		int[] slot = layout[Math.floorMod(index, layout.length)];
		return cellAt(slot[0], slot[1]);
	}

	/** 敌方第 index 个站位。 */
	public static int enemySlot(int index) {
		int[][] layout = {
				{12, 7}, {12, 6}, {12, 8}, {12, 5}, {12, 9}, {11, 7}, {11, 6}, {11, 8}
		};
		int[] slot = layout[Math.floorMod(index, layout.length)];
		return cellAt(slot[0], slot[1]);
	}

	private static int cellAt(int x, int y) {
		if (Dungeon.level == null) {
			return -1;
		}
		if (x <= 0 || y <= 0 || x >= SIZE - 1 || y >= SIZE - 1) {
			return -1;
		}
		int cell = y * Dungeon.level.width() + x;
		if (!Dungeon.level.passable[cell] || Actor.findChar(cell) != null) {
			// 被占了就找最近的空位
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

	/** 这个格子是否在玩家半场（含中线左侧）。 */
	public static boolean isPlayerHalf(int cell) {
		if (cell < 0 || Dungeon.level == null) return false;
		return cell % Dungeon.level.width() <= PLAYER_MAX_X;
	}

	/** 这个格子是否在敌方半场。 */
	public static boolean isEnemyHalf(int cell) {
		if (cell < 0 || Dungeon.level == null) return false;
		return cell % Dungeon.level.width() >= ENEMY_MIN_X;
	}

	/** 玩家半场某坐标的格子（越界返回 -1）。 */
	public static int playerCellAt(int x, int y) {
		if (x < 1 || y < 1 || x > PLAYER_MAX_X || y >= SIZE - 1) return -1;
		int cell = y * Dungeon.level.width() + x;
		return Dungeon.level.passable[cell] ? cell : -1;
	}

	/** 棋盘上指定阵营的单位数量。 */
	public static int countSide(boolean player) {
		if (Dungeon.level == null) {
			return 0;
		}
		int count = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive()) continue;
			boolean ally = mob.alignment == Char.Alignment.ALLY;
			if (ally == player) count++;
		}
		return count;
	}
}
