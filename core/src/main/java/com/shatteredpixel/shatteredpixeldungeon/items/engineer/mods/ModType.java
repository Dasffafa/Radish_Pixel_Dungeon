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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.Locale;

/**
 * 改装部件的种类表。数值全部来自设计文档。
 *
 * 存档里按名字记录种类，所以这里的名字定了就不要改；要废弃某一项，直接删掉即可，
 * 读档时不认识的名字会被丢弃。
 */
public enum ModType {

	//             装在哪     零件 能量 寿命 工匠专属
	//锐化刀片：触发 10 次后失效
	SHARP_BLADE      ( Slot.WEAPON,  2, 0, 10, false ),
	//长柄改良：15 次攻击
	LONG_HANDLE      ( Slot.WEAPON,  3, 0, 15, false ),
	//导能回路（武器）：25 次攻击
	WEAPON_CONDUIT   ( Slot.WEAPON,  5, 2, 25, false ),
	//术战模块：25 次攻击
	SPELL_MODULE     ( Slot.WEAPON,  8, 3, 25, true ),
	//气动模块：25 次攻击
	PNEUMATIC_MODULE ( Slot.WEAPON,  8, 3, 25, true ),

	//薄弱区加固：受击 25 次
	REINFORCE        ( Slot.ARMOR,   5, 0, 25, false ),
	//爆反装甲：触发 1 次
	REACTIVE_ARMOR   ( Slot.ARMOR,   2, 0,  1, false ),
	//导能回路（护甲）：受击 15 次
	ARMOR_CONDUIT    ( Slot.ARMOR,   5, 2, 15, false ),
	//偏转力场：闪避 15 次
	DEFLECTOR        ( Slot.ARMOR,   5, 2, 15, true ),
	//外置骨骼：受击 30 次
	EXOSKELETON      ( Slot.ARMOR,  10, 0, 30, true ),

	//「静电」：自然回复 15 点充能
	STATIC           ( Slot.WAND,    5, 2, 15, false ),
	//「雀喉」：25 次施法
	FINCH_THROAT     ( Slot.WAND,    5, 2, 25, false ),
	//「双牙」：成功连发 15 次
	TWIN_FANG        ( Slot.WAND,    5, 2, 15, true ),
	//「棱镜」：施法 15 次
	PRISM            ( Slot.WAND,    5, 2, 15, true ),
	//「熔核」：投掷 15 次
	FUSION_CORE      ( Slot.WAND,   10, 5, 15, true );

	public enum Slot {
		WEAPON, ARMOR, WAND;

		/** 这件物品属于哪一类可改装的装备；不能改装的返回 null。投掷武器不能改装。 */
		public static Slot of( Item item ) {
			if (item instanceof MeleeWeapon) return WEAPON;
			if (item instanceof Armor) return ARMOR;
			if (item instanceof Wand) return WAND;
			return null;
		}
	}

	public final Slot slot;
	//制作花费的金属零件
	public final int parts;
	//制作花费的炼金能量
	public final int energy;
	//基础寿命（次数）；0 表示不会损耗
	public final int life;
	//是否转职工匠后才能制作
	public final boolean craftsmanOnly;

	ModType( Slot slot, int parts, int energy, int life, boolean craftsmanOnly ) {
		this.slot = slot;
		this.parts = parts;
		this.energy = energy;
		this.life = life;
		this.craftsmanOnly = craftsmanOnly;
	}

	/** 会不会损耗。 */
	public boolean wears() {
		return life > 0;
	}

	public boolean fits( Item item ) {
		return Slot.of( item ) == slot;
	}

	private String key() {
		return name().toLowerCase( Locale.ROOT );
	}

	public String title() {
		return Messages.get( ModType.class, key() + ".name" );
	}

	public String desc() {
		return Messages.get( ModType.class, key() + ".desc" );
	}

	/** 按名字找种类；不认识的名字返回 null（读档时用）。 */
	public static ModType byName( String name ) {
		if (name == null) return null;
		for (ModType type : values()) {
			if (type.name().equals( name )) return type;
		}
		return null;
	}
}
