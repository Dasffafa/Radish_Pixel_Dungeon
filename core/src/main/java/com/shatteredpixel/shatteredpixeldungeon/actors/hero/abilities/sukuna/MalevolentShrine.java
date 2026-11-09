package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShrineDomain;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

/** 伏魔御厨子。 */
public class MalevolentShrine extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	public String icon() {
		return HeroIcon.MALEVOLENT_SHRINE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.SHRINE_POWER };
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (hero.buff(ShrineDomain.class) != null) {
			return;
		}
		armor.charge -= chargeUse(hero);
		Item.updateQuickslot();
		ShrineDomain domain = Buff.affect(hero, ShrineDomain.class);
		domain.setCenter(hero.pos);
		domain.applyMastery(hero);
	}
}