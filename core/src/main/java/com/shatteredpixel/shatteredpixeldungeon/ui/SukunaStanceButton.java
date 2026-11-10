package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/** 宿傩形态切换按钮：近战 / 斩击。 */
public class SukunaStanceButton extends IconButton {

	public SukunaStanceButton() {
		super(new ItemSprite(new SukunaClaws()));
	}

	@Override
	public void onClick() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return;
		}
		if (hero.belongings.weapon() instanceof SukunaClaws) {
			((SukunaClaws) hero.belongings.weapon()).toggleStance();
			com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();
		}
	}
}
