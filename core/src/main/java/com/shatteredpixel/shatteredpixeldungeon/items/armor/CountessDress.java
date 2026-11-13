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
 */
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** 伯爵礼服：二阶护甲，获得或花费金币后回复少量生命（赎回商品的花费不算）。 */
public class CountessDress extends Armor {

	{ image = ItemSpriteSheet.ARMOR_COUNTESS; }

	public CountessDress() { super( 2 ); }

	@Override public int DRMax(int lvl){ return 4 + 2 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 1 + lvl; }
	@Override protected ArmorBuff buff(){ return new CountessBuff(); }

	public class CountessBuff extends ArmorBuff {}

	@Override
	public String statsInfo() {
		return Messages.get( this, "stats_desc", Math.round( 1 + 0.5f * buffedLvl() ) );
	}

	/** 金币变化回调：Gold 拾取、Midas 附魔与三处商店扣款调用；赎回扣款刻意不调用。 */
	public static void onGoldChanged(){
		Hero hero = Dungeon.hero;
		if (hero == null || !(hero.belongings.armor instanceof CountessDress)) return;
		CountessDress dress = (CountessDress) hero.belongings.armor;
		int heal = Math.round( 1 + 0.5f * dress.buffedLvl() );
		if (heal <= 0 || hero.HP >= hero.HT || !hero.isAlive()) return;
		hero.HP = Math.min( hero.HT, hero.HP + heal );
		hero.sprite.showStatus( CharSprite.POSITIVE, "+" + heal );
	}
}
