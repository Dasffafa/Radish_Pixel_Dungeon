package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class HuiSheng extends HeroSubClass {

	public HuiSheng() {
		super("HUISHENG", HeroIcon.HUISHENG);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.HUISHENG_HEAL, Talent.HUISHENG_VITALITY };
	}

	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.HUISHENG_REGEN, Talent.HUISHENG_REBIRTH };
	}
}