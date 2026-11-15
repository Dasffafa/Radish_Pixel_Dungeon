package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.events.GateSignalEvent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.watabou.noosa.Camera;

import rpdtest.H;
import rpdtest.Scenario;

public class GateVisuals implements Scenario {

	@Override
	public String describe() {
		return "特殊门锁头、连续缩放、解锁切换及开门通行";
	}

	@Override
	public void run(H h) {
		Arena.start(h);
		int[] cells = {Arena.c(7, 8), Arena.c(9, 8), Arena.c(11, 8), Arena.c(13, 8)};
		for (int cell : cells) h.setTerrain(cell, Terrain.DOOR);
		h.setTerrain(Arena.c(7, 7), Terrain.WALL);
		h.setTerrain(Arena.c(9, 7), Terrain.WALL);
		h.setTerrain(Arena.c(12, 9), Terrain.WATER);
		h.world("放置缩放参照物品", () -> Dungeon.level.drop(new Gold(), Arena.c(12, 10)));
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
		float originalZoom = h.get(() -> Camera.main.zoom);
		for (float factor : new float[]{1.5f, 0.75f, 1f}) {
			h.world("缩放地图 " + factor, () -> Camera.main.zoom(originalZoom * factor));
			h.frames(5);
			h.screenshot("zoom-" + factor);
		}
		h.world("显示条件门的检查图标", () -> GameScene.show(new WndInfoCell(cells[3])));
		h.frames(5);
		h.screenshot("door-inspection");
		h.closeWindows();
		h.world("关闭检查窗口并触发信号解锁", () -> {
			SpecialDoors.onEvent(Dungeon.level, new GateSignalEvent("visual", -1));
		});
		h.check(h.get(() -> !SpecialDoors.isBlocking(Dungeon.level, cells[3])), "信号解锁后门可通行");
		h.frames(5);
		h.screenshot("unlocked-door-colour");
		h.walkTo(cells[3]);
		h.check(h.heroPos() == cells[3], "解锁后的条件门可以进入");
		h.check(h.get(() -> Dungeon.level.map[cells[3]] == Terrain.OPEN_DOOR), "进入条件门后显示打开的门");
		h.frames(5);
		h.screenshot("conditional-door-open");
		h.walkTo(cells[0] + 21);
		h.walkTo(cells[0]);
		h.check(h.heroPos() == cells[0], "单向门从允许侧可以进入");
		h.check(h.get(() -> Dungeon.level.map[cells[0]] == Terrain.OPEN_DOOR), "手动开启单向门后显示打开的门");
		h.frames(5);
		h.screenshot("one-way-door-open");
		int normal = Arena.c(11, 10);
		h.setTerrain(normal, Terrain.DOOR);
		h.walkTo(normal);
		h.check(h.heroPos() == normal, "普通门可以正常推开并进入");
		h.check(h.get(() -> SpecialDoors.get(Dungeon.level, normal) == null), "普通门没有特殊门规则");
	}
}
