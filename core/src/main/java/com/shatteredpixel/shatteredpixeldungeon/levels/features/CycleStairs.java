package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 循环楼梯的判定与传送。 */
public class CycleStairs {

	//传送中不重复触发，否则会 A→B→C→A 连环跳
	private static boolean teleporting = false;

	public static boolean isActive(CycleStairData s) {
		return Dungeon.cycleChains.getOrDefault(s.chain, 0) == s.slot;
	}

	public static int activeSlot(String chain) {
		return Dungeon.cycleChains.getOrDefault(chain, 0);
	}

	public static String slotName(int slot) {
		switch (slot % 3) {
			case 0: return "A";
			case 1: return "B";
			default: return "C";
		}
	}

	public static void onStep(Hero hero, int cell) {
		if (teleporting) {
			return;
		}

		CycleStairData s = Dungeon.level.cycleStairs.get(cell);
		if (s == null) {
			return;
		}

		if (!isActive(s)) {
			GLog.w(Messages.get(CycleStairs.class, "not_active", slotName(activeSlot(s.chain))));
			return;
		}

		if (!s.destBranch.isEmpty() && s.destDepth > 0) {
			Dungeon.cycleChains.put(s.chain, (s.slot + 1) % 3);
			hero.curAction = null;
			hero.sprite.interruptMotion();
			GLog.i(Messages.get(CycleStairs.class, "used", slotName(s.slot), slotName(s.slot + 1)));

			com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode =
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.RETURN;
			com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.returnBranchId = s.destBranch;
			com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.returnDepth = s.destDepth;
			com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.returnPos =
					com.shatteredpixel.shatteredpixeldungeon.levels.CycleTestLevel.ENTRANCE_CELL;
			com.watabou.noosa.Game.switchScene(com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.class);
			return;
		}

		if (s.destCell >= 0 && s.destCell < Dungeon.level.length()) {
			//先推进链条，再传送
			Dungeon.cycleChains.put(s.chain, (s.slot + 1) % 3);

			//打断当前寻路/动作，避免传送后继续沿原路线走
			hero.curAction = null;
			hero.sprite.interruptMotion();

			teleporting = true;
			try {
				hero.pos = s.destCell;
				hero.sprite.place(s.destCell);
				Dungeon.level.occupyCell(hero);
				Dungeon.observe();
				GameScene.updateMap();
			} finally {
				teleporting = false;
			}
			GLog.i(Messages.get(CycleStairs.class, "used", slotName(s.slot), slotName(s.slot + 1)));
		}
	}
}