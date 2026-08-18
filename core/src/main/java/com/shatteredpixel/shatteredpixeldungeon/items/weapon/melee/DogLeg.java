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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

/**
 * 狗腿 (Dog Leg)
 * 杀死狗后15%掉落一个。
 * 可以对1距离的敌人使用，视为一次伤害为16且必中的近战攻击。使用后消失。
 */
public class DogLeg extends Item {

	public static final String AC_USE = "USE";

	{
		defaultAction = AC_USE;
		usesTargeting = true;

		image = ItemSpriteSheet.DOG_LEG;
		stackable = false;

		bones = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_USE)) {
			curUser = hero;
			curItem = this;

			// 只能对 1 距离的敌人使用
			GameScene.selectCell(attacker);
		}
	}

	protected static CellSelector.Listener attacker = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell == null || curUser == null || curItem == null) return;

			// 必须相邻（1 距离）
			if (!Dungeon.level.adjacent(curUser.pos, cell)) {
				GLog.w(Messages.get(DogLeg.class, "too_far"));
				return;
			}

			Char enemy = Actor.findChar(cell);
			if (enemy == null || enemy == curUser) {
				GLog.w(Messages.get(DogLeg.class, "no_target"));
				return;
			}

			// 消耗狗腿
			curItem.detach(curUser.belongings.backpack);
			curUser.spendAndNext(1f);

			Sample.INSTANCE.play(Assets.Sounds.HIT_STAB);
			curUser.sprite.attack(cell, new com.watabou.utils.Callback() {
				@Override
				public void call() {
					// 16 点必中伤害（法术/真实伤害，忽略防御）
					enemy.damage(DamageInfo.of(16, DamageType.TRUE, curUser, curItem));
					enemy.sprite.bloodBurstA(enemy.sprite.center(), 16);
					enemy.sprite.flash();

					if (!enemy.isAlive() && enemy == Dungeon.hero) {
						Dungeon.fail(DogLeg.class);
						GLog.n(Messages.get(DogLeg.class, "kill_desc"));
					}
				}
			});
		}

		@Override
		public String prompt() {
			return Messages.get(DogLeg.class, "prompt");
		}
	};

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	@Override
	public int value() {
		return 30;
	}
}
