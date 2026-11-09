package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class QianRen extends HeroSubClass {

	public QianRen() {
		super("QIANREN", HeroIcon.QIANREN);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.QIANREN_ECHO, Talent.QIANREN_STORM };
	}

	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.QIANREN_REACH, Talent.QIANREN_MASTER };
	}
}