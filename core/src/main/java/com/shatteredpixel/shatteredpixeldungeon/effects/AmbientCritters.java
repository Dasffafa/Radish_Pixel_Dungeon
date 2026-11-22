/*
 * Radish Pixel Dungeon
 *
 * 环境小生物：在英雄视野内的空地上偶尔钻出蜘蛛/甲虫/蚱蜢，跳几步再消失。
 * 只影响画面，不参与回合与战斗（不会进 Dungeon.level.mobs）。
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.noosa.Group;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class AmbientCritters {

	private static final String SHEET = "effects/dungeon_critters.png";
	private static final int TILE = 16;

	// 精灵表 8 列，每行一种小生物
	private static final int[][] KINDS = {
			{ 0, 1, 2, 3, 4, 5 },        // 蚱蜢
			{ 8, 9, 10, 11, 12, 13 },    // 甲虫
			{ 40, 41, 42, 43, 44 },      // 蜘蛛
	};

	private static final List<Critter> alive = new ArrayList<>();
	private static Group host;
	private static float nextIn = 2f;

	/** 调试：生成计数 */
	public static int spawned;
	public static int tries_used;

	public static void init( Group group ) {
		host = group;
		alive.clear();
		nextIn = 2f;
	}

	public static void update() {
		if (host == null || Dungeon.level == null || Dungeon.hero == null) return;

		for (int i = alive.size() - 1; i >= 0; i--) {
			if (!alive.get(i).alive) alive.remove(i);
		}

		nextIn -= com.watabou.noosa.Game.elapsed;
		if (nextIn <= 0f) {
			nextIn = 1.8f + Random.Float( 2.6f );
			if (alive.size() < 2) spawn();
		}
	}

	private static void spawn() {
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		int width = Dungeon.level.width();
		for (int tries = 0; tries < 12; tries++) {
			int dx = Random.Int( 7 ) - 3;
			int dy = Random.Int( 7 ) - 3;
			if (dx == 0 && dy == 0) continue;
			int cell = hero.pos + dx + dy * width;
			if (cell < 0 || cell >= Dungeon.level.map.length) continue;
			if (!Dungeon.level.heroFOV[cell]) continue;
			if (!Dungeon.level.passable[cell]) continue;
			// 水边/水里也能钻出（青蛙类）
			if (Dungeon.level.solid[cell]) continue;

			Critter c = new Critter( KINDS[Random.Int( KINDS.length )], cell );
			host.add( c );
			alive.add( c );
			spawned++;
			tries_used += tries + 1;
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
			float dt = com.watabou.noosa.Game.elapsed;

			life -= dt;
			alphaT += dt;
			if (life <= 0f) {
				killAndErase();
				return;
			}
			// 出现时淡入，消失前淡出
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
			if (!Dungeon.level.passable[next] || Dungeon.level.water[next]) return;
			if (Math.abs( (next % width) - (cell % width) ) > 1) return; // 不跨越整行

			cell = next;
			fromX = x;
			fromY = y;
			toX = (cell % width) * TILE;
			toY = (cell / width) * TILE;
			hopT = HOP;
		}
	}
}
