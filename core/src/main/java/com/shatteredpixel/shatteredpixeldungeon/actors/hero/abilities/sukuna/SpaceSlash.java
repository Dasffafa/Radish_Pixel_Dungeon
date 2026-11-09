package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpaceSlashChant;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class SpaceSlash extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	public String targetingPrompt() {
		return null;
	}

	@Override
	public String icon() {
		return HeroIcon.SPACE_SLASH;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.SPACE_SLASH_POWER };
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (hero.buff(SpaceSlashChant.class) != null) {
			return;
		}
		if (target == null || target == hero.pos) {
			return;
		}

		armor.charge -= chargeUse(hero);
		Item.updateQuickslot();
		SpaceSlashChant chant = Buff.affect(hero, SpaceSlashChant.class);
		chant.startedAt = com.watabou.noosa.Game.timeTotal;
		chant.setDirection(hero, target);
	}
}