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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

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

	/**
	 * 有东西压到陷阱格时问一次，返回 false 表示这次不触发。
	 * presser 是踩上去的角色；物品落地、法术落点等没有踩踏者的情况为 null。
	 * hard 为 true 表示「硬踩」（英雄、物品落地），明面上的陷阱只有硬踩或已骇入时才会被走上去的怪物触发。
	 */
	public static boolean allowsPress( Trap trap, Char presser, boolean hard ) {
		return true;
	}

	/**
	 * 某一格的地形刚被重设之后调用：已骇入陷阱的「可通行、不避开」标记要重新盖上去。
	 * 关卡生成和读档期间也会调到，只能用传进来的 level，不能碰 Dungeon.level / Dungeon.hero。
	 */
	public static void applyFlags( Level level, int cell ) {
	}

	/** 整张地图的标记重建之后调用（Level.buildFlagMaps）。限制同上。 */
	public static void applyFlags( Level level ) {
	}

	/** 每回合一次（工程师的常驻状态调用）：骇客的主动触发、冷却递减。 */
	public static void tick( Level level, Hero hero ) {
	}

	public static void onAttackHit( AttackHitEvent event ) {
	}

	public static void onAttackMiss( AttackMissEvent event ) {
	}

	public static void onFinalDamage( CharFinalDamageEvent event ) {
	}
}
