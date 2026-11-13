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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CausticSlime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalGuardian;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalWisp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.DM175;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.stats.DM100H;
import com.shatteredpixel.shatteredpixeldungeon.items.engineer.mods.ModHooks;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;

import java.util.HashMap;

/**
 * 工程师的分类规则与两个职业被动。
 *
 * 「机械类」按怪物的类判断而不是给 Char.Property 加一项：这样不用改 Char.java，
 * 名单也集中在这一个文件里。
 */
public class EngineerRules {

	//机械类名单，子类一并算（DM-201 继承 DM-200，矮人王的魔像继承魔像）。
	//DM-300 和能量塔是头目战单位，不算
	private static final Class<?>[] MECHANICAL = {
			DM100.class, DM200.class, Golem.class, DM175.class, DM100H.class
	};

	//依赖工程师专属物品（脉冲法杖、多用军刀、金属零件、已骇入陷阱）的天赋，别的职业拿到也用不了
	private static final Talent[] ENGINEER_ONLY = {
			Talent.GENERAL_DISARM, Talent.PULSE_ENERGY,
			Talent.STRONG_PULSE, Talent.DISMANTLE_URGE, Talent.FAVORITE_WORK,
			Talent.COORDINATED_ACTION, Talent.ELECTRONIC_REPAIR
	};

	public static boolean isEngineer( Hero hero ) {
		return hero != null && hero.heroClass == HeroClasses.ENGINEER;
	}

	public static boolean isMechanicalClass( Class<?> actorClass ) {
		if (actorClass == null) return false;
		for (Class<?> cls : MECHANICAL) {
			if (cls.isAssignableFrom( actorClass )) return true;
		}
		return false;
	}

	public static boolean isMechanical( Char ch ) {
		return ch != null && isMechanicalClass( ch.getClass() );
	}

	/**
	 * 脉冲法杖（骇客）能否把这个目标沉沦。
	 * 头目与小头目免疫一切「变成友军」的效果，在免疫检查那一步就被挡掉，
	 * 所以新生火元素、水晶守卫虽然在名单里，实际上沉沦不了。
	 */
	public static boolean canEnthrall( Hero hero, Char target ) {
		if (!(target instanceof Mob) || target.alignment != Char.Alignment.ENEMY) return false;
		if (target.isImmune( ScrollOfSirensSong.Enthralled.class )) return false;

		if (isMechanical( target )) return true;
		//召唤守卫是活化石像的子类，一并算；装甲石像除外
		if (target instanceof Statue && !(target instanceof ArmoredStatue)) return true;
		if (target instanceof Elemental) return true;
		if (target instanceof CrystalWisp || target instanceof CrystalGuardian) return true;
		if (target instanceof Slime && !(target instanceof CausticSlime)) return true;

		if (hero != null && hero.pointsInTalent( Talent.DARK_MAGIC ) >= 2) {
			return target.properties().contains( Char.Property.UNDEAD )
					|| target.properties().contains( Char.Property.DEMONIC );
		}
		return false;
	}

	/** 英雄闪避的接缝（Hero.defenseSkill，在护甲折算之前）。 */
	public static float heroEvasion( Hero hero, Char enemy, float evasion ) {
		if (isEngineer( hero ) && isMechanical( enemy )) {
			evasion *= 1.25f;
		}
		return ModHooks.heroEvasion( hero, evasion );
	}

	/**
	 * 英雄潜行值的接缝（Hero.stealth）。
	 * 怪物是在自己的回合里读英雄潜行值的，所以看「当前正在行动的是谁」就知道是不是机械怪在找人，
	 * 不需要改 Mob.java。
	 */
	public static float heroStealth( Hero hero, float stealth ) {
		if (isEngineer( hero ) && isMechanicalClass( Actor.getCurrentActorClass() )) {
			stealth += 1f;
		}
		return stealth;
	}

	/**
	 * 脉冲效果的统一入口：脉冲法杖命中、「附着脉冲」触发都走这里。
	 * wandLevel 是脉冲法杖当时的等级。
	 */
	public static void applyPulse( Hero src, Char target, int wandLevel ) {
		float duration = 2f + 1.5f * wandLevel;
		if (isMechanical( target )) {
			Buff.prolong( target, Amok.class, duration );
		} else if (target.properties().contains( Char.Property.INORGANIC )) {
			Buff.prolong( target, Vertigo.class, duration );
		} else {
			if (src.hasTalent( Talent.STRONG_PULSE )) duration += 2f;
			Buff.prolong( target, Paralysis.class, duration );
		}
		int engineering = src.pointsInTalent( Talent.WAND_ENGINEERING );
		if (engineering >= 1) Buff.prolong( target, Weakness.class, duration );
		if (engineering >= 2) Buff.prolong( target, Vulnerable.class, duration );
	}

	/** 蜕变秘卷与「混乱变化」各有一份「只能给某职业」的名单，两处都来这里登记工程师的。 */
	public static void restrictToEngineer( HashMap<Talent, HeroClass> restricted ) {
		for (Talent talent : ENGINEER_ONLY) {
			restricted.put( talent, HeroClasses.ENGINEER );
		}
	}
}
