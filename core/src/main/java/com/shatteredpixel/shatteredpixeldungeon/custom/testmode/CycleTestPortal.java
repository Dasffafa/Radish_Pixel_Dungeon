package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

import java.util.ArrayList;

/**
 * 测试工具：直接进入循环门测试支线。
 */
public class CycleTestPortal extends TestItem {

	private static final String AC_ENTER = "ENTER";

	{
		image = ItemSpriteSheet.SOMETHING;
		defaultAction = AC_ENTER;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_ENTER);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_ENTER.equals(action)) {
			return;
		}
		if (Branches.TEST.equals(Dungeon.branchId)) {
			GLog.w(Messages.get(this, "already"));
			return;
		}
		if (!Branches.MAIN.equals(Dungeon.branchId) || Dungeon.depth != 1) {
			GLog.w(Messages.get(this, "wrong_depth"));
			return;
		}

		LevelTransition source = LevelTransition.branchDown(Dungeon.level, hero.pos,
				"cycletest:main", Branches.TEST, 1);
		InterlevelScene.curTransition = source;
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		Game.switchScene(InterlevelScene.class);
	}
}