package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.CycleStairVisual;


import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * 主线第 1 层不应再生成循环门（通往测试支线的楼梯）：
 * testPortalCell 保持 -1，场景里没有 CycleStairVisual。
 */
public class H3_NoCyclePortalInMainline implements Scenario {

	@Override
	public String describe() {
		return "主线第 1 层不再生成循环门";
	}

	@Override
	public void run( H h ) {
		h.newGame( HeroClasses.WARRIOR, "1" );
		h.frames( 5 );

		final int[] m = h.get( () -> {
			Level level = Dungeon.level;
			int visuals = 0;
			for (Object t : (java.util.Collection<?>) Reflect.get( level, "customTiles" )) {
				if (t instanceof CycleStairVisual) {
					visuals++;
				}
			}
			return new int[]{ level.testPortalCell, visuals };
		} );

		h.note( "testPortalCell=" + m[0] + " cycleStairVisuals=" + m[1] + " depth=" + h.depth() );
		h.require( h.depth() == 1, "英雄在主线第 1 层" );
		h.check( m[0] == -1, "主线第 1 层不应有循环门目标格（testPortalCell=" + m[0] + "）" );
		h.check( m[1] == 0, "主线第 1 层不应有循环门贴图（CycleStairVisual×" + m[1] + "）" );
	}
}
