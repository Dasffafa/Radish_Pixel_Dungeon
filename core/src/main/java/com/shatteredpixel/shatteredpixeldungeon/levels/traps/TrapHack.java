/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2026 TheCatist
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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/**
 * 「骇入陷阱」的全部规则。
 *
 * Level 里踩踏和地格标记的几处各插了一行调用，调到这里；规则本身都写在这个文件里。
 */
public class TrapHack {

	public static boolean isHacked( Trap trap ) {
		return false;
	}

	/** 把一个未失效的陷阱骇入（脉冲法杖调用）。 */
	public static void hack( Trap trap ) {
	}

	/**
	 * 「增效修复」：脉冲法杖打到失效陷阱时调用。
	 * 返回 true 表示已经接手（弹了确认窗口或直接修好）。
	 */
	public static boolean offerRepair( Hero hero, Trap trap ) {
		return false;
	}
}
