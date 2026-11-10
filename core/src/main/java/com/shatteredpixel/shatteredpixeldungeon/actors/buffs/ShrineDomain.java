package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.SukunaSlashEffect;

/** 伏魔御厨子：领域展开期间每回合斩击范围内所有敌人。 */
public class ShrineDomain extends Buff {

	private int turns = 10;

	private int center = -1;
	private int radius = 20;

	public void setCenter(int cell) {
		center = cell;
	}

	public void applyMastery(Hero hero) {
		int points = hero.pointsInTalent(
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SHRINE_MASTERY);
		turns += 2 * points;
		radius = 20 + 3 * points;
	}

	{
		actPriority = 1;
	}

	@Override
	public void detach() {
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.setDomainOverlay(false, center);
		super.detach();
	}

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		Hero hero = (Hero) target;

		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.setDomainOverlay(true, center < 0 ? hero.pos : center);

		int dmg = 5 + hero.lvl / 2;
		dmg = Math.round(dmg * (1f + 0.08f * hero.pointsInTalent(
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SHRINE_POWER)));
		for (Char ch : Actor.chars()) {
			if (ch == hero || !ch.isAlive()) {
				continue;
			}
			if (Dungeon.level.distance(center < 0 ? hero.pos : center, ch.pos) <= radius) {
				com.shatteredpixel.shatteredpixeldungeon.effects.SlashLinesEffect.show(ch);
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