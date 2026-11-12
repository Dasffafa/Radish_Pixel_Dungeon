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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * 一件装备上的改装部件列表（0 到 2 项）。
 *
 * 挂在 Weapon / Armor / Wand 的 mods 字段上，没有改装时那个字段是 null。
 * 这里只管数据和存取，数值怎么变、什么时候扣耐久都在 ModHooks 里。
 */
public class ModState {

	/** 一项改装。 */
	public static class Entry {
		public ModType type;
		//剩余耐久；不会损耗的种类恒为 0
		public int left;
		//耐久上限（制作时已经算进「耐用套件」的加成）
		public int max;
		//制作与维修累计花掉的零件数，「动能破片」按它算伤害
		public int partsSpent;
		//制作时是否花过炼金能量，「有效回收」按它决定退不退能量
		public boolean usedEnergy;

		public Entry copy() {
			Entry e = new Entry();
			e.type = type;
			e.left = left;
			e.max = max;
			e.partsSpent = partsSpent;
			e.usedEnergy = usedEnergy;
			return e;
		}
	}

	private final ArrayList<Entry> entries = new ArrayList<>();

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public int size() {
		return entries.size();
	}

	/** 按安装先后排列的副本；改这个列表不影响装备。 */
	public List<Entry> entries() {
		return new ArrayList<>( entries );
	}

	public Entry find( ModType type ) {
		for (Entry e : entries) {
			if (e.type == type) return e;
		}
		return null;
	}

	public boolean has( ModType type ) {
		return find( type ) != null;
	}

	/** 装上一项新的（满耐久）。同一种已经有了就先拿掉旧的。 */
	public Entry add( ModType type, int max, int partsSpent, boolean usedEnergy ) {
		remove( type );
		Entry e = new Entry();
		e.type = type;
		e.max = Math.max( 0, max );
		e.left = e.max;
		e.partsSpent = partsSpent;
		e.usedEnergy = usedEnergy;
		entries.add( e );
		return e;
	}

	public boolean remove( ModType type ) {
		Entry e = find( type );
		return e != null && entries.remove( e );
	}

	public boolean remove( Entry entry ) {
		return entries.remove( entry );
	}

	public void clear() {
		entries.clear();
	}

	public ModState copy() {
		ModState copy = new ModState();
		for (Entry e : entries) {
			copy.entries.add( e.copy() );
		}
		return copy;
	}

	// ---- 存取 ----

	private static final String MODS       = "mods";
	private static final String TYPES      = "types";
	private static final String LEFT       = "left";
	private static final String MAX        = "max";
	private static final String PARTS      = "parts";
	private static final String ENERGY     = "energy";

	/** 写进装备自己的存档块里；没有改装时什么都不写。 */
	public void storeInBundle( Bundle bundle ) {
		if (entries.isEmpty()) return;

		int n = entries.size();
		String[] types = new String[n];
		int[] left = new int[n];
		int[] max = new int[n];
		int[] parts = new int[n];
		boolean[] energy = new boolean[n];
		for (int i = 0; i < n; i++) {
			Entry e = entries.get( i );
			types[i] = e.type.name();
			left[i] = e.left;
			max[i] = e.max;
			parts[i] = e.partsSpent;
			energy[i] = e.usedEnergy;
		}

		Bundle node = new Bundle();
		node.put( TYPES, types );
		node.put( LEFT, left );
		node.put( MAX, max );
		node.put( PARTS, parts );
		node.put( ENERGY, energy );
		bundle.put( MODS, node );
	}

