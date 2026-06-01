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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 可附着到护甲的玩具基类。
 * 类似于 BrokenSeal，玩具作为 Item 存在，附着到护甲后在战斗逻辑中被检测并应用效果。
 * 子类需要实现 applyEffect(Hero) 和 removeEffect(Hero) 方法。
 */
public abstract class ItemArmorAttachable extends Item {

	public static final String AC_ATTACH = "ATTACH";
	public static final String AC_DETACH = "DETACH";

	{
		stackable = false;
		levelKnown = false;
		image = ItemSpriteSheet.SNAKE_BITE; // 默认贴图
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_ATTACH);

		// 如果已附着在护甲上，提供取下选项
		if (attachedTo != null) {
			actions.add(AC_DETACH);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_ATTACH)) {
			curItem = this;
			GameScene.selectItem(armorSelector);
		} else if (action.equals(AC_DETACH)) {
			detachFromArmor(hero);
		}
	}

	/**
	 * 将玩具附着到护甲上。由 Armor.attachToy() 调用。
	 */
	public void attachToArmor(Armor armor) {
		this.attachedTo = armor;
		applyEffect(Dungeon.hero);
	}

	/**
	 * 从护甲取下玩具。放回背包。
	 */
	public void detachFromArmor(Hero hero) {
		if (attachedTo != null) {
			attachedTo = null;
			removeEffect(hero);
			if (hero != null && !hero.belongings.backpack.contains(this)) {
				collect(hero.belongings.backpack);
				GLog.i(Messages.get(this, "detached", name()));
			}
		}
	}

	/**
	 * 子类实现：应用玩具效果到英雄
	 */
	public abstract void applyEffect(Hero hero);

	/**
	 * 子类实现：移除玩具效果
	 */
	public abstract void removeEffect(Hero hero);

	// attachedTo 引用当前附着到的护甲（运行时）
	public transient Armor attachedTo;

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		// 注意：attachedTo 是 transient，不序列化
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		attachedTo = null; // 恢复时需要重新绑定
	}

	@Override
	public String info() {
		String info = desc();
		if (attachedTo != null) {
			info += "\n\n" + Messages.get(this, "attached_info");
		}
		return info;
	}

	// 护甲选择器
	protected static WndBag.ItemSelector armorSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(BrokenSeal.class, "prompt"); // 复用破损纹章的提示
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Armor;
		}

		@Override
		public void onSelect(Item item) {
			ItemArmorAttachable toy = (ItemArmorAttachable) curItem;
			if (item instanceof Armor) {
				Armor armor = (Armor) item;
				armor.attachToy(toy);
				toy.detach(Dungeon.hero.belongings.backpack); // 从背包移除
				GLog.p(Messages.get(ItemArmorAttachable.class, "attached", toy.name(), armor.name()));
				Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.UNLOCK);
			}
		}
	};

	// ========== 便捷方法 ==========

	/**
	 * 获取当前英雄护甲上附着的指定类型玩具
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ItemArmorAttachable> T getAttachedToy(Class<T> toyClass) {
		if (Dungeon.hero == null || Dungeon.hero.belongings.armor == null) {
			return null;
		}
		return Dungeon.hero.belongings.armor.getToy(toyClass);
	}

	/**
	 * 检查英雄护甲上是否附着了指定类型的玩具
	 */
	public static boolean hasAttached(Class<? extends ItemArmorAttachable> toyClass) {
		return getAttachedToy(toyClass) != null;
	}

	/**
	 * 获取英雄护甲上所有附着的玩具列表
	 */
	public static ArrayList<ItemArmorAttachable> getAllAttachedToys() {
		if (Dungeon.hero == null || Dungeon.hero.belongings.armor == null) {
			return new ArrayList<>();
		}
		return Dungeon.hero.belongings.armor.getToys();
	}
}
