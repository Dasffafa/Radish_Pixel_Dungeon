package com.shatteredpixel.shatteredpixeldungeon.custom.breakball;

import com.watabou.utils.PointF;

import java.util.ArrayList;

public final class BreakBallBallistics {

	public static final float MAX_CELLS = 30f;
	public static final float FRICTION = 16f;
	public static final float BOUNCE_KEEP = 0.7f;
	public static final float WALL_PROBE = 0.12f;
	public static final float WALL_PROBE_NORTH = 0.04f;
	public static final float MIN_BOUNCE_SPEED = 5f;
	public static final float REF_MASS = 14f;
	public static final float REF_DAMAGE = 15f;
	public static final float FULL_SPEED = 31f;
	public static final float MAX_SPEED = 55f;

	private static final float DT = 1f / 240f;
	private static final float MIN_SPEED = 0.6f;
	private static final int MAX_BOUNCES = 64;
	private static final float MAX_TIME = 12f;

	public interface SolidTest {
		boolean solid( int cellX, int cellY );
	}

	public static final class Result {
		public final ArrayList<PointF> path;
		public final ArrayList<Float> times;
		public final ArrayList<Float> speeds;
		public final float duration;
		public final float distance;
		public final float speedFactor;
		public final float initialSpeed;
		public final int bounces;

		Result( ArrayList<PointF> path, ArrayList<Float> times, ArrayList<Float> speeds,
				float duration, float distance, float speedFactor, float initialSpeed, int bounces ) {
			this.path = path;
			this.times = times;
			this.speeds = speeds;
			this.duration = duration;
			this.distance = distance;
			this.speedFactor = speedFactor;
			this.initialSpeed = initialSpeed;
			this.bounces = bounces;
		}
	}

	// 初速由蓄力、武器伤害与质量共同决定：质量越大越难击飞，武器伤害越高初速越快
	public static float computeInitialSpeed( float charge, float speedFactor, float weaponDamage, float mass ) {
		float m = Math.max( 1f, mass );
		float dmgFactor = (float) Math.sqrt( Math.max( 1f, weaponDamage ) / REF_DAMAGE );
		float speed = FULL_SPEED * clamp01( charge ) * speedFactor * (REF_MASS / m) * dmgFactor;
		return Math.min( MAX_SPEED, speed );
	}

	public static Result compute( float heroX, float heroY, float ballX, float ballY,
			float aimX, float aimY, float charge, float ballRadius,
			float weaponDamage, float mass, SolidTest solid ) {

		float aimLen = (float) Math.hypot( aimX, aimY );
		if ( aimLen < 1e-4f ) return null;

		float dirX = aimX / aimLen;
		float dirY = aimY / aimLen;

		float relX = ballX - heroX;
		float relY = ballY - heroY;

		if ( relX * dirX + relY * dirY <= 0f ) return null;

		float perp = relX * -dirY + relY * dirX;
		float offset = Math.abs( perp );
		if ( offset >= ballRadius ) return null;

		float theta = (float) Math.asin( offset / ballRadius );
		float side = perp >= 0 ? 1f : -1f;
		double normalAngle = Math.atan2( dirY, dirX ) + side * theta;
		float velX = (float) Math.cos( normalAngle );
		float velY = (float) Math.sin( normalAngle );

		float speedFactor = (float) Math.sqrt( Math.max( 0f, 1f - (offset / ballRadius) * (offset / ballRadius) ) );
		float v0 = computeInitialSpeed( charge, speedFactor, weaponDamage, mass );

		return simulateWithSpeed( ballX, ballY, velX, velY, v0, speedFactor, solid );
	}

	public static Result computeStraight( float ballX, float ballY,
			float aimX, float aimY, float charge,
			float weaponDamage, float mass, SolidTest solid ) {

		float aimLen = (float) Math.hypot( aimX, aimY );
		if ( aimLen < 1e-4f ) return null;

		float v0 = computeInitialSpeed( charge, 1f, weaponDamage, mass );
		return simulateWithSpeed( ballX, ballY, aimX / aimLen, aimY / aimLen, v0, 1f, solid );
	}

