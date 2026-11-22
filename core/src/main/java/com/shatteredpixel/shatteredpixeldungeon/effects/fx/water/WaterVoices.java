package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.ambience.AmbientPlayer;
import com.shatteredpixel.shatteredpixeldungeon.levels.ambience.AmbientSound;
import com.shatteredpixel.shatteredpixeldungeon.levels.ambience.AmbientSounds;
import com.watabou.noosa.Game;

/** 水面拍岸声：视野里有水时，每 2.5~6 秒响一次拍岸。 */
public final class WaterVoices {

	private WaterVoices(){}

	private static float nextIn = 3f;
	public static int played;

	public static void reset(){
		nextIn = 3f;
	}

	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null || Dungeon.hero == null) return;
		Level level = Dungeon.level;
		nextIn -= dt;
		if (nextIn > 0f) return;
		nextIn = 2.5f + FxRandom.Float( 3.5f );
		if (WaterLive.waterInView() <= 0) return;
		if (AmbientSounds.full()) return;
		AmbientSounds.played( AmbientSound.LAP.length );
		AmbientPlayer.play( AmbientSound.LAP, 0.5f * AmbientSound.LAP.gain,
				1f + FxRandom.Float( -0.1f, 0.1f ), FxRandom.Float( -0.6f, 0.6f ) );
		played++;
	}
}
