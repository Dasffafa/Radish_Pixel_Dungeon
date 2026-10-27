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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Firebomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FrostBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ShockBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SupplyRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;

import java.util.ArrayList;
import java.util.List;

/**
 * 游商目录 —— 名字、台词、货单、贴图变体全在这里。
 *
 * 你之后调整内容，只需要改这个文件：
 *  - 改名字/台词：改 assets/messages/actors_zh.properties 里 actors.mobs.npcs.Peddler.name_N 等键
 *  - 改卖什么：改下面 GOODS[N]（N = 游商序号 0~6）
 *  - 改用哪张图：改 PEDDLERS[N] 的贴图行号（对应 sprites/npc_peddlerN.png）
 *
 * 价格一律走标准商人价（Shopkeeper 的算法），这里不写价格。
 */
public final class PeddlerCatalog {

	private PeddlerCatalog() {
	}

	/** 一楼一共几个游商。 */
	public static final int COUNT = 7;

	/**
	 * 每位游商的货单。
	 * 首位是「主力货」，后面几样是搭头，总计价格大致贴近标准商店。
	 */
	public static List<Item> itemsFor(int variant) {
		List<Item> out = new ArrayList<>();
		Item item = primaryFor(variant);
		if (item != null) {
			out.add(item);
		}
		for (Item extra : extrasFor(variant)) {
			out.add(extra);
		}
		return out;
	}

	/** 每位游商的主打货。 */
	private static Item primaryFor(int variant) {
		switch (variant) {
			//0 粉发少女 —— 药水
			case 0:
				return new PotionOfHealing();
			//1 紫发青色 —— 鉴定
			case 1:
				return new ScrollOfIdentify();
			//2 白金发 —— 强化
			case 2:
				return new ScrollOfUpgrade();
			//3 灰紫发 —— 地图
			case 3:
				return new ScrollOfMagicMapping();
			//4 蓝发护士 —— 解咒
			case 4:
				return new ScrollOfRemoveCurse();
			//5 白发红眼 —— 力量
			case 5:
				return new PotionOfStrength();
			//6 粉色兔耳 —— 传送
			case 6:
			default:
				return new ScrollOfTeleportation();
		}
	}

	/** 每位游商的搭配货。 */
	private static List<Item> extrasFor(int variant) {
		List<Item> out = new ArrayList<>();
		switch (variant) {
			case 0:
				out.add(new PotionOfHealing());
				out.add(new PotionOfPurity());
				out.add(new Pasty());
				break;
			case 1:
				out.add(new ScrollOfIdentify());
				out.add(new PotionOfMindVision());
				out.add(new SupplyRation());
				break;
			case 2:
				out.add(new WornShortsword());
				out.add(new Dagger());
				out.add(new Food());
				break;
			case 3:
				out.add(new WornShortsword());
				out.add(new Quarterstaff());
				out.add(new PotionOfFrost());
				break;
			case 4:
				out.add(new PotionOfPurity());
				out.add(new PotionOfHaste());
				out.add(new Firebomb());
				break;
			case 5:
				out.add(new PotionOfExperience());
				out.add(new PotionOfInvisibility());
				out.add(new FrostBomb());
				break;
			case 6:
			default:
				out.add(new PotionOfLiquidFlame());
				out.add(new ShockBomb());
				out.add(new Pasty());
				break;
		}
		return out;
	}
}
