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
 * 素材本身就是项目标准 12×15 网格、每行 21 帧：每张 256×80 的图里
 * 4 套动画各占 15 行，只是每张图顶部透明行数不同（y=5/10/8/9/9/8）。
 * 四行依次对应伯爵礼服 / 寒铁大衣 / 棱镜护铠 / 混灵，逐字节复制到对应
 * 角色贴图的第 18~21 行，零缩放、零裁剪、零拉伸。
 * 六张素材依次对应 战士 / 法师 / 盗贼 / 女猎手 / 牧师 / 戏卡者（红头发）；
 * 月华有专属贴图逻辑、工程师没有对应素材，都不加载新外观图。
 */
public final class ArmorOutfit {

	/**
	 * 与项目既有护甲（PrisonArmor=t7、DarkCoat=t9、RatArmor=t12 …）完全同一套机制：
	 * 四套新护甲动画直接追加在角色贴图 sprites/warrior.png 等的第 18~21 行
	 * （每行 21 帧 12x15，贴图扩到 256x512，原有 0..255 行像素保持不变），
	 * 这里只返回行索引，绝不切换贴图。
	 */
	public static final int ROW_COUNTESS = 18;
	public static final int ROW_COLDIRON = 19;
	public static final int ROW_PRISM    = 20;
	public static final int ROW_SOULMIX  = 21;

	private ArmorOutfit() {}

	/** 追加了 18~21 行的六张角色贴图；月华与工程师没有对应素材，保持原生护甲行。 */
	public static boolean hasOutfit( HeroClass cl ){
		return cl == HeroClasses.WARRIOR
				|| cl == HeroClasses.MAGE
				|| cl == HeroClasses.ROGUE
				|| cl == HeroClasses.HUNTRESS
				|| cl == HeroClasses.RECTOR
				|| cl == HeroClasses.CARDTRICKSTER;
	}

	public static int rowFor( Armor armor ){
		if (armor instanceof CountessDress) return ROW_COUNTESS;
		if (armor instanceof ColdIronCoat) return ROW_COLDIRON;
		if (armor instanceof PrismArmor)   return ROW_PRISM;
		if (armor instanceof SoulMix)      return ROW_SOULMIX;
		return -1;
	}
}
