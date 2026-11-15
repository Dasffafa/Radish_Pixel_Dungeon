package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.CombatRoom;
import com.watabou.utils.Bundle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import rpdtest.H;
import rpdtest.Scenario;

public class CombatRoomDoors implements Scenario {
	@Override
	public String describe() { return "战斗房间进入、撤退和清怪后的门状态"; }

	@Override
	public void run(H h) {
		h.newGame(HeroClasses.WARRIOR, "combat-door-regression");
		h.world("验证战斗房间门状态", () -> {
			try {
				RegularLevel level = (RegularLevel) Dungeon.level;
				CombatRoom room = new CombatRoom();
				room.set(2, 2, 6, 6);
				int door = 4 + 2 * level.width();
				int inside = door + level.width();
				int oldTerrain = level.map[door];
				int oldPos = Dungeon.hero.pos;
				Field field = RegularLevel.class.getDeclaredField("rooms");
				field.setAccessible(true);
				@SuppressWarnings("unchecked") ArrayList<Room> rooms = (ArrayList<Room>) field.get(level);
				ArrayList<Room> oldRooms = new ArrayList<>(rooms);
				rooms.clear();
				rooms.add(room);
				Bundle saved = new Bundle();
				room.storeInBundle(saved);
				saved.put("door_cells", new int[]{door});
				room.restoreFromBundle(saved);
				Rat guard = new Rat();
				guard.pos = inside + level.width();
				level.mobs.add(guard);
				try {
					level.map[door] = Terrain.DOOR;
					room.onLevelLoad(level);
					h.check(!SpecialDoors.isBlocking(level, door), "进入前特殊门可通行");
					Dungeon.hero.pos = door;
					CombatRoom.onHeroMove(new HeroMoveEvent(Dungeon.hero, door - level.width(), door));
					CombatRoom.onHeroAct(new HeroActEvent(Dungeon.hero));
					h.check(!SpecialDoors.isBlocking(level, door), "停在门上不会锁门");
					CombatRoom.onHeroMove(new HeroMoveEvent(Dungeon.hero, door, door - level.width()));
					h.check(!SpecialDoors.isBlocking(level, door), "从门退回不会锁门");
					Dungeon.hero.pos = inside;
					CombatRoom.onHeroMove(new HeroMoveEvent(Dungeon.hero, door, inside));
					h.check(SpecialDoors.isBlocking(level, door), "进入房间后锁门");
					level.mobs.remove(guard);
					CombatRoom.onHeroAct(new HeroActEvent(Dungeon.hero));
					h.check(SpecialDoors.get(level, door) == null && level.passable[door], "清怪后恢复普通门");
					room.onLevelLoad(level);
					h.check(SpecialDoors.get(level, door) == null, "读档后不会恢复特殊门");
				} finally {
					level.mobs.remove(guard);
					SpecialDoors.remove(level, door);
					level.map[door] = oldTerrain;
					Dungeon.hero.pos = oldPos;
					rooms.clear();
					rooms.addAll(oldRooms);
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
