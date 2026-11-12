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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroEatFoodEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.ReadScrollEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;

/**
 * 不属于其它子系统的工程师天赋：进食、卷轴、协同行动、黑魔法学 +4。
 */
public class EngineerTalents {

	/** 「协同行动」：给触发过已骇入陷阱、或被脉冲法杖命中的敌人做标记。 */
	public static void markCoordinated( Char enemy ) {
	}

	/**
	 * 「卷藏警觉」+2：英雄踩中隐藏陷阱时问一次，返回 true 表示这次不触发。
	 * 返回 true 时由这里负责消耗那一次机会、让陷阱显形。
	 */
	public static boolean absorbsHiddenTrap( Hero hero, Trap trap ) {
		return false;
	}

	public static void onReadScroll( ReadScrollEvent event ) {
	}

	public static void onEatFood( HeroEatFoodEvent event ) {
	}

	public static void onAttackHit( AttackHitEvent event ) {
	}

	public static void onAttackMiss( AttackMissEvent event ) {
	}

	public static void onFinalDamage( CharFinalDamageEvent event ) {
	}
}
