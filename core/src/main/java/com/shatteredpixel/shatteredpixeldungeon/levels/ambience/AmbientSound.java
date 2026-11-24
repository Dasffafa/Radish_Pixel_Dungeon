package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

import java.util.ArrayList;

/** 环境音：sounds/amb_*.mp3（来自 Warped 的合成音库）。天气音没保留。 */
public enum AmbientSound {
	SPLASH  ( "splash",  2, 1.7f, 0.32f ),
	LAP     ( "lap",     3, 1.7f, 0.28f ),
	FROG    ( "frog",    2, 1.5f, 0.34f ),
	RAT     ( "rat",     2, 1.7f, 0.23f ),
	FLY     ( "fly",     1, 2.2f, 0.28f ),
	SKITTER ( "skitter", 2, 1.5f, 1.00f ),
	LAVA    ( "lava",    2, 4.3f, 0.32f ),
	DRIP    ( "drip",    3, 1.2f, 0.30f ),
	DRIP_METAL ( "drip_metal", 2, 1.0f, 0.28f ),
	DRIP_LEAF  ( "drip_leaf",  2, 0.7f, 0.30f ),
	TRICKLE    ( "trickle",   2, 2.2f, 0.34f ),
	POUR       ( "pour",      1, 3.0f, 0.36f ),
	GURGLE     ( "gurgle",    2, 2.0f, 0.30f ),
	CRACKLE    ( "crackle",   1, 2.6f, 0.34f ),
	CREAK      ( "creak",     2, 1.4f, 0.30f ),
	MOAN       ( "moan",      2, 2.6f, 0.34f ),
	WHISPER    ( "whisper",   2, 2.4f, 0.28f ),
	RUMBLE     ( "rumble",    1, 3.5f, 0.30f ),
	PEBBLES    ( "pebbles",   2, 1.2f, 0.26f ),
	BAT        ( "bat",       2, 1.2f, 0.26f ),
	ANVIL      ( "anvil",     1, 1.2f, 0.30f ),
	CHIME      ( "chime",     1, 2.0f, 0.24f ),
	COO        ( "coo",       1, 1.6f, 0.22f ),
	WIND       ( "wind",      2, 4.0f, 0.22f ),
	ICE        ( "ice",       2, 2.0f, 0.26f ),
	//weather: rain, snow, thunder and the wind that carries them (files already in assets/sounds)
	RAIN_LIGHT   ( "rain_light",   3, 5.62f, 0.08f ),
	RAIN         ( "rain",         2, 5.62f, 0.10f ),
	RAIN_HEAVY   ( "rain_heavy",   2, 5.67f, 0.14f ),
	RAIN_WATER   ( "rain_water",   2, 5.67f, 0.06f ),
	RAIN_LEAVES  ( "rain_leaves",  2, 5.59f, 0.06f ),
	RAIN_ROOF    ( "rain_roof",    2, 5.77f, 0.09f ),
	THUNDER_FAR  ( "thunder_far",  2, 6.48f, 0.17f ),
	THUNDER_NEAR ( "thunder_near", 2, 6.48f, 0.50f ),
	GALE         ( "gale",         2, 5.88f, 0.10f ),
	GUST         ( "gust",         2, 5.88f, 0.27f ),
	HAIL         ( "hail",         1, 5.67f, 0.08f ),
	SLEET        ( "sleet",        1, 5.62f, 0.09f ),
	SNOW         ( "snow",         1, 5.77f, 0.03f ),
	BLIZZARD     ( "blizzard",     2, 5.77f, 0.15f ),
	SANDSTORM    ( "sandstorm",    1, 5.67f, 0.11f );

	/** 每一次录音的文件 */
	public final String[] takes;
	/** 最长一段的时长（秒）：一次播放占住一个声道的时长 */
	public final float length;
	/** 混音增益 */
	public final float gain;

	AmbientSound( String name, int takes, float length, float gain ){
		this.takes = new String[takes];
		for (int k = 0; k < takes; k++){
			this.takes[k] = takes == 1 ? "sounds/amb_" + name + ".mp3"
					: "sounds/amb_" + name + "_" + (k + 1) + ".mp3";
		}
		this.length = length;
		this.gain = gain;
	}

	public static String[] assets(){
		ArrayList<String> all = new ArrayList<>();
		for (AmbientSound s : values()) for (String t : s.takes) all.add( t );
		return all.toArray( new String[0] );
	}
}
