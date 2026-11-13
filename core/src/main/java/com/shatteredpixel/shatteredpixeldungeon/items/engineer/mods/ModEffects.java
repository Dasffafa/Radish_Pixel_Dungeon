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

package com.shatteredpixel.shatteredpixeldungeon.items.engineer.mods;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.WandZapTimeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.WeaponReachEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 改装部件的效果，全部通过事件生效。
 * 新的改装在这里订阅它需要的事件；没有合适的事件时，先在发布方加事件，不要往公共文件里直接插调用。
 */
public class ModEffects {

	// 长柄：攻击距离 +1，每次攻击（命中或落空）扣 1 点耐久
	@SubscribeEvent(event = WeaponReachEvent.class, priority = 0)
	public static void onWeaponReach( WeaponReachEvent event ) {
		if (has( event.getWeapon(), ModType.LONG_HANDLE )) {
			event.setReach( event.getReach() + 1 );
		}
	}

	@SubscribeEvent(event = AttackHitEvent.class, priority = 0)
	public static void onAttackHit( AttackHitEvent event ) {
		if (event.getAttacker() instanceof Hero) {
			wear( event.getAttackerWeapon(), ModType.LONG_HANDLE );
		}
	}

	@SubscribeEvent(event = AttackMissEvent.class, priority = 0)
	public static void onAttackMiss( AttackMissEvent event ) {
		if (event.getAttacker() instanceof Hero) {
			wear( event.getAttackerWeapon(), ModType.LONG_HANDLE );
		}
	}

	// 雀喉：施法耗时减半，每次施法扣 1 点耐久
	@SubscribeEvent(event = WandZapTimeEvent.class, priority = 0)
	public static void onWandZapTime( WandZapTimeEvent event ) {
		if (wear( event.getWand(), ModType.FINCH_THROAT )) {
			event.setTime( event.getTime() * 0.5f );
		}
	}

	private static boolean has( Item item, ModType type ) {
		ModState state = ModState.of( item );
		return state != null && state.has( type );
	}

	private static boolean wear( Item item, ModType type ) {
		ModState state = ModState.of( item );
		ModState.Entry entry = state == null ? null : state.find( type );
		if (entry == null) return false;
		if (--entry.left <= 0) state.remove( entry );
		return true;
	}
}
