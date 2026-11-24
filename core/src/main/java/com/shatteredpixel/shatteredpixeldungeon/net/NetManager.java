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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.net;

/**
 * A stand-in for Warped's co-op layer: this repo has no networking, so it is always the host and
 * never a client — the combat presentation (effects/fx/combat) then never broadcasts anything.
 */
public final class NetManager {

	private NetManager(){}

	/** Always the host here: nothing is sent anywhere anyway. */
	public static boolean isHost(){
		return true;
	}

	/** Never a guest. */
	public static boolean isNetClient(){
		return false;
	}

	/** No net at all. */
	public static boolean isNet(){
		return false;
	}
}
