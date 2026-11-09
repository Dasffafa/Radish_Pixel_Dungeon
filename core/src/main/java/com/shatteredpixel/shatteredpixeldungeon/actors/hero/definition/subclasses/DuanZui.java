package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class DuanZui extends HeroSubClass {

	public DuanZui() {
		super("DUANZUI", HeroIcon.NONE);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.LETHAL_MOMENTUM, Talent.STRONGMAN };
	}

	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.IRON_MUSCLE, Talent.HIGH_DIET };
	}
}