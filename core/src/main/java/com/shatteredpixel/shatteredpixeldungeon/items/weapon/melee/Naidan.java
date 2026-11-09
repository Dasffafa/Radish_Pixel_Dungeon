/*
 * Radish Pixel Dungeon
 *
 * 新增武器：奶蛋
 * 装备时在英雄身边生成一只奶蛋生物（NaidanEgg）。
 * 武器与生物共用 sprites/items/naidan.png 与 sprites/naidan.png 的同一张贴图。
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NaidanEgg;
import com.shatteredpixel.shatteredpixeldungeon.custom.naidan.NaidanBallistics;
import com.shatteredpixel.shatteredpixeldungeon.custom.naidan.NaidanChargeOverlay;
import com.shatteredpixel.shatteredpixeldungeon.custom.naidan.NaidanFlight;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.input.PointerEvent;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class Naidan extends MeleeWeapon {

	/** 武技：打飞 */
	public static final String AC_KNOCK = "KNOCK";
	/** 武技：收回（把场上的奶蛋传送到身边） */
	public static final String AC_RECALL = "RECALL";

	{
		image = ItemSpriteSheet.NAIDAN;
		tier = 1;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ( hero != null && findEgg() != null ) {
			actions.add( AC_KNOCK );
			actions.add( AC_RECALL );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if ( !hero.ready ) return;

		if ( !isEquipped( hero ) ) {
			GLog.w( Messages.get( this, "ability_unequipped" ) );
			return;
		}

		if ( AC_RECALL.equals( action ) ) {
			NaidanEgg egg = findEgg();
			if ( egg == null ) {
				GLog.w( Messages.get( this, "no_egg" ) );
				return;
			}
			if ( NaidanFlight.isFlying() ) {
				GLog.w( Messages.get( this, "flight_active" ) );
				return;
			}
			if ( recallEgg( egg ) ) {
				Dungeon.hero.spendAndNext( 1f );
			}
			updateQuickslot();
			return;
		}
		if ( !AC_KNOCK.equals( action ) ) return;

		// 回合/动画进行中不要接管输入（物品窗口在调用前已经 hide，不能拿 showingWindow 当判据）
		// （hero.ready 已在方法开头统一检查）

		NaidanEgg egg = findEgg();
		if ( egg == null ) {
			GLog.w( Messages.get( this, "no_egg" ) );
			return;
		}

		DungeonTilemap map = GameScene.tilemap();
		if ( map == null || hero.sprite == null ) return;

		// 接管一次全屏按压：按住蓄力、松手打飞
		new NaidanChargeOverlay( map, egg );
		GLog.i( Messages.get( this, "charging" ) );
		com.watabou.utils.DeviceCompat.log( "Naidan", "execute KNOCK ok, egg@" + egg.pos + " hero@" + hero.pos );
		updateQuickslot();
	}

	/**
	 * 回收：把场上的奶蛋滚回英雄身边（不是传送）。
	 * 预算 = 到英雄的距离，匀减速恰好停在英雄位置；NaidanFlight 回收模式扫到英雄格即消散。
	 * 路被墙挡时依旧给弹道（撞墙反弹继续滚），超时 6 秒兜底强制回收。
	 * 武器菜单「收回」与 C 快捷键共用。成功返回 true（调用方决定是否扣回合）。
	 */
	public static boolean recallEgg( NaidanEgg egg ) {
		DungeonTilemap map = GameScene.tilemap();
		if ( map == null || Dungeon.hero.sprite == null ) return false;

		float size = DungeonTilemap.SIZE;
		// 瞄准英雄「格子中心」而不是精灵中心：消散判定比对的是 hero.pos（格索引），
		// 精灵立绘有 2.5D 上溢偏移（约 0.3 格），瞄精灵会让终点格落在相邻行、永远扫不到英雄格
		float heroX = Dungeon.hero.pos % Dungeon.level.width();
		float heroY = Dungeon.hero.pos / Dungeon.level.width();

		PointF eggCenter = egg.sprite != null ? egg.sprite.center() : null;
		float eggX = eggCenter != null ? eggCenter.x / size - 0.5f : egg.pos % Dungeon.level.width();
		float eggY = eggCenter != null ? eggCenter.y / size - 0.5f : egg.pos / Dungeon.level.width();

		// 终点裕量 0.35：滚「过」英雄格心而不是恰好停在格心，扫格判定必命中英雄格
		NaidanBallistics.Result result = NaidanBallistics.computeRecall(
				eggX, eggY, heroX, heroY, 0.35f, NaidanFlight.solidTest() );
		if ( result == null ) return false;

		DeviceCompat.log( "Naidan", String.format(
				"recall roll hero=(%.2f,%.2f) egg=(%.2f,%.2f) dur=%.2fs",
				heroX, heroY, eggX, eggY, result.duration ) );
		Actor.add( new NaidanFlight( egg, result, NaidanChargeOverlay.BALL_RADIUS, true ) );
		return true;
	}

	/** 场上找蛋（静态版，供 quickFire 使用） */
	private static NaidanEgg findEggStatic() {
		for ( Char c : Actor.chars() ) {
			if ( c instanceof NaidanEgg && c.isAlive() ) return (NaidanEgg) c;
		}
		return null;
	}

	/** V 键快速发射：蛋从英雄位置沿鼠标方向满力直接滚出。场上无蛋时从武器里「装填」一颗再发射 */
	public static boolean quickFire() {
		if ( GameScene.tilemap() == null || Dungeon.hero.sprite == null ) return false;

		NaidanEgg egg = findEggStatic();
		if ( egg == null ) {
			// 回收后的蛋存在武器里：从英雄身上重新孵一颗并发射（V 的语义 = 从玩家身上射出）
			// appear 的登场特效由下方公共段统一做，这里不重复
			egg = new NaidanEgg();
			egg.pos = Dungeon.hero.pos;
			egg.state = egg.PASSIVE;
			GameScene.add( egg, 1f );
			DeviceCompat.log( "Naidan", "quickfire: no egg on floor, hatched one at hero" );
		}

		Camera cam = Camera.main;
		if ( cam == null ) return false;
		PointF hover = PointerEvent.currentHoverPos();
		if ( hover == null ) return false;
		PointF mouse = cam.screenToCamera( (int) hover.x, (int) hover.y );

		float size = DungeonTilemap.SIZE;
		// 发射原点 = 英雄格中心（与回收消散判定同基准，避免精灵 2.5D 偏移）
		float heroX = Dungeon.hero.pos % Dungeon.level.width();
		float heroY = Dungeon.hero.pos / Dungeon.level.width();
		PointF originWorld = new PointF( (heroX + 0.5f) * size, (heroY + 0.5f) * size );

		// 直射：蛋被搬到英雄脚下，沿鼠标方向滚出（从英雄身上射出）
		NaidanBallistics.Result result = NaidanBallistics.computeStraight(
				heroX, heroY, mouse.x - originWorld.x, mouse.y - originWorld.y, 1f, NaidanFlight.solidTest() );
		if ( result == null ) return false;

		DeviceCompat.log( "Naidan", String.format(
				"quickfire hero=(%.2f,%.2f) dir=(%.2f,%.2f) dur=%.2fs dist=%.1f",
				heroX, heroY,
				(mouse.x - originWorld.x), (mouse.y - originWorld.y),
				result.duration, result.distance ) );

		// 视觉：先把蛋挪到英雄脚下再起飞，读起来就是「从玩家身上射出」
		egg.pos = Dungeon.hero.pos;
		ScrollOfTeleportation.appear( egg, Dungeon.hero.pos );
		Dungeon.level.occupyCell( egg );

		Actor.add( new NaidanFlight( egg, result, NaidanChargeOverlay.BALL_RADIUS ) );
		return true;
	}

	private NaidanEgg findEgg() {
		if ( Dungeon.level == null ) return null;
		for ( Char c : Actor.chars() ) {
			if ( c instanceof NaidanEgg && c.isAlive() ) return (NaidanEgg) c;
		}
		return null;
	}

	@Override
	public String statsInfo() {
		return Messages.get( this, "ability_desc" );
	}

	/**
	 * 只在「真正装备」时孵蛋。
	 * 不用 activate()：读档时 Belongings.restoreFromBundle 也会调用 activate，
	 * 那时关卡/角色还在还原，生成怪物会出问题。doEquip 只在玩家装备动作里被调用。
	 */
	@Override
	public boolean doEquip( Hero hero ) {
		boolean equipped = super.doEquip( hero );
		if (equipped && isEquipped( hero )) {
			spawnEgg( hero );
		}
		return equipped;
	}

	/** 装到副手也算装备，同样孵蛋。 */
	@Override
	public boolean equipSecondary( Hero hero ) {
		boolean equipped = super.equipSecondary( hero );
		if (equipped) {
			spawnEgg( hero );
		}
		return equipped;
	}

	private void spawnEgg( Hero hero ) {
		if (Dungeon.level == null || hero == null) return;

		// 同一层只保留一只，避免反复穿脱刷出一堆怪
		for (Char c : Actor.chars()) {
			if (c instanceof NaidanEgg) return;
		}

		int cell = -1;
		for (int i : PathFinder.NEIGHBOURS8) {
			int p = hero.pos + i;
			if (Dungeon.level.insideMap( p )
					&& !Dungeon.level.solid[p]
					&& !Dungeon.level.avoid[p]
					&& Actor.findChar( p ) == null) {
				cell = p;
				break;
			}
		}

		if (cell == -1) {
			GLog.w( Messages.get( this, "no_space" ) );
			return;
		}

		NaidanEgg egg = new NaidanEgg();
		egg.pos = cell;
		egg.state = egg.PASSIVE;
		GameScene.add( egg, 1f );
		Dungeon.level.occupyCell( egg );
		ScrollOfTeleportation.appear( egg, cell );

		GLog.i( Messages.get( this, "spawned" ) );
	}
}
