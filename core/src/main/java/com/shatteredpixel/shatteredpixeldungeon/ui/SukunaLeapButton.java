package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna.CurseLeap;

/** 后跃斩按钮。 */
public class SukunaLeapButton extends StyledButton {

	public SukunaLeapButton() {
		super(Chrome.Type.GREY_BUTTON, "后跃", 6);
		textColor(0xFFFFFF);
	}

	@Override
	protected void onClick() {
		Hero hero = Dungeon.hero;
		if (hero != null) {
			CurseLeap.use(hero);
		}
	}


}
