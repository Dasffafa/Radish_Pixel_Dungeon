package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;
import com.watabou.noosa.Image;

final class SpecialDoorLock extends Image {

	private final Image locked = new Image("interfaces/special_door_locked.png");

	SpecialDoorLock() {
		super("interfaces/special_door_lock.png");
		width = 10;
		height = 10;
		updateVertices();
		locked.scale.set(1f, 1f);
	}

	void draw(SpecialDoor door) {
		//门打开后锁头自然消失，不再渲染
		if (door == null || Dungeon.level.map[door.cell] == Terrain.OPEN_DOOR) {
			return;
		}
		int color = SpecialDoorTint.color(door);
		if (color == -1) return;
		boolean blocked = door.sealed || door.locked() || (door.toggle && !door.appeared);
		if (door.oneWay && Dungeon.level.map[door.cell] != Terrain.OPEN_DOOR) {
			blocked |= !SpecialDoors.allowsOpen(Dungeon.level, door.cell, Dungeon.hero.pos);
		}
		//左右走向的门（两侧是墙）锁头上移 1px，上下走向的门下移 3px
		boolean horizontal = Dungeon.level.solid[door.cell - 1] && Dungeon.level.solid[door.cell + 1];
		Image sprite = blocked ? locked : this;
		sprite.x = x + (horizontal ? -1 : 0) * scale.x;
		sprite.y = y + (horizontal ? 3 : -3) * scale.y;
		sprite.scale.set(scale);
		sprite.camera = camera;
		sprite.hardlight(color);
		sprite.draw();
	}

	@Override
	public void destroy() {
		locked.destroy();
		super.destroy();
	}
}
