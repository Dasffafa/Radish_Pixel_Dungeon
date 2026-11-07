package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 普通门的规则判定：能否通过、通过后的状态变更、事件与爆炸触发的开门。
 */
public class DoorGates {

	private static float lastMessageAt = -100f;

	/** 这扇门当前能否被该角色通过 */
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

	/** 玩家主动点击被挡住的门口时提示一次；返回 true 表示被挡住 */
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

	/** 通过之后的状态变更 */
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
				com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell, Terrain.WALL, Dungeon.level);
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(cell);
				break;
			case CYCLE_ABC:
				g.index = (g.index + 1) % 3;
				break;
			default:
				break;
		}
	}

	/** 事件触发开门 */
	public static void onEvent(com.shatteredpixel.shatteredpixeldungeon.events.GameEvent event) {
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
	}

	/** 爆炸炸开炸药门 */
	public static void onExplosion(int cell) {
		if (Dungeon.level == null || Dungeon.level.doorGates == null) {
			return;
		}
		DoorGate g = Dungeon.level.doorGates.get(cell);
		if (g != null && g.gate == GateType.BOMB_ONLY && !g.opened) {
			g.opened = true;
			GLog.w(Messages.get(DoorGates.class, "bomb_opened"));
		}
	}
}