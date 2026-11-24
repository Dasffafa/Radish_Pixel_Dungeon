/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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
 */

package com.shatteredpixel.shatteredpixeldungeon.items.rarity;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * A masterwork core, the way Warped's is spent: use it to push the worn weapon or armour five
 * kills further along its masterwork track (one level). A cheap stand-in for Warped's whole core
 * economy (which also rerolls lines and needs its own loot table).
 */
public class MasterworkCore extends Item {

	{
		image = ItemSpriteSheet.SOMETHING;
		stackable = true;
	}

	@Override
	public boolean isUpgradable(){
		return false;
	}

	@Override
	public boolean isIdentified(){
		return true;
	}

	@Override
	protected void onThrow( int cell ){
		//no throwing these away: they are too precious
		super.onThrow( cell );
	}

	@Override
	public void execute( Hero hero ){
		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate( hero.pos );
		apply( hero );
	}

	private void apply( Hero hero ){
		Item target = null;
		if (hero.belongings.weapon != null && hero.belongings.weapon instanceof Weapon){
			target = hero.belongings.weapon;
		} else if (hero.belongings.armor() != null){
			target = hero.belongings.armor();
		}

		if (target == null || Quality.masterworkLevel( target ) >= 5){
			GLog.w( "\u6ca1\u6709\u53ef\u5f3a\u5316\u7684\u88c5\u5907\uff08\u6216\u5df2\u6ee1\u7ea7\uff09" );
			return;
		}

		if (target instanceof Weapon){
			((Weapon)target).masterworkKills += 5;
		} else if (target instanceof Armor){
			((Armor)target).masterworkKills += 5;
		}
		detach( hero.belongings.backpack );
		GLog.i( "\u6770\u4f5c\u6838\u5fc3\u5df2\u88ab\u6d88\u8017\uff0c\u88c5\u5907\u664b\u5347\u81f3\u6770\u4f5c " + Quality.masterworkLevel( target ) + "/5" );
	}

	@Override
	public int value(){
		return 200 * quantity;
	}

	@Override
	public int energyVal(){
		return 20 * quantity;
	}

	@Override
	public String desc(){
		return "\u4e00\u9897\u6770\u4f5c\u6838\u5fc3\u3002\u4f7f\u7528\u540e\u4f1a\u628a\u5f53\u524d\u88c5\u5907\u7684\u6770\u4f5c\u8fdb\u5ea6\u63a8\u8fdb 5 \u6b21\u6740\u654c\uff08\u5373\u4e00\u7ea7\uff09\u3002\n\n"
				+ "\u6770\u4f5c\u6bcf\u5347\u4e00\u7ea7\uff0c\u8be5\u88c5\u5907\u81ea\u8eab\u7684\u7a00\u6709\u5ea6\u52a0\u6210\u518d\u63d0\u5347 15%\u3002";
	}
}
