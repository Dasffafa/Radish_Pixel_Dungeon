package com.shatteredpixel.shatteredpixeldungeon.custom.breakball;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BreakBallEntity;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.BreakBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.ScrollArea;
import com.watabou.noosa.Visual;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

public class BreakBallChargeOverlay extends ScrollArea {

	public static final float CHARGE_TIME = 1.1f;
	public static final float BALL_RADIUS = 0.35f;
	private static final float MIN_CHARGE = 0.04f;
	private static final float MAX_HOLD = 30f;

	private static final float BAR_MAX_WIDTH = 30f;
	private static final float ARROW_MIN = 9f;
	private static final float ARROW_MAX = 42f;

	private final BreakBallEntity entity;
	private final Group gfx;

	private boolean charging;
	private boolean holding;
	private float chargeStart;
	private float charge;
	private final PointF aimAt = new PointF();

	private final ColorBlock barBg;
	private final ColorBlock bar;
	private final ColorBlock arrow;
	private final ColorBlock headA;
	private final ColorBlock headB;

	public BreakBallChargeOverlay( DungeonTilemap map, BreakBallEntity entity ) {
		super( map );
		this.entity = entity;

		camera = map.camera();
		blockLevel = ALWAYS_BLOCK;
		active = true;
		visible = false;

		barBg = new ColorBlock( 1, 5, 0xCC000000 );
		bar = new ColorBlock( 1, 3, 0xFFFFC64B );
		arrow = new ColorBlock( 1, 2, 0xFFFFFFFF );
		headA = new ColorBlock( 5, 2, 0xFFFFFFFF );
		headB = new ColorBlock( 5, 2, 0xFFFFFFFF );

		arrow.origin.set( 0, 1f );
		headA.origin.set( 0, 1f );
		headB.origin.set( 0, 1f );

		gfx = Dungeon.hero.sprite != null ? Dungeon.hero.sprite.parent : null;
		if ( gfx != null ) {
			gfx.add( barBg );
			gfx.add( bar );
			gfx.add( arrow );
			gfx.add( headA );
			gfx.add( headB );
			gfx.add( this );
		}

		hideVisuals();
		givePointerPriority();
	}

	private void hideVisuals() {
		barBg.visible = false;
		bar.visible = false;
		arrow.visible = false;
		headA.visible = false;
		headB.visible = false;
	}

	@Override
	protected void onPointerDown( PointerEvent event ) {
		if ( event.button != PointerEvent.LEFT ) return;

		holding = true;
		charging = true;
		charge = 0f;
		chargeStart = Game.timeTotal;
		aimAt.set( Camera.main.screenToCamera( (int) event.current.x, (int) event.current.y ) );
	}

	@Override
	protected void onDrag( PointerEvent event ) {
		if ( holding ) {
			aimAt.set( Camera.main.screenToCamera( (int) event.current.x, (int) event.current.y ) );
		}
	}

	@Override
	protected void onPointerUp( PointerEvent event ) {
		if ( !holding ) {
			cancel();
			return;
		}
		holding = false;
		charging = false;

		if ( charge < MIN_CHARGE ) {
			cancel();
			return;
		}
		launch();
	}

	@Override
	public void update() {
		super.update();

		if ( !charging ) return;

		charge = GameMath.gate( 0f, (Game.timeTotal - chargeStart) / CHARGE_TIME, 1f );
		layout();

		if ( Game.timeTotal - chargeStart > MAX_HOLD ) {
			cancel();
		}
	}

	private PointF heroCenter() {
		return Dungeon.hero.sprite.center();
	}

	private PointF aimDir() {
		PointF c = heroCenter();
		PointF dir = new PointF( aimAt.x - c.x, aimAt.y - c.y );
		float len = (float) Math.hypot( dir.x, dir.y );
		if ( len < 1e-3f ) return new PointF( 1, 0 );
		return new PointF( dir.x / len, dir.y / len );
	}

	private void layout() {
		if ( gfx == null || Dungeon.hero.sprite == null ) return;

		PointF c = heroCenter();
		PointF d = aimDir();

		barBg.visible = bar.visible = arrow.visible = headA.visible = headB.visible = true;

		float w = 2f + BAR_MAX_WIDTH * charge;
		barBg.size( w + 2f, 5 );
		barBg.x = c.x - w / 2f - 1f;
		barBg.y = c.y + 8f;

		bar.size( w, 3 );
		bar.x = c.x - w / 2f;
		bar.y = c.y + 9f;

		float len = ARROW_MIN + (ARROW_MAX - ARROW_MIN) * charge;
		float deg = (float) Math.toDegrees( Math.atan2( d.y, d.x ) );

		arrow.size( len, 2 );
		arrow.x = c.x;
		arrow.y = c.y;
		arrow.angle = deg;

		float tipX = c.x + d.x * len;
		float tipY = c.y + d.y * len;

		headA.x = tipX;
		headA.y = tipY;
		headA.angle = deg + 152f;

		headB.x = tipX;
		headB.y = tipY;
		headB.angle = deg - 152f;
	}

	private void launch() {
		PointF c = heroCenter();
		PointF d = aimDir();

		float size = DungeonTilemap.SIZE;
		float heroX = c.x / size - 0.5f;
		float heroY = c.y / size - 0.5f;

		PointF center = entity.sprite != null ? entity.sprite.center() : c;
		float bx = center.x / size - 0.5f;
		float by = center.y / size - 0.5f;

		float radius = entity.ballRadius();
		BreakBallBallistics.Result result = BreakBallBallistics.compute(
				heroX, heroY, bx, by, d.x, d.y, charge, radius,
				Dungeon.hero.damageRoll(), entity.mass(), BreakBallFlight.solidTest() );

		cleanup();

		if ( result == null ) {
			GLog.w( Messages.get( BreakBall.class, "miss" ) );
			GameScene.ready();
			return;
		}

		entity.consumeDurability();
		Actor.add( new BreakBallFlight( entity, result, radius ) );
		Dungeon.hero.spendAndNext( 1f );
	}

	private void cancel() {
		cleanup();
		GameScene.ready();
	}

	private void cleanup() {
		charging = false;
		holding = false;
		hideVisuals();

		for ( Visual v : new Visual[]{ barBg, bar, arrow, headA, headB } ) {
			v.killAndErase();
		}

		PointerEvent.removePointerListener( this );
		killAndErase();
	}
}
