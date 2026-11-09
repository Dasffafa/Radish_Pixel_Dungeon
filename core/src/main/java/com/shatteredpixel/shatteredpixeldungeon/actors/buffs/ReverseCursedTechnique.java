package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

/** 反转术式：按与饥饿相同的速度修复肉体，饱食时缓慢回血，饥饿时刚好抵消损耗。 */
public class ReverseCursedTechnique extends Buff {

	private float partialHeal = 0f;

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		Hero hero = (Hero) target;

		if (hero.HP < hero.HT) {
			float rate = hero.HT / 1000f;
			rate *= 1f + 0.5f * hero.pointsInTalent(Talent.HUISHENG_REGEN);
			partialHeal += rate;

			if (partialHeal > 1f) {
				int heal = (int) partialHeal;
				hero.HP = Math.min(hero.HT, hero.HP + heal);
				partialHeal -= heal;
			}
		} else {
			partialHeal = 0f;
		}

		spend(TICK);
		return true;
	}
}
