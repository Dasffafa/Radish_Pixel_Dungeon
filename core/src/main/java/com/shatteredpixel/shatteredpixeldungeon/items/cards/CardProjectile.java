package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 仅用于卡牌弹道飞行动画的投掷物代理。
 */
public class CardProjectile extends Item {

	public CardProjectile() {
		image = ItemSpriteSheet.CARD_BLANK;
	}

	public CardProjectile(Card card) {
		image = (card != null && card.type != null) ? card.type.image : ItemSpriteSheet.CARD_BLANK;
	}
}
