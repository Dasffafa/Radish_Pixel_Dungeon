/*
 * Radish Pixel Dungeon
 * Copyright (C) 2026
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeddlerSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 游商 —— 移植自 Darkest PD 的 BarterMan（_upstream/darkest/.../npcs/BarterMan.kt）。
 *
 * 关键机制（与源码一致，不要改回「地上铺货 + 花金币」）：
 * 1. 货品只存在内存里的 {@link #goods} 列表，地上不铺任何堆；
 * 2. 以物换物：玩家从货架上挑一件，再从自己背包里挑一件作为交换；
 * 3. 交换时双方的诅咒状态互换（{@code goodsItem.cursed = exchangeItem.cursed}）；
 * 4. 商品一旦被换走就从货架上消失。
 */
public class Peddler extends NPC {

	private static final String VARIANT = "variant";
	private static final String GOODS   = "goods";

	/** 一楼一共几位；与 PeddlerCatalog.COUNT 保持一致。 */
	public static final int VARIANTS = PeddlerCatalog.COUNT;

	private int variant = 0;

	/** 货架上的货。不存在地上，只在这份列表里。 */
	private ArrayList<Item> goods = new ArrayList<>();

	{
		//NPC 的通用属性（对齐 BarterMan 继承 Merchant 后的默认值）
		alignment = Alignment.NEUTRAL;
		state = PASSIVE;
		properties.add(Char.Property.IMMOVABLE);
	}

	public Peddler() {
		this(0);
	}

	public Peddler(int variant) {
		super();
		this.variant = Math.max(0, Math.min(VARIANTS - 1, variant));
	}

	public int variant() {
		return variant;
	}

	@Override
	public CharSprite sprite() {
		return new PeddlerSprite(variant);
	}

	@Override
	public String name() {
		return Messages.get(Peddler.class, "name_" + variant);
	}

	@Override
	public String description() {
		return Messages.get(Peddler.class, "desc_" + variant);
	}

	public String greeting() {
		return Messages.get(Peddler.class, " talk_" + variant + " ").trim();
	}

	/**
	 * 生成进关卡时立刻备货。
	 * 注意：这里只是生成货单，**不往地上放任何东西**（对齐 BarterMan.initSellItems）。
	 */
	@Override
	protected void onAdd() {
		super.onAdd();
		if (goods.isEmpty()) {
			goods.addAll(PeddlerCatalog.rollGoods());
		}
	}

	@Override
	public boolean interact(Char c) {

		if (goods.isEmpty()) {
			//货架空了：只在玩家自己来的时候提示一次
			if (c == Dungeon.hero) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new WndTitledMessage(
								new PeddlerSprite(variant),
								Messages.titleCase(name()),
								Messages.get(Peddler.class, "sold_out")));
					}
				});
			}
			return true;
		}

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndOptions(
						new PeddlerSprite(variant),
						Messages.titleCase(name()),
						greeting(),
						Messages.get(Peddler.class, "opt_trade"),
						Messages.get(Peddler.class, "opt_talk"),
						Messages.get(Peddler.class, "opt_leave")) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							openShelf();
						} else if (index == 1) {
							GameScene.show(new WndTitledMessage(
									new PeddlerSprite(variant),
									Messages.titleCase(name()),
									Messages.get(Peddler.class, "talk_more_" + variant)));
						}
					}
				});
			}
		});

		return true;
	}

	/** 打开货架：玩家从自己的背包里挑一件来交换。 */
	private void openShelf() {
		GameScene.selectItem(new WndBag.ItemSelector() {

			@Override
			public String textPrompt() {
				return Messages.get(Peddler.class, "prompt_choose");
			}

			@Override
			public boolean itemSelectable(Item item) {
				return item != null && !goods.isEmpty();
			}

			@Override
			public void onSelect(Item item) {
				if (item == null) return;

				//挑一件货：优先同类的，否则随便一件
				Item goodsItem = null;
				for (Item g : goods) {
					if (g.getClass() == item.getClass()) {
						goodsItem = g;
						break;
					}
				}
				if (goodsItem == null && !goods.isEmpty()) {
					goodsItem = goods.get(com.watabou.utils.Random.Int(goods.size() - 1));
				}
				if (goodsItem == null) {
					GameScene.show(new WndTitledMessage(
							new PeddlerSprite(variant),
							Messages.titleCase(name()),
							Messages.get(Peddler.class, "sold_out")));
					return;
				}
				if (!item.isIdentified() && !item.isSimilar(goodsItem)) {
					//未鉴定的物品不能拿来盲换，提示一下就退回
					GameScene.show(new WndTitledMessage(
							new PeddlerSprite(variant),
							Messages.titleCase(name()),
							Messages.get(Peddler.class, "need_identified")));
					return;
				}

				//交换：双方诅咒状态互换（对齐 BarterMan 第 88 行）
				goodsItem.cursed = item.cursed;
				goodsItem.cursedKnown = item.cursedKnown;
				item.cursed = false;
				item.cursedKnown = true;

				//换到玩家手里
				goods.remove(goodsItem);
				if (goodsItem instanceof MeleeWeapon && item instanceof MeleeWeapon) {
					((MeleeWeapon) goodsItem).enchant(((MeleeWeapon) item).enchantment);
				}
				if (!goodsItem.collect(Dungeon.hero.belongings.backpack)) {
					Dungeon.level.drop(goodsItem, Dungeon.hero.pos);
				}

				Dungeon.hero.sprite.operate(Dungeon.hero.pos);
				GameScene.show(new WndTitledMessage(
						new PeddlerSprite(variant),
						Messages.titleCase(name()),
						Messages.get(Peddler.class, "traded")));
			}
		});
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VARIANT, variant);
		bundle.put(GOODS, goods);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		variant = bundle.getInt(VARIANT);
		goods.clear();
		goods.addAll((Collection<Item>) ((Collection<?>) bundle.getCollection(GOODS)));
	}
}
