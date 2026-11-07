package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.events.GameEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 条件门的开关逻辑：门在创建时记录一个事件类名，
 * 当该事件被 EventManager 派发时，对应的条件门就会打开。
 */
public class GateEvents {

	public static void onEvent(GameEvent event) {
		if (event == null || Dungeon.level == null || Dungeon.level.transitions == null) {
			return;
		}

		DoorGates.onEvent(event);

		String fullName = event.getClass().getName();
		String simpleName = event.getClass().getSimpleName();

		for (LevelTransition t : Dungeon.level.transitions) {
			if (t.gate != GateType.CONDITIONAL || t.gateOpened) {
				continue;
			}
			if (t.gateEvent == null || t.gateEvent.isEmpty()) {
				continue;
			}
			if (t.gateEvent.equals(fullName) || t.gateEvent.equals(simpleName)) {
				t.gateOpened = true;
				GLog.p(Messages.get(GateEvents.class, "opened"));
			}
		}
	}
}