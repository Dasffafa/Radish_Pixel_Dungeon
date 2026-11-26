package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Shaders;
import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.ProfessionWaveShader;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Wheelchair;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;

/**
 * 维护杂散变身时职业专属道具之间的关联、等级共享与紫黑色水波着色器标识。
 */
public final class ChangingProfessionItem {

	private ChangingProfessionItem() { }

	public static Item transform(Item oldItem, Hero hero) {
		Item replacement = ClassSpecificItems.replacementFor(oldItem, hero);
		if (replacement == null) return null;
		syncSharedLevel(oldItem, replacement);
		return replacement;
	}

	public static void syncSharedLevel(Item from, Item to) {
		if (from == null || to == null) return;
		if (isSealOrStaff(from) && isSealOrStaff(to)) {
			to.level(from.level());
		} else if (isWheelchairOrCloak(from) && isWheelchairOrCloak(to)) {
			to.level(from.level());
		}
	}

	public static ProfessionWaveShader waveShader(float time) {
		Shaders.professionWave.setTime(time);
		return Shaders.professionWave;
	}

	private static boolean isSealOrStaff(Item item) {
		return item instanceof BrokenSeal || item instanceof MagesStaff;
	}

	private static boolean isWheelchairOrCloak(Item item) {
		return item instanceof Wheelchair || item instanceof CloakOfShadows;
	}
}
