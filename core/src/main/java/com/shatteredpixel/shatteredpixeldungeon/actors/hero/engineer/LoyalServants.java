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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.engineer;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/**
 * 「忠诚仆从」：首次进入新的一层时，把上一层还活着的友方单位带过去。
 */
public class LoyalServants {

	/** 离开 from 这一层、去往 destDepth / destBranch 之前调用：挑出要带走的单位并从 from 里拿掉。 */
	public static void hold( Level from, int destDepth, int destBranch ) {
	}

	/** 英雄在新的一层落位之后调用：把带来的单位放到 heroPos 附近。 */
	public static void release( Level to, int heroPos ) {
	}

	/** 这次换层不带人（坠落、传送、读档等）时调用，清掉暂存的单位。 */
	public static void clear() {
	}
}
