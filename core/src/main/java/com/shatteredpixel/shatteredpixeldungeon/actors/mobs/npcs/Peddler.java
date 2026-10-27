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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeddlerSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * 游商（照暗黑地牢 BarterMan 的路子）。
 *
 * 与标准 Shopkeeper 的关键差异：
 *  - 不占商店房、不摆货架，货品由 catalog 直接落成 FOR_SALE 堆；
 *  - 交谈面板只有「交易 / 闲聊 / 离开」，价格不在面板上明码；
 *  - 真正的成交走 WndTradeItem，价格仍然是标准商人价，
 *    也就是「商人的价格」—— 外部口径不变。
 *
 * 名字、台词、卖什么、用哪张图，全部从 PeddlerCatalog 取；
 * 之后调整内容只改 catalog，不动这个类。
 */
public class Peddler extends NPC {

	public static final String VARIANT = "variant";
	public static final String STOCKED = "stocked";

	//同一楼层只铺一次货，防止反复进出刷货
	private boolean stocked = false;

	private int variant = 0;

	public Peddler() {
		this(0);
	}

	public Peddler(int variant) {
		this.variant = variant;
		alignment = Alignment.NEUTRAL;
		//游商站着不动，不逃跑、不换位
		properties.add(Char.Property.IMMOVABLE);
		//spriteClass 是无参反射构造用的字段，变体号在 sprite() 之后补；
		//PeddlerSprite 有默认构造（= variant 0），不会 NPE
		spriteClass = PeddlerSprite.class;
	}

	public int variant() {
		return variant;
	}

	/**
	 * 覆写父类的无参反射构造：必须把自己的 variant 传给精灵，
	 * 否则 7 位游商全都会走 PeddlerSprite() 默认构造、显示出同一张图。
	 */
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
		return Messages.get(Peddler.class, "talk_" + variant);
	}

	/**
	 * 生成进关卡时立刻铺货，玩家进一楼就能看见摊位。
	 * 不放在 interact() 里，否则玩家不点就一直空手站着。
	 */
	@Override
	protected void onAdd() {
		super.onAdd();
		stock();
	}

	/** 铺货：把这位游商的货单落成 FOR_SALE 堆，价格按标准商人价。 */
	public void stock() {
		if (stocked || Dungeon.level == null) {
			return;
		}
		stocked = true;

		List<Integer> cells = freeCellsAround(2);
		if (cells.isEmpty()) {
			return;
		}

		//整个货单一次性铺在脚边，货不够位就多轮循环填
		List<Item> goods = PeddlerCatalog.itemsFor(variant);
		int idx = 0;
		for (Item item : goods) {
			int cell = cells.get(idx % cells.size());
			//同一格已有货就叠上去，不占新格
			Heap h = Dungeon.level.heaps.get(cell);
			if (h != null) {
				h.drop(item);
			} else {
				Dungeon.level.drop(item, cell).type = Heap.Type.FOR_SALE;
				idx++;
			}
		}
	}

	/**
	 * 收集 pos 附近 radius 格以内的可落地空位（不含人、不含已有堆）。
	 * Level 没有现成的「找最近空格」方法，这里按 passable[] + heaps 自己算。
	 */
	private List<Integer> freeCellsAround(int radius) {
		List<Integer> inner = new ArrayList<>();
		List<Integer> outer = new ArrayList<>();
		if (Dungeon.level == null) {
			return inner;
		}
		int w = Dungeon.level.width();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				int cell = pos + dx + dy * w;
				if (cell < 0 || cell >= Dungeon.level.passable.length) {
					continue;
				}
				if (!Dungeon.level.passable[cell]) {
					continue;
				}
				if (Dungeon.level.heaps.get(cell) != null) {
					continue;
				}
				if (Actor.findChar(cell) != null) {
					continue;
				}
				if (Dungeon.level.trueDistance(cell, pos) <= 1) {
					inner.add(cell);
				} else {
					outer.add(cell);
				}
			}
		}
		//先填贴身的，再填外圈，保证货品摆在游商脚边
		inner.addAll(outer);
		return inner;
	}

	@Override
	public boolean interact(Char c) {
		if (c != Dungeon.hero) {
			return true;
		}
		stock();

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndOptions(
						Messages.titleCase(name()),
						greeting(),
						Messages.get(Peddler.class, "opt_trade"),
						Messages.get(Peddler.class, "opt_talk"),
						Messages.get(Peddler.class, "opt_leave")) {
					@Override
					protected void onSelect(int index) {
						super.onSelect(index);
						if (index == 0) {
							//不弹「明码价目表」，只提示玩家自己去挑地上的货
							GLog.i(Messages.get(Peddler.class, "opt_trade_hint"));
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

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VARIANT, variant);
		bundle.put(STOCKED, stocked);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		variant = bundle.getInt(VARIANT);
		stocked = bundle.getBoolean(STOCKED);
	}
}
