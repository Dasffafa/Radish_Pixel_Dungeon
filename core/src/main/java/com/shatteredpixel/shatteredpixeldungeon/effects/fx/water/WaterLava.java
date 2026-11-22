package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/** Warped 的岩浆活体没有搬：保留 WaterAmbience 需要的接口，全部空实现。 */
public final class WaterLava {

	public WaterLava(){}

	public void banks( Level level, int x0, int y0, int x1, int y1 ){}

	public void dark(){}

	public void update( Level level, int[] cells, int n, float dt ){}

	public int lit(){ return 0; }

	public static void reset(){}
}
