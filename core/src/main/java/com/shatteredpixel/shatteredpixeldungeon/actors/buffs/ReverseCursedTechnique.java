package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 反转术式：每回合消耗咒力恢复 1 点生命。 */
public class ReverseCursedTechnique extends Buff {

	{
		actPriority = BUFF_PRIO - 3;
	}

	public static int cost(Hero hero) {
		return hero.pointsInTalent(Talent.IRON_BODY) >= 2 ? 3 : 5;
	}

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		Hero hero = (Hero) target;

		if (hero.pointsInTalent(Talent.IRON_BODY) <= 0) {
			detach();
			return true;
		}

		CursedEnergy ce = hero.buff(CursedEnergy.class);
		int cost = cost(hero);
		if (ce == null || ce.energy() < cost) {
			detach();
			return true;
		}

		if (hero.HP < hero.HT) {
			ce.spend(cost);
			hero.HP = Math.min(hero.HT, hero.HP + 1);
			hero.sprite.operate(hero.pos);
		}

		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.HEALING;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		Hero hero = target instanceof Hero ? (Hero) target : null;
		return Messages.get(this, "desc", hero == null ? 5 : cost(hero));
	}

	@Override
	public void tintIcon(com.watabou.noosa.Image icon) {
		icon.hardlight(0.5f, 1f, 0.6f);
	}
}
