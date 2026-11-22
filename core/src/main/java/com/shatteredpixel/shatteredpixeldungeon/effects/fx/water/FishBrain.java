package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;

/** 鱼群 AI 没有搬；这里只提供小动物精灵需要的两个只读值。 */
public final class FishBrain {
	private FishBrain(){}

	public static int shade( Element.Liquid l ){ return 0xFF000000; }

	public static float[] disturbances(){ return EMPTY; }

	public static int disturbed(){ return 0; }

	public static float dist( float x0, float y0, float x1, float y1 ){
		float dx = x1 - x0, dy = y1 - y0;
		return (float)Math.sqrt( dx * dx + dy * dy );
	}

	private static final float[] EMPTY = new float[0];
}
