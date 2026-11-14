package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom;
import com.watabou.noosa.Game;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpecialDoorsTest {

	@BeforeClass
	public static void initializeVersion() {
		Game.version = "test";
	}

	@Test
	public void ordinaryRoomPaintAndFlagRefreshDoNotRegisterSpecialDoors() {
		SewerLevel level = new SewerLevel();
		level.setSize(9, 9);
		EmptyRoom room = new EmptyRoom();
		room.set(2, 2, 6, 6);
		Room.Door doorway = new Room.Door(4, 2);
		room.connected.put(new EmptyRoom(), doorway);
		room.paint(level);
		assertEquals(Room.Door.Type.REGULAR, doorway.type);
		int cell = doorway.x + doorway.y * level.width();
		level.map[cell] = Terrain.DOOR;
		level.passable[cell] = true;

		SpecialDoors.applyLevelFlags(level);
		assertTrue(level.specialDoors.isEmpty());
		assertNull(SpecialDoors.get(level, cell));
		assertFalse(SpecialDoors.isBlocking(level, cell));
		assertTrue(level.passable[cell]);
	}

	@Test
	public void explicitSpecialDoorDoesNotChangeOtherOrdinaryDoors() {
		SewerLevel level = new SewerLevel();
		level.setSize(9, 9);
		int special = 22, ordinary = 24;
		level.map[special] = level.map[ordinary] = Terrain.DOOR;
		level.passable[special] = level.passable[ordinary] = true;
		SpecialDoors.add(level, special).locked("signal:test");

		SpecialDoors.applyLevelFlags(level);
		assertTrue(SpecialDoors.isBlocking(level, special));
		assertFalse(level.passable[special]);
		assertNull(SpecialDoors.get(level, ordinary));
		assertFalse(SpecialDoors.isBlocking(level, ordinary));
		assertTrue(level.passable[ordinary]);
		assertEquals(Terrain.DOOR, level.map[ordinary]);
	}
}
