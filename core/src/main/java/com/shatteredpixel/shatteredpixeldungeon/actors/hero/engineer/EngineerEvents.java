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

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.ForceField;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroEatFoodEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.ReadScrollEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.GrenadierBombs;
import com.shatteredpixel.shatteredpixeldungeon.items.engineer.mods.ModHooks;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TrapHack;

/**
 * 工程师各子系统共用的事件入口。
 *
 * 订阅只写在这一个类里，再按固定顺序转给各子系统的同名静态方法：
 * 命中、未命中、最终伤害这三个时机好几个子系统都要用，集中转发可以保证先后顺序固定，
 * 也保证其中一个出错时其余的照常收到。
 */
public class EngineerEvents {

	//一个子系统出错不应该连累后面的，也不应该打断这次攻击；错误照常上报
	private static void safe( Runnable handler ) {
		try {
			handler.run();
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}

	@SubscribeEvent(event = AttackHitEvent.class, priority = 0)
	public static void onAttackHit( AttackHitEvent event ) {
		safe( () -> PulseWand.onAttackHit( event ) );
		safe( () -> TrapHack.onAttackHit( event ) );
		safe( () -> EngineerLoot.onAttackHit( event ) );
		safe( () -> ModHooks.onAttackHit( event ) );
		safe( () -> GrenadierBombs.onAttackHit( event ) );
		safe( () -> ForceField.onAttackHit( event ) );
		safe( () -> EngineerTalents.onAttackHit( event ) );
	}

	@SubscribeEvent(event = AttackMissEvent.class, priority = 0)
	public static void onAttackMiss( AttackMissEvent event ) {
		safe( () -> PulseWand.onAttackMiss( event ) );
		safe( () -> TrapHack.onAttackMiss( event ) );
		safe( () -> EngineerLoot.onAttackMiss( event ) );
		safe( () -> ModHooks.onAttackMiss( event ) );
		safe( () -> GrenadierBombs.onAttackMiss( event ) );
		safe( () -> ForceField.onAttackMiss( event ) );
		safe( () -> EngineerTalents.onAttackMiss( event ) );
	}

	@SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
	public static void onFinalDamage( CharFinalDamageEvent event ) {
		safe( () -> PulseWand.onFinalDamage( event ) );
		safe( () -> TrapHack.onFinalDamage( event ) );
		safe( () -> EngineerLoot.onFinalDamage( event ) );
		safe( () -> ModHooks.onFinalDamage( event ) );
		safe( () -> GrenadierBombs.onFinalDamage( event ) );
		safe( () -> ForceField.onFinalDamage( event ) );
		safe( () -> EngineerTalents.onFinalDamage( event ) );
	}

	@SubscribeEvent(event = ReadScrollEvent.class, priority = 0)
	public static void onReadScroll( ReadScrollEvent event ) {
		safe( () -> EngineerTalents.onReadScroll( event ) );
	}

	@SubscribeEvent(event = HeroEatFoodEvent.class, priority = 0)
	public static void onEatFood( HeroEatFoodEvent event ) {
		safe( () -> EngineerTalents.onEatFood( event ) );
	}

	//常驻状态的兜底：旧存档里的工程师、或者状态被意外清掉时，下一回合补上
	@SubscribeEvent(event = HeroActEvent.class, priority = 0)
	public static void onHeroAct( HeroActEvent event ) {
		Hero hero = event.getHero();
		if (EngineerRules.isEngineer( hero ) && hero.buff( EngineerPassive.class ) == null) {
			Buff.affect( hero, EngineerPassive.class );
		}
	}
}
