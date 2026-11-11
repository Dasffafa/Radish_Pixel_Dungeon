package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/** 宿傩形态切换按钮：近战 / 斩击（斩击时图标泛红）。 */
public class SukunaStanceButton extends IconButton {

	private final ItemSprite sprite;

	public SukunaStanceButton() {
		super(new ItemSprite(ItemSpriteSheet.SUKUNA_CLAWS));
		sprite = (ItemSprite) icon;
	}

	@Override
	protected void onClick() {
		Hero hero = Dungeon.hero;
		if (hero != null && hero.belongings.weapon() instanceof SukunaClaws) {
			((SukunaClaws) hero.belongings.weapon()).toggleStance();
			com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();
		}
	}

	@Override
	public void update() {
		super.update();
		Hero hero = Dungeon.hero;
		boolean ranged = hero != null
				&& hero.belongings.weapon() instanceof SukunaClaws
				&& ((SukunaClaws) hero.belongings.weapon()).isRanged();
		if (ranged) {
			sprite.hardlight(1f, 0.45f, 0.45f);
		} else {
			sprite.resetColor();
		}
	}
}
