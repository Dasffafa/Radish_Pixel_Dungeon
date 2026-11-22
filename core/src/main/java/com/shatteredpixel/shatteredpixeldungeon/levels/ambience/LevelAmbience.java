package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.noosa.Game;

import java.util.Random;

/**
 * 关卡环境音床：按关卡类型每隔几秒放一个低音量的环境音
 * （下水道滴水/拍岸、监狱金属滴落、洞窟滴水、城市金属、殿堂岩浆…）。
 */
public final class LevelAmbience {

	private LevelAmbience(){}

	private static final Random RNG = new Random();

	/** 一条音床：音、最短/最长间隔、音量 */
	private static final class Bed {
		final AmbientSound sound;
		final float minGap, maxGap, volume;
		Bed( AmbientSound s, float min, float max, float vol ){ sound = s; minGap = min; maxGap = max; volume = vol; }
	}

	private static final Bed[] SEWER = {
			new Bed( AmbientSound.DRIP, 4f, 9f, 0.35f ),
			new Bed( AmbientSound.LAP, 5f, 11f, 0.40f ),
			new Bed( AmbientSound.RAT, 8f, 16f, 0.22f ),
			new Bed( AmbientSound.FLY, 9f, 18f, 0.18f ),
			new Bed( AmbientSound.TRICKLE, 6f, 13f, 0.30f ),
	};
	private static final Bed[] PRISON = {
			new Bed( AmbientSound.DRIP_METAL, 4f, 9f, 0.32f ),
			new Bed( AmbientSound.DRIP, 5f, 10f, 0.30f ),
			new Bed( AmbientSound.SKITTER, 7f, 14f, 0.20f ),
			new Bed( AmbientSound.RAT, 9f, 18f, 0.20f ),
	};
	private static final Bed[] CAVES = {
			new Bed( AmbientSound.DRIP, 3f, 7f, 0.34f ),
			new Bed( AmbientSound.SKITTER, 8f, 16f, 0.18f ),
	};
	private static final Bed[] CITY = {
			new Bed( AmbientSound.DRIP_METAL, 4f, 9f, 0.30f ),
			new Bed( AmbientSound.RAT, 7f, 14f, 0.22f ),
	};
	private static final Bed[] HALLS = {
			new Bed( AmbientSound.LAVA, 3f, 7f, 0.40f ),
			new Bed( AmbientSound.DRIP, 6f, 12f, 0.22f ),
	};
	private static final Bed[] FROZEN = {
			new Bed( AmbientSound.DRIP_LEAF, 4f, 9f, 0.28f ),
			new Bed( AmbientSound.DRIP, 6f, 12f, 0.24f ),
	};
	private static final Bed[] GENERIC = {
			new Bed( AmbientSound.DRIP, 5f, 12f, 0.28f ),
	};

	private static float nextIn = 4f;
	public static int played;
	public static String lastLevel = "";

	public static void sceneCreated(){
		nextIn = 2f + RNG.nextFloat() * 4f;
	}

	public static void update(){
		Level level = Dungeon.level;
		if (level == null || Dungeon.hero == null) return;
		if (!AmbientPlayer.audible()) return;

		nextIn -= Game.elapsed;
		if (nextIn > 0f) return;

		Bed[] beds = bedsFor( level );
		Bed b = beds[RNG.nextInt( beds.length )];
		nextIn = b.minGap + RNG.nextFloat() * (b.maxGap - b.minGap);

		if (AmbientSounds.full()) return;
		AmbientSounds.played( b.sound.length );
		float pan = RNG.nextFloat() * 1.4f - 0.7f;
		AmbientPlayer.play( b.sound, b.volume, 1f + RNG.nextFloat() * 0.12f - 0.06f, pan );
		played++;
	}

	private static Bed[] bedsFor( Level level ){
		String n = level.getClass().getSimpleName().toLowerCase();
		lastLevel = n;
		if (n.contains( "sewer" )) return SEWER;
		if (n.contains( "prison" )) return PRISON;
		if (n.contains( "cave" ) || n.contains( "crystal" ) || n.contains( "gnoll" )) return CAVES;
		if (n.contains( "city" )) return CITY;
		if (n.contains( "hall" ) || n.contains( "level" )) return HALLS;
		if (n.contains( "frozen" ) || n.contains( "ice" )) return FROZEN;
		return GENERIC;
	}
}
