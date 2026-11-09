/*
 * Radish Pixel Dungeon — 奶蛋滚动
 *
 * 参考《植物大战僵尸》坚果保龄球：贴着地面滚，边走边自转，撞墙非弹性反弹后继续滚。
 * 路径由 NaidanBallistics 以时间域模拟给出（含每个顶点的时间戳与每段速度），
 * 这里只做「按时间采样 + 播放」：速度天然带摩擦衰减，撞墙时速度掉 30%。
 * 撞墙瞬间播放冲击波特效、画面震动与「安迪」音效；
 * 沿途撞上非玩家生物时，按撞击时的速度结算无视护甲的物理伤害。
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.naidan;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NaidanEgg;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamagePipeline;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Naidan;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.HashMap;

public class NaidanFlight extends Actor {

	/** 自转观感上限（度/秒）：压低后滚动更「沉」，不会读成模糊 */
	private static final float MAX_ROLL_SPEED = 720f;
	/** 满初速（约 31 格/秒）对应的撞击基础伤害 */
	private static final int MAX_IMPACT_DAMAGE = 30;
	/** 满蓄力无反弹的初速（格/秒），用于撞击伤害归一化，与 v0 = sqrt(2·a·D) 一致 */
	private static final float FULL_SPEED = 31f;
	/** 单次滚动最多撞同一个目标的次数（同一格滞留不会连击；反弹再次碾过算新一轮） */
	private static final int MAX_HITS_PER_CHAR = 2;

	private final NaidanEgg egg;
	private final ArrayList<PointF> path;
	private final ArrayList<Float> times;
	private final ArrayList<Float> speeds;
	private final float duration;
	private final float radiusCells;
	private final int width;

	/** 一只奶蛋同一时刻只能有一个飞行实例（收回按钮要靠它防重入） */
	private static NaidanFlight active;

	/** 回收模式：不造伤害、不播撞墙特效，滚到英雄所在格即消散 */
	private final boolean recallMode;
	/** 回收模式兜底：滚动超过这个时长还没碰到英雄就强制回收，避免卡死 */
	private static final float RECALL_MAX_TIME = 6f;
	/** 回收接触距离（格）：蛋中心与英雄中心小于此值视为「碰到玩家」，即消散 */
	private static final float CONTACT_DIST = 0.65f;

	public static boolean isFlying() {
		return active != null;
	}

	{
		actPriority = VFX_PRIO + 11;
	}

	/** 常规发射（武技蓄力 / V 键直射） */
	public NaidanFlight( NaidanEgg egg, NaidanBallistics.Result result, float radiusCells ) {
		this( egg, result, radiusCells, false );
	}

	/** 回收滚动：滚向英雄，接触即消失 */
	public NaidanFlight( NaidanEgg egg, NaidanBallistics.Result result, float radiusCells, boolean recallMode ) {
		this.egg = egg;
		this.path = result.path;
		this.times = result.times;
		this.speeds = result.speeds;
		this.duration = Math.max( 0.05f, result.duration );
		this.radiusCells = radiusCells;
		this.width = Dungeon.level.width();
		this.recallMode = recallMode;
		active = this;
	}

	@Override
	protected boolean act() {
		Actor.remove( this );

		if ( egg == null || !egg.isAlive() || egg.sprite == null || egg.sprite.parent == null ) {
			DeviceCompat.log( "Naidan", "flight aborted (egg/sprite gone)" );
			active = null;
			return true;
		}

		DeviceCompat.log( "Naidan", String.format( "flight start dur=%.2fs bounces=%d pts=%d",
				duration, path.size() - 2, path.size() ) );
		new Effect();
		return false;
	}

	/** 滚完落地：位置对齐到可站的格子，但画面保持滚停时的姿态，不回格心、不归零 */
	private void land() {
		// 回收模式滚完还没碰到英雄（路被挡、距离太短衰减过早等）：就地消散，效果与碰到英雄相同
		if ( recallMode ) {
			vanishRecall( true );
			return;
		}

		PointF end = path.get( path.size() - 1 );
		int cell = freeCell( (int) Math.floor( end.x ), (int) Math.floor( end.y ) );

		egg.pos = cell;
		// 刻意不调用 sprite.place(cell)、不重置 angle、不还原脚底原点：
		// 保留最后一帧的像素位置与朝向，看起来就是「恰好滚到这里停住」。
		Dungeon.level.occupyCell( egg );
		DeviceCompat.log( "Naidan", "land at " + cell + " ("
				+ (cell % width) + "," + (cell / width) + ")"
				+ " visual=" + (int) egg.sprite.x + "," + (int) egg.sprite.y );
		if ( Dungeon.level.heroFOV[cell] ) {
			egg.sprite.flash();
		}

		Actor.remove( this );
		active = null;
		next();
	}

	/**
	 * 回收消散：奶蛋从场景移除并播放特效（滚动碰到英雄 / 兜底超时 / 特例路径共用）。
	 * 参照 Dread/Ratmogrify 的移除写法：destroy() 出 Actor 队列 + killAndErase() 摘精灵 + mobs.remove()。
	 */
	private static void vanishEgg( NaidanEgg egg, String logTag ) {
		DeviceCompat.log( "Naidan", logTag );
		CellEmitter.get( Dungeon.hero.pos ).burst( Speck.factory( Speck.WOOL ), 8 );
		Sample.INSTANCE.play( Assets.Sounds.PUFF );
		GLog.i( Messages.get( Naidan.class, "recalled" ) );

		egg.destroy();
		egg.sprite.killAndErase();
		Dungeon.level.mobs.remove( egg );
	}

	private void vanishRecall( boolean fallback ) {
		vanishEgg( egg, "recall vanish at hero, fallback=" + fallback );
		Actor.remove( this );
		active = null;
		next();
	}

	/** 落点被占或是墙时，向四周找最近的空位 */
	private int freeCell( int cx, int cy ) {
		int h = Dungeon.level.height();
		int best = -1;
		float bestDist = Float.MAX_VALUE;

		for ( int y = Math.max( 0, cy - 3 ); y <= Math.min( h - 1, cy + 3 ); y++ ) {
			for ( int x = Math.max( 0, cx - 3 ); x <= Math.min( width - 1, cx + 3 ); x++ ) {
				int cell = y * width + x;
				if ( Dungeon.level.solid[cell] || Actor.findChar( cell ) != null ) continue;
				float d = PointF.distance( new PointF( cx, cy ), new PointF( x, y ) );
				if ( d < bestDist ) {
					bestDist = d;
					best = cell;
				}
			}
		}

		return best != -1 ? best : egg.pos;
	}

	/** 撞墙反馈：冲击波大小、震屏强度、音量都随撞击速度缩放 */
	private void impactFx( PointF at, float speed ) {
		// 用初速量级做归一化：满初速约 31 格/秒
		float f = GameMath.gate( 0.15f, speed / 28f, 1f );

		int cell = ((int) Math.floor( at.y )) * width + ((int) Math.floor( at.x ));
		if ( cell >= 0 && cell < Dungeon.level.length() ) {
			WandOfBlastWave.BlastWave.blast( cell, 1.2f + 2.4f * f );
		}

		PixelScene.shake( 1, 0.15f + 0.55f * f );
		// 撞墙时奶蛋喊「安迪～」，音量随撞击速度缩放
		Sample.INSTANCE.play( Assets.Sounds.NAIDAN_ANDY, 0.35f + 0.65f * f, 1f );
	}

	/** 撞上生物：按撞击速度结算物理伤害（玩家和奶蛋自己免疫） */
	private void hitChar( Char ch, float speed ) {
		if ( ch == Dungeon.hero || ch == egg || !ch.isAlive() ) return;

		// 伤害 = MAX_IMPACT_DAMAGE × (speed / 满初速)，下限 1
		int dmg = Math.max( 1, Math.round( MAX_IMPACT_DAMAGE * GameMath.gate( 0f, speed / FULL_SPEED, 1f ) ) );
		DeviceCompat.log( "Naidan", String.format(
				"hit %s at speed=%.1f -> dmg=%d", ch.name(), speed, dmg ) );

		DamagePipeline.apply( ch, new DamageInfo( dmg, DamageType.PHYSICAL, null, null,
				new MilkImpact() ) );

		// 被撞者惊醒并锁定奶蛋（sleeping → hunting）
		if ( ch instanceof Mob ) {
			((Mob) ch).aggro( egg );
		}
	}

	/** 奶蛋撞击的伤害来源标记（参照 WandOfBlastWave.Knockback 的空标记类） */
	public static class MilkImpact {}

	/** 地图实心测试（格坐标）：蓄力发射与 V 键快速发射共用 */
	public static NaidanBallistics.SolidTest solidTest() {
		return ( cx, cy ) -> {
			int w = Dungeon.level.width();
			int h = Dungeon.level.height();
			if ( cx < 0 || cy < 0 || cx >= w || cy >= h ) return true;
			return Dungeon.level.solid[cy * w + cx];
		};
	}

	private static PointF cellToWorld( float x, float y ) {
		final float size = DungeonTilemap.SIZE;
		return new PointF( (x + 0.5f) * size, (y + 0.5f) * size );
	}

	/** 驱动滚动的视觉对象（本身不画东西，只借 update 每帧推进） */
	private class Effect extends Visual {

		private float elapsed;
		private float prevElapsed;
		private int lastBounce;
		private final HashMap<Char, Integer> hitCount = new HashMap<>();

		public Effect() {
			super( 0, 0, 0, 0 );
			elapsed = 0f;
			prevElapsed = 0f;
			lastBounce = 0;

			// MobSprite 的旋转原点默认在脚底，拿它转会绕脚底甩圈；滚动要绕贴图中心自转
			if ( egg.sprite != null ) {
				egg.sprite.origin.set( egg.sprite.width() / 2f, egg.sprite.height() / 2f );
			}
			if ( egg.sprite != null && egg.sprite.parent != null ) {
				egg.sprite.parent.add( this );
			}
		}

		@Override
		public void update() {
			super.update();

			elapsed += Game.elapsed;
			float t = Math.min( elapsed, duration );

			// 按时间戳定位所在线段（times 与 path 等长）
			int seg = 0;
			while ( seg < times.size() - 2 && t > times.get( seg + 1 ) ) {
				seg++;
			}

			PointF a = path.get( seg );
			PointF b = path.get( Math.min( seg + 1, path.size() - 1 ) );
			float t0 = times.get( seg );
			float t1 = times.get( Math.min( seg + 1, times.size() - 1 ) );
			float f = GameMath.gate( 0f, (t - t0) / Math.max( 1e-3f, t1 - t0 ), 1f );

			PointF world = cellToWorld( a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f );

			egg.sprite.x = world.x - egg.sprite.width() / 2f;
			egg.sprite.y = world.y - egg.sprite.height() / 2f;

			// 自转角速度跟随当前线速度（段长 / 段时长），停下自然不转
			float segLen = PointF.distance( a, b );
			float segSpeed = segLen / Math.max( 1e-3f, t1 - t0 );
			float degPerSec = Math.min( MAX_ROLL_SPEED,
					segSpeed / (2f * (float) Math.PI * Math.max( 0.05f, radiusCells )) * 360f );
			egg.sprite.angle += degPerSec * Game.elapsed;

			// 进入新线段（= 撞墙反弹）时给反馈：撞击速度 = 上一段结束时的解析速度
			// （回收模式静音：不震屏、不冲击波、不喊安迪）
			if ( !recallMode && seg > lastBounce ) {
				lastBounce = seg;
				float prevDur = Math.max( 1e-3f, times.get( seg ) - times.get( seg - 1 ) );
				float impactSpeed = Math.max( 0f, speeds.get( seg - 1 ) - NaidanBallistics.FRICTION * prevDur );
				impactFx( a, impactSpeed );
			}

			// 回收模式：按当前时间点做接触判定（世界坐标距离，贴角/贴边不漏）。
			// 帧步长最大约 0.52 格 < 2×CONTACT_DIST(1.3 格)，逐帧采样不会隧穿透漏。
			if ( recallMode ) {
				PointF pos = pathPointAt( t );
				PointF heroC = heroCellCenter();
				float dxp = pos.x - heroC.x;
				float dyp = pos.y - heroC.y;
				if ( dxp * dxp + dyp * dyp <= CONTACT_DIST * CONTACT_DIST ) {
					killAndErase();
					vanishRecall( false );
					return;
				}
			} else {
				// 普通模式：帧间扫过的格子结算伤害（帧率无关）
				ArrayList<Integer> cells = cellsBetween( prevElapsed, t );
				for ( int i = 0; i < cells.size(); i++ ) {
					Char ch = Actor.findChar( cells.get( i ) );
					if ( ch == null || ch == Dungeon.hero || ch == egg ) continue;
					int hits = hitCount.getOrDefault( ch, 0 );
					if ( hits >= MAX_HITS_PER_CHAR ) continue;
					hitCount.put( ch, hits + 1 );
					float vAt = Math.max( 0f, speeds.get( Math.min( seg, speeds.size() - 1 ) )
							- NaidanBallistics.FRICTION * Math.max( 0f, t - times.get( seg ) ) );
					hitChar( ch, vAt );
				}
			}

			// 回收模式超时兜底：路径可能被墙挡住绕远，超时强制消散，避免永远滚不完
			if ( recallMode && elapsed >= RECALL_MAX_TIME ) {
				killAndErase();
				vanishRecall( true );
				return;
			}

			prevElapsed = elapsed;
			if ( elapsed >= duration ) {
				killAndErase();
				land();
			}
		}
	}

	/** t 时刻蛋在路径上的位置（格坐标）：定位所在线段后按时间戳插值，与 Effect.update 同一算法 */
	private PointF pathPointAt( float t ) {
		int seg = 0;
		while ( seg < times.size() - 2 && t > times.get( seg + 1 ) ) seg++;
		PointF a = path.get( seg );
		PointF b = path.get( Math.min( seg + 1, path.size() - 1 ) );
		float t0 = times.get( seg );
		float t1 = times.get( Math.min( seg + 1, times.size() - 1 ) );
		float f = GameMath.gate( 0f, (t - t0) / Math.max( 1e-3f, t1 - t0 ), 1f );
		return new PointF( a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f );
	}

	/** 英雄所在格的中心（弹道坐标）：路径点 (cx,cy) 即格 (cx,cy) 的中心（cellToWorld 负责 +0.5 换算成像素），这里不能再 +0.5 */
	private PointF heroCellCenter() {
		return new PointF( Dungeon.hero.pos % width, Dungeon.hero.pos / width );
	}

	/** 时间戳区间 (t0, t1] 扫过的格子（按时间顺序，跨多段时每段至少收一个格） */
	private ArrayList<Integer> cellsBetween( float t0, float t1 ) {
		ArrayList<Integer> result = new ArrayList<>();
		if ( t1 <= t0 ) return result;
		int seg = 0;
		while ( seg < times.size() - 2 && t0 > times.get( seg + 1 ) ) seg++;
		while ( seg < times.size() - 1 && times.get( seg ) < t1 ) {
			float segStart = Math.max( t0, times.get( seg ) );
			float segEnd = Math.min( t1, times.get( seg + 1 ) );
			if ( segEnd > segStart ) {
				PointF p0 = path.get( seg );
				PointF p1 = path.get( seg + 1 );
				float dur = Math.max( 1e-3f, times.get( seg + 1 ) - times.get( seg ) );
				float f0 = (segStart - times.get( seg )) / dur;
				float f1 = (segEnd - times.get( seg )) / dur;
				addCell( result, p0.x + (p1.x - p0.x) * f0, p0.y + (p1.y - p0.y) * f0 );
				addCell( result, p0.x + (p1.x - p0.x) * f1, p0.y + (p1.y - p0.y) * f1 );
			}
			seg++;
		}
		return result;
	}

	private void addCell( ArrayList<Integer> result, float x, float y ) {
		int cell = ((int) y) * width + ((int) x);
		if ( !result.isEmpty() && result.get( result.size() - 1 ) == cell ) return;
		result.add( cell );
	}
}
