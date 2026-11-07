package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;

/**
 * 挂在普通门（Terrain.DOOR）上的逻辑门规则。
 */
public class DoorGate implements Bundlable {

	public int cell = -1;
	public GateType gate = GateType.TWO_WAY;
	public boolean opened = false;
	public String event = "";

	/** 循环门当前状态：0=A(开) 1=B(关) 2=C(开) */
	public int index = 0;

	/** 单向门允许进入的一侧（相对门格的偏移） */
	public int fromDx = 0;
	public int fromDy = 0;

	public DoorGate() {
	}

	public DoorGate(int cell) {
		this.cell = cell;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put("cell", cell);
		bundle.put("gate", gate.name());
		bundle.put("opened", opened);
		bundle.put("event", event);
		bundle.put("index", index);
		bundle.put("from_dx", fromDx);
		bundle.put("from_dy", fromDy);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		cell = bundle.getInt("cell");
		String gateName = bundle.getString("gate");
		gate = GateType.TWO_WAY;
		if (gateName != null) {
			try {
				gate = GateType.valueOf(gateName);
			} catch (IllegalArgumentException ignored) {
				gate = GateType.TWO_WAY;
			}
		}
		opened = bundle.getBoolean("opened");
		event = bundle.getString("event");
		if (event == null) event = "";
		index = bundle.getInt("index");
		fromDx = bundle.getInt("from_dx");
		fromDy = bundle.getInt("from_dy");
	}
}