package com.shatteredpixel.shatteredpixeldungeon.custom.breakball;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BreakBallEntity;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamagePipeline;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
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
import java.util.HashMap;

public class BreakBallFlight extends Actor {

	private static final float MAX_ROLL_SPEED = 720f;
	private static final int MAX_IMPACT_DAMAGE = 30;
	private static final float FULL_SPEED = 31f;
	private static final int MAX_HITS_PER_CHAR = 2;

	private final BreakBallEntity entity;
	private final ArrayList<PointF> path;
	private final ArrayList<Float> times;
	private final ArrayList<Float> speeds;
	private final float duration;
	private final float radiusCells;
	private final int width;

	private static BreakBallFlight active;

	private final boolean recallMode;
	private static final float RECALL_MAX_TIME = 6f;
	private static final float CONTACT_DIST = 0.65f;

	public static boolean isFlying() {
		return active != null;
	}

	{
		actPriority = VFX_PRIO + 11;
	}

	public BreakBallFlight( BreakBallEntity entity, BreakBallBallistics.Result result, float radiusCells ) {
		this( entity, result, radiusCells, false );
	}

	public BreakBallFlight( BreakBallEntity entity, BreakBallBallistics.Result result, float radiusCells, boolean recallMode ) {
		this.entity = entity;
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

		if ( entity == null || !entity.isAlive() || entity.sprite == null || entity.sprite.parent == null ) {
			active = null;
			return true;
		}

		new Effect();
		return false;
	}

	private void land() {
		if ( recallMode ) {
			vanishRecall( true );
			return;
		}

		PointF end = path.get( path.size() - 1 );
		int cell = freeCell( (int) Math.floor( end.x ), (int) Math.floor( end.y ) );

		entity.pos = cell;
		if ( entity.isBrokenAfterFlight() ) {
			entity.shatter();
		} else {
			Dungeon.level.occupyCell( entity );
			if ( Dungeon.level.heroFOV[cell] ) {
				entity.sprite.flash();
			}
		}

		Actor.remove( this );
		active = null;
		next();
	}

	private void vanishRecall( boolean fallback ) {
		DeviceCompat.log( "BreakBall", "recall vanish at hero, fallback=" + fallback );
		entity.collectToHero();
		Actor.remove( this );
		active = null;
		next();
	}

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

		return best != -1 ? best : entity.pos;
	}

	private void impactFx( PointF at, float speed ) {
		float f = GameMath.gate( 0.15f, speed / 28f, 1f );

		int cell = ((int) Math.floor( at.y )) * width + ((int) Math.floor( at.x ));
		if ( cell >= 0 && cell < Dungeon.level.length() ) {
			WandOfBlastWave.BlastWave.blast( cell, 1.2f + 2.4f * f );
		}

		PixelScene.shake( 1, 0.15f + 0.55f * f );
		Sample.INSTANCE.play( Assets.Sounds.BREAK_BALL_ANDY, 0.35f + 0.65f * f, 1f );
	}

	private void hitChar( Char ch, float speed ) {
		if ( ch == Dungeon.hero || ch == entity || !ch.isAlive() ) return;

		// 碰撞伤害与动量（质量 × 速度）成正比
		float massFactor = entity.mass() / BreakBallBallistics.REF_MASS;
		int dmg = Math.max( 1, Math.round( MAX_IMPACT_DAMAGE * massFactor * (speed / FULL_SPEED) ) );
		DamagePipeline.apply( ch, new DamageInfo( dmg, DamageType.PHYSICAL, null, null,
				new BreakBallImpact() ) );

		if ( ch instanceof Mob ) {
			((Mob) ch).aggro( entity );
		}
	}

	public static class BreakBallImpact {}

	public static BreakBallBallistics.SolidTest solidTest() {
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

			if ( entity.sprite != null ) {
				entity.sprite.origin.set( entity.sprite.width() / 2f, entity.sprite.height() / 2f );
			}
			if ( entity.sprite != null && entity.sprite.parent != null ) {
				entity.sprite.parent.add( this );
			}
		}

		@Override
		public void update() {
			super.update();

			elapsed += Game.elapsed;
			float t = Math.min( elapsed, duration );

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

			entity.sprite.x = world.x - entity.sprite.width() / 2f;
			entity.sprite.y = world.y - entity.sprite.height() / 2f;

			float segLen = PointF.distance( a, b );
			float segSpeed = segLen / Math.max( 1e-3f, t1 - t0 );
			float degPerSec = Math.min( MAX_ROLL_SPEED,
					segSpeed / (2f * (float) Math.PI * Math.max( 0.05f, radiusCells )) * 360f );
			entity.sprite.angle += degPerSec * Game.elapsed;

			if ( !recallMode && seg > lastBounce ) {
				lastBounce = seg;
				float prevDur = Math.max( 1e-3f, times.get( seg ) - times.get( seg - 1 ) );
				float impactSpeed = Math.max( 0f, speeds.get( seg - 1 ) - BreakBallBallistics.FRICTION * prevDur );
				impactFx( a, impactSpeed );
			}

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
				ArrayList<Integer> cells = cellsBetween( prevElapsed, t );
				for ( int i = 0; i < cells.size(); i++ ) {
					Char ch = Actor.findChar( cells.get( i ) );
					if ( ch == null || ch == Dungeon.hero || ch == entity ) continue;
					int hits = hitCount.getOrDefault( ch, 0 );
					if ( hits >= MAX_HITS_PER_CHAR ) continue;
					hitCount.put( ch, hits + 1 );
					float vAt = Math.max( 0f, speeds.get( Math.min( seg, speeds.size() - 1 ) )
							- BreakBallBallistics.FRICTION * Math.max( 0f, t - times.get( seg ) ) );
					hitChar( ch, vAt );
				}
			}

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

	private PointF heroCellCenter() {
		return new PointF( Dungeon.hero.pos % width, Dungeon.hero.pos / width );
	}

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
