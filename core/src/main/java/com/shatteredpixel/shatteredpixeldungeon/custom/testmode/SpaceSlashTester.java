package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpaceSlashChant;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/** 测试工具：选择方向后开始空间斩吟唱。 */
public class SpaceSlashTester extends TestItem {

	private static final String AC_CAST = "CAST";

	{
		image = ItemSpriteSheet.SCROLL_KAUNAN;
		defaultAction = AC_CAST;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_CAST);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_CAST.equals(action)) {
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
				GLog.p(Messages.get(SpaceSlashTester.class, "start"));
			}

			@Override
			public String prompt() {
				return Messages.get(SpaceSlashTester.class, "prompt");
			}
		});
	}
}