/*
 * Radish Pixel Dungeon
 *
 * 环境小生物：
 *  - 地面小虫（蚱蜢/甲虫/蜘蛛）：在英雄视野内的空地上钻出、跳几步、淡出
 *  - 萤火虫：在英雄附近的空中飘动、明暗呼吸
 * 都只影响画面，不参与回合与战斗（不会进 Dungeon.level.mobs）。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class AmbientCritters {

	private static final String SHEET = "effects/dungeon_critters.png";
	private static final String FLY_SHEET = "effects/critters.png";
	private static final int TILE = 16;

	// 小虫精灵表 8 列，每行一种
	private static final int[][] KINDS = {
			{ 0, 1, 2, 3, 4, 5 },        // 蚱蜢
			{ 8, 9, 10, 11, 12, 13 },    // 甲虫
			{ 40, 41, 42, 43, 44 },      // 蜘蛛
	};
	// 萤火虫：critters.png 第 8 行的光点帧
	private static final int[] FLY_FRAMES = { 56, 57, 58 };

	private static final List<Critter> alive = new ArrayList<>();
	private static final List<Firefly> flies = new ArrayList<>();
	private static Group host;
	// 生成间隔：小虫 9~18s，萤火虫 12~24s
	private static float nextIn = 9f, nextFlyIn = 12f;

	/** 调试计数 */
	public static int spawned, firefliesSpawned, tries_used;
	public static int lastCritterCell = -1, lastFlyCell = -1, badCells = 0;

	public static void init( Group group ) {
		host = group;
		alive.clear();
		flies.clear();
		nextIn = 9f;
		nextFlyIn = 12f;
		spawned = 0;
		firefliesSpawned = 0;
		tries_used = 0;
		lastCritterCell = -1;
		lastFlyCell = -1;
		badCells = 0;
	}

	public static void update() {
		if (host == null || Dungeon.level == null || Dungeon.hero == null) return;

		for (int i = alive.size() - 1; i >= 0; i--) {
			if (!alive.get(i).alive) alive.remove(i);
		}
		for (int i = flies.size() - 1; i >= 0; i--) {
			if (!flies.get(i).alive) flies.remove(i);
		}

		nextIn -= Game.elapsed;
		if (nextIn <= 0f) {
			nextIn = 9f + Random.Float( 9f );
			if (alive.size() < 1) spawnCritter();
		}

		nextFlyIn -= Game.elapsed;
		if (nextFlyIn <= 0f) {
			nextFlyIn = 12f + Random.Float( 12f );
			if (flies.size() < 2) spawnFirefly();
		}
	}

	private static void spawnCritter() {
		Hero hero = Dungeon.hero;
		int width = Dungeon.level.width();
		// 只放在干燥、可通行、看得见、没被占的格子；找不到就不生成
		for (int tries = 0; tries < 16; tries++) {
			int dx = Random.Int( 5 ) - 2;
			int dy = Random.Int( 5 ) - 2;
			if (dx == 0 && dy == 0) continue;
			int cell = hero.pos + dx + dy * width;
			if (cell < 0 || cell >= Dungeon.level.map.length) continue;
			if (!Dungeon.level.heroFOV[cell]) continue;
			if (!Dungeon.level.passable[cell]) continue;
			if (Dungeon.level.map[cell] == Terrain.CHASM) continue;
			if (Dungeon.level.water[cell]) continue;
			if (Actor.findChar( cell ) != null) continue;

			Critter c = new Critter( KINDS[Random.Int( KINDS.length )], cell );
			host.add( c );
			alive.add( c );
			spawned++;
			tries_used += tries + 1;
			lastCritterCell = cell;
			return;
		}
	}

	private static void spawnFirefly() {
		Hero hero = Dungeon.hero;
		int width = Dungeon.level.width();
		for (int tries = 0; tries < 12; tries++) {
			int dx = Random.Int( 5 ) - 2;
			int dy = Random.Int( 5 ) - 2;
			int cell = hero.pos + dx + dy * width;
			if (cell < 0 || cell >= Dungeon.level.map.length) continue;
			if (!Dungeon.level.heroFOV[cell]) continue;
			if (Dungeon.level.map[cell] == Terrain.CHASM) continue;
			if (!Dungeon.level.passable[cell] && !Dungeon.level.water[cell]) continue;

			float px = (cell % width) * TILE + Random.Float( 16f );
			float py = (cell / width) * TILE + Random.Float( 16f );
			Firefly f = new Firefly( px, py );
			host.add( f );
			flies.add( f );
			firefliesSpawned++;
			lastFlyCell = cell;
			return;
		}
	}

	public static class Critter extends MovieClip {

		private static final float HOP = 0.22f;

		private final int[] frames;
		private int cell;
		private float life, alphaT, hopIn, hopT = -1f;
		private float fromX, fromY, toX, toY;

		public Critter( int[] frames, int cell ) {
			super( SHEET );
			this.frames = frames;
			this.cell = cell;

			TextureFilm film = new TextureFilm( texture, TILE, TILE );
			play( new Animation( 8, true ).frames( film, toObjects( frames ) ) );

			x = (cell % Dungeon.level.width()) * TILE;
			y = (cell / Dungeon.level.width()) * TILE;
			fromX = toX = x;
			fromY = toY = y;

			life = 4f + Random.Float( 3f );
			hopIn = Random.Float( 0.4f );
			alpha( 0f );
		}

		private static Object[] toObjects( int[] ints ) {
			Object[] out = new Object[ints.length];
			for (int i = 0; i < ints.length; i++) out[i] = ints[i];
			return out;
		}

		@Override
		public void update() {
			super.update();
			float dt = Game.elapsed;

			life -= dt;
			alphaT += dt;
			if (life <= 0f) {
				killAndErase();
				return;
			}
			float target = life < 0.7f ? Math.max( 0f, life / 0.7f ) : Math.min( 1f, alphaT / 0.25f );
			alpha( target );

			hopIn -= dt;
			if (hopIn <= 0f && hopT < 0f) {
				hopIn = 0.45f + Random.Float( 0.5f );
				startHop();
			}

			if (hopT >= 0f) {
				hopT -= dt;
				float p = 1f - Math.max( 0f, hopT ) / HOP;
				x = fromX + (toX - fromX) * p;
				y = fromY + (toY - fromY) * p - (float)Math.sin( p * Math.PI ) * 2f;
				if (hopT < 0f) {
					x = toX;
					y = toY;
				}
			}
		}

		private void startHop() {
			int width = Dungeon.level.width();
			int[][] dirs = { {0,-1}, {0,1}, {-1,0}, {1,0} };
			int d = Random.Int( 4 );
			int next = cell + dirs[d][0] + dirs[d][1] * width;
			if (next < 0 || next >= Dungeon.level.map.length) return;
			if (!Dungeon.level.passable[next]) return;
			if (Math.abs( (next % width) - (cell % width) ) > 1) return;

			cell = next;
			fromX = x;
			fromY = y;
			toX = (cell % width) * TILE;
			toY = (cell / width) * TILE;
			hopT = HOP;
		}
	}

	public static class Firefly extends MovieClip {

		private float life, age, vx, vy, phase;

		public Firefly( float px, float py ) {
			super( FLY_SHEET );

			TextureFilm film = new TextureFilm( texture, TILE, TILE );
			play( new Animation( 6, true ).frames( film, 56, 57, 58 ) );

			x = px;
			y = py;
			vx = Random.Float( -7f, 7f );
			vy = Random.Float( -5f, 5f );
			life = 6f + Random.Float( 6f );
			phase = Random.Float( 6.28f );
			hardlight( 0xFFFF99 );
			alpha( 0f );
		}

		@Override
		public void update() {
			super.update();
			float dt = Game.elapsed;
			age += dt;
			life -= dt;
			if (life <= 0f) {
				killAndErase();
				return;
			}

			x += vx * dt;
			y += vy * dt + (float)Math.sin( age * 2.4f + phase ) * 8f * dt;

			float pulse = 0.5f + 0.5f * (float)Math.sin( age * 4f + phase );
			float fade = Math.min( 1f, Math.min( age * 2f, life * 0.8f ) );
			alpha( Math.min( 1f, 0.35f + pulse * 0.65f ) * fade );
		}
	}
}
