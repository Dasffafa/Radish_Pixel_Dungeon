package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/** 反转术式：每 10 回合回复 1 点生命。 */
public class ReverseCursedTechnique extends Buff {

	private int counter = 0;

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		Hero hero = (Hero) target;

		counter++;
		if (counter >= 10) {
			counter = 0;
			if (hero.HP < hero.HT) {
				hero.HP = Math.min(hero.HT, hero.HP + 1);
			}
		}

		spend(TICK);
		return true;
	}
}