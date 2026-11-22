package com.shatteredpixel.shatteredpixeldungeon.levels.ambience;

/** Warped 的"声源"系统没有搬，这里只提供 WaterFalls 需要的判定。 */
public final class Source {
	private Source(){}

	/** 该格的水是否在拍岸（岸边、靠干燥地面）：简单判定，四邻有干地就算 */
	public static boolean lapping( int[] map, int w, int cell ){
		int[] d = { cell - w, cell + w, cell - 1, cell + 1 };
		for (int c : d){
			if (c < 0 || c >= map.length) return true;
			if (map[c] != 29) return true; // 29 = Terrain.WATER
		}
		return false;
	}

	/** 该格的水是否"倾泻"（三分之一概率，按格稳定） */
	public static boolean pours( int pos ){
		return pos % 3 == 0;
	}
}
