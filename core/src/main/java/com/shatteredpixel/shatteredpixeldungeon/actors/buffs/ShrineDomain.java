package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.SukunaSlashEffect;

/** 伏魔御厨子：领域展开期间每回合斩击范围内所有敌人。 */
public class ShrineDomain extends Buff {

	private int turns = 5;

	{
		actPriority = 1;
	}

	@Override
	public void detach() {
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.setDomainOverlay(false);
		super.detach();
	}

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		Hero hero = (Hero) target;

		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.setDomainOverlay(true);

		int dmg = 10 + hero.lvl;
		for (Char ch : Actor.chars()) {
			if (ch == hero || !ch.isAlive()) {
				continue;
			}
			if (Dungeon.level.distance(hero.pos, ch.pos) <= 20) {
				com.shatteredpixel.shatteredpixeldungeon.effects.BloodSlashEffect.show(hero, ch);
				ch.damage(DamageInfo.physical(dmg, hero));
			}
		}

		turns--;
		if (turns <= 0) {
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}
}