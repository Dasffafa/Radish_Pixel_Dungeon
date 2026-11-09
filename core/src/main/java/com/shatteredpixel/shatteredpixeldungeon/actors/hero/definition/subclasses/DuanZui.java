package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class DuanZui extends HeroSubClass {

	public DuanZui() {
		super("DUANZUI", HeroIcon.DUANZUI);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.DUANZUI_EXECUTE };
	}

	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.DUANZUI_LINE };
	}
}