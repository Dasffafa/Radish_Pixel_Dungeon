package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * 循环门楼梯的创建工具。
 *
 * 循环门是一组独立生成的楼梯（不是普通门），三扇一组按 A/B/C 排在同一条链上，
 * 只有当前轮到的那一扇可以进入，进入后传送到它自己的目的地（可同层、可跨层）。
 */
public class CycleStair {

	/**
	 * 在指定格子上生成一扇循环楼梯。
	 *
	 * @param level      所在关卡
	 * @param cell       楼梯格子
	 * @param chain      链 id，同一组三扇必须相同
	 * @param slot       0=A 1=B 2=C
	 * @param destBranch 目的地分支（同层传本分支 id）
	 * @param destDepth  目的地层数（同层传本层）
	 * @param up         true=向上（入口外观），false=向下（出口外观）
	 */
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

	/** 补上光圈外观 */
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