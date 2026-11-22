package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

import com.watabou.noosa.Game;

/** 同时发声数限制：最多 4 个，超出就丢掉新的。 */
public final class AmbientSounds {

	private static final int MAX = 4;
	private static final float[] ends = new float[MAX];
	private static int next;

	private AmbientSounds(){}

	public static boolean full(){
		float now = Game.timeTotal;
		int busy = 0;
		for (float e : ends) if (e > now) busy++;
		return busy >= MAX;
	}

	/** 新场景：清空占用的声道（换层后时间基数可能重置，不清会一直算满） */
	public static void reset(){
		for (int i = 0; i < MAX; i++) ends[i] = 0f;
		next = 0;
	}

	public static void played( float seconds ){
		ends[next] = Game.timeTotal + seconds;
		next = (next + 1) % MAX;
	}
}
