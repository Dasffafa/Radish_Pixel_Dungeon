/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

/**
 * How warm it is, and how warm the hero feels - **without any harm**.
 *
 * Warped's ClimateManager works out an air temperature from season, biome, depth, weather and wind
 * (a "feels like" figure), and below -5 °C or above 30 °C the hero takes frost/heat damage through
 * Hypothermia and Heatstroke (slowed, chilled, burnt). This is the climate only: the air, a body
 * temperature that follows it, and what it looks like.
 *
 * **Nothing here touches HP, buffs, speed or anything else of the game's** - there is no
 * Hypothermia, no Heatstroke, no damage. Temperature is climate, not a hazard: the worst a blizzard
 * does is make the hero breathe vapour and shiver.
 *
 * The state is read off the air (Warped's thresholds: 0 / -5 / 30 °C); the body temperature is a
 * smoothed copy of it, used for the pictures and never sharp enough to hurt.
 */
public final class Thermal {

	/** The dungeon's still air by depth (index is depth - 1). */
	private static final float[] DEPTH_AIR = { 13f, 12f, 11f, 10f, 9f, 8f, 8f, 9f, 10f, 12f };
	/** Wind chill per unit of (sideways) wind, and what a nearby heat adds, °C. */
	private static final float WIND_CHILL = 9f, HEAT_EMBERS = 8f, HEAT_FIRE = 15f;

	/** Warped's thresholds, °C of air. */
	public static final float COLD = 0f, FREEZING = -5f, HOT = 30f;
	/** The hero's own warmth at rest, and how fast his body follows the air, °C a second. */
	public static final float WARM = 36.5f, DRIFT = 0.06f;

	/** The air shown instead of the real one (the self-tests). */
	public static float debugAir = Float.NaN;

	/** The hero's own warmth now, °C. */
	private static float body = WARM;
	/** What this frame's air and state are. */
	private static float air = 13f, state;

	/** Acceptance counters. */
	public static int updates, announcements, breaths, tint;
	/** Whether he is soaked through this frame (the self-tests). */
	public static boolean wet;

	private static float breathIn;

	private Thermal(){}

	/** A new game or level: the hero starts warm, as any hero would. */
	public static void reset(){
		body = WARM;
		breathIn = 0f;
		tint = 0;
		debugAir = Float.NaN;
	}

	/** The air about the hero, °C: depth, weather, wind and whatever is burning nearby. */
	public static float ambient(){
		if (!Float.isNaN( debugAir )) return debugAir;

		int idx = Dungeon.depth <= 0 ? 0 : Math.min( DEPTH_AIR.length - 1, Dungeon.depth - 1 );
		float t = DEPTH_AIR[idx];
		switch (Weather.precip){
			case RAIN:     t -= 3f; break;
			case SLEET:    t -= 5f; break;
			case SNOW:     t -= 7f; break;
			case HAIL:     t -= 6f; break;
			case BLIZZARD: t -= 12f; break;
			default: break;
		}
		float chill = Math.abs( Weather.windX ) * WIND_CHILL;
		//quality lines on worn armour: WINDBREAK keeps some of the wind off
		if (Dungeon.hero != null && Dungeon.hero.belongings != null && Dungeon.hero.belongings.armor() != null){
			com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
					com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( Dungeon.hero.belongings.armor() );
			if (q.has( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.WINDBREAK )){
				chill *= 1f - 0.6f * q.strength( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.WINDBREAK );
			}
		}
		t -= chill;

		//quality lines: a held WARM_GRIP weapon keeps the cold off the hand
		if (Dungeon.hero != null && Dungeon.hero.belongings != null
				&& Dungeon.hero.belongings.weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon){
			com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
					com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( Dungeon.hero.belongings.weapon );
			if (q.has( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.WARM_GRIP )){
				t += 3f * q.strength( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.WARM_GRIP );
			}
		}

		t += heat();

		return coolAir( t );
	}

	/** COOLING on worn armour takes the edge off a hot air reading, never below 25 C. */
	public static float coolAir( float t ){
		if (t <= 25f || Dungeon.hero == null || Dungeon.hero.belongings == null
				|| Dungeon.hero.belongings.armor() == null) return t;
		com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
				com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( Dungeon.hero.belongings.armor() );
		if (q.has( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.COOLING )){
			return Math.max( 25f, t - 5f * q.strength( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.COOLING ) );
		}
		return t;
	}

	/** What embers underfoot and a fire or lava beside add to the air, °C. */
	public static float heat(){
		if (Dungeon.hero == null || Dungeon.level == null) return 0f;
		float h = 0f;
		int pos = Dungeon.hero.pos, wdt = Dungeon.level.width();
		if (Dungeon.level.map[pos] == Terrain.EMBERS) h = HEAT_EMBERS;
		for (int dy = -1; dy <= 1; dy++){
			for (int dx = -1; dx <= 1; dx++){
				int cx = pos % wdt + dx, cy = pos / wdt + dy;
				if (cx < 0 || cy < 0 || cx >= wdt || cy >= Dungeon.level.height()) continue;
				int cell = cy * wdt + cx;
				if (Dungeon.level.map[cell] == Terrain.EMBERS) h = Math.max( h, HEAT_EMBERS );
				//a molten pool is lava, not water, when the level's liquid says so
				if (Dungeon.level.water[cell]
						&& com.shatteredpixel.shatteredpixeldungeon.effects.fx.WaterFX.liquid()
							== com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element.Liquid.LAVA){
					h = Math.max( h, HEAT_FIRE );
				}
				for (Blob b : Dungeon.level.blobs.values()){
					if (b instanceof com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire
							&& b.volume > 0 && b.cur != null && b.cur[cell] > 0) h = Math.max( h, HEAT_FIRE );
				}
			}
		}
		return h;
	}

