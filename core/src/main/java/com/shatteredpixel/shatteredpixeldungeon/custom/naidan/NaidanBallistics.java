/*
 * Radish Pixel Dungeon — 奶蛋「打飞」弹道
 *
 * 时间域物理模拟（不再按距离均分）：
 *  · 起始速度由「满蓄力 30 格」反推：v0 = sqrt(2·a·D)，因此无反弹时恰好滚 30 格；
 *  · 全程受恒定摩擦减速，速度线性衰减到 0 —— 起步快、末尾慢；
 *  · 撞墙为非弹性碰撞：法向/切向速度等比乘以 BOUNCE_KEEP（吸收 30%）；
 *  · 撞墙判定点比球体小（WALL_PROBE），让球在 2.5D 视角下先贴到墙根再弹。
 *
 * 纯函数，不依赖游戏状态，便于离线数值验证。
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.naidan;

import com.watabou.utils.PointF;

import java.util.ArrayList;

public final class NaidanBallistics {

	/** 满蓄力（无反弹）最大行程，格 */
	public static final float MAX_CELLS = 30f;
	/** 摩擦减速度，格/秒² */
	public static final float FRICTION = 16f;
	/** 撞墙后保留的速度比例（吸收 30%） */
	public static final float BOUNCE_KEEP = 0.7f;
	/** 撞墙判定点半径（格）：小于球体半径，避免「还没碰到墙就弹回来」 */
	public static final float WALL_PROBE = 0.12f;
	/** 撞墙时低于这个速度就直接停住，不再反弹（格/秒） */
	public static final float MIN_BOUNCE_SPEED = 5f;

	private static final float DT = 1f / 240f;
	private static final float MIN_SPEED = 0.6f;
	private static final int MAX_BOUNCES = 64;
	private static final float MAX_TIME = 12f;

	/** 地图实心测试（格坐标） */
	public interface SolidTest {
		boolean solid( int cellX, int cellY );
	}

	public static final class Result {
		/** 顶点：起点、各反弹点、终点（格坐标浮点） */
		public final ArrayList<PointF> path;
		/** 每个顶点的时间戳（秒），与 path 等长 */
		public final ArrayList<Float> times;
		/** 总时长（秒） */
		public final float duration;
		/** 总行程（格） */
		public final float distance;
		/** 侧击衰减 cosθ，1 = 正中 */
		public final float speedFactor;
		/** 起始速度（格/秒） */
		public final float initialSpeed;
		/** 反弹次数 */
		public final int bounces;

		Result( ArrayList<PointF> path, ArrayList<Float> times, float duration,
				float distance, float speedFactor, float initialSpeed, int bounces ) {
			this.path = path;
			this.times = times;
			this.duration = duration;
			this.distance = distance;
			this.speedFactor = speedFactor;
			this.initialSpeed = initialSpeed;
			this.bounces = bounces;
		}
	}

	public static Result compute( float heroX, float heroY, float eggX, float eggY,
			float aimX, float aimY, float charge, float ballRadius, SolidTest solid ) {

		float aimLen = (float) Math.hypot( aimX, aimY );
		if ( aimLen < 1e-4f ) return null;

		float dirX = aimX / aimLen;
		float dirY = aimY / aimLen;

		float relX = eggX - heroX;
		float relY = eggY - heroY;

		// 球必须在瞄准方向前方
		if ( relX * dirX + relY * dirY <= 0f ) return null;

		// 有符号垂直偏移（击球点偏移量）
		float perp = relX * -dirY + relY * dirX;
		float offset = Math.abs( perp );
		if ( offset >= ballRadius ) return null;

		// 接触点法线 = 瞄准方向偏转 θ = asin(b/r)
		float theta = (float) Math.asin( offset / ballRadius );
		float side = perp >= 0 ? 1f : -1f;
		double normalAngle = Math.atan2( dirY, dirX ) + side * theta;
		float velX = (float) Math.cos( normalAngle );
		float velY = (float) Math.sin( normalAngle );

		// 光滑球只传递法向分量，速度按 cosθ 衰减
		float speedFactor = (float) Math.sqrt( Math.max( 0f, 1f - (offset / ballRadius) * (offset / ballRadius) ) );
		float budget = MAX_CELLS * clamp01( charge ) * speedFactor;

		ArrayList<PointF> path = new ArrayList<>();
		ArrayList<Float> times = new ArrayList<>();
		path.add( new PointF( eggX, eggY ) );
		times.add( 0f );

		if ( budget < 0.05f ) {
			return new Result( path, times, 0f, 0f, speedFactor, 0f, 0 );
		}

		// 由「匀减速滑行 D 格停下」反推初速：v0 = sqrt(2·a·D)
		float speed = (float) Math.sqrt( 2f * FRICTION * budget );
		float vx = velX * speed;
		float vy = velY * speed;

		float x = eggX;
		float y = eggY;
		float t = 0f;
		float travelled = 0f;
		int bounces = 0;

		while ( t < MAX_TIME && bounces < MAX_BOUNCES ) {

			float curSpeed = (float) Math.hypot( vx, vy );
			if ( curSpeed < MIN_SPEED ) break;

			float nx = x + vx * DT;
			float ny = y + vy * DT;

			boolean hitX = solid.solid( cellOf( nx + Math.signum( vx ) * WALL_PROBE ), cellOf( y ) );
			boolean hitY = solid.solid( cellOf( x ), cellOf( ny + Math.signum( vy ) * WALL_PROBE ) );

			if ( hitX || hitY ) {
				// 速度太低：撞墙直接停住（不反弹、不加反弹顶点）
				if ( curSpeed < MIN_BOUNCE_SPEED ) {
					break;
				}
				// 非弹性：撞到的那一轴等比衰减
				if ( hitX ) vx = -vx * BOUNCE_KEEP;
				if ( hitY ) vy = -vy * BOUNCE_KEEP;
				path.add( new PointF( x, y ) );
				times.add( t );
				bounces++;
				continue;
			}

			x = nx;
			y = ny;
			travelled += curSpeed * DT;
			t += DT;

			// 摩擦：沿运动反方向线性衰减到 0
			float next = Math.max( 0f, curSpeed - FRICTION * DT );
			vx = vx / curSpeed * next;
			vy = vy / curSpeed * next;
		}

		// 终点：与最后一个顶点重合时就不重复记录了（例如恰好停在反弹点上）
		PointF last = path.get( path.size() - 1 );
		if ( PointF.distance( last, new PointF( x, y ) ) > 1e-4f ) {
			path.add( new PointF( x, y ) );
			times.add( t );
		}

		return new Result( path, times, t, travelled, speedFactor,
				(float) Math.sqrt( 2f * FRICTION * budget ), bounces );
	}

	private static int cellOf( float v ) {
		return (int) Math.floor( v );
	}

	private static float clamp01( float v ) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}
}
