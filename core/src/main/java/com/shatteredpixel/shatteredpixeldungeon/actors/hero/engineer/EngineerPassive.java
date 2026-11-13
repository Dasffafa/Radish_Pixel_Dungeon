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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand;
import com.watabou.utils.Bundle;

/**
 * 工程师身上不显示的常驻状态：补发脉冲法杖、提供对机械敌人的潜行加成。
 * 其他规则各自订阅事件或实现伤害管线的修正接口，不经这里转发。
 */
public class EngineerPassive extends Buff {

	{
		//复活类效果会清掉大部分状态，这个要留下
		revivePersists = true;
	}

	//脉冲法杖是否已经发过。旧存档里的工程师没有法杖，要补发一次，但只补一次
	public boolean pulseWandGranted = false;

	@Override
	public boolean act() {
		if (target instanceof Hero && target.isAlive()) {
			PulseWand.ensureGranted( (Hero) target );
		}
		spend( TICK );
		return true;
	}

	@Override
	public float stealthBonus() {
		return target instanceof Hero ? EngineerRules.stealthBonus( (Hero) target ) : 0f;
	}

	//常驻状态的兜底：旧存档里的工程师、或者状态被意外清掉时，下一回合补上
	@SubscribeEvent(event = HeroActEvent.class, priority = 0)
	public static void onHeroAct( HeroActEvent event ) {
		Hero hero = event.getHero();
		if (EngineerRules.isEngineer( hero ) && hero.buff( EngineerPassive.class ) == null) {
			Buff.affect( hero, EngineerPassive.class );
		}
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
