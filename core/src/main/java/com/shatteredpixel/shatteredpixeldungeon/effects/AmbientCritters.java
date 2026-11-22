package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 环境小动物：使用 Warped 的 DungeonCritterSprite。
 * 生成：视野内 6~11 秒一只，上限 5 只；只在可行走/水面上落脚，活动范围受地图限制（不会穿墙）。
 * 寿命：25~45 秒后自己淡出消失。
 */
public final class AmbientCritters {

	private AmbientCritters(){}

	private static final float SIZE = DungeonTilemap.SIZE;
	private static final int MAX = 5;
	private static final float MIN_GAP = 6f, MAX_GAP = 11f;
	private static final float LIFE_MIN = 25f, LIFE_MAX = 45f;
	/** 主角靠得多近就吓跑，像素（约 2.5 格） */
	private static final float SCARE_DIST = SIZE * 2.5f;

	private static final Random RNG = new Random();

	private static Group host;
	private static final List<Entry> live = new ArrayList<>();
	private static float spawnIn = 4f;
	public static int spawned;
	public static int expired;
	public static int scared;
	public static int badCells;
	public static int lastCell = -1;

	private static final class Entry {
		final DungeonCritterSprite sprite;
		final float bornAt;
		boolean asked;

		Entry( DungeonCritterSprite s, float bornAt ){
			this.sprite = s;
			this.bornAt = bornAt;
		}
	}

	public static void init( Group effects ){
		host = effects;
		live.clear();
		spawned = expired = badCells = scared = 0;
		lastCell = -1;
		spawnIn = 4f;
	}

	public static void update(){
		if (host == null || Dungeon.level == null || Dungeon.hero == null) return;

		float now = com.watabou.noosa.Game.timeTotal;

		for (int i = live.size() - 1; i >= 0; i--){
			Entry e = live.get( i );
			if (!e.sprite.alive){
				live.remove( i );
				continue;
			}
			float age = now - e.bornAt;
			if (!e.asked && age > LIFE_MIN + (e.bornAt % 1f) * (LIFE_MAX - LIFE_MIN)){
				e.asked = true;
				expired++;
				e.sprite.leave( 2f );
			}
			// 跑到不可走的地方（飞出地图/穿墙）：直接淡出，别让它留在黑边
			int cell = cellOf( e.sprite.ax, e.sprite.ay );
			if (cell < 0 || !walkable( cell )){
				badCells++;
				if (!e.asked){
					e.asked = true;
					e.sprite.leave( 0.6f );
				}
			}
		}

		if (Emitter.freezeEmitters) return;

		// 主角走近：吓跑（Warped 的 scare），被吓过的过几秒淡出
		float hx = Dungeon.hero.sprite != null ? Dungeon.hero.sprite.x + Dungeon.hero.sprite.width() / 2f
				: (Dungeon.hero.pos % Dungeon.level.width()) * SIZE + SIZE / 2f;
		float hy = Dungeon.hero.sprite != null ? Dungeon.hero.sprite.y + Dungeon.hero.sprite.height() / 2f
				: (Dungeon.hero.pos / Dungeon.level.width()) * SIZE + SIZE / 2f;
		for (Entry e : live){
			if (e.asked) continue;
			float dx = e.sprite.ax - hx, dy = e.sprite.ay - hy;
			if (dx * dx + dy * dy <= SCARE_DIST * SCARE_DIST){
				if (e.sprite.calm()){
					e.sprite.scare( hx, hy );
					scared++;
				}
				e.asked = true;
				e.sprite.leave( 2.5f );
			}
		}

		spawnIn -= Game.elapsed;
		if (spawnIn > 0f) return;

		spawnIn = MIN_GAP + RNG.nextFloat() * (MAX_GAP - MIN_GAP);
		if (live.size() >= MAX) return;
		spawnOne();
	}

