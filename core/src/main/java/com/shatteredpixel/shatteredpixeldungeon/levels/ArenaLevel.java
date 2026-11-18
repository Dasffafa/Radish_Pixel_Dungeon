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
import com.watabou.utils.Random;

/**
 * 「三分钟一把」竞技场：一张干净的大空房间，四面是墙，四角有几块掩体。
 *
 * 这里不生成怪物、不生成物品、也没有真正的楼梯——出怪完全由
 * {@link com.shatteredpixel.shatteredpixeldungeon.challenges.ArenaRun} 按波次投放。
 */
public class ArenaLevel extends DeadEndLevel {

	/** 地图边长（含外围墙）。21 格在手机上正好是一屏多一点，跑位不累。 */
	public static final int SIZE = 21;

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
		return Assets.Environment.TILES_CAVES;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
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

		// 四角实心掩体：给玩家一点绕柱走位的空间，也不至于空得像测试场
		int center = SIZE / 2;
		int offset = center - 4;
		int[][] pillars = {
				{center - offset, center - offset},
				{center + offset, center - offset},
				{center - offset, center + offset},
				{center + offset, center + offset}
		};
		for (int[] p : pillars) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dx = -1; dx <= 1; dx++) {
					int x = p[0] + dx;
					int y = p[1] + dy;
					if (x <= 0 || y <= 0 || x >= SIZE - 1 || y >= SIZE - 1) continue;
					map[y * width() + x] = Terrain.WALL;
				}
			}
		}

		// 玩家从正中登场
		int entrance = center * width() + center;
		map[entrance] = Terrain.ENTRANCE;

		LevelTransition t = LevelTransition.surface(this, entrance);
		t.type = LevelTransition.Type.REGULAR_ENTRANCE;
		transitions.add(t);

		return true;
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		// 竞技场没有上一层可去。不拦的话会按「上楼」读第 0 层直接闪退
		if (transition.direction == LevelTransition.Direction.SURFACE) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndMessage(M.L(ArenaLevel.class, "no_way_up")));
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

	/** 竞技场没有出口，指北针指回中心，免得指向地图外。 */
	@Override
	public int exit() {
		return entrance();
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}

	/** 波次出怪要一个「离玩家远一点」的落点，roll 不到就退回入口。 */
	@Override
	public int randomRespawnCell(Char ch) {
		int cell = super.randomRespawnCell(ch);
		if (cell != -1 && Dungeon.hero != null && distance(cell, Dungeon.hero.pos) < 4) {
			// 太贴脸会直接把玩家围死，再摇几次
			for (int tries = 0; tries < 10; tries++) {
				int alt = super.randomRespawnCell(ch);
				if (alt != -1 && distance(alt, Dungeon.hero.pos) >= 4) return alt;
			}
		}
		if (cell == -1 && Dungeon.hero != null) {
			// 极端情况（场地被占满）就给一个随机可站格
			for (int tries = 0; tries < 200; tries++) {
				int alt = Random.Int(length());
				if (passable[alt] && Actor.findChar(alt) == null) return alt;
			}
		}
		return cell;
	}
}
