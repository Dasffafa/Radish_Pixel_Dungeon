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
	public static int trickles;
	public static String lastSound = "";

	public static void reset(){
		nextIn = 3f;
	}

	public static void update( float dt ){
		if (dt <= 0f || Dungeon.level == null || Dungeon.hero == null) return;
		Level level = Dungeon.level;
		nextIn -= dt;
		if (nextIn > 0f) return;
		nextIn = 2.0f + FxRandom.Float( 3.0f );
		if (WaterLive.waterInView() <= 0) return;
		if (AmbientSounds.full()) return;
		AmbientSound sound;
		float vol;
		float r = FxRandom.Float( 1f );
		if (r < 0.45f){          // 拍岸
			sound = AmbientSound.LAP; vol = 0.50f;
		} else if (r < 0.80f){   // 管道细流
			sound = AmbientSound.TRICKLE; vol = 0.34f;
			trickles++;
		} else if (r < 0.94f){   // 排水口咕噜
			sound = AmbientSound.GURGLE; vol = 0.30f;
		} else {                 // 倒水/落水
			sound = AmbientSound.POUR; vol = 0.26f;
		}
		lastSound = sound.name();
		AmbientSounds.played( sound.length );
		AmbientPlayer.play( sound, vol * sound.gain,
				1f + FxRandom.Float( -0.1f, 0.1f ), FxRandom.Float( -0.6f, 0.6f ) );
		played++;
	}
}
