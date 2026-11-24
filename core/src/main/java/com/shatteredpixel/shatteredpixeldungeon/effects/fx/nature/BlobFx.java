/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Warped Pixel Dungeon
 * Copyright (C) 2026 Gabriel Duarte Guerra (gabriwar)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.nature;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFactories;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.particles.Emitter;

/**
 * The area's blobs' particles, each blob's factory swapped in its use() (its emitter's one draw of
 * the game's Random unchanged): what a cell of it shows each tick, by chances that keep each cell
 * to a few particles (the per-cell numbers below) and each blob to its cap. Light goes to the
 * light layer (a matter blob's through the scene's holder of loose light); FxRandom throughout.
 * The rates are per emit; a blob's emitter emits once per visible cell per its interval.
 */
public final class BlobFx {

	private BlobFx(){}

	/** The growth blobs' interval (Regrowth, growth water), as their use() pours. */
	//验收用计数（selftest 的 Z_Nature 场景会看）
	public static int grown, foliageTicks, wellTicks;

	public static final float GROWTH_EVERY = 0.2f;
	/** The most particles a growth blob keeps. */
	public static final int GROWTH_CAP = 300;

	private static float cellX( int cell ){
		return (cell % Dungeon.level.width()) * DungeonTilemap.SIZE;
	}

	private static float cellY( int cell ){
		return (cell / Dungeon.level.width()) * DungeonTilemap.SIZE;
	}

	/**
	 * A factory of a blob whose volume is a count of turns or a constant (a wall, a well, a
	 * portal, a patch of light), not a cloud's thickness: every visible cell emits each tick (no
	 * thinning by volume, as a gas's), and the factory's own chances and clocks set its rates.
	 */
	public abstract static class Steady extends Emitter.Factory implements FxBudget.Prioritized {
		@Override
		public int priority(){
			return FxBudget.P1;
		}

		@Override
		public float density( int cur ){
			return 1f;
		}
	}

	/**
	 * Seconds since something last happened at a cell, by channel, for parts that come at their
	 * own pace however rarely their blob emits (a blob of one unit emits a tick in five): CELLS
	 * cells remembered (or as many as it is made for), the oldest forgotten. No allocation. Render
	 * thread.
	 */
	static final class Clock {
		static final int CELLS = 8;
		private final int[] cells;
		private final float[][] at;
		private int next;

		Clock( int channels ){
			this( channels, CELLS );
		}

		Clock( int channels, int cells ){
			this.cells = new int[cells];
			java.util.Arrays.fill( this.cells, -1 );
			at = new float[cells][channels];
		}

		private int slot( int cell ){
			for (int i = 0; i < cells.length; i++) if (cells[i] == cell) return i;
			int i = next;
			next = (next + 1) % cells.length;
			cells[i] = cell;
			java.util.Arrays.fill( at[i], -1e9f );
			return i;
		}

		/** Seconds since `channel` last marked at `cell` (a very long time if never, or if the
		 *  clock went back: a test's or a tool's scene starting again at 0). */
		float since( int cell, int channel ){
			float d = Game.timeTotal - at[slot( cell )][channel];
			return d < 0 ? 1e9f : d;
		}

		void mark( int cell, int channel ){
			at[slot( cell )][channel] = Game.timeTotal;
		}

		/** A time kept on a channel (when something ends, say). */
		float get( int cell, int channel ){
			return at[slot( cell )][channel];
		}

		void set( int cell, int channel, float time ){
			at[slot( cell )][channel] = time;
		}
	}

	/**
	 * How many ticks an emit of this cell stands for: a cell holding under FADE units of blob
	 * emits on only cur / FADE of its ticks (BlobEmitter thins a cloud running out), which a blob
	 * of one or two units (a garden's foliage, a potion's sunlight) always is; its factory makes
	 * up the rest so its rates hold. A whole number, on average FADE / cur.
	 */
	public static int times( BlobEmitter.Cell c ){
		if (c == null || c.cur >= FADE || c.cur <= 0) return 1;
		float k = FADE / (float)c.cur;
		int n = (int)k;
		return n + (FxRandom.Float() < k - n ? 1 : 0);
	}

