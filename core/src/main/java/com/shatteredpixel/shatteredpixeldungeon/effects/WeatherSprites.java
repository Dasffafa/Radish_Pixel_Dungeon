package com.shatteredpixel.shatteredpixeldungeon.effects;

/** 天气精灵没有搬；小动物只问"是否可见"（天气遮挡），这里恒 true。 */
public final class WeatherSprites {
	private WeatherSprites(){}
	public static boolean visible( float x, float y ){ return true; }
}
