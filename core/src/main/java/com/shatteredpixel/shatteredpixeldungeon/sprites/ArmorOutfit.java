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
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ColdIronCoat;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CountessDress;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PrismArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SoulMix;

/**
 * 六个角色 × 四套护甲动画的外观映射。
 *
 * 每张 armor_outfit_N.png 是一个角色的 5 行动画（行高 15、每行 21 帧 12×15），
 * 前四行依次对应伯爵礼服 / 寒铁大衣 / 棱镜护铠 / 混灵。
 * 六张按 HeroClasses.ALL 的前六个职业顺序对应。
 */
public final class ArmorOutfit {

	private static final String[] SHEETS = {
			Assets.Sprites.ARMOR_OUTFIT_1, Assets.Sprites.ARMOR_OUTFIT_2,
			Assets.Sprites.ARMOR_OUTFIT_3, Assets.Sprites.ARMOR_OUTFIT_4,
			Assets.Sprites.ARMOR_OUTFIT_5, Assets.Sprites.ARMOR_OUTFIT_6
	};

	private ArmorOutfit() {}

	public static String sheetFor( HeroClass cl ){
		int i = 0;
		for (HeroClass h : HeroClasses.ALL){
			if (h == cl) return i < SHEETS.length ? SHEETS[i] : null;
			i++;
		}
		return null;
	}

	public static int rowFor( Armor armor ){
		if (armor instanceof CountessDress) return 0;
		if (armor instanceof ColdIronCoat) return 1;
		if (armor instanceof PrismArmor)   return 2;
		if (armor instanceof SoulMix)      return 3;
		return -1;
	}
}
