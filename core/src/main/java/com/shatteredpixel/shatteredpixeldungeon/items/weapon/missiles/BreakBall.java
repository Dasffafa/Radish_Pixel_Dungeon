package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BreakBallEntity;
import com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallBallistics;
import com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallChargeOverlay;
import com.shatteredpixel.shatteredpixeldungeon.custom.breakball.BreakBallFlight;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class BreakBall extends MissileWeapon {

	public static final String AC_KNOCK = "KNOCK";
	public static final String AC_RECALL = "RECALL";

	public static final int VARIANT_1 = 0;
	public static final int VARIANT_2 = 1;
	public static final int VARIANT_EGG = 2;

	public int variant;
	public int baseStrReq;

	{
		tier = 1;
		baseUses = 10;
		sticky = false;
	}

	public BreakBall() {
		super();
		// 生成时在 10-18 之间随机力量需求，同时作为破坏球的质量
		baseStrReq = Random.IntRange( 10, 18 );
		// 0.5% 几率生成原先的奶蛋贴图，其余在两张破坏球贴图间等概率随机
		float roll = Random.Float();
		if ( roll < 0.005f ) {
			setVariant( VARIANT_EGG );
		} else if ( roll < 0.5025f ) {
			setVariant( VARIANT_1 );
		} else {
			setVariant( VARIANT_2 );
		}
	}

	@Override
	public int STRReq( int lvl ) {
		lvl = Math.max( 0, lvl );
		return baseStrReq - (int) (Math.sqrt( 8 * lvl + 1 ) - 1) / 2;
	}

	public float mass() {
		return Math.max( 1f, STRReq() );
	}

	public void setVariant( int variant ) {
		this.variant = variant;
		switch ( variant ) {
			case VARIANT_2:
				image = ItemSpriteSheet.BREAK_BALL_2;
				break;
			case VARIANT_EGG:
				image = ItemSpriteSheet.BREAK_BALL_EGG;
				break;
			case VARIANT_1:
			default:
				image = ItemSpriteSheet.BREAK_BALL_1;
				break;
		}
	}

	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar( item )
				&& ((BreakBall) item).variant == variant
				&& ((BreakBall) item).baseStrReq == baseStrReq;
	}

	@Override
	public Item split( int amount ) {
		Item split = super.split( amount );
		if ( split instanceof BreakBall ) {
			((BreakBall) split).setVariant( this.variant );
			((BreakBall) split).baseStrReq = this.baseStrReq;
		}
		return split;
	}

	// 正常投掷只能投掷较短距离：(力量 - 力量需求) 格
	@Override
	public int throwPos( Hero user, int dst ) {
		int maxDist = Math.max( 1, user.STR() - STRReq() );
		Ballistica trajectory = new Ballistica( user.pos, dst, Ballistica.PROJECTILE );
		int dist = Math.min( trajectory.dist, maxDist );
		return trajectory.path.get( dist );
	}

	// 点击放下时在英雄身边生成 BreakBallEntity
	@Override
	public void doDrop( Hero hero ) {
		if ( Dungeon.level == null || hero == null ) {
			super.doDrop( hero );
			return;
		}

		int cell = findSpawnCell( hero.pos );
		if ( cell == -1 ) {
			GLog.w( Messages.get( this, "no_space" ) );
			return;
		}

		BreakBall single = (BreakBall) detach( hero.belongings.backpack );
		if ( single == null ) return;

		spawnEntityAt( single, cell );
		hero.spendAndNext( TIME_TO_DROP );
		GLog.i( Messages.get( this, "spawned" ) );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ( hero != null && findEntity() != null ) {
			actions.add( AC_KNOCK );
			actions.add( AC_RECALL );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if ( !hero.ready ) return;

		if ( AC_RECALL.equals( action ) ) {
			BreakBallEntity entity = findEntity();
			if ( entity == null ) {
				GLog.w( Messages.get( this, "no_egg" ) );
				return;
			}
			if ( BreakBallFlight.isFlying() ) {
				GLog.w( Messages.get( this, "flight_active" ) );
				return;
			}
			if ( recallEntity( entity ) ) {
				Dungeon.hero.spendAndNext( 1f );
			}
			updateQuickslot();
			return;
		}

		if ( !AC_KNOCK.equals( action ) ) return;

		BreakBallEntity entity = findEntity();
		if ( entity == null ) {
			GLog.w( Messages.get( this, "no_egg" ) );
			return;
		}

		DungeonTilemap map = GameScene.tilemap();
		if ( map == null || hero.sprite == null ) return;

		new BreakBallChargeOverlay( map, entity );
		GLog.i( Messages.get( this, "charging" ) );
		updateQuickslot();
	}

	/** 每次发射消耗一次耐久，返回是否已损毁 */
	public boolean consumeLaunchDurability() {
		decrementDurability();
		return durability <= 0;
	}

	public static boolean recallEntity( BreakBallEntity entity ) {
		DungeonTilemap map = GameScene.tilemap();
		if ( map == null || Dungeon.hero.sprite == null ) return false;

		float size = DungeonTilemap.SIZE;
		float heroX = Dungeon.hero.pos % Dungeon.level.width();
		float heroY = Dungeon.hero.pos / Dungeon.level.width();

		PointF center = entity.sprite != null ? entity.sprite.center() : null;
		float ex = center != null ? center.x / size - 0.5f : entity.pos % Dungeon.level.width();
		float ey = center != null ? center.y / size - 0.5f : entity.pos / Dungeon.level.width();

		BreakBallBallistics.Result result = BreakBallBallistics.computeRecall(
				ex, ey, heroX, heroY, 0.35f, BreakBallFlight.solidTest() );
		if ( result == null ) {
			entity.collectToHero();
			return true;
		}

		DeviceCompat.log( "BreakBall", String.format(
				"recall roll hero=(%.2f,%.2f) entity=(%.2f,%.2f) dur=%.2fs",
				heroX, heroY, ex, ey, result.duration ) );
		Actor.add( new BreakBallFlight( entity, result, entity.ballRadius(), true ) );
		return true;
	}

	public static BreakBallEntity findEntity() {
		if ( Dungeon.level == null ) return null;
		for ( Char c : Actor.chars() ) {
			if ( c instanceof BreakBallEntity && c.isAlive() ) return (BreakBallEntity) c;
		}
		return null;
	}

	public static boolean quickFire() {
		if ( GameScene.tilemap() == null || Dungeon.hero.sprite == null ) return false;

		Camera cam = Camera.main;
		if ( cam == null ) return false;
		PointF hover = PointerEvent.currentHoverPos();
		if ( hover == null ) return false;
		PointF mouse = cam.screenToCamera( (int) hover.x, (int) hover.y );

		float size = DungeonTilemap.SIZE;
		float heroX = Dungeon.hero.pos % Dungeon.level.width();
		float heroY = Dungeon.hero.pos / Dungeon.level.width();
		PointF originWorld = new PointF( (heroX + 0.5f) * size, (heroY + 0.5f) * size );

		BreakBallEntity entity = findEntity();
		BreakBall previewItem = entity != null ? entity.getSourceItem() : Dungeon.hero.belongings.getItem( BreakBall.class );
		if ( entity == null && previewItem == null ) return false;

		float mass = entity != null ? entity.mass() : previewItem.mass();
		float weaponDmg = Dungeon.hero.damageRoll();
		BreakBallBallistics.Result result = BreakBallBallistics.computeStraight(
				heroX, heroY, mouse.x - originWorld.x, mouse.y - originWorld.y,
				1f, weaponDmg, mass, BreakBallFlight.solidTest() );
		if ( result == null ) return false;

		if ( entity == null ) {
			BreakBall single = (BreakBall) previewItem.detach( Dungeon.hero.belongings.backpack );
			if ( single == null ) return false;
			entity = spawnEntityAt( single, Dungeon.hero.pos );
		} else {
			entity.pos = Dungeon.hero.pos;
			ScrollOfTeleportation.appear( entity, Dungeon.hero.pos );
			Dungeon.level.occupyCell( entity );
		}

		entity.consumeDurability();
		Actor.add( new BreakBallFlight( entity, result, entity.ballRadius() ) );
		return true;
	}

	private static int findSpawnCell( int around ) {
		for ( int i : PathFinder.NEIGHBOURS8 ) {
			int p = around + i;
			if ( Dungeon.level.insideMap( p )
					&& !Dungeon.level.solid[p]
					&& !Dungeon.level.avoid[p]
					&& Actor.findChar( p ) == null ) {
				return p;
			}
		}
		return -1;
	}

	private static BreakBallEntity spawnEntityAt( BreakBall item, int cell ) {
		BreakBallEntity entity = new BreakBallEntity( item );
		entity.pos = cell;
		entity.state = entity.PASSIVE;
		GameScene.add( entity, 1f );
		Dungeon.level.occupyCell( entity );
		ScrollOfTeleportation.appear( entity, cell );
		return entity;
	}

	@Override
	public String statsInfo() {
		return Messages.get( this, "ability_desc" );
	}

	private static final String VARIANT = "variant";
	private static final String BASE_STR_REQ = "base_str_req";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VARIANT, variant );
		bundle.put( BASE_STR_REQ, baseStrReq );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		setVariant( bundle.getInt( VARIANT ) );
		baseStrReq = bundle.contains( BASE_STR_REQ ) ? bundle.getInt( BASE_STR_REQ ) : 14;
	}
}
