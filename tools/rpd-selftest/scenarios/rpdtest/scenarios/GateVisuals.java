package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.events.GateSignalEvent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;

import rpdtest.H;
import rpdtest.Scenario;

public class GateVisuals implements Scenario {

	@Override
	public String describe() {
		return "特殊门灰度着色、解锁切换及普通门通行";
	}

	@Override
	public void run(H h) {
		Arena.start(h);
		int[] cells = {Arena.c(7, 8), Arena.c(9, 8), Arena.c(11, 8), Arena.c(13, 8)};
		for (int cell : cells) h.setTerrain(cell, Terrain.DOOR);
		h.world("设置单向、一次性、爆炸和信号门", () -> {
			SpecialDoors.add(Dungeon.level, cells[0]).oneWayFromCells(Dungeon.level, cells[0] + 21);
			SpecialDoors.add(Dungeon.level, cells[1]).oneShot();
			SpecialDoors.add(Dungeon.level, cells[2]).locked("explosion");
			SpecialDoors.add(Dungeon.level, cells[3]).locked("signal:visual");
			SpecialDoors.applyLevelFlags(Dungeon.level);
			GameScene.updateMap();
		});
		h.frames(5);
		h.screenshot("locked-door-colours");
		h.world("显示着色门的检查图标", () -> GameScene.show(new WndInfoCell(cells[3])));
		h.frames(5);
		h.screenshot("door-inspection");
		h.closeWindows();
		h.world("关闭检查窗口并触发信号解锁", () -> {
			SpecialDoors.onEvent(Dungeon.level, new GateSignalEvent("visual", -1));
		});
		h.check(h.get(() -> !SpecialDoors.isBlocking(Dungeon.level, cells[3])), "信号解锁后门可通行");
		h.frames(5);
		h.screenshot("unlocked-door-colour");
		int normal = Arena.c(11, 10);
		h.setTerrain(normal, Terrain.DOOR);
		h.walkTo(normal);
		h.check(h.heroPos() == normal, "普通门可以正常推开并进入");
		h.check(h.get(() -> SpecialDoors.get(Dungeon.level, normal) == null), "普通门没有特殊门规则");
	}
}
