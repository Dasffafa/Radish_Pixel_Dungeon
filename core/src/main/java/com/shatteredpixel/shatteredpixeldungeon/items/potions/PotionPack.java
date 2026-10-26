/*
 * Radish Pixel Dungeon
 * Copyright (C) 2026
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

package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 投掷药水包：奇械师炼金配方的产物。
 *
 * <p>把至多 3 瓶药剂打包成一个物品，投掷时一次性砸碎并释放全部药剂效果。</p>
 */
public class PotionPack extends Item {

	{
		image = ItemSpriteSheet.POTION_PACK;
		stackable = false;
	}

	public ArrayList<Potion> potions = new ArrayList<>();

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public boolean isEmpty() {
		return potions.isEmpty();
	}

	@Override
	public String name() {
		return super.name();
	}

	@Override
	public String info() {
		StringBuilder sb = new StringBuilder( super.info() );
		if (!potions.isEmpty()) {
			sb.append( "\n\n" );
			for (Potion p : potions) {
				sb.append( "\n• " ).append( p.name() );
			}
		}
		return sb.toString();
	}

	@Override
	protected void onThrow( int cell ) {
		if (curUser == null) {
			return;
		}
		for (Potion p : potions) {
			if (p != null) {
				p.shatter( cell );
			}
		}
		potions.clear();
	}

	@Override
	public int value() {
		int v = 0;
		for (Potion p : potions) {
			v += p.value();
		}
		return v;
	}

	private static final String CONTENTS = "contents";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		StringBuilder sb = new StringBuilder();
		for (Potion p : potions) {
			if (p == null) continue;
			if (sb.length() > 0) sb.append( '|' );
			sb.append( p.getClass().getName() );
		}
		bundle.put( CONTENTS, sb.toString() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		potions.clear();
		String raw = bundle.getString( CONTENTS );
		if (raw == null || raw.isEmpty()) return;
		for (String cn : raw.split( "\\|" )) {
			try {
				Potion p = (Potion) Class.forName( cn ).newInstance();
				p.identify();
				potions.add( p );
			} catch (Exception e) {
				// 旧存档里可能出现已删除的药剂类型，直接忽略
			}
		}
	}

	/** 供配方使用：把一瓶药剂放进包里。 */
	public void add( Potion p ) {
		if (p != null && potions.size() < 3) {
			p.quantity( 1 );
			potions.add( p );
		}
	}
}
