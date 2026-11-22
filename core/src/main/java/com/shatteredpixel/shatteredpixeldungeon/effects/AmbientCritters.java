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
 * 环境小动物：直接使用 Warped 的 DungeonCritterSprite（青蛙/鼠妇/蜘蛛/蛾子/飞虫/蜗牛/长脚虫）。
 * 生成节奏：视野内每 6~11 秒一只，最多 5 只，死亡/离开后自动补。
 */
public final class AmbientCritters {

	private AmbientCritters(){}

	private static final float SIZE = DungeonTilemap.SIZE;
	private static final int MAX = 5;
	private static final float MIN_GAP = 6f, MAX_GAP = 11f;

	private static final Random RNG = new Random();

	private static Group host;
	private static final List<DungeonCritterSprite> live = new ArrayList<>();
	private static float spawnIn = 4f;
	public static int spawned;
	public static int lastCell = -1;

	public static void init( Group effects ){
		host = effects;
		live.clear();
		spawned = 0;
		lastCell = -1;
		spawnIn = 4f;
	}

	/** 每帧：清掉已经离开的，按节奏补新的 */
	public static void update(){
		if (host == null || Dungeon.level == null || Dungeon.hero == null) return;

		for (int i = live.size() - 1; i >= 0; i--){
			if (!live.get( i ).alive) live.remove( i );
		}

		if (Emitter.freezeEmitters) return;

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
			if (Dungeon.level.map[cell] == Terrain.CHASM || Dungeon.level.map[cell] == Terrain.EMPTY) continue;

			float ax = x * SIZE + SIZE / 2f;
			float ay = y * SIZE + SIZE / 2f;

			DungeonCritterSprite s = null;

			boolean water = Dungeon.level.map[cell] == Terrain.WATER;
			int waterCell = nearWater( cell );
			boolean passable = Dungeon.level.passable[cell];

			if (waterCell != -1 && !water){
				// 岸边：青蛙在岸与水之间蹦跶
				int wx = waterCell % w, wy = waterCell / w;
				s = new DungeonCritterSprite.Frog( ax, ay, wx * SIZE + SIZE / 2f, wy * SIZE + SIZE / 2f, true );
			} else if (water){
				// 水里不生成陆生小动物，换成水面的飞虫
				s = new DungeonCritterSprite.Fly( ax, ay );
			} else if (passable){
				int roll = RNG.nextInt( 100 );
				if (roll < 34){
					s = new DungeonCritterSprite.Scuttler( DungeonCritterSprite.Scuttler.Look.ROACH, ax, ay, new SimpleGround() );
				} else if (roll < 58){
					s = new DungeonCritterSprite.Strider( ax, ay );
				} else if (roll < 76){
					s = new DungeonCritterSprite.Fly( ax, ay );
				} else if (roll < 88){
					s = new DungeonCritterSprite.Moth( ax, ay, 0f, 0xE8D9A0, false, true );
				} else if (roll < 96){
					s = new DungeonCritterSprite.Snail( ax, ay );
				} else {
					s = new DungeonCritterSprite.Spider( ax, ay, 0f, 2f );
				}
			}

			if (s == null) continue;

			s.ax = ax;
			s.ay = ay;
			host.add( s );
			live.add( s );
			lastCell = cell;
			spawned++;
			return;
		}
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

	/** 在出生点附近晃悠 */
	private static final class SimpleGround implements DungeonCritterSprite.Ground {
		@Override
		public float[] wander( float x, float y ){
			float[] pts = new float[4];
			for (int i = 0; i < 2; i++){
				float px = x + (RNG.nextFloat() * 2f - 1f) * SIZE * 1.5f;
				float py = y + (RNG.nextFloat() * 2f - 1f) * SIZE * 1.5f;
				pts[i * 2] = px;
				pts[i * 2 + 1] = py;
			}
			return pts;
		}

		@Override
		public float[] flee( float x, float y, float sx, float sy ){
			float dx = x - sx, dy = y - sy;
			float len = (float)Math.sqrt( dx * dx + dy * dy );
			if (len < 0.01f){ dx = 0; dy = 1; len = 1; }
			float[] pts = new float[4];
			pts[0] = x + dx / len * SIZE * 3f;
			pts[1] = y + dy / len * SIZE * 3f;
			pts[2] = x + dx / len * SIZE * 6f;
			pts[3] = y + dy / len * SIZE * 6f;
			return pts;
		}
	}
}
