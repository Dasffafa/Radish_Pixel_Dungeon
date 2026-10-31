/*
 * Radish Pixel Dungeon
 *
 * 舔狗面具：典狱长死后掉落的纪念品。
 *
 * 目前只是一件收藏品（不可升级、无使用效果）——
 * 用户只要求“掉落一个舔狗面具”，具体功能未指定，先把道具与掉落链路做出来。
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class LickDogMask extends Item {

	{
		stackable = false;
		image = ItemSpriteSheet.MASK;

		unique = true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
