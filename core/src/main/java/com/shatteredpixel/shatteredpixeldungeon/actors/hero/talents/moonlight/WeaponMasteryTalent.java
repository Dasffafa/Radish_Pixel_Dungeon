package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 武器掌握天赋
 * +1：回合数/125为层数，每层0-层数的伤害加成，最多5层
 * +2：回合数/100为层数，每层0-层数的伤害加成，最多5层
 */
public class WeaponMasteryTalent {

	public static class MasteryTracker extends Buff {
		{
			type = buffType.POSITIVE;
		}

		private int turnCount;

		@Override
		public int icon() { return BuffIndicator.WEAPON; }

		@Override
		public void tintIcon(Image icon) {
			int stacks = getStacks();
			float brightness = stacks / 5f;
			icon.hardlight(1f, 0.5f + brightness * 0.5f, brightness);
		}

		@Override
		public String desc() {
			int stacks = getStacks();
			int bonus = stacks; // 最大加成等于层数
			return Messages.get( this,"desc", stacks, getThreshold() - turnCount,getThreshold());
		}

		private int getThreshold() {
			return ((Hero) target).pointsInTalent(Talent.WEAPON_MASTERY) == 1 ? 125 : 100;
		}

		public int getStacks() {
			return Math.min(turnCount / getThreshold(), 5);
		}

		public int getBonusDamage() {
			if (((Hero) target).belongings.weapon() == null) return 0;
			int stacks = getStacks();
			return stacks > 0 ? Random.Int(stacks + 1) : 0;
		}

		public void reset() { turnCount = 0; }

		@Override
		public boolean act() {
			turnCount++;
			spend(TICK);
			return true;
		}

		private static final String TURN_COUNT = "turn_count";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TURN_COUNT, turnCount);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			turnCount = bundle.getInt(TURN_COUNT);
		}
	}

	private static KindOfWeapon lastWeapon;

	@SubscribeEvent(event = HeroActEvent.class, priority = 0)
	public static void onHeroAct(HeroActEvent event) {
		Hero hero = event.getHero();

		if (hero.heroClass != HeroClass.MOONLIGHT || !hero.hasTalent(Talent.WEAPON_MASTERY)) return;

		KindOfWeapon weapon = hero.belongings.weapon();

		// 初始化 Buff
		MasteryTracker tracker = Buff.affect(hero, MasteryTracker.class);

		// 检测武器切换
		if (weapon != lastWeapon) {
			tracker.reset();
			lastWeapon = weapon;
		}
	}

	public static int getBonusDamage(Hero hero) {
		if (hero.heroClass != HeroClass.MOONLIGHT || !hero.hasTalent(Talent.WEAPON_MASTERY)) return 0;
		MasteryTracker tracker = hero.buff(MasteryTracker.class);
		return tracker != null ? tracker.getBonusDamage() : 0;
	}
}