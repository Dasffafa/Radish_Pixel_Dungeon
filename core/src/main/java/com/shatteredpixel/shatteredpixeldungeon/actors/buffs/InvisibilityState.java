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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/**
 * 隐身状态的统一入口。
 * 普通隐身与斗篷隐身都通过这里增减 {@link Char#invisible}，
 * 保证计数器、刺客准备与保护阴影走同一条链路。
 */
public final class InvisibilityState {

	private InvisibilityState() {
	}

	/** 附加一层隐身。 */
	public static void attach( Char target ) {
		target.invisible++;
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			if (hero.subClass == HeroSubClasses.ASSASSIN) {
				Buff.affect( hero, Preparation.class );
			}
			if (hero.hasTalent( Talent.PROTECTIVE_SHADOWS )) {
				Buff.affect( hero, Talent.ProtectiveShadowsTracker.class );
			}
		}
	}

	/** 移除一层隐身，返回本次移除后目标是否已完全可见。 */
	public static boolean detach( Char target ) {
		if (target.invisible > 0) {
			target.invisible--;
		}
		return target.invisible == 0;
	}

	/** 同步隐身贴图状态。 */
	public static void fx( Char target, boolean on ) {
		if (target.sprite == null) {
			return;
		}
		if (on) {
			target.sprite.add( CharSprite.State.INVISIBLE );
		} else if (target.invisible == 0) {
			target.sprite.remove( CharSprite.State.INVISIBLE );
		}
	}
}
