/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

/**
 * 玩具背包护甲技能
 * 月华专属护甲技能，允许生成和管理玩具物品
 */
public class ToyBackpack extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		// 玩具背包的核心功能通过Armor.java的AC_ATTACH和AC_TOY实现
		// 此处仅作为护甲技能标识，不在此处执行具体逻辑
	}

	@Override
	public int icon() {
		return HeroIcon.TOY_BACKPACK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BETTER_ITEM, Talent.EXTRA_POCKET, Talent.ACCEPT_CHALLENGE, Talent.HEROIC_ENERGY};
	}
}