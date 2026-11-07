package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.GateType;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * 测试工具：站在有门的格子上使用，循环切换这扇门的逻辑类型。
 */
public class GateTester extends TestItem {

	private static final String AC_CYCLE = "CYCLE";

	{
		image = ItemSpriteSheet.RECLAIM_TRAP;
		defaultAction = AC_CYCLE;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_CYCLE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_CYCLE.equals(action)) {
			return;
		}

		LevelTransition t = Dungeon.level.getTransition(hero.pos);
		if (t == null) {
			GLog.w(Messages.get(this, "no_gate"));
			return;
		}

		GateType[] values = GateType.values();
		t.gate = values[(t.gate.ordinal() + 1) % values.length];
		t.gateOpened = false;
		GLog.i(Messages.get(this, "switched", Messages.get(GateType.class, t.gate.name().toLowerCase() + "_short")));
	}
}