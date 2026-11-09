package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpaceSlashChant;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 宿傩专属 HUD 按钮：选择方向后开始空间斩吟唱。 */
public class SukunaSlashButton extends IconButton {

	public SukunaSlashButton() {
		super(new ItemSprite(new SukunaClaws()));
	}

	@Override
	public void onClick() {
		Hero hero = Dungeon.hero;
		if (hero == null || hero.heroClass != HeroClasses.SUKUNA || !hero.isAlive()) {
			return;
		}
		if (hero.buff(SpaceSlashChant.class) != null) {
			GLog.w(Messages.get(this, "chanting"));
			return;
		}

		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null || cell == hero.pos) {
					return;
				}
				SpaceSlashChant chant = Buff.affect(hero, SpaceSlashChant.class);
				chant.startedAt = com.watabou.noosa.Game.timeTotal;
				chant.setDirection(hero, cell);
				GLog.p(Messages.get(SukunaSlashButton.class, "start"));
			}

			@Override
			public String prompt() {
				return Messages.get(SukunaSlashButton.class, "prompt");
			}
		});
	}
}