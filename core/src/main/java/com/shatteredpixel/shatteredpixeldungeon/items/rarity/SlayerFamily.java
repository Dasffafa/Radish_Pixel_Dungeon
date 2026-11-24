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

package com.shatteredpixel.shatteredpixeldungeon.items.rarity;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/**
 * The kinds of foe a slayer weapon hunts, Warped's idea in a small table: a weapon with the SLAYER
 * line names one of these, and hits that family harder. Warped's own table is long and tied to its
 * whole bestiary; this one reads this repo's mob names.
 */
public enum SlayerFamily {

	VERMIN( "\u866b\u9f20" ),
	BEAST( "\u91ce\u517d" ),
	UNDEAD( "\u4ea1\u7075" ),
	DEMON( "\u6076\u9b54" ),
	HUMANOID( "\u4eba\u5f62" ),
	NONE( "" );

	private final String title;

	SlayerFamily( String title ){
		this.title = title;
	}

	public String title(){
		return title;
	}

	/** What family a character belongs to (NONE for the hero and anything unreadable). */
	public static SlayerFamily of( Char ch ){
		if (ch == null) return NONE;
		String n = ch.getClass().getSimpleName().toLowerCase();
		if (n.contains( "rat" ) || n.contains( "bat" ) || n.contains( "spinner" ) || n.contains( "spider" )
				|| n.contains( "snake" ) || n.contains( "fly" ) || n.contains( "scorpion" ) || n.contains( "worm" )
				|| n.contains( "swarm" )){
			return VERMIN;
		}
		if (n.contains( "skeleton" ) || n.contains( "skull" ) || n.contains( "wraith" ) || n.contains( "ghoul" )
				|| n.contains( "lich" ) || n.contains( "revenant" ) || n.contains( "zombie" )){
			return UNDEAD;
		}
		if (n.contains( "demon" ) || n.contains( "succubus" ) || n.contains( "imp" ) || n.contains( "fiend" )
				|| n.contains( "devil" )){
			return DEMON;
		}
		if (n.contains( "bandit" ) || n.contains( "guard" ) || n.contains( "thief" ) || n.contains( "berserker" )
				|| n.contains( "monk" ) || n.contains( "brute" ) || n.contains( "shaman" ) || n.contains( "warlock" )){
			return HUMANOID;
		}
		return BEAST;
	}
}
