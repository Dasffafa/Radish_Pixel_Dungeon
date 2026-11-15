package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

final class SpecialDoorLock extends Image {

	private static long generation = -1;
	private static SmartTexture locks;
	private static final RectF CLOSED = new RectF(0, 0, 0.5f, 1);
	private static final RectF OPEN = new RectF(0.5f, 0, 1, 1);

	SpecialDoorLock() {
		if (generation != TextureCache.generation()) {
			Pixmap pixels = new Pixmap(16, 9, Pixmap.Format.RGBA8888);
			// 两帧共用锁身，开锁帧只抬起右侧锁梁。
			for (int offset : new int[]{0, 8}) {
				pixels.setColor(0x202020FF);
				pixels.fillRectangle(offset + 1, 4, 6, 5);
				pixels.setColor(0xFFFFFFFF);
				pixels.fillRectangle(offset + 2, 5, 4, 3);
				pixels.setColor(0x505050FF);
				pixels.drawPixel(offset + 3, 6);
				pixels.drawPixel(offset + 3, 7);
			}
			pixels.setColor(0xD0D0D0FF);
			pixels.drawRectangle(2, 1, 4, 5);
			pixels.drawLine(10, 1, 10, 4);
			pixels.drawLine(10, 1, 13, 1);
			pixels.drawPixel(13, 2);
			locks = TextureCache.put(SpecialDoorLock.class, new SmartTexture(pixels));
			generation = TextureCache.generation();
		}
		texture(locks);
		frame(CLOSED);
	}

	void draw(SpecialDoor door) {
		int color = SpecialDoorTint.color(door);
		if (color == -1) return;
		boolean blocked = door.sealed || door.locked() || (door.toggle && !door.appeared);
		if (door.oneWay && Dungeon.level.map[door.cell] != Terrain.OPEN_DOOR) {
			blocked |= !SpecialDoors.allowsOpen(Dungeon.level, door.cell, Dungeon.hero.pos);
		}
		frame(blocked ? CLOSED : OPEN);
		hardlight(color);
		super.draw();
	}
}
