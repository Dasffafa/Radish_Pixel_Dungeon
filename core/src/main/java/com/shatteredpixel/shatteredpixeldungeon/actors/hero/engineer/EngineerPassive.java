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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.IDamageModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.GrenadierBombs;
import com.shatteredpixel.shatteredpixeldungeon.items.engineer.mods.ModHooks;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TrapHack;
import com.watabou.utils.Bundle;

/**
 * 工程师身上不显示的常驻状态。
 *
 * 它自己不写规则，只把「每回合一次」「我打人时」「我挨打时」这三个时机
 * 转给各子系统的静态方法，这样各子系统改自己的文件就行，不用都来改这里。
 */
public class EngineerPassive extends Buff implements IDamageModifier {

	{
		//复活类效果会清掉大部分状态，这个要留下
		revivePersists = true;
	}

	//脉冲法杖是否已经发过。旧存档里的工程师没有法杖，要补发一次，但只补一次
	public boolean pulseWandGranted = false;

	@Override
	public boolean act() {
		if (target instanceof Hero && target.isAlive()) {
			Hero hero = (Hero) target;
			PulseWand.ensureGranted( hero );
			if (Dungeon.level != null) {
				TrapHack.tick( Dungeon.level, hero );
			}
		}
		spend( TICK );
		return true;
	}

	@Override
	public void modifyOutgoingAttackDamage( Char attacker, Char defender, DamageInfo info ) {
		if (attacker instanceof Hero) {
			ModHooks.confidentDamage( (Hero) attacker, defender, info );
		}
	}

	@Override
	public float finalAddFlat( int dmg, DamageInfo info ) {
		return target instanceof Hero ? GrenadierBombs.incomingFlat( (Hero) target, dmg, info ) : 0f;
	}

	@Override
	public float finalMultiply( int dmg, DamageInfo info ) {
		return target instanceof Hero ? GrenadierBombs.incomingMultiplier( (Hero) target, info ) : 1f;
	}

	private static final String WAND_GRANTED = "pulse_wand_granted";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( WAND_GRANTED, pulseWandGranted );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( WAND_GRANTED )) {
			pulseWandGranted = bundle.getBoolean( WAND_GRANTED );
		}
	}
}
