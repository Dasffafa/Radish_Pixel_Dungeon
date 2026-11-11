package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ReverseCursedTechnique;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

/** 反转术式开关按钮。 */
public class SukunaReverseButton extends IconButton {

	private final TalentIcon talentIcon;

	public SukunaReverseButton() {
		super(new TalentIcon(Talent.IRON_BODY));
		talentIcon = (TalentIcon) icon;
	}

	@Override
	protected void onClick() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return;
		}
		if (hero.buff(ReverseCursedTechnique.class) != null) {
			Buff.detach(hero, ReverseCursedTechnique.class);
		} else if (hero.pointsInTalent(Talent.IRON_BODY) <= 0) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("尚未学会反转术式。");
		} else if (hero.buff(CursedEnergy.class) == null) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("没有咒力。");
		} else {
			Buff.affect(hero, ReverseCursedTechnique.class);
		}
	}

	@Override
	public void update() {
		super.update();
		Hero hero = Dungeon.hero;
		boolean active = hero != null && hero.buff(ReverseCursedTechnique.class) != null;
		if (active) {
			talentIcon.hardlight(0.5f, 1f, 0.6f);
		} else {
			talentIcon.resetColor();
		}
	}
}
