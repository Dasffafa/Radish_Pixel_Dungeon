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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Callback;

import java.util.ArrayList;

/**
 * 改装部件的全部计算钩子。
 *
 * Weapon / Armor / Wand / Hero 等公共文件里各插了一行调用，调到这里的静态方法；
 * 「装了什么改装、数值怎么变、什么时候扣耐久」都写在这个文件里，公共文件不再改动。
 * 每个方法在没有相关改装时必须原样返回传进来的值。
 */
public class ModHooks {

	// ---- 武器 ----

	/** Weapon.accuracyFactor 返回前。 */
	public static float weaponAccuracy( Weapon weapon, Char owner, Char target, float accuracy ) {
		return accuracy;
	}

	/** Weapon.baseDelay 返回前。 */
	public static float weaponDelay( Weapon weapon, Char owner, float delay ) {
		return delay;
	}

	/** Weapon.reachFactor 的每一个返回值。 */
	public static int weaponReach( Weapon weapon, Char owner, int reach ) {
		return reach;
	}

	/** Weapon.STRReq() 返回前。 */
	public static int weaponStrReq( Weapon weapon, int req ) {
		return req;
	}

	/** 武器附魔的触发概率倍率（Weapon.Enchantment.genericProcChanceMultiplier 返回前）。 */
	public static float enchantProcMultiplier( Char attacker, float multiplier ) {
		return multiplier;
	}

	// ---- 护甲 ----

	/** 无参的 Armor.DRMin()：min、max 是这件护甲当前的防御下限和上限。 */
	public static int armorDRMin( Armor armor, int min, int max ) {
		return min;
	}

	/** Armor.evasionFactor 返回前。 */
	public static float armorEvasion( Armor armor, Char owner, float evasion ) {
		return evasion;
	}

	/** Armor.STRReq() 返回前。 */
	public static int armorStrReq( Armor armor, int req ) {
		return req;
	}

	/** 护甲刻印的触发概率倍率（Armor.Glyph.procChanceMultiplier）。armor 可能为 null。 */
	public static float glyphProcMultiplier( Armor armor, Char defender, float multiplier ) {
		return multiplier;
	}

	// ---- 英雄 ----

	/** Hero.STR() 的返回值。 */
	public static int heroStr( Hero hero, int str ) {
		return str;
	}

	/** Hero.speed() 里护甲那一段之后。 */
	public static float heroSpeed( Hero hero, float speed ) {
		return speed;
	}

	/** Hero.defenseSkill 里、护甲折算闪避之前（经 EngineerRules.heroEvasion 转调）。 */
	public static float heroEvasion( Hero hero, float evasion ) {
		return evasion;
	}

	/** 英雄原地等待一回合时（Hero.rest 和持续休息）。 */
	public static void onHeroWait( Hero hero ) {
	}

	/** 英雄普通攻击的加伤时机（常驻状态的 modifyOutgoingAttackDamage 转调）。 */
	public static void confidentDamage( Hero hero, Char defender, DamageInfo info ) {
	}

	// ---- 法杖 ----

	/** Wand.actions 返回前，可以增删动作。 */
	public static void wandActions( Wand wand, Hero hero, ArrayList<String> actions ) {
	}

	/** Wand.defaultAction 的返回值。 */
	public static String wandDefaultAction( Wand wand, String action ) {
		return action;
	}

	/** Wand.tryToZap 开头：返回 false 表示这根法杖现在不能施放。 */
	public static boolean wandCanZap( Wand wand, Hero owner ) {
		return true;
	}

	/** Wand.onThrow：返回 true 表示这次投掷已经在这里处理完，不再走普通的落地流程。 */
	public static boolean wandThrown( Wand wand, int cell ) {
		return false;
	}

	/** Wand.wandUsed 里扣充能的那一行：cost 是这次要扣的充能。 */
	public static int wandChargeCost( Wand wand, int cost ) {
		return cost;
	}

	/** Wand.wandUsed 里的施法耗时。 */
	public static float wandZapTime( Wand wand, float time ) {
		return time;
	}

	/**
	 * 一次非诅咒施放的 onZap 之后、wandUsed 之前。
	 * 必须恰好调用一次 done（它里面是 wandUsed，负责扣充能、耗时并交还回合），可以等动画播完再调。
	 */
	public static void afterZap( Wand wand, Hero hero, int targetCell, Callback done ) {
		done.call();
	}

	/** 充能器算出「回复 1 点充能要多少回合」之后。 */
	public static float wandRechargeTurns( Wand wand, float turns ) {
		return turns;
	}

	/** 充能器每自然回复 1 点充能时。 */
	public static void onNaturalCharge( Wand wand ) {
	}

	/** DamageWand.damageRoll 返回前。 */
	public static int wandDamage( DamageWand wand, int damage ) {
		return damage;
	}

	// ---- 显示 ----

	/** 接在物品说明后面的文字；没有改装时返回空串。 */
	public static String infoText( Item item ) {
		return "";
	}

	/** 物品格子左下角的小字；没有改装时返回 null（沿用物品原来的显示）。 */
	public static String slotText( Item item ) {
		return null;
	}

	// ---- 事件 ----

	public static void onAttackHit( AttackHitEvent event ) {
	}

	public static void onAttackMiss( AttackMissEvent event ) {
	}

	public static void onFinalDamage( CharFinalDamageEvent event ) {
	}
}