	private static void spawnOne(){
		int w = Dungeon.level.width(), h = Dungeon.level.height();
		int heroX = Dungeon.hero.pos % w, heroY = Dungeon.hero.pos / w;

		for (int tries = 0; tries < 24; tries++){
			int x = heroX + RNG.nextInt( 7 ) - 3;
			int y = heroY + RNG.nextInt( 7 ) - 3;
			if (x < 0 || y < 0 || x >= w || y >= h) continue;

			int cell = x + y * w;
			if (!Dungeon.level.heroFOV[cell]) continue;
			if (Actor.findChar( cell ) != null) continue;
			if (!walkable( cell )) continue;

			float ax = x * SIZE + SIZE / 2f;
			float ay = y * SIZE + SIZE / 2f;

			DungeonCritterSprite s = null;
			boolean water = Dungeon.level.map[cell] == Terrain.WATER;
			int waterCell = nearWater( cell );

			if (waterCell != -1 && !water){
				int wx = waterCell % w, wy = waterCell / w;
				s = new DungeonCritterSprite.Frog( ax, ay, wx * SIZE + SIZE / 2f, wy * SIZE + SIZE / 2f, true );
			} else if (water){
				s = new DungeonCritterSprite.Fly( ax, ay );
			} else {
				// 地面生物为主，飞虫少一些（飞虫会飘过墙，容易显得突兀）
				int roll = RNG.nextInt( 100 );
				if (roll < 30){
					s = new DungeonCritterSprite.Scuttler( DungeonCritterSprite.Scuttler.Look.ROACH, ax, ay, new MapGround() );
				} else if (roll < 55){
					s = new DungeonCritterSprite.Strider( ax, ay );
				} else if (roll < 67){
					s = new DungeonCritterSprite.Fly( ax, ay );
				} else if (roll < 77){
					s = new DungeonCritterSprite.Moth( ax, ay, 0f, 0xE8D9A0, false, true );
				} else if (roll < 89){
					s = new DungeonCritterSprite.Snail( ax, ay );
				} else {
					s = new DungeonCritterSprite.Spider( ax, ay, 0f, 2f );
				}
			}

			if (s == null) continue;

			s.ax = ax;
			s.ay = ay;
			host.add( s );
			live.add( new Entry( s, com.watabou.noosa.Game.timeTotal ) );
			lastCell = cell;
			spawned++;
			return;
		}
	}

	/** 该格能不能站（地板或水面，排除虚空/深渊/墙） */
	private static boolean walkable( int cell ){
		if (cell < 0 || cell >= Dungeon.level.length()) return false;
		if (Dungeon.level.map[cell] == Terrain.CHASM || Dungeon.level.map[cell] == Terrain.EMPTY) return false;
		return Dungeon.level.passable[cell] || Dungeon.level.water[cell];
	}

	private static int cellOf( float x, float y ){
		int w = Dungeon.level.width(), h = Dungeon.level.height();
		int cx = (int)( x / SIZE ), cy = (int)( y / SIZE );
		if (cx < 0 || cy < 0 || cx >= w || cy >= h) return -1;
		return cx + cy * w;
	}

	/** 四邻里有水就返回那格，否则 -1 */
	private static int nearWater( int cell ){
		int w = Dungeon.level.width();
		int[] d = { cell - w, cell + w, cell - 1, cell + 1 };
		for (int c : d){
			if (c < 0 || c >= Dungeon.level.map.length) continue;
			if (Dungeon.level.map[c] == Terrain.WATER) return c;
		}
		return -1;
	}

	/**
	 * 按地图走的 Ground：只在出生点附近 2 格内的可走格之间移动，
	 * 越界/墙体/虚空一律不选，避免小动物穿墙跑到黑边去。
	 */
	private static final class MapGround implements DungeonCritterSprite.Ground {
		@Override
		public float[] wander( float x, float y ){
			int start = cellOf( x, y );
			if (start < 0) return null;
			int w = Dungeon.level.width();
			int cx = start % w, cy = start / w;

			for (int tries = 0; tries < 12; tries++){
				int nx = cx + RNG.nextInt( 5 ) - 2;
				int ny = cy + RNG.nextInt( 5 ) - 2;
				int c = nx + ny * w;
				if (nx < 0 || ny < 0 || !walkable( c )) continue;
				float[] pts = new float[2];
				pts[0] = nx * SIZE + SIZE / 2f;
				pts[1] = ny * SIZE + SIZE / 2f;
				return pts;
			}
			return null;
		}

		@Override
		public float[] flee( float x, float y, float sx, float sy ){
			int start = cellOf( x, y );
			if (start < 0) return null;
			int w = Dungeon.level.width();
			int cx = start % w, cy = start / w;

			// 往远离威胁的方向找一个可走格（最多 4 格远）
			float dx = x - sx, dy = y - sy;
			float len = (float)Math.sqrt( dx * dx + dy * dy );
			if (len < 0.01f){ dx = 0; dy = 1; len = 1; }
			dx /= len; dy /= len;

			for (int step = 4; step >= 2; step--){
				int nx = cx + Math.round( dx * step );
				int ny = cy + Math.round( dy * step );
				int c = nx + ny * w;
				if (nx < 0 || ny < 0 || !walkable( c )) continue;
				float[] pts = new float[2];
				pts[0] = nx * SIZE + SIZE / 2f;
				pts[1] = ny * SIZE + SIZE / 2f;
				return pts;
			}
			return null;
		}
	}
}
