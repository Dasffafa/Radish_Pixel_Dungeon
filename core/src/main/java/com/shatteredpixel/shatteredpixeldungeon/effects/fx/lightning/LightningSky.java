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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning;

import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;

/**
 * A stand-in for Warped's weather layer (`WeatherOverlay`), which this repo does not have: the
 * lightning keeps its own looks (arcs, joints, twigs, halos, pools, crackle, afterglow) but the
 * sky's answer (screen flash, thunder, shake, the overlay it is carried on) is dropped.
 *
 * 本仓库没有天气层。Warped 的 `Lightning` 把「闪屏 / 雷声 / 震屏 / 载体」都交给天气层，
 * 这里给一个空实现：`Lightning.layer` 永远返回 null，于是这些调用不会发生，其余表现照旧。
 */
public final class LightningSky {

	private LightningSky(){}

	/** The overlay that would carry a lightning (none here). */
	public static Object carry( Object lightning ){ return null; }

	/** The storm's crack where a bolt strikes (none here). */
	public static void struck( int cell ){}

	/** A zap's crack where it strikes (none here). */
	public static void crackOnly( int cell ){}

	/** A wand of lightning's thunder (none here). */
	public static void wandThunder( int cell ){}

	/** The storm's own bolt (none here). */
	public static void stormStruck( Lightning lightning ){}

	/** Sparks thrown where a bolt lands (none here). */
	public static void spark( float x, float y, int n ){}
}
