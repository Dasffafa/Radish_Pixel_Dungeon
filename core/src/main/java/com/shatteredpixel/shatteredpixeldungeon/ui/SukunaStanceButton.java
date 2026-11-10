package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;

/** 宿傩形态切换按钮，按钮上直接显示当前形态。 */
public class SukunaStanceButton extends StyledButton {

	private boolean lastRanged = false;

	public SukunaStanceButton() {
		super(Chrome.Type.GREY_BUTTON, "近战", 6);
		textColor(0xFFFFFF);
		refreshText();
	}

	@Override
	protected void onClick() {
		Hero hero = Dungeon.hero;
		if (hero != null && hero.belongings.weapon() instanceof SukunaClaws) {
			((SukunaClaws) hero.belongings.weapon()).toggleStance();
			com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();
			refreshText();
		}
	}

	@Override
	public void update() {
		super.update();
		Hero hero = Dungeon.hero;
		boolean ranged = hero != null
				&& hero.belongings.weapon() instanceof SukunaClaws
				&& ((SukunaClaws) hero.belongings.weapon()).isRanged();
		if (ranged != lastRanged) {
			lastRanged = ranged;
			refreshText();
		}
	}

	private void refreshText() {
		Hero hero = Dungeon.hero;
		boolean ranged = hero != null
				&& hero.belongings.weapon() instanceof SukunaClaws
				&& ((SukunaClaws) hero.belongings.weapon()).isRanged();
		text(ranged ? "斩击" : "近战");
	}
}
