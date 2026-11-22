package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

/** Warped 的"声源"系统没有搬，这里只提供 WaterFalls 需要的判定。 */
public final class Source {
	private Source(){}

	/** 该格的水是否"倾泻"（三分之一概率，按格稳定） */
	public static boolean pours( int pos ){
		return pos % 3 == 0;
	}
}