	/** BlobEmitter's FADE_VOLUME (private there: a kit request to open it). */
	static final int FADE = 5;

	// ------------------------------------------------------------------ growth

	/**
	 * Regrowth (water false) or growth water (true), one per blob (its cap is its own): leaves
	 * thrown up (by the blob's thickness, the emitter's density), pollen rising and swaying, dew
	 * glinting by day; growth water's drips falling a few px to the floor or the water and its wet
	 * glints. At most GROWTH_CAP alive; sprouts and buds are the growth's own (GrowthPulse).
	 */
	public static Emitter.Factory growth( final boolean water ){
		return new Emitter.Factory(){
			private float countedAt = -1;
			private int alive;

			@Override
			public void emit( Emitter e, int index, float x, float y ){
				grown++;
				if (countedAt != Game.timeTotal){
					countedAt = Game.timeTotal;
					alive = e.countLiving();
				}
				if (alive >= GROWTH_CAP) return;
				float r = FxRandom.Float();
				//a leaf in three (growth water: four), about 1.5 a cell a second
				if (r < (water ? 0.25f : 0.3f)){
					LeafParticle.color1 = LeafParticle.color2 = Nature.levelLeaf();
					((LeafParticle) e.recycle( LeafParticle.class )).reset( x, y );
					alive++;
					return;
				}
				r -= water ? 0.25f : 0.3f;
				if (water){
					//a drip in seven, a wet glint in fifteen
					if (r < 0.15f){
						drip( e, x, y );
						alive++;
					} else if (r < 0.15f + 1 / 15f){
						glint( x, y, Nature.DRIP, 0.4f );
					}
				} else {
					//pollen a cell in three a second, dew a cell in four every two seconds by day
					if (r < 1 / 15f){
						pollen( x, y );
					} else if (r < 1 / 15f + 1 / 40f && Nature.day()){
						glint( x, y, Nature.DEW, 0.3f );
					}
				}
			}
		};
	}

	//a drip off the growth: falling 6-10 px to a small splat, or a ring on water
	private static void drip( Emitter e, float x, float y ){
		if (!FxBudget.keep( FxBudget.P3, x, y )) return;
		Nature.make( e, x, y ).look( FxFrames.DROP_2 ).orient().paint( Nature.DRIP ).vel( 0, 4f ).gravity( 240f )
				.ground( y + FxRandom.Float( 6f, 10f ), FxParticle.RING, 0 ).landDry( FxParticle.WET ).strength( 0.1f )
				.life( 1f ).alpha( 1f, 0, 0.05f ).priority( FxBudget.P3 );
	}

	//pollen: a dot of gold rising slowly and swaying, 1.5 s
	private static void pollen( float x, float y ){
		Emitter glow = Nature.glow( null );
		if (glow == null || !FxBudget.keep( FxBudget.P3, x, y )) return;
		Nature.make( glow, x, y ).look( FxFrames.MOTE_1 ).paint( Nature.POLLEN ).light( true )
				.vel( 0, -FxRandom.Float( 3f, 6f ) ).sway( FxRandom.Float( 3f, 5f ), FxRandom.Float( 2f, 3f ) )
				.life( 1.5f ).alpha( 0.7f, 0.3f, 0.5f ).snap().priority( FxBudget.P3 );
	}

	//a glint: a plus opening and closing
	private static void glint( float x, float y, int color, float peak ){
		Emitter glow = Nature.glow( null );
		if (glow == null || !FxBudget.keep( FxBudget.P3, x, y )) return;
		Nature.make( glow, x, y ).look( FxFrames.PLUS_3 ).paint( color ).light( true )
				.life( 0.35f ).alpha( peak, 0.12f, 0.2f ).snap().priority( FxBudget.P3 );
	}

