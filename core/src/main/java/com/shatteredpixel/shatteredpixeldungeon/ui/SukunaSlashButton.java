package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpaceSlashChant;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/** 宿傩专属 HUD 按钮：吟唱期间点一次蓄力一次。 */
public class SukunaSlashButton extends IconButton {

	public SukunaSlashButton() {
		super(new ItemSprite(new SukunaClaws()));
	}

	@Override
	public void onClick() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return;
		}
		SpaceSlashChant chant = hero.buff(SpaceSlashChant.class);
		if (chant == null) {
			return;
		}
		chant.charge();
		hero.spendAndNext(1f);
	}
}