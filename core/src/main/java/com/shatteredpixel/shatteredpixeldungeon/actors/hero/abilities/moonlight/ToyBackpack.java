/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import java.util.ArrayList;

/**
 * 玩具背包护甲技能
 * 月华专属护甲技能，允许生成和管理玩具物品
 */
public class ToyBackpack extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		requestGeneration(armor, hero);
	}

	public void requestGeneration(ClassArmor armor, Hero hero) {
		ArrayList<Armor.ToyRef> toys = Armor.ownedToys(hero);
		if (toys.size() >= 5) {
			GameScene.show(new WndDestroyToy(armor, hero, toys));
			return;
		}
		finishGeneration(armor, hero);
	}

	private void finishGeneration(ClassArmor armor, Hero hero) {
		ItemArmorAttachable toy = armor.generateRandomToy();
		if (toy == null) return;
		com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToyEffects.onAbilityUsed(hero);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Item.updateQuickslot();

		GLog.p(Messages.get(Armor.class, "toy_generated", toy.name()));
		if (!toy.collect(hero.belongings.backpack)) {
			if (toy instanceof com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal) {
				Dungeon.level.drop(toy, hero.pos).sprite.drop();
			} else {
				 toy.vanishOnGround(false, hero.pos);
			}
		}

		hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
	}

	private class WndDestroyToy extends WndOptions {
		private final ClassArmor armor;
		private final Hero hero;
		private final ArrayList<Armor.ToyRef> toys;

		private WndDestroyToy(ClassArmor armor, Hero hero, ArrayList<Armor.ToyRef> toys) {
			super(Messages.get(ToyBackpack.class, "destroy_title"),
					Messages.get(ToyBackpack.class, "destroy_message"),
					toys.stream().map(ref -> ref.toy.name()).toArray(String[]::new));
			this.armor = armor;
			this.hero = hero;
			this.toys = toys;
		}

		@Override protected void onSelect(int index) {
			if (index < 0 || index >= toys.size()) return;
			toys.get(index).destroy(hero);
			finishGeneration(armor, hero);
		}
	}

	@Override
	public int icon() {
		return HeroIcon.TOY_BACKPACK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BETTER_ITEM, Talent.EXTRA_POCKET, Talent.ACCEPT_CHALLENGE, Talent.HEROIC_ENERGY};
	}
}
