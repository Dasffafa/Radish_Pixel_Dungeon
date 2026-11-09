package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.events.GameEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 条件门：记录事件类名，事件派发时自动开门。 */
public class GateEvents {

	public static void onEvent(GameEvent event) {
		try {
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
		} catch (Exception e) {
			//事件回调里出问题不能把游戏带崩
			com.watabou.utils.DeviceCompat.log("GateEvents", "event handling failed: " + e);
		}
	}
}