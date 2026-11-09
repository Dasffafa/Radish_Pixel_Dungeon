package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** 宿傩的初始护甲：护甲值很低，但随等级提供额外生命上限。 */
public class CurseKimono extends ClothArmor {

	{
		image = ItemSpriteSheet.ARMOR_CLOTH;
	}

	public CurseKimono() {
		super();
	}

	@Override
	public int DRMax(int lvl) {
		return 1 + lvl;
	}

	@Override
	public int DRMin(int lvl) {
		return 1;
	}
}