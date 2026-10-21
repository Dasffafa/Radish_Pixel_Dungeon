package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 戏卡者的抽卡驱动器：没有手牌且冷却结束时自动抽卡（不消耗回合）。
 * 常驻 buff，由 CardDeck.getOrCreate 挂载。
 */
public class CardAutoDraw extends Buff {

	{
		type = buffType.NEUTRAL;
		actPriority = HERO_PRIO + 6;
	}

	@Override
	public boolean act() {
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			if (hero.heroClass == HeroClasses.CARDTRICKSTER && hero.isAlive() && hero.ready) {
				CardDrawer.autoDraw(hero);
			}
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}
}
