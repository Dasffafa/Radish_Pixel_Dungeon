package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.DoorGate;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.GateType;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/** 测试工具：选中一扇门并循环切换其逻辑类型。 */
public class GateTester extends TestItem {

	private static final String AC_CYCLE = "CYCLE";

	{
		image = ItemSpriteSheet.RECLAIM_TRAP;
		defaultAction = AC_CYCLE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_CYCLE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_CYCLE.equals(action)) {
			return;
		}

		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null) {
					return;
				}
				apply(cell);
			}

			@Override
			public String prompt() {
				return Messages.get(GateTester.class, "prompt");
			}
		});
	}

	private void apply(int cell) {
		GateType[] values = GateType.values();

		LevelTransition t = Dungeon.level.getTransition(cell);
		if (t != null) {
			t.gate = values[(t.gate.ordinal() + 1) % values.length];
			t.gateOpened = false;
			if (t.gate == GateType.CONDITIONAL) {
				t.gateEvent = "HeroMoveEvent";
			}
			if (t.gate == GateType.CYCLE_ABC) {
				int slot = 0;
				for (LevelTransition other : Dungeon.level.transitions) {
					if (other != t && other.gate == GateType.CYCLE_ABC && "test".equals(other.cycleChain)) {
						slot = (other.cycleSlot + 1) % 3;
					}
				}
				t.cycleChain = "test";
				t.cycleSlot = slot;

				//立刻补上光圈外观，不用重新进出关卡
				boolean hasRing = false;
				for (com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap ct : Dungeon.level.customTiles) {
					if (ct instanceof com.shatteredpixel.shatteredpixeldungeon.levels.features.CycleStairVisual
							&& ct.tileX == t.cell() % Dungeon.level.width()
							&& ct.tileY == t.cell() / Dungeon.level.width()) {
						hasRing = true;
						break;
					}
				}
				if (!hasRing) {
					com.shatteredpixel.shatteredpixeldungeon.levels.features.CycleStairVisual ring =
							new com.shatteredpixel.shatteredpixeldungeon.levels.features.CycleStairVisual();
					ring.pos(t.cell(), Dungeon.level);
					Dungeon.level.customTiles.add(ring);
					GameScene.updateMap();
				}
			}
			GLog.i(Messages.get(this, "switched",
					Messages.get(GateType.class, t.gate.name().toLowerCase() + "_short")));
			return;
		}

		int terrain = Dungeon.level.map[cell];
		if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR) {
			DoorGate g = Dungeon.level.doorGates.get(cell);
			if (g == null) {
				g = new DoorGate(cell);
				Dungeon.level.doorGates.put(cell, g);
			}
			GateType next = values[(g.gate.ordinal() + 1) % values.length];
			//循环门是独立生成的楼梯，不能加在普通门上
			if (next == GateType.CYCLE_ABC) {
				next = values[(next.ordinal() + 1) % values.length];
			}
			g.gate = next;
			g.opened = false;
			if (g.gate == GateType.CONDITIONAL) {
				g.event = "HeroMoveEvent";
			}
			if (g.gate == GateType.ONE_WAY) {
				//默认允许从英雄当前所在的一侧进入
				int w = Dungeon.level.width();
				g.fromDx = (Dungeon.hero.pos % w) - (cell % w);
				g.fromDy = (Dungeon.hero.pos / w) - (cell / w);
			}
			GLog.i(Messages.get(this, "switched",
					Messages.get(GateType.class, g.gate.name().toLowerCase() + "_short")));
			return;
		}

		GLog.w(Messages.get(this, "no_gate"));
	}
}