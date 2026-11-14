package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.CombatRoom;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;

import rpdtest.H;
import rpdtest.Scenario;

/** 正常开局及测试模式房间池的生成隔离。 */
public class CombatRoomChallengeGate implements Scenario {

	@Override
	public String describe() {
		return "SewerLevel第1层仅在TEST_MODE开启时生成CombatRoom";
	}

	@Override
	public void run(H h) {
		h.newGame(HeroClasses.WARRIOR, "RPDTEST-COMBAT-ROOM");
		h.check(h.get(() -> Dungeon.challenges == 0), "正常开局未开启挑战");
		h.check(h.get(() -> ((RegularLevel) Dungeon.level).rooms().stream()
				.noneMatch(room -> room instanceof CombatRoom)), "正常开局没有战斗测试房间");
		h.check(h.get(() -> Dungeon.level.specialDoors.isEmpty()), "正常房间的门不注册特殊门");
		h.check(h.get(() -> {
			for (int cell = 0; cell < Dungeon.level.length(); cell++) {
				if ((Dungeon.level.map[cell] == Terrain.DOOR || Dungeon.level.map[cell] == Terrain.OPEN_DOOR)
						&& !Dungeon.level.passable[cell]) return false;
			}
			return true;
		}), "正常开局的普通门均可通行");
		h.world("检查测试模式房间池", () -> {
			int previous = Dungeon.challenges;
			try {
				Dungeon.challenges = Challenges.TEST_MODE;
				h.check(new RoomPool().hasCombatRoom(), "测试模式保留战斗测试房间");
			} finally {
				Dungeon.challenges = previous;
			}
		});
	}

	private static class RoomPool extends SewerLevel {
		boolean hasCombatRoom() {
			return initRooms().stream().anyMatch(room -> room instanceof CombatRoom);
		}
	}
}
