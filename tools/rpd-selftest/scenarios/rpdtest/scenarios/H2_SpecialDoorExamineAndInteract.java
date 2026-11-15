package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * 特殊门的检视与交互：
 * 1. 检视特殊门应显示当前状态与真实解锁条件（signal:combat 触发器）；
 * 2. 点击无法解锁的门，英雄应先走到门前，到位后提示真实解锁条件。
 */
public class H2_SpecialDoorExamineAndInteract implements Scenario {

	@Override
	public String describe() {
		return "特殊门检视显示状态与真实解锁条件，无法解锁时走到门前再提示";
	}

	@Override
	public void run( H h ) {
		h.newGame( HeroClasses.WARRIOR, "1" );

		//找一扇英雄当前可达的门并给它上条件锁（爆炸解锁）：
		//寻路只认已探索格子，所以必须选英雄能走到的门
		final int[] doorCell = new int[1];
		h.world( "lock a reachable door behind explosion trigger", () -> {
			boolean[] p = new boolean[Dungeon.level.length()];
			for (int i = 0; i < p.length; i++) {
				p[i] = Dungeon.level.passable[i] && (Dungeon.level.visited[i] || Dungeon.level.mapped[i]);
			}
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.map[i] != Terrain.DOOR) {
					continue;
				}
				boolean reachable = false;
				for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8) {
					int c = i + n;
					if (c >= 0 && c < p.length && p[c]
							&& Dungeon.findPath( Dungeon.hero, c, p, Dungeon.level.heroFOV, true ) != null) {
						reachable = true;
						break;
					}
				}
				if (reachable) {
					SpecialDoor door = SpecialDoors.add( Dungeon.level, i );
					door.locked( "signal:combat:9,9" );
					Dungeon.level.passable[i] = false;
					doorCell[0] = i;
					return;
				}
			}
			doorCell[0] = -1;
		} );
		h.require( h.get( () -> doorCell[0] != -1 ), "本层至少有一扇门格" );

		int cell = h.get( () -> doorCell[0] );
		h.note( "door cell=" + cell );

		//诊断：英雄到门的路是否存在（hero 应站在门所属房间内侧）
		final int[] diag = h.get( () -> {
			boolean[] p = new boolean[Dungeon.level.length()];
			for (int i = 0; i < p.length; i++) {
				p[i] = Dungeon.level.passable[i] && (Dungeon.level.visited[i] || Dungeon.level.mapped[i]);
			}
			com.watabou.utils.PathFinder.Path path =
					Dungeon.findPath( Dungeon.hero, doorCell[0], p, Dungeon.level.heroFOV, true );
			int reach = -1;
			for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8) {
				int c = doorCell[0] + n;
				if (c >= 0 && c < p.length && p[c]
						&& Dungeon.findPath( Dungeon.hero, c, p, Dungeon.level.heroFOV, true ) != null) {
					reach = c;
					break;
				}
			}
			return new int[]{ path == null ? -1 : path.size(), reach };
		} );
		h.note( "findPathToDoor=" + diag[0] + " reachableNeighbor=" + diag[1] );
		h.require( diag[1] != -1, "英雄能走到门的某个邻格（站在门所属一侧）" );

		//先走到门旁（保证门在视野内，检视才有内容）
		int nearCell = diag[1];
		h.require( nearCell != -1, "门旁存在可站立的格子" );
		h.walkTo( nearCell );
		h.note( "hero near door at " + h.heroPos() );

		//1) 检视：窗口应包含真实解锁条件（爆炸）
		h.examine( cell );
		h.frames( 5 );
		WndInfoCell wnd = h.findWindow( WndInfoCell.class );
		String desc = h.get( () -> {
			if (wnd == null) {
				return "";
			}
			java.util.List<?> wndMembers = Reflect.get( wnd, "members" );
			for (Object g : wndMembers) {
				if (g instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock) {
					return ((com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock) g).text();
				}
			}
			return "";
		} );
		h.note( "examine desc=" + desc );
		h.check( desc.contains( "机关锁" ) && desc.contains( "击败战斗房间" ),
				"检视特殊门应显示锁定状态与真实解锁条件（实际：" + desc + "）" );
		h.closeWindows();

		//2) 走开几步后点击锁住的门：英雄先走到门前，到位后提示真实解锁条件
		int awayCell = h.get( () -> {
			for (int i = 0; i < Dungeon.level.length(); i++) {
				int dist = Dungeon.level.distance( Dungeon.hero.pos, i );
				if (dist >= 4 && dist <= 10 && Dungeon.level.passable[i]
						&& (Dungeon.level.visited[i] || Dungeon.level.mapped[i])
						&& Dungeon.findPath( Dungeon.hero, i,
								Dungeon.level.passable, Dungeon.level.heroFOV, true ) != null) {
					return i;
				}
			}
			return -1;
		} );
		h.require( awayCell != -1, "附近存在可以走开的格子" );
		h.walkTo( awayCell );
		h.note( "hero walked away to " + h.heroPos() );

		h.note( "tryTap=" + h.tryTap( cell ) );
		h.waitReady();
		h.frames( 10 );
		boolean adjacent = h.get( () -> Dungeon.level.adjacent( Dungeon.hero.pos, doorCell[0] ) );
		h.note( "heroPos " + h.heroPos() + " adjacent=" + adjacent
				+ " logHit=" + h.gameLogContains( "击败战斗房间内的所有守卫" ) );
		h.check( adjacent, "英雄应走到门前（与门格相邻）" );
		h.check( h.gameLogContains( "击败战斗房间内的所有守卫" ),
				"走到门前应提示真实解锁条件（击败战斗房间守卫）" );
		h.check( h.get( () -> Dungeon.hero.pos != doorCell[0] ), "锁住的门不能被穿过" );
	}
}
