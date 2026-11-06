/*
 * Radish Pixel Dungeon — 奶蛋滚动
 *
 * 参考《植物大战僵尸》坚果保龄球：贴着地面滚，边走边自转，撞墙非弹性反弹后继续滚。
 * 路径由 NaidanBallistics 以时间域模拟给出（含每个顶点的时间戳），
 * 这里只做「按时间采样 + 播放」：速度天然带摩擦衰减，撞墙时速度掉 30%。
 * 撞墙瞬间播放冲击波特效、画面震动与砸地音效。
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.naidan;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NaidanEgg;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class NaidanFlight extends Actor {

	/** 自转观感上限（度/秒） */
	private static final float MAX_ROLL_SPEED = 1080f;

	private final NaidanEgg egg;
	private final ArrayList<PointF> path;
	private final ArrayList<Float> times;
	private final float duration;
	private final float radiusCells;

	{
		actPriority = VFX_PRIO + 11;
	}

	public NaidanFlight( NaidanEgg egg, NaidanBallistics.Result result, float radiusCells ) {
		this.egg = egg;
		this.path = result.path;
		this.times = result.times;
		this.duration = Math.max( 0.05f, result.duration );
		this.radiusCells = radiusCells;
	}

	@Override
	protected boolean act() {
		Actor.remove( this );

		if ( egg == null || !egg.isAlive() || egg.sprite == null || egg.sprite.parent == null ) {
			DeviceCompat.log( "Naidan", "flight aborted (egg/sprite gone)" );
			return true;
		}

		DeviceCompat.log( "Naidan", String.format( "flight start dur=%.2fs bounces=%d pts=%d",
				duration, path.size() - 2, path.size() ) );
		new Effect();
		return false;
	}

	/** 滚完落地：位置对齐到可站的格子，但画面保持滚停时的姿态，不回格心、不归零 */
	private void land() {
		PointF end = path.get( path.size() - 1 );
		int cell = freeCell( (int) Math.floor( end.x ), (int) Math.floor( end.y ) );

		egg.pos = cell;
		// 刻意不调用 sprite.place(cell)、不重置 angle、不还原脚底原点：
		// 保留最后一帧的像素位置与朝向，看起来就是「恰好滚到这里停住」。
		Dungeon.level.occupyCell( egg );
		DeviceCompat.log( "Naidan", "land at " + cell + " (" + (cell % Dungeon.level.width()) + "," + (cell / Dungeon.level.width()) + ")"
				+ " visual=" + (int) egg.sprite.x + "," + (int) egg.sprite.y );
		if ( Dungeon.level.heroFOV[cell] ) {
			egg.sprite.flash();
		}

		Actor.remove( this );
		next();
	}

	/** 落点被占或是墙时，向四周找最近的空位 */
	private int freeCell( int cx, int cy ) {
		int w = Dungeon.level.width();
		int h = Dungeon.level.height();

		int best = -1;
		float bestDist = Float.MAX_VALUE;

		for ( int y = Math.max( 0, cy - 3 ); y <= Math.min( h - 1, cy + 3 ); y++ ) {
			for ( int x = Math.max( 0, cx - 3 ); x <= Math.min( w - 1, cx + 3 ); x++ ) {
				int cell = y * w + x;
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
		// 用初速量级做归一化：满蓄力初速约 31 格/秒
		float f = GameMath.gate( 0.15f, speed / 28f, 1f );

		int w = Dungeon.level.width();
		int cell = ((int) Math.floor( at.y )) * w + ((int) Math.floor( at.x ));
		if ( cell >= 0 && cell < Dungeon.level.length() ) {
			WandOfBlastWave.BlastWave.blast( cell, 1.2f + 2.4f * f );
		}

		PixelScene.shake( 1, 0.15f + 0.55f * f );
		Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 0.35f + 0.65f * f, 1f );
	}

	private static PointF cellToWorld( float x, float y ) {
		final float size = DungeonTilemap.SIZE;
		return new PointF( (x + 0.5f) * size, (y + 0.5f) * size );
	}

	/** 驱动滚动的视觉对象（本身不画东西，只借 update 每帧推进） */
	private class Effect extends Visual {

		private float elapsed;
		private int lastBounce;

		public Effect() {
			super( 0, 0, 0, 0 );
			elapsed = 0f;
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

			// 进入新线段（= 撞墙反弹）时给反馈，强度按撞击瞬间的速度缩放
			if ( seg > lastBounce ) {
				lastBounce = seg;
				float prevLen = PointF.distance( path.get( seg - 1 ), path.get( seg ) );
				float prevDur = Math.max( 1e-3f, times.get( seg ) - times.get( seg - 1 ) );
				impactFx( a, prevLen / prevDur );
			}

			if ( elapsed >= duration ) {
				killAndErase();
				land();
			}
		}
	}
}
