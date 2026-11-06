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
import com.shatteredpixel.shatteredpixeldungeon.custom.naidan.NaidanChargeOverlay;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Naidan extends MeleeWeapon {

	/** 武技：打飞 */
	public static final String AC_KNOCK = "KNOCK";

	{
		image = ItemSpriteSheet.NAIDAN;
		tier = 1;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ( hero != null && findEgg() != null ) {
			actions.add( AC_KNOCK );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if ( !AC_KNOCK.equals( action ) ) return;

		// 回合/动画进行中不要接管输入（物品窗口在调用前已经 hide，不能拿 showingWindow 当判据）
		if ( !hero.ready ) return;

		if ( !isEquipped( hero ) ) {
			GLog.w( Messages.get( this, "ability_unequipped" ) );
			return;
		}

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
