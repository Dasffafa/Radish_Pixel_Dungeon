package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class QianRen extends HeroSubClass {

	public QianRen() {
		super("QIANREN", HeroIcon.NONE);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.QIANREN_ECHO };
	}

	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.QIANREN_REACH };
	}
}