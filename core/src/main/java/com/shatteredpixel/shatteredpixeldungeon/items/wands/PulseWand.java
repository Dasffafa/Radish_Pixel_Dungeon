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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 脉冲法杖：工程师的初始法杖。
 *
 * 放在 items.wands 包里是因为它要改充能器的充能因数，那个字段只在包内可见。
 */
public class PulseWand extends Wand {

	{
		image = ItemSpriteSheet.PULSE;

		unique = true;
		bones = false;
	}

	@Override
	public void onZap( Ballistica attack ) {
	}

	@Override
	public void onHit( MagesStaff staff, Char attacker, Char defender, int damage ) {
	}

	/** 点天赋时调用（Talent.onTalentUpgraded）：「杖构工学」+3 把充能上限提到 2。 */
	public static void onTalentUpgraded( Hero hero, Talent talent ) {
	}

	/** 工程师身上没有脉冲法杖、也没发过时补发一根（旧存档）。由常驻状态每回合调用，要足够便宜。 */
	public static void ensureGranted( Hero hero ) {
	}

	public static void onAttackHit( AttackHitEvent event ) {
	}

	public static void onAttackMiss( AttackMissEvent event ) {
	}

	public static void onFinalDamage( CharFinalDamageEvent event ) {
	}
}
