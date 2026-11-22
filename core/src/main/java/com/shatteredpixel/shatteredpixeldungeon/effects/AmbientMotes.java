package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CaveMoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;

import java.util.Random;

/** 洞穴飞尘：洞窟/殿堂里缓缓飘起的亮点。 */
public final class AmbientMotes {

	private AmbientMotes(){}

	private static final Random RNG = new Random();
	private static final float SIZE = DungeonTilemap.SIZE;

	private static float nextIn = 1f;
	public static int spawned;

	public static void init(){
		nextIn = 1f;
		spawned = 0;
	}

	public static void update(){
		Level level = Dungeon.level;
		if (level == null || Dungeon.hero == null) return;
		if (!motesHere( level )) return;

		nextIn -= Game.elapsed;
		if (nextIn > 0f) return;
		nextIn = 0.45f + RNG.nextFloat() * 0.9f;

		FxEmitter air = Fx.air();
		if (air == null) return;

		int w = level.width();
		int heroX = Dungeon.hero.pos % w, heroY = Dungeon.hero.pos / w;
		for (int tries = 0; tries < 10; tries++){
			int x = heroX + RNG.nextInt( 11 ) - 5;
			int y = heroY + RNG.nextInt( 9 ) - 4;
			if (x < 0 || y < 0 || x >= w || y >= level.height()) continue;
			int cell = x + y * w;
			if (!level.heroFOV[cell]) continue;
			if (Dungeon.level.map[cell] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL) continue;

			air.pos( x * SIZE + RNG.nextFloat() * SIZE, y * SIZE + RNG.nextFloat() * SIZE );
			air.burst( CaveMoteParticle.EMBERS, 1 );
			spawned++;
			return;
		}
	}

	private static boolean motesHere( Level level ){
		String n = level.getClass().getSimpleName().toLowerCase();
		return n.contains( "cave" ) || n.contains( "hall" ) || n.contains( "crystal" ) || n.contains( "gnoll" );
	}
}
