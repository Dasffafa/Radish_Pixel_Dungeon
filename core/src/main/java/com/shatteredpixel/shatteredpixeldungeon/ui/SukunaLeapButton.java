package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna.CurseLeap;

/** 后跃斩按钮。 */
public class SukunaLeapButton extends IconButton {

	public SukunaLeapButton() {
		super(new TalentIcon(Talent.SUKUNA_LOW_HP));
	}

	@Override
	protected void onClick() {
		Hero hero = Dungeon.hero;
		if (hero != null) {
			CurseLeap.use(hero);
		}
	}

	@Override
	public void update() {
		super.update();
		Hero hero = Dungeon.hero;
		boolean usable = hero != null && CurseLeap.canUse(hero);
		icon.alpha(usable ? 1f : 0.4f);
	}
}
