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

package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;

/**
 * 爆破手的炸弹规则：用零件造炸弹、给炸弹升级、对爆炸伤害的减免。
 */
public class GrenadierBombs {

	/** 爆破手在金属零件的「合成」里选中零件自身：花零件和炼金能量造一个炸弹。 */
	public static void craftBomb( Hero hero ) {
	}

	/** 「加量赠礼」：在金属零件的「合成」里选中一个炸弹，花零件给它升级。 */
	public static void upgradeBomb( Hero hero, Bomb bomb ) {
	}

	/** 英雄受到伤害时的最终乘算倍率（常驻状态转调）；与爆炸无关的伤害必须返回 1。 */
	public static float incomingMultiplier( Hero hero, DamageInfo info ) {
		return 1f;
	}

	/** 英雄受到伤害时的最终固定加减（常驻状态转调，先加减再乘上面的倍率）；与爆炸无关的伤害必须返回 0。 */
	public static float incomingFlat( Hero hero, int dmg, DamageInfo info ) {
		return 0f;
	}

	public static void onAttackHit( AttackHitEvent event ) {
	}

	public static void onAttackMiss( AttackMissEvent event ) {
	}

	public static void onFinalDamage( CharFinalDamageEvent event ) {
	}
}
