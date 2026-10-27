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

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;

import java.util.ArrayList;
import java.util.List;

/**
 * 游商的货单。
 *
 * 移植自 Darkest PD 的 BarterMan.initSellItems()（_upstream/darkest/.../npcs/BarterMan.kt:35-54）：
 * 1~3 件药水、1~3 件卷轴、2~5 件【近战】武器。
 * 注意它是随机货单，不是每位游商固定卖一样东西。
 */
public final class PeddlerCatalog {

	private PeddlerCatalog() {
	}

	/** 一楼一共几位游商。 */
	public static final int COUNT = 7;

	/** 货架上最多放几件（对齐 Merchant 的 CAPACITY）。 */
	public static final int CAPACITY = 25;

	/**
	 * 生成一份随机货单。
	 *
	 * 对应 BarterMan.initSellItems()：
	 * <pre>
	 * val np = Random.IntRange(1, 3)
	 * val ns = Random.IntRange(1, 3)
	 * var nw = Random.NormalIntRange(2, 5)
	 * repeat(np) { addItemToSell(Generator.POTION.generate()) }
	 * repeat(ns) { addItemToSell(Generator.SCROLL.generate()) }
	 * while (nw &gt; 0) { val w = Generator.WEAPON.generate(); if (w is MeleeWeapon) { addItemToSell(w); nw-- } }
	 * </pre>
	 */
	public static List<Item> rollGoods() {
		List<Item> out = new ArrayList<>();

		int nPotions = com.watabou.utils.Random.IntRange(1, 3);
		int nScrolls = com.watabou.utils.Random.IntRange(1, 3);
		int nWeapons = com.watabou.utils.Random.NormalIntRange(2, 5);

		for (int i = 0; i < nPotions; i++) {
			out.add(Generator.random(Generator.Category.POTION));
		}
		for (int i = 0; i < nScrolls; i++) {
			out.add(Generator.random(Generator.Category.SCROLL));
		}

		//只收近战武器：远程/投掷类不进货（与 BarterMan 的 `if (w is MeleeWeapon)` 一致）
		int guard = 0;
		while (nWeapons > 0 && guard++ < 100) {
			Item w = Generator.random(Generator.Category.WEAPON);
			if (w instanceof MeleeWeapon) {
				out.add(w);
				nWeapons--;
			}
		}

		return out;
	}
}