	/** The hero's body temperature now, °C (a smoothed copy of the air). */
	public static float body(){
		return body;
	}

	/** The air about him now, °C. */
	public static float air(){
		return air;
	}

	/** The hero's comfort: COLD, FREEZING or HOT, or NaN when it is neither too cold nor too warm. */
	public static float stress(){
		return state;
	}

	/** Sets the hero's warmth outright: the self-tests use it. */
	public static void debugBody( float t ){
		body = t;
	}

	/** Every frame: the body follows the air, and the cold or the heat shows. */
	public static void update( float dt ){
		if (dt <= 0f || Dungeon.hero == null || Dungeon.level == null) return;
		updates++;

		air = ambient();
		//his own body follows the air slowly, and never changes faster than the drift
		//soaked through (standing in open water, or out in the rain) he loses and gains heat faster;
		//ice is not water: a frozen pool keeps him as dry as stone
		boolean inWater = Dungeon.level.water[Dungeon.hero.pos];
		boolean rainy = Weather.precip == PrecipType.RAIN || Weather.precip == PrecipType.SLEET
				|| Weather.precip == PrecipType.BLIZZARD;
		wet = !Weather.freezing() && (inWater || rainy);
		float wetMul = 1.6f;
		//quality lines: RAINPROOF armour takes turns to soak its wearer
		if (!inWater && rainy && Dungeon.hero.belongings != null && Dungeon.hero.belongings.armor() != null){
			com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality q =
					com.shatteredpixel.shatteredpixeldungeon.items.rarity.Quality.of( Dungeon.hero.belongings.armor() );
			if (q.has( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.RAINPROOF )){
				wetMul = 1f + 0.6f * (1f - q.strength( com.shatteredpixel.shatteredpixeldungeon.items.rarity.RarityLine.RAINPROOF ));
			}
		}
		float step = DRIFT * (wet ? wetMul : 1f) * dt;
		if (body > air) body = Math.max( air, body - step );
		else if (body < air) body = Math.min( air, body + step );

		state = air <= FREEZING ? FREEZING : air <= COLD ? COLD : air >= HOT ? HOT : Float.NaN;

		if (!(Game.scene() instanceof GameScene)) return;
		CharSprite s = Dungeon.hero.sprite;
		if (s == null || !s.visible) return;

		//cold breath: a little white puff from the mouth now and then
		breathIn -= dt;
		if (air <= 5f && breathIn <= 0f){
			breathIn = 1.1f + FxRandom.Float( 0.8f );
			FxEmitter e = Fx.air();
			if (e != null){
				FxParticle p = (FxParticle) e.recycle( FxParticle.class );
				if (p != null){
					p.reset( s.x + s.width() / 2f + (s.flipHorizontal ? -3f : 3f), s.y + s.height() * 0.35f )
							.look( FxRandom.chance( 0.5f ) ? FxFrames.MOTE_2 : FxFrames.MOTE_1 )
							.paint( 0xEAF2FF )
							.vel( -2f + FxRandom.Float( 4f ), -8f - FxRandom.Float( 6f ) )
							.life( 0.9f ).alpha( 0.45f, 0.1f, 0.5f )
							.priority( FxBudget.P3 );
					breaths++;
				}
			}
		}

		//too warm: a shimmer of heat off the hero
		if (air >= 25f && FxRandom.chance( dt * 2f )){
			FxEmitter e = Fx.air();
			if (e != null){
				FxParticle p = (FxParticle) e.recycle( FxParticle.class );
				if (p != null){
					p.reset( s.x + FxRandom.Float( s.width() ), s.y + s.height() * 0.6f )
							.look( FxFrames.MOTE_1 ).paint( 0xFFD9A0 )
							.vel( Weather.windX * 6f, -10f ).life( 1.2f )
							.alpha( 0.3f, 0.2f, 0.6f ).priority( FxBudget.P3 );
				}
			}
		}

		//the body's state on the hero himself: a cold blue, a warm flush, and a word when it turns
		show( s );
	}

	/** The hero's own colour and the log's word: blue when freezing, flush when too warm, plain between. */
	private static void show( CharSprite s ){
		boolean cold = state == COLD || state == FREEZING;
		boolean hot = state == HOT;
		int want = cold ? -1 : hot ? 1 : 0;
		if (want == tint) return;
		if (s.tintFree( null )){
			if (want == 0) s.resetColor();
			else if (want < 0) s.hardlight( 0xC8DDFF );
			else s.hardlight( 0xFFD2B0 );
		}
		if (want == 0 && tint != 0) GLog.p( "You feel comfortable again" );
		else if (want < 0) GLog.w( state == FREEZING ? "You are shivering with cold" : "You feel a little cold" );
		else GLog.w( "You feel a little warm" );
		announcements++;
		tint = want;
	}
}