	public static Result computeRecall( float ballX, float ballY, float heroX, float heroY, SolidTest solid ) {
		return computeRecall( ballX, ballY, heroX, heroY, 0f, solid );
	}

	public static Result computeRecall( float ballX, float ballY, float heroX, float heroY,
			float extraBudget, SolidTest solid ) {

		float dx = heroX - ballX;
		float dy = heroY - ballY;
		float len = (float) Math.hypot( dx, dy );
		if ( len < 1e-4f ) return null;

		return simulate( ballX, ballY, dx / len, dy / len, len + extraBudget, 1f, solid );
	}

	private static Result simulate( float ballX, float ballY, float dirX, float dirY,
			float budget, float speedFactor, SolidTest solid ) {
		if ( budget < 0.05f ) {
			return simulateWithSpeed( ballX, ballY, dirX, dirY, 0f, speedFactor, solid );
		}
		float speed = (float) Math.sqrt( 2f * FRICTION * budget );
		return simulateWithSpeed( ballX, ballY, dirX, dirY, speed, speedFactor, solid );
	}

	private static Result simulateWithSpeed( float ballX, float ballY, float dirX, float dirY,
			float speed, float speedFactor, SolidTest solid ) {

		ArrayList<PointF> path = new ArrayList<>();
		ArrayList<Float> times = new ArrayList<>();
		ArrayList<Float> speeds = new ArrayList<>();
		path.add( new PointF( ballX, ballY ) );
		times.add( 0f );

		if ( speed < MIN_SPEED ) {
			return new Result( path, times, speeds, 0f, 0f, speedFactor, 0f, 0 );
		}

		float vx = dirX * speed;
		float vy = dirY * speed;

		float x = ballX;
		float y = ballY;
		float t = 0f;
		float travelled = 0f;
		int bounces = 0;
		float segStartSpeed = speed;

		while ( t < MAX_TIME && bounces < MAX_BOUNCES ) {

			float curSpeed = (float) Math.hypot( vx, vy );
			if ( curSpeed < MIN_SPEED ) break;

			float nx = x + vx * DT;
			float ny = y + vy * DT;

			boolean hitX = solid.solid( cellOf( nx + Math.signum( vx ) * WALL_PROBE ), cellOf( y ) );
			float probeY = vy < 0f ? WALL_PROBE_NORTH : WALL_PROBE;
			boolean hitY = solid.solid( cellOf( x ), cellOf( ny + Math.signum( vy ) * probeY ) );

			if ( hitX || hitY ) {
				if ( curSpeed < MIN_BOUNCE_SPEED ) {
					break;
				}
				if ( hitX ) vx = -vx * BOUNCE_KEEP;
				if ( hitY ) vy = -vy * BOUNCE_KEEP;
				path.add( new PointF( x, y ) );
				times.add( t );
				speeds.add( segStartSpeed );
				segStartSpeed = (float) Math.hypot( vx, vy );
				bounces++;
				continue;
			}

			x = nx;
			y = ny;
			travelled += curSpeed * DT;
			t += DT;

			float next = Math.max( 0f, curSpeed - FRICTION * DT );
			vx = vx / curSpeed * next;
			vy = vy / curSpeed * next;
		}

		PointF last = path.get( path.size() - 1 );
		if ( PointF.distance( last, new PointF( x, y ) ) > 1e-4f ) {
			path.add( new PointF( x, y ) );
			times.add( t );
			speeds.add( segStartSpeed );
		}

		return new Result( path, times, speeds, t, travelled, speedFactor,
				speed, bounces );
	}

	private static int cellOf( float v ) {
		return (int) Math.floor( v );
	}

	private static float clamp01( float v ) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}
}
