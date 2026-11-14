package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class Jumble extends HeroSubClass {

	public Jumble() {
		super("JUMBLE", HeroIcon.NONE);
	}

	@Override public Talent[] subclassT3() { return new Talent[0]; }
	@Override public Talent[] subclassT4() { return new Talent[0]; }
}
