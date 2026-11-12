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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.MetalPart;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

/**
 * 金属零件与炼金能量的各个来源，以及工匠的翻倍。
 */
public class EngineerLoot {

	public enum Source {
		//击杀机械敌人
		MECH_KILL,
		//拆失效陷阱
		DISMANTLE_TRAP,
		//拆已骇入、未失效的陷阱（另给炼金能量）
		DISMANTLE_HACKED_TRAP,
		//拆门（拆解欲望 +1）
		DISMANTLE_DOOR,
		//拆机械敌人（拆解欲望 +2）
		DISMANTLE_MECH
	}

	/** 这个来源给多少零件。 */
	public static int parts( Hero hero, Source src ) {
		return 1;
	}

	/** 这个来源给多少炼金能量。 */
	public static int energy( Hero hero, Source src ) {
		return 0;
	}

	/** 把零件放进背包，放不下就掉在 cell；能量直接加到炼金能量上。不耗回合。 */
	public static void give( Hero hero, int parts, int energy, int cell ) {
		if (hero == null) return;

		if (parts > 0) {
			Item part = new MetalPart().quantity( parts );
			if (part.collect( hero.belongings.backpack )) {
				GameScene.pickUp( part, hero.pos );
				Sample.INSTANCE.play( Assets.Sounds.ITEM );
			} else {
				Dungeon.level.drop( part, cell ).sprite.drop();
			}
		}

		if (energy > 0) {
			Dungeon.energy += energy;
			if (hero.sprite != null) {
				hero.sprite.showStatusWithIcon( 0x44CCFF, Integer.toString( energy ), FloatingText.ENERGY );
			}
			Item.updateQuickslot();
		}
	}

	/**
	 * 机械敌人结算掉落时调用（各机械怪的 rollToDropLoot；只有敌对阵营的怪死亡才会走到）。
	 * 调用点在 super.rollToDropLoot() 之后：基类里「英雄等级超过怪物 maxLvl + 2 就不掉落」的判断拦不到这里，
	 * 要按同样的规则拦，得在这里自己判断。
	 */
	public static void onMechanicalDeath( Mob mob ) {
	}

	public static void onAttackHit( AttackHitEvent event ) {
	}

	public static void onAttackMiss( AttackMissEvent event ) {
	}

	public static void onFinalDamage( CharFinalDamageEvent event ) {
	}
}