	// ------------------------------------------------------------------ foliage

	/** Foliage's interval: one emit a cell every FOLIAGE_EVERY s; its warm shafts' peak by day
	 *  (ambient light: under 60% of an impact's). */
	public static final float FOLIAGE_EVERY = 0.6f, FOLIAGE_PEAK = 0.35f;

	/**
	 * Foliage: a warm shaft of light a cell every 1.3 s (the moon's at night), flecks of sun on the
	 * floor drifting a px or two a second for 2.5-3.5 s (0.12; at night the moon's, half as
	 * bright), seed fluff drifting off a cell in three a second. About four a cell.
	 */
	public static final Emitter.Factory FOLIAGE = new Steady(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			foliageTicks++;
			//as many as the ticks this emit stands for, the later ones elsewhere on the cell
			int n = times( cellOf( index ) );
			for (int i = 0; i < n; i++){
				if (i == 0) once( e, x, y, 0 );
				else once( e, (float)Math.floor( x / 16 ) * 16 + FxRandom.Float( 16 ), (float)Math.floor( y / 16 ) * 16 + FxRandom.Float( 16 ),
						i * FOLIAGE_EVERY );
			}
		}

		//`later`: the tick it stands for, s from now (its light waits that long)
		private void once( Emitter e, float x, float y, float later ){
			boolean night = Nature.night();
			float r = FxRandom.Float();
			if (r < 0.45f){
				Emitter glow = Nature.glow( e );
				if (!FxBudget.keep( FxBudget.P2, x, y )) return;
				ShaftParticle.FACTORY.emit( glow, 0, x, y );
			} else if (r < 0.57f){
				Emitter glow = Nature.glow( e );
				if (!FxBudget.keep( FxBudget.P3, x, y )) return;
				float life = FxRandom.Float( 2.5f, 3.5f );
				Nature.lit( glow, x, y + 4 ).delay( later ).look( FxFrames.Light.POOL_15 ).paint( night ? Nature.MOON : Nature.FLECK )
						.vel( FxRandom.Float( -2f, 2f ), FxRandom.Float( -1f, 1f ) ).life( life )
						.alpha( night ? 0.06f : 0.12f, 1f, 1.2f ).snap().priority( FxBudget.P3 );
			} else if (r < 0.75f){
				if (!FxBudget.keep( FxBudget.P3, x, y )) return;
				Nature.make( e, x, y ).look( FxFrames.MOTE_1 ).paint( Nature.FLUFF )
						.vel( FxRandom.Float( -4f, 4f ), -FxRandom.Float( 2f, 5f ) ).sway( FxRandom.Float( 2f, 4f ), 2f )
						.wind( 0.6f ).life( FxRandom.Float( 1.5f, 2.5f ) ).alpha( 0.8f, 0.3f, 0.5f ).smooth().snap()
						.priority( FxBudget.P3 );
			}
		}
	};

	// ------------------------------------------------------------------ the well of health

	/** The well's interval. */
	public static final float WELL_EVERY = 0.4f;

	/** The well's glints: WELL_GLINTS at most at once, one coming up every GLINT_EVERY s after
	 *  another as places free; its shaft and its cross every SHAFT_EVERY and CROSS_EVERY s. */
	public static final float WELL_GLINT_EVERY = 0.45f, WELL_SHAFT_EVERY = 2.2f, WELL_CROSS_EVERY = 2.4f;
	public static final int WELL_GLINTS = 4;
	//channels: the shaft, the cross, then each glint's end
	private static final Clock WELL_CLOCK = new Clock( 2 + WELL_GLINTS );

	/**
	 * The well of health: caustic glints turning lazily on its water (four at most, each 1.3-1.9 s
	 * past its fade-in, a new one as one goes, coming up 0.45 s apart), a tall shaft of healing light breathing over it (one every 2.2 s, 2.6 s
	 * long, 0.25), the red cross rising now and then. Its rates by its own clock (a well is one unit
	 * of blob: it emits on one tick in five). Sixteen at most.
	 */
	public static final Emitter.Factory WELL = new Steady(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			Emitter glow = Nature.glow( e );
			BlobEmitter.Cell c = cellOf( index );
			int cell = c != null ? c.cell : -2;
			float cx = c != null ? cellX( c.cell ) + 8 : x, cy = c != null ? cellY( c.cell ) + 9 : y;
			//a glint in each place left by one gone, coming up one after another
			int born = 0;
			for (int k = 0; k < WELL_GLINTS; k++){
				if (WELL_CLOCK.get( cell, 2 + k ) > Game.timeTotal || !FxBudget.keep( FxBudget.P3, cx, cy )) continue;
				float in = 0.3f + WELL_GLINT_EVERY * born++, life = in + FxRandom.Float( 1.3f, 1.9f );
				WELL_CLOCK.set( cell, 2 + k, Game.timeTotal + life );
				((EllipseMote) glow.recycle( EllipseMote.class )).round( cx, cy, FxRandom.Float( 3f, 5f ), FxRandom.Float( 1.5f, 2.5f ),
						FxRandom.angle(), FxRandom.sign() * FxRandom.Float( 1f, 1.8f ) )
						.look( FxRandom.chance( 0.6f ) ? FxFrames.PLUS_3 : FxFrames.MOTE_1 ).paint( Nature.HEAL ).light( true )
						.life( life ).alpha( 0.6f, in, 0.5f ).priority( FxBudget.P3 );
			}
			if (WELL_CLOCK.since( cell, 0 ) >= WELL_SHAFT_EVERY && FxBudget.keep( FxBudget.P2, cx, cy )){
				WELL_CLOCK.mark( cell, 0 );
				ShaftParticle.FACTORY.emit( glow, 0, cx, cy + 2 );
			}
			if (WELL_CLOCK.since( cell, 1 ) >= WELL_CROSS_EVERY){
				WELL_CLOCK.mark( cell, 1 );
				Speck.factory( Speck.HEALING ).emit( e, index, x, y );
			}
		}
	};

	//the cell a blob's emit is for, if it is a blob's
	private static BlobEmitter.Cell cellOf( int index ){
		BlobEmitter.Cell c = BlobEmitter.cell();
		Level level = Dungeon.level;
		return c != null && level != null && c.cell >= 0 && c.cell < level.length() ? c : null;
	}

	// ------------------------------------------------------------------ sunlight

	/** Sunlight's interval: it pours while it lasts. */
	public static final float SUN_EVERY = 0.3f;

	/**
	 * Sunlight (and raw sunlight): a white-gold shaft (SHAFT_40, 0.55) a cell in four an emit, a
	 * shimmering glare on the floor (POOL_31, 0.3), heat haze rising and dust glinting. Four a cell.
	 */
	public static final Emitter.Factory SUN = new Steady(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			//as many as the ticks this emit stands for, the later ones elsewhere on the cell
			int n = times( cellOf( index ) );
			for (int i = 0; i < n; i++){
				if (i == 0) once( e, x, y, 0 );
				else once( e, (float)Math.floor( x / 16 ) * 16 + FxRandom.Float( 16 ), (float)Math.floor( y / 16 ) * 16 + FxRandom.Float( 16 ),
						i * SUN_EVERY );
			}
		}

		//`later`: the tick it stands for, s from now (its light waits that long)
		private void once( Emitter e, float x, float y, float later ){
			Emitter glow = Nature.glow( e );
			float r = FxRandom.Float();
			if (r < 0.25f){
				if (!FxBudget.keep( FxBudget.P2, x, y )) return;
				ShaftParticle.FACTORY.emit( glow, 0, x, y );
			} else if (r < 0.37f){
				if (!FxBudget.keep( FxBudget.P2, x, y )) return;
				Nature.lit( glow, x, y + 4 ).delay( later ).flicker( 0.06f, FxRandom.Float( 18f, 24f ) ).look( FxFrames.Light.POOL_31 )
						.paint( Nature.SUN ).life( 2f ).alpha( 0.3f, 0.5f, 0.7f ).snap().priority( FxBudget.P2 );
			} else if (r < 0.52f){
				if (!FxBudget.keep( FxBudget.P3, x, y )) return;
				Nature.make( e, x, y ).look( FxFrames.EDGE_WISP[FxRandom.Int( FxFrames.EDGE_WISP.length )] ).paint( 0xF8F0D8 )
						.vel( 0, -FxRandom.Float( 4f, 7f ) ).turbulence( 1.5f, 4f ).life( 1.5f ).alpha( 0.15f, 0.5f, 0.6f )
						.smooth().priority( FxBudget.P3 );
			} else if (r < 0.82f){
				if (!FxBudget.keep( FxBudget.P3, x, y )) return;
				Nature.make( glow, x, y ).look( FxFrames.MOTE_1 ).paint( 0xFFE8A0 ).light( true )
						.vel( FxRandom.Float( -2f, 2f ), -FxRandom.Float( 1f, 3f ) ).sway( 2f, 2f )
						.life( 0.8f ).alpha( 0.8f, 0.2f, 0.3f ).snap().priority( FxBudget.P3 );
			}
		}

		@Override
		public boolean lightMode(){
			return true;
		}
	};

	// ------------------------------------------------------------------ glowing gas

	/** Glowing gas's interval; lanterns within LANTERN_REACH cells of the hero (the 25 nearest). */
	public static final float GLOW_EVERY = 0.3f;
	public static final int LANTERN_REACH = 2;

	/**
	 * Glowing gas, drawn as light (its blob is emissive): twinkles rising (a cell in two an emit)
	 * and over the cells nearest the hero a warm lantern pool (FFF0A0, 0.15) renewed every 1.2 s.
	 * Five a cell at most.
	 */
	public static final Emitter.Factory GLOW = new Emitter.Factory(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			BlobEmitter.Cell c = cellOf( index );
			if (c != null && index % 4 == 0 && Dungeon.hero != null
					&& Dungeon.level.distance( c.cell, Dungeon.hero.pos ) <= LANTERN_REACH
					&& FxBudget.keep( FxBudget.P2, x, y )){
				Nature.lit( e, cellX( c.cell ) + 8, cellY( c.cell ) + 10 ).look( FxFrames.Light.POOL_15 ).paint( Nature.LANTERN )
						.life( 1.4f ).alpha( 0.15f, 0.35f, 0.45f ).snap().priority( FxBudget.P2 );
			}
			if (FxRandom.chance( 0.5f )) ShaftParticle.FACTORY.emit( e, index, x, y );
		}

		@Override
		public boolean lightMode(){
			return true;
		}
	};

	// ------------------------------------------------------------------ hallowed ground

	/** Hallowed ground's interval. */
	public static final float HALLOWED_EVERY = 0.5f;

	/**
	 * Hallowed ground: a golden sheen on each cell breathing at 0.4 Hz (POOL_15, 0.12, renewed every
	 * 2.5 s), motes rising a cell in two a second, a shaft a cell every two seconds. Three a cell.
	 */
	public static final Emitter.Factory HALLOWED = new Steady(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			Emitter glow = Nature.glow( e );
			BlobEmitter.Cell c = cellOf( index );
			if (c != null && index % 5 == 0 && FxBudget.keep( FxBudget.P3, x, y )){
				Nature.lit( glow, cellX( c.cell ) + 8, cellY( c.cell ) + 10 ).flicker( 0.08f, 0.4f )
						.look( FxFrames.Light.POOL_15 ).paint( Nature.HALO ).life( 2.8f ).alpha( 0.12f, 0.4f, 0.4f )
						.snap().priority( FxBudget.P3 );
			}
			float r = FxRandom.Float();
			if (r < 0.25f){
				if (!FxBudget.keep( FxBudget.P3, x, y )) return;
				Nature.make( glow, x, y ).look( FxFrames.MOTE_1 ).paint( Nature.HOLY ).light( true )
						.vel( FxRandom.Float( -1f, 1f ), -FxRandom.Float( 6f, 10f ) ).life( 1.2f ).alpha( 0.8f, 0.2f, 0.4f )
						.snap().priority( FxBudget.P3 );
			} else if (r < 0.5f && FxBudget.keep( FxBudget.P2, x, y )){
				ShaftParticle.FACTORY.emit( e, index, x, y );
			}
		}
	};

	// ------------------------------------------------------------------ the portal

	/** The portal's interval. */
	public static final float PORTAL_EVERY = 0.25f;

	private static final Emitter.Factory PORTAL_SHAFT = ShaftParticle.FACTORY;

	/** The portal's motes: PORTAL_MOTES at most, coming up PORTAL_MOTE_EVERY s apart as places
	 *  free; its shaft every PORTAL_SHAFT_EVERY s. */
	public static final float PORTAL_MOTE_EVERY = 0.25f, PORTAL_SHAFT_EVERY = 1.5f;
	public static final int PORTAL_MOTES = 6;
	//channels: the shaft, then each mote's end
	private static final Clock PORTAL_CLOCK = new Clock( 1 + PORTAL_MOTES );

	/** A portal: motes going round a 10 x 4 ellipse on its floor (six alive) and a shaft every
	 *  1.5 s, by its own clock (a portal is one unit of blob: it emits on one tick in five). */
	public static final Emitter.Factory PORTAL = new Steady(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			Emitter glow = Nature.glow( e );
			BlobEmitter.Cell c = cellOf( index );
			int cell = c != null ? c.cell : -2;
			float cx = c != null ? cellX( c.cell ) + 8 : x, cy = c != null ? cellY( c.cell ) + 11 : y;
			//a mote in each place left by one gone, coming up one after another
			int born = 0;
			for (int k = 0; k < PORTAL_MOTES; k++){
				if (PORTAL_CLOCK.get( cell, 1 + k ) > Game.timeTotal || !FxBudget.keep( FxBudget.P3, cx, cy )) continue;
				float in = 0.2f + PORTAL_MOTE_EVERY * born++, life = in + 1.2f;
				PORTAL_CLOCK.set( cell, 1 + k, Game.timeTotal + life );
				((EllipseMote) glow.recycle( EllipseMote.class )).round( cx, cy, 5f, 2f, FxRandom.angle(), 2.2f )
						.look( FxFrames.MOTE_2 ).paint( Element.ARCANE.ramp.stop( 2 ) ).light( true )
						.life( life ).alpha( 0.9f, in, 0.4f ).priority( FxBudget.P3 );
			}
			if (PORTAL_CLOCK.since( cell, 0 ) >= PORTAL_SHAFT_EVERY && FxBudget.keep( FxBudget.P2, cx, cy )){
				PORTAL_CLOCK.mark( cell, 0 );
				PORTAL_SHAFT.emit( e, index, cx, cy );
			}
		}

		@Override
		public boolean lightMode(){
			return true;
		}
	};

	// ------------------------------------------------------------------ divine wrath

	/** Holy ground's flames: a holy tongue an emit (one every 0.3 s a cell) and a glint on the
	 *  scorched floor an emit in four. Light; four a cell. */
	public static final Emitter.Factory WRATH = new Emitter.Factory(){
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			Emitter glow = Nature.glow( e );
			FxFactories.tongue( Element.HOLY ).emit( glow, index, x, y + 4 );
			if (index % 4 == 0) FxFactories.glint( Element.HOLY.ember.stop( 0 ) ).emit( glow, index, x + FxRandom.Float( -4f, 4f ), y + 6 );
		}

		@Override
		public boolean lightMode(){
			return true;
		}
	};
}
