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
	DRIP    ( "drip",    3, 1.2f, 0.30f );

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