	/**
	 * 从装备的存档块里读。没有记录（旧存档、没改装过）返回 null。
	 * 只读存档里的数据，不碰英雄和关卡：读档时它们还没建好。
	 */
	public static ModState restoreFromBundle( Bundle bundle ) {
		if (!bundle.contains( MODS )) return null;

		Bundle node = bundle.getBundle( MODS );
		if (!node.contains( TYPES )) return null;

		String[] types = node.getStringArray( TYPES );
		int[] left = node.contains( LEFT ) ? node.getIntArray( LEFT ) : null;
		int[] max = node.contains( MAX ) ? node.getIntArray( MAX ) : null;
		int[] parts = node.contains( PARTS ) ? node.getIntArray( PARTS ) : null;
		boolean[] energy = node.contains( ENERGY ) ? node.getBooleanArray( ENERGY ) : null;
		if (types == null) return null;

		ModState state = new ModState();
		for (int i = 0; i < types.length; i++) {
			//以后删掉或改名的种类，旧存档里读到就丢弃这一项
			ModType type = ModType.byName( types[i] );
			if (type == null) continue;

			Entry e = new Entry();
			e.type = type;
			e.max = max != null && i < max.length ? max[i] : type.life;
			e.left = left != null && i < left.length ? left[i] : e.max;
			e.partsSpent = parts != null && i < parts.length ? parts[i] : type.parts;
			e.usedEnergy = energy != null && i < energy.length ? energy[i] : type.energy > 0;
			state.entries.add( e );
		}
		return state.isEmpty() ? null : state;
	}

	// ---- 装备上的存取 ----

	/** 这件装备的改装列表；没有改装（或不是可改装的装备）返回 null。 */
	public static ModState of( Item item ) {
		ModState state = null;
		if (item instanceof Weapon)     state = ((Weapon) item).mods;
		else if (item instanceof Armor) state = ((Armor) item).mods;
		else if (item instanceof Wand)  state = ((Wand) item).mods;
		return state == null || state.isEmpty() ? null : state;
	}

	/** 取这件装备的改装列表，没有就建一个空的。不是可改装的装备返回 null。 */
	public static ModState ensure( Item item ) {
		if (item instanceof Weapon) {
			Weapon w = (Weapon) item;
			if (w.mods == null) w.mods = new ModState();
			return w.mods;
		} else if (item instanceof Armor) {
			Armor a = (Armor) item;
			if (a.mods == null) a.mods = new ModState();
			return a.mods;
		} else if (item instanceof Wand) {
			Wand w = (Wand) item;
			if (w.mods == null) w.mods = new ModState();
			return w.mods;
		}
		return null;
	}

	public static void set( Item item, ModState state ) {
		if (state != null && state.isEmpty()) state = null;
		if (item instanceof Weapon)     ((Weapon) item).mods = state;
		else if (item instanceof Armor) ((Armor) item).mods = state;
		else if (item instanceof Wand)  ((Wand) item).mods = state;
	}

	/** 装备被换成另一个对象（嬗变、升级成英雄护甲）时，把改装带过去。 */
	public static void copyTo( Item from, Item to ) {
		ModState state = of( from );
		//只在两件是同一类装备时带过去：武器的改装装不到护甲上
		if (state != null && ModType.Slot.of( from ) == ModType.Slot.of( to )) {
			set( to, state.copy() );
		}
	}

	public static boolean has( Item item, ModType type ) {
		ModState state = of( item );
		return state != null && state.has( type );
	}

	public static Entry find( Item item, ModType type ) {
		ModState state = of( item );
		return state == null ? null : state.find( type );
	}

	/** 这件装备能同时装几项改装：平时 1 项，工匠的「多重改造」依次给护甲、武器、法杖开第 2 项。 */
	public static int slots( Hero hero, Item item ) {
		ModType.Slot slot = ModType.Slot.of( item );
		if (slot == null) return 0;
		if (hero == null) return 1;

		int points = hero.pointsInTalent( Talent.MULTI_MODIFY );
		switch (slot) {
			case ARMOR:  return points >= 1 ? 2 : 1;
			case WEAPON: return points >= 2 ? 2 : 1;
			case WAND:   return points >= 3 ? 2 : 1;
			default:     return 1;
		}
	}
}
