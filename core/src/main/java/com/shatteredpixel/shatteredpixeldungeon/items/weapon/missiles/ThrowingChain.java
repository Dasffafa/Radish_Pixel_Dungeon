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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 投掷武器链：奇械师炼金配方的产物。
 *
 * <p>把至多 3 件投掷武器串成一条链，投掷时全部砸向目标；
 * 命中后链子散开，每件投掷武器各消耗 1 点耐久。</p>
 */
public class ThrowingChain extends Item {

	{
		image = ItemSpriteSheet.THROWING_CHAIN;
		stackable = false;
	}

	public ArrayList<MissileWeapon> missiles = new ArrayList<>();

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public boolean isEmpty() {
		return missiles.isEmpty();
	}

	@Override
	public String info() {
		StringBuilder sb = new StringBuilder( super.info() );
		if (!missiles.isEmpty()) {
			sb.append( "\n\n" );
			for (MissileWeapon m : missiles) {
				sb.append( "\n• " ).append( m.name() );
			}
		}
		return sb.toString();
	}

	@Override
	protected void onThrow( int cell ) {
		if (!(curUser instanceof Hero)) {
			return;
		}
		Hero hero = (Hero) curUser;
		for (MissileWeapon m : missiles) {
			if (m == null) continue;
			m.cast( hero, cell );
			if (m.durabilityLeft() > 0) {
				m.repair( -Math.min( 1f, m.durabilityLeft() ) );
			}
		}
		missiles.clear();
	}

	@Override
	public int value() {
		int v = 0;
		for (MissileWeapon m : missiles) {
			v += m.value();
		}
		return v;
	}

	private static final String CONTENTS = "contents";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		StringBuilder sb = new StringBuilder();
		for (MissileWeapon m : missiles) {
			if (m == null) continue;
			if (sb.length() > 0) sb.append( '|' );
			sb.append( m.getClass().getName() ).append( ':' ).append( m.buffedLvl() );
		}
		bundle.put( CONTENTS, sb.toString() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		missiles.clear();
		String raw = bundle.getString( CONTENTS );
		if (raw == null || raw.isEmpty()) return;
		for (String entry : raw.split( "\\|" )) {
			try {
				String[] parts = entry.split( ":" );
				MissileWeapon m = (MissileWeapon) Class.forName( parts[0] ).newInstance();
				if (parts.length > 1) {
					m.level( Integer.parseInt( parts[1] ) );
				}
				m.identify();
				missiles.add( m );
			} catch (Exception e) {
				// 旧存档里可能出现已删除的武器类型，直接忽略
			}
		}
	}

	/** 供配方使用：把一件投掷武器串进链里。 */
	public void add( MissileWeapon m ) {
		if (m != null && missiles.size() < 3) {
			m.quantity( 1 );
			missiles.add( m );
		}
	}
}
