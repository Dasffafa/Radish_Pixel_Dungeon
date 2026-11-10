package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** 宿傩的咒力：近战每回合回复，斩击形态下消耗。 */
public class CursedEnergy extends Buff {

	public static final int BASE_MAX = 100;
	public static final int PER_LEVEL = 10;
	public static final int REGEN = 10;

	private int energy = BASE_MAX;

	{
		actPriority = BUFF_PRIO - 2;
	}

	public int energy() {
		return energy;
	}

	public int max() {
		Hero hero = target instanceof Hero ? (Hero) target : Dungeon.hero;
		int lvl = hero == null ? 1 : Math.max(1, hero.lvl);
		int bonus = hero == null ? 0
				: 10 * hero.pointsInTalent(
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.CURSED_SURGE);
		return BASE_MAX + PER_LEVEL * (lvl - 1) + bonus;
	}

	public void spend(int amount) {
		energy = Math.max(0, energy - amount);
	}

	public void restoreAll() {
		energy = max();
	}

	public boolean isRanged() {
		Hero hero = Dungeon.hero;
		return hero != null
				&& hero.belongings.weapon() instanceof SukunaClaws
				&& ((SukunaClaws) hero.belongings.weapon()).isRanged();
	}

	@Override
	public boolean act() {
		int max = max();
		if (energy > max) {
			energy = max;
		}
		//只有近战形态可以自然回复咒力
		if (target instanceof Hero && !isRanged()) {
			energy = Math.min(max, energy + REGEN);
		}
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.MONK_ENERGY;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(energy);
	}

	@Override
	public void tintIcon(com.watabou.noosa.Image icon) {
		icon.hardlight(0.55f, 0.15f, 0.75f);
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", energy, max());
	}

	private static final String ENERGY = "energy";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ENERGY, energy);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		energy = bundle.getInt(ENERGY);
	}
}