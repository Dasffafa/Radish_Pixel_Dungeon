package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/** 生成循环楼梯：一组三扇按 A/B/C 排在同一条链上，可同层或跨层。 */
public class CycleStair {

	//同组三扇的 chain 必须相同，slot 取 0/1/2
	public static LevelTransition create(Level level, int cell, String chain, int slot,
	                                     String destBranch, int destDepth, boolean up) {
		LevelTransition t = up
				? LevelTransition.up(level, cell, chain + ":" + slot, destBranch, destDepth)
				: LevelTransition.down(level, cell, chain + ":" + slot, destBranch, destDepth);

		t.gate = GateType.CYCLE_ABC;
		t.cycleChain = chain;
		t.cycleSlot = slot;
		level.transitions.add(t);

		Painter.set(level, cell, up ? Terrain.ENTRANCE : Terrain.EXIT);
		addRing(level, cell);
		return t;
	}

	public static void addRing(Level level, int cell) {
		for (com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap ct : level.customTiles) {
			if (ct instanceof CycleStairVisual
					&& ct.tileX == cell % level.width()
					&& ct.tileY == cell / level.width()) {
				return;
			}
		}
		CycleStairVisual vis = new CycleStairVisual();
		vis.pos(cell, level);
		level.customTiles.add(vis);
		if (Dungeon.level == level) {
			GameScene.updateMap();
		}
	}
}