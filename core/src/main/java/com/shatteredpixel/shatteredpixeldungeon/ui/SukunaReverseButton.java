package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ReverseCursedTechnique;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

/** 反转术式开关。 */
public class SukunaReverseButton extends StyledButton {

	private boolean on = false;

	public SukunaReverseButton() {
		super(Chrome.Type.GREY_BUTTON, "反转", 6);
		textColor(0xFFFFFF);
	}

	@Override
	protected void onClick() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return;
		}
		if (hero.buff(ReverseCursedTechnique.class) != null) {
			Buff.detach(hero, ReverseCursedTechnique.class);
			on = false;
		} else if (hero.pointsInTalent(Talent.IRON_BODY) > 0
				&& hero.buff(CursedEnergy.class) != null) {
			Buff.affect(hero, ReverseCursedTechnique.class);
			on = true;
		}
		refresh();
	}

	@Override
	public void update() {
		super.update();
		Hero hero = Dungeon.hero;
		boolean active = hero != null && hero.buff(ReverseCursedTechnique.class) != null;
		if (active != on) {
			on = active;
			refresh();
		}
	}

	private void refresh() {
		text(on ? "反转·开" : "反转");
	}
}
