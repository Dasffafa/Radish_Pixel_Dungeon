/*
 * Radish Pixel Dungeon — 奶蛋「打飞」蓄力界面
 *
 * 用武技后接管一次全屏按压：
 *  · 按住 → 英雄脚下出现横向蓄力条，并从英雄中心生成指向按压点的箭头；
 *  · 蓄力条越长、箭头越长；按住期间拖动可改方向；
 *  · 松手 → 按台球模型把奶蛋打飞。
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.naidan;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NaidanEgg;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Naidan;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.ScrollArea;
import com.watabou.noosa.Visual;
import com.watabou.input.PointerEvent;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

public class NaidanChargeOverlay extends ScrollArea {

	/** 蓄满所需时间（秒） */
	public static final float CHARGE_TIME = 1.1f;
	/** 球体碰撞半径（格） */
	public static final float BALL_RADIUS = 0.35f;
	/** 低于这个蓄力量视作「只是点了一下」，直接退出，不算出手 */
	private static final float MIN_CHARGE = 0.04f;
	/** 兜底：按住超过这个时间仍未收到抬起（丢焦点等），自动退出 */
	private static final float MAX_HOLD = 30f;

	private static final float BAR_MAX_WIDTH = 30f;
	private static final float ARROW_MIN = 9f;
	private static final float ARROW_MAX = 42f;

	private final NaidanEgg egg;
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

	public NaidanChargeOverlay( DungeonTilemap map, NaidanEgg egg ) {
		super( map );
		this.egg = egg;

		camera = map.camera();
		blockLevel = ALWAYS_BLOCK;
		active = true;
		visible = false;

		// ColorBlock 用 Noosa ARGB（0xAARRGGBB）：最高位是 alpha，漏了 0xFF 就是全透明
		barBg = new ColorBlock( 1, 5, 0xCC000000 );
		bar = new ColorBlock( 1, 3, 0xFFFFC64B );
		arrow = new ColorBlock( 1, 2, 0xFFFFFFFF );
		headA = new ColorBlock( 5, 2, 0xFFFFFFFF );
		headB = new ColorBlock( 5, 2, 0xFFFFFFFF );

		// 以「左端中点」为旋转原点，长度方向即箭头方向
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

		DeviceCompat.log( "Naidan", "overlay created, gfx=" + (gfx != null) + " active=" + isActive() );
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
		// 桌面端鼠标：只认左键（触摸事件同样是 LEFT）
		if ( event.button != PointerEvent.LEFT ) return;

		holding = true;
		charging = true;
		charge = 0f;
		chargeStart = Game.timeTotal;
		aimAt.set( Camera.main.screenToCamera( (int) event.current.x, (int) event.current.y ) );
		DeviceCompat.log( "Naidan", "DOWN btn=" + event.button + " at " + (int) event.current.x + "," + (int) event.current.y
				+ " -> world " + (int) aimAt.x + "," + (int) aimAt.y );
	}

	@Override
	protected void onDrag( PointerEvent event ) {
		// 按住时允许改方向
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

		// 只是轻点一下（几乎没蓄力）→ 直接退出，不消耗回合
		if ( charge < MIN_CHARGE ) {
			DeviceCompat.log( "Naidan", "UP with tiny charge " + charge + " -> cancel" );
			cancel();
			return;
		}
		DeviceCompat.log( "Naidan", "UP charge=" + charge + " -> launch" );
		launch();
	}

	@Override
	public void update() {
		super.update();

		if ( !charging ) return;

		charge = GameMath.gate( 0f, (Game.timeTotal - chargeStart) / CHARGE_TIME, 1f );
		layout();

		// 丢焦点之类的意外：别把玩家永久卡在蓄力态
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

		// 横向蓄力条：英雄脚下方，从中间向两侧生长（深色底衬 + 橙色填充）
		float w = 2f + BAR_MAX_WIDTH * charge;
		barBg.size( w + 2f, 5 );
		barBg.x = c.x - w / 2f - 1f;
		barBg.y = c.y + 8f;

		bar.size( w, 3 );
		bar.x = c.x - w / 2f;
		bar.y = c.y + 9f;

		// 箭头：中心 → 按压方向，长度随蓄力增长
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

	/** 松手：算弹道并起飞 */
	private void launch() {
		PointF c = heroCenter();
		PointF d = aimDir();

		float size = DungeonTilemap.SIZE;
		float heroX = c.x / size - 0.5f;
		float heroY = c.y / size - 0.5f;

		PointF eggCenter = egg.sprite != null ? egg.sprite.center() : c;
		float eggX = eggCenter.x / size - 0.5f;
		float eggY = eggCenter.y / size - 0.5f;

		NaidanBallistics.Result result = NaidanBallistics.compute(
				heroX, heroY, eggX, eggY, d.x, d.y, charge, BALL_RADIUS, solidTest() );

		if ( result == null ) {
			DeviceCompat.log( "Naidan", String.format(
					"MISS hero=(%.2f,%.2f) egg=(%.2f,%.2f) dir=(%.2f,%.2f) charge=%.2f",
					heroX, heroY, eggX, eggY, d.x, d.y, charge ) );
		} else {
			DeviceCompat.log( "Naidan", String.format(
					"HIT dist=%.2f speedFactor=%.3f pathPts=%d end=(%.2f,%.2f)",
					result.distance, result.speedFactor, result.path.size(),
					result.path.get( result.path.size() - 1 ).x,
					result.path.get( result.path.size() - 1 ).y ) );
		}

		cleanup();

		if ( result == null ) {
			GLog.w( Messages.get( Naidan.class, "miss" ) );
			GameScene.ready();
			return;
		}

		// Actor 必须显式入队，否则永远不会 act（之前不飞就是因为漏了这一步）
		Actor.add( new NaidanFlight( egg, result, BALL_RADIUS ) );
		Dungeon.hero.spendAndNext( 1f );
	}

	private static NaidanBallistics.SolidTest solidTest() {
		return ( cx, cy ) -> {
			int w = Dungeon.level.width();
			int h = Dungeon.level.height();
			if ( cx < 0 || cy < 0 || cx >= w || cy >= h ) return true;
			return Dungeon.level.solid[cy * w + cx];
		};
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

		// Group.erase() 不会调用 destroy()，而 PointerArea 是全局注册的监听器：
		// 不手动摘掉的话，界面虽然消失了，后面的点击仍会被它全部吞掉。
		PointerEvent.removePointerListener( this );
		killAndErase();
	}
}
