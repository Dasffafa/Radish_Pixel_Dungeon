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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.gas;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.WaterFX;
import com.watabou.noosa.particles.Emitter;

/**
 * The details a gas cloud mixes into its bodies: bubbles welling up and popping, motes that
 * twinkle, the confusion's swirl, flies, drops that splat (or sizzle, for an acid), soot, frost
 * crystals, withered leaf bits and sparkles.
 *
 * 这是 Warped `WeatherBlobFX` 里「任何 blob 都能用的细节」那一段，单独摘出来：天气自己的
 * （雷云、暴雪、雾、季节风）没有搬，所以这里只剩细节工厂。原来的 `drift()` 用 DriftParticle，
 * 本仓库没有那个粒子，soot / 落叶改成直接喂 FxParticle（帧改用 MOTE_2/PEBBLE_2）。
 */
public final class GasDetails {

	private GasDetails(){}

	private static FxParticle fx( Emitter e, float x, float y ){
		return ((FxParticle)e.recycle( FxParticle.class )).reset( x, y );
	}

	/** A bubble's end: it pops into a little ring for a frame or two. */
	static final FxParticle.Fate BUBBLE_POP = ( p, landed ) -> {
		Emitter e = (Emitter)p.parent;
		if (e == null || !FxBudget.keep( FxBudget.P3 )) return;
		fx( e, p.px, p.py ).look( FxFrames.POP_5 ).paint( p.colorNow() ).life( 0.08f ).alpha( 0.8f, 0, 0.04f )
				.snap().priority( FxBudget.P3 );
	};

