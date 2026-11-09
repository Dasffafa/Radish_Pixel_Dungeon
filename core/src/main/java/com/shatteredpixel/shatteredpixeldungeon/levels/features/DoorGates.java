package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 普通门（Terrain.DOOR）上的规则判定与状态变更。 */
public class DoorGates {

	private static float lastMessageAt = -100f;

	public static boolean canPass(int cell, Char ch) {
		DoorGate g = Dungeon.level.doorGates.get(cell);
		if (g == null) {
			return true;
		}

		boolean can;
		switch (g.gate) {
			case ONE_WAY:
				can = (ch.pos % Dungeon.level.width()) - (cell % Dungeon.level.width()) == g.fromDx
						&& (ch.pos / Dungeon.level.width()) - (cell / Dungeon.level.width()) == g.fromDy;
				break;
			case SINGLE_USE:
				can = !g.opened;
				break;
			case BOMB_ONLY:
				can = g.opened;
				break;
			case CONDITIONAL:
				can = g.opened;
				break;
			case CYCLE_ABC:
				// A=开 B=关 C=开，每次尝试后切到下一态
				can = g.index != 1;
				break;
			case TWO_WAY:
			default:
				can = true;
		}

		return can;
	}

	public static boolean messageIfBlocked(int cell, Char ch) {
		DoorGate g = Dungeon.level.doorGates.get(cell);
		if (g == null) {
			return false;
		}
		if (canPass(cell, ch)) {
			return false;
		}
		GLog.w(blockedMessage(g));
		return true;
	}

	public static String blockedMessage(DoorGate g) {
		switch (g.gate) {
			case ONE_WAY:
				return Messages.get(DoorGates.class, "blocked_one_way");
			case SINGLE_USE:
				return Messages.get(DoorGates.class, "blocked_single_use");
			case BOMB_ONLY:
				return Messages.get(DoorGates.class, "blocked_bomb");
			case CONDITIONAL:
				return Messages.get(DoorGates.class, "blocked_condition");
			case CYCLE_ABC:
				return Messages.get(DoorGates.class, "blocked_cycle");
			default:
				return Messages.get(DoorGates.class, "blocked_one_way");
		}
	}

	public static void onPass(int cell, Char ch) {
		DoorGate g = Dungeon.level.doorGates.get(cell);
		if (g == null) {
			return;
		}
		switch (g.gate) {
			case SINGLE_USE:
				g.opened = true;
				//一次性门用掉后直接封死成墙
				Dungeon.level.doorGates.remove(cell);
				//按墙处理（不可通行），再叠一张红叉门的贴图
				com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell, Terrain.WALL, Dungeon.level);
				SealedDoorVisual vis =
						new SealedDoorVisual();
				vis.pos(cell, Dungeon.level);
				Dungeon.level.customTiles.add(vis);
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap();
				break;
			case CYCLE_ABC:
				g.index = (g.index + 1) % 3;
				break;
			default:
				break;
		}
	}

	public static void onEvent(com.shatteredpixel.shatteredpixeldungeon.events.GameEvent event) {
		try {
		if (event == null || Dungeon.level == null || Dungeon.level.doorGates == null) {
			return;
		}
		String full = event.getClass().getName();
		String simple = event.getClass().getSimpleName();
		for (DoorGate g : Dungeon.level.doorGates.values()) {
			if (g.gate == GateType.CONDITIONAL && !g.opened && !g.event.isEmpty()
					&& (g.event.equals(full) || g.event.equals(simple))) {
				g.opened = true;
				GLog.p(Messages.get(DoorGates.class, "opened"));
			}
		}
		} catch (Exception e) {
			com.watabou.utils.DeviceCompat.log("DoorGates", "event handling failed: " + e);
		}
	}

	public static void onExplosion(int cell) {
		if (Dungeon.level == null || Dungeon.level.doorGates == null) {
			return;
		}
		DoorGate g = Dungeon.level.doorGates.get(cell);
		if (g != null && g.gate == GateType.BOMB_ONLY && !g.opened) {
			g.opened = true;
			//炸药门不会被炸毁，只是永久保持打开
			com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell, Terrain.OPEN_DOOR, Dungeon.level);
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(cell);
			GLog.w(Messages.get(DoorGates.class, "bomb_opened"));
		}
	}
}