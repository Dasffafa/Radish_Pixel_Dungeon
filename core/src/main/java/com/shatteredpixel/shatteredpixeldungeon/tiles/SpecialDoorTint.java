package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;

final class SpecialDoorTint {

	private SpecialDoorTint() {
	}

	static int color(SpecialDoor door) {
		if (door == null) return -1;
		if (door.sealed) return 0xBF2E2E;
		if (door.locked()) {
			if ("explosion".equals(door.triggerId)) return 0xFF7A1F;
			if (door.triggerId.startsWith("signal:combat:")) return 0xFF3D3D;
			return 0xAD6BFF;
		}
		if (door.oneWay) return 0x40BFFF;
		if (door.oneShot) return 0xFFD140;
		if (door.toggle || !door.triggerId.isEmpty()) return 0x59FF8C;
		return -1;
	}
}
