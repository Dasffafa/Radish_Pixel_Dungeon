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
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

/**
 * The season's share of the climate, Warped's ClimateManager cut down to what this repo has: how
 * many degrees a season shifts the air, and how much likelier it makes bad weather. Warped's own
 * manager (1614 lines) also runs the climate crystal, per-biome air and the seasonal events.
 */
public final class ClimateManager {

	private ClimateManager(){}

	/** How many degrees the season adds to the air, C. */
	public static float seasonalOffset( GameCalendar.Season s ){
		if (s == null) return 0f;
		switch (s){
			case SUMMER: return 6f;
			case WINTER: return -8f;
			case SPRING: return 1f;
			case AUTUMN:
			default:     return 0f;
		}
	}

	/** The current season's offset. */
	public static float seasonalOffset(){
		return seasonalOffset( GameCalendar.season() );
	}

	/** How much likelier rain and the like are this season (1 = as usual). */
	public static float badWeatherChance( GameCalendar.Season s ){
		if (s == null) return 1f;
		switch (s){
			case SUMMER: return 0.8f;
			case WINTER: return 1.3f;
			case SPRING: return 1.2f;
			case AUTUMN:
			default:     return 1.1f;
		}
	}
}
