/*
 * Radish Pixel Dungeon
 * 炼金学徒挑战：规则判定与可调参数的集中点
 */

package com.shatteredpixel.shatteredpixeldungeon.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;

public class AlchemyApprenticeChallenge {

	//每个炼金配方额外消耗的炼金能量
	public static final int EXTRA_ENERGY_COST = 2;

	//原本不消耗能量的配方（种子酿药水、卷轴拆符石、冰霜炸弹、羊毛炸弹）是否也要加价。
	//设计原文是「所有炼金配方」，这里按字面实现；如果只想让本来就耗能的配方变贵，把它改成 false 即可
	public static final boolean CHARGE_FREE_RECIPES = true;

	public static boolean isActive(){
		//配方图鉴在标题界面也能打开，那时 Dungeon.challenges 还留着上一局或刚看过的排行榜记录的值，
		//所以要先确认当前确实有一局游戏
		return Dungeon.hero != null && Dungeon.isChallenged(Challenges.ALCHEMY_APPRENTICE);
	}

	//配方自身的基础消耗经过本挑战之后的数值。
	//加价算在配方本身上，所以它先于工匠的减免和炼金工具箱的代付结算
	public static int recipeCost( int baseCost ){
		if (!isActive()) return baseCost;
		if (baseCost <= 0 && !CHARGE_FREE_RECIPES) return baseCost;
		return baseCost + EXTRA_ENERGY_COST;
	}

	//把这件物品转化为炼金能量时是否不再顺带鉴定它。
	//设计原文只提到药水与卷轴（合剂、秘卷也是它们的子类），其它物品照旧
	public static boolean blocksEnergizeIdentify( Item item ){
		return isActive() && (item instanceof Potion || item instanceof Scroll);
	}

}
