package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Grenadier extends HeroSubClass {

	public Grenadier() {
		super("GRENADIER", HeroIcon.GRENADIER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{
				Talent.EXPLOSION_PROOF, Talent.SHOCKWAVE, Talent.ADDED_POWDER };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.URGENT_DEMOLITION, Talent.CRITICAL_OPERATION };
	}
}
