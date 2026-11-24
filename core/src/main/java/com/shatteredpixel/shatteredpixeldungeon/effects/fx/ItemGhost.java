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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx;

import com.watabou.noosa.Image;

/**
 * A stand-in for Warped's ItemGhost (an item image's afterimage, drawn from its own item film):
 * this repo's item art is a runtime atlas with no such film, so there is no ghost to draw and
 * `of` answers null. The enchant framework asks for one; nothing is drawn.
 */
public class ItemGhost extends Image {

	private ItemGhost(){}

	public static ItemGhost of( int image, float x, float y, float angle, int color, float alpha, float life,
			float ox, float oy ){
		return null;
	}
}