	/** bubbles welling up through the cloud, swaying, popping into a ring as they go */
	public static Emitter.Factory bubbles( final int color ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter e, int index, float x, float y ){
				if (!FxBudget.keep( FxBudget.P2, x, y )) return;
				fx( e, x, y + FxRandom.Float( 2, 8 ) ).look( FxRandom.chance( 0.6f ) ? FxFrames.BUBBLE_2 : FxFrames.BUBBLE_3 )
						.paint( color ).life( FxRandom.Float( 0.6f, 1.1f ) ).alpha( 0.9f, 0.1f, 0.12f )
						.vel( FxRandom.Float( -1f, 1f ), -FxRandom.Float( 6f, 12f ) ).sway( 1.5f, 5f )
						.fate( BUBBLE_POP ).snap().priority( FxBudget.P2 );
			}
		};
	}

	//a mote's twinkle: a dot that swells to two pixels a moment now and then
	private static final int[][] TWINKLE = { FxFrames.MOTE_1, FxFrames.MOTE_1, FxFrames.MOTE_2, FxFrames.MOTE_1 };

	/** motes that hang and twinkle, on whole pixels */
	public static Emitter.Factory motes( final int color ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter e, int index, float x, float y ){
				if (!FxBudget.keep( FxBudget.P2, x, y )) return;
				fx( e, x, y ).look( TWINKLE, FxRandom.Float( 3f, 5f ) ).paint( color ).life( FxRandom.Float( 1f, 2f ) )
						.alpha( 0.85f, 0.2f, 0.4f ).smooth().vel( FxRandom.Float( -2f, 2f ), FxRandom.Float( -4f, 0f ) ).drag( 1f )
						.sway( 2f, 2f ).snap().priority( FxBudget.P2 );
			}
		};
	}

	//the confusion's colours: one array, never one an emit
	private static final int[] SWIRL_COLORS = { 0xF080F0, 0x80C0FF, 0x80F0A0, 0xFFE080 };

	/** specks swinging in wide loops, in the confusion's colours */
	public static final Emitter.Factory SWIRL = new Emitter.Factory() {
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			if (!FxBudget.keep( FxBudget.P2, x, y )) return;
			fx( e, x, y ).look( FxRandom.chance( 0.7f ) ? FxFrames.MOTE_2 : FxFrames.PLUS_3 ).paint( FxRandom.element( SWIRL_COLORS ) )
					.life( FxRandom.Float( 1.2f, 2f ) ).alpha( 0.8f, 0.2f, 0.4f ).smooth()
					.vel( 0, -FxRandom.Float( 2f, 6f ) ).sway( 10f, 5f ).turbulence( 2f, 4f ).priority( FxBudget.P2 );
		}
	};

	/** small dark things that dart about */
	public static final Emitter.Factory FLIES = new Emitter.Factory() {
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			if (!FxBudget.keep( FxBudget.P2, x, y )) return;
			fx( e, x, y ).look( FxFrames.MOTE_1 ).paint( 0x2A2418 ).life( FxRandom.Float( 1.5f, 2.5f ) ).alpha( 0.9f, 0.1f, 0.1f )
					.vel( FxRandom.Float( -6f, 6f ), FxRandom.Float( -6f, 6f ) ).sway( 7f, 14f ).turbulence( 2f, 9f )
					.snap().priority( FxBudget.P2 );
		}
	};

	/** an acid's sizzle */
	public static final int SIZZLE = 0xFFB040;

	/** A drip's landing: on the water a ring, on the floor a splat a moment. */
	static final FxParticle.Fate DRIP_END = ( p, landed ) -> {
		if (!landed) return;
		if (WaterFX.onWater( p.px, p.py )){
			WaterFX.drop( p.px, p.py, 0.1f, p.colorNow() );
			return;
		}
		Emitter e = (Emitter)p.parent;
		if (e == null || !FxBudget.keep( FxBudget.P3 )) return;
		fx( e, p.px, p.py ).look( FxFrames.SPLAT[0] ).paint( p.colorNow() ).life( 0.15f ).alpha( 0.8f, 0, 0.1f )
				.snap().priority( FxBudget.P3 );
	};

	/** An acid drip's landing: a drip's, and two sparks of sizzle over the night's tint. */
	static final FxParticle.Fate ACID_END = ( p, landed ) -> {
		DRIP_END.end( p, landed );
		if (!landed || WaterFX.onWater( p.px, p.py )) return;
		FxEmitter glow = Fx.glowAir();
		if (glow == null) return;
		for (int i = 0; i < 2; i++){
			if (!FxBudget.keep( FxBudget.P3 )) return;
			fx( glow, p.px, p.py - 1 ).look( FxFrames.MOTE_1 ).paint( SIZZLE ).light( true ).life( 0.2f ).alpha( 1f, 0, 0.1f )
					.polar( FxRandom.Float( (float)-Math.PI, 0f ), FxRandom.Float( 10f, 20f ) ).gravity( 60f )
					.priority( FxBudget.P3 );
		}
	};

	private static Emitter.Factory drips( final int color, final FxParticle.Fate end ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter e, int index, float x, float y ){
				if (!FxBudget.keep( FxBudget.P2, x, y )) return;
				fx( e, x, y - FxRandom.Float( 4, 10 ) ).look( FxRandom.chance( 0.5f ) ? FxFrames.DROP_2 : FxFrames.DROP_3 ).orient()
						.paint( color ).life( 1f ).alpha( 0.9f, 0.05f, 0.05f )
						.vel( 0, FxRandom.Float( 16f, 28f ) ).gravity( 60f ).ground( y + FxRandom.Float( 6f, 10f ), FxParticle.DIE, 0 )
						.fate( end ).priority( FxBudget.P2 );
			}
		};
	}

	/** drops that fall out of the cloud: a splat on the floor, a ring on the water */
	public static Emitter.Factory drips( final int color ){
		return drips( color, DRIP_END );
	}

	/** acid dripping out of the cloud: a drip that sizzles where it lands */
	public static Emitter.Factory acidDrips( final int color ){
		return drips( color, ACID_END );
	}

	/** soot: dark flakes that lift and turn (本仓库用 MOTE_2/PEBBLE_2 代替天气图集的 ASH_2/SPECK_1) */
	public static final Emitter.Factory SOOT = new Emitter.Factory() {
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			if (!FxBudget.keep( FxBudget.P2, x, y )) return;
			float life = FxRandom.Float( 1.5f, 3f );
			fx( e, x, y ).look( FxRandom.Float() < 0.6f ? FxFrames.PEBBLE_2 : FxFrames.MOTE_2 )
					.paint( FxRandom.Float() < 0.5f ? 0x3A3638 : 0x4A4448 ).life( life ).alpha( 0.7f, life * 0.2f, life * 0.5f )
					.vel( FxRandom.Float( -2f, 2f ), -FxRandom.Float( 3f, 6f ) ).gravity( 2f )
					.sway( 4f, 2.5f ).spin( 0.3f ).turbulence( 1f, 2f ).priority( FxBudget.P2 );
		}
	};

	//frost's crystal: a plus that winks to a dot and back
	private static final int[][] CRYSTAL = { FxFrames.PLUS_3, FxFrames.MOTE_1 };

	/** frost: crystals that wink and drift down */
	public static final Emitter.Factory FROST = new Emitter.Factory() {
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			if (!FxBudget.keep( FxBudget.P2, x, y )) return;
			fx( e, x, y ).look( CRYSTAL, FxRandom.Float( 2.5f, 4f ) ).paint( 0xE8F4FF ).life( FxRandom.Float( 1f, 2f ) )
					.alpha( 0.9f, 0.15f, 0.3f ).vel( FxRandom.Float( -1f, 1f ), FxRandom.Float( 2f, 4f ) ).sway( 2f, 2f )
					.snap().priority( FxBudget.P2 );
		}
	};

	/** bits of withered leaf, dry gold to brown, tumbling up out of the cloud */
	private static final int[] WITHERED = { 0x9A8A58, 0x6A5A38, 0xB0A068 };
	public static final Emitter.Factory LEAF_BITS = new Emitter.Factory() {
		@Override
		public void emit( Emitter e, int index, float x, float y ){
			if (!FxBudget.keep( FxBudget.P3, x, y )) return;
			float life = FxRandom.Float( 1.2f, 2f );
			fx( e, x, y ).look( FxRandom.Float() < 0.5f ? FxFrames.PEBBLE_2 : FxFrames.MOTE_2 )
					.paint( WITHERED[FxRandom.Int( WITHERED.length )] ).life( life ).alpha( 1f, 0.1f, life * 0.5f )
					.vel( FxRandom.Float( -6f, 6f ), -FxRandom.Float( 10f, 16f ) ).gravity( 20f )
					.sway( 6f, 3f ).spin( 0.6f ).turbulence( 1f, 2f ).priority( FxBudget.P3 );
		}
	};

	/** a glint now and then, over the night's tint */
	public static Emitter.Factory sparkles( final int color ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter e, int index, float x, float y ){
				FxEmitter glow = Fx.glowAir();
				com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFactories.glint( color )
						.emit( glow != null ? glow : e, index, Math.round( x ), Math.round( y ) );
			}
		};
	}
}
