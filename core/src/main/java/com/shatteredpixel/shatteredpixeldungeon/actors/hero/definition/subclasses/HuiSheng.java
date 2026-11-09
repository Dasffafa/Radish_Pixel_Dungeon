package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class HuiSheng extends HeroSubClass {

	public HuiSheng() {
		super("HUISHENG", HeroIcon.NONE);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.HEARTY_MEAL, Talent.IRON_STOMACH };
	}

	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.IRON_WILL, Talent.HIGH_DIET };
	}
}