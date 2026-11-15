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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeddlerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeddlerActionIcon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.PeddlerMenu;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndPeddlerShop;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

import java.util.ArrayList;
/**
 * 一楼游商。
 *
 * 每个游商按 variant 绑定一种交易模式；货架窗口由 WndPeddlerShop 实现。
 */
public class Peddler extends NPC {

	/** 按 TradeMode 组装游商头顶的图标气泡。 */
	private void openDialogue(final Peddler self) {
		if (Dungeon.hero == null || self.sprite == null) return;
		final PeddlerCatalog.TradeMode mode = PeddlerCatalog.mode(variant);
		ArrayList<Image> icons = new ArrayList<>();
		ArrayList<Runnable> actions = new ArrayList<>();
		icons.add(Icons.get(Icons.GOLD));
		actions.add(new Runnable() {
			@Override
			public void run() {
				if (mode == PeddlerCatalog.TradeMode.SERVICE) {
					buyService(variant);
				} else if (mode == PeddlerCatalog.TradeMode.BUY || !PeddlerCatalog.goods(variant).isEmpty()) {
					GameScene.show(new WndPeddlerShop(self));
				}
			}
		});
		icons.add(new PeddlerActionIcon(PeddlerActionIcon.TALK));
		actions.add(new Runnable() {
			@Override
			public void run() {
				GLog.i(Messages.get(Peddler.class, "talk_more_" + variant));
			}
		});
		icons.add(new PeddlerActionIcon(PeddlerActionIcon.SWAP));
		actions.add(new Runnable() {
			@Override
			public void run() {
				self.swapWithHero();
			}
		});
		icons.add(Icons.get(Icons.CLOSE));
		actions.add(new Runnable() {
			@Override
			public void run() {}
		});
		PeddlerMenu menu = new PeddlerMenu(icons.toArray(new Image[0]), actions.toArray(new Runnable[0]));
		menu.camera = PixelScene.uiCamera;
		PointF spritePos = self.sprite.worldToCamera(self.pos);
		Point screenPos = Camera.main.cameraToScreen(spritePos.x + self.sprite.width() / 2, spritePos.y);
		PointF uiPos = PixelScene.uiCamera.screenToCamera(screenPos.x, screenPos.y);
		menu.placeAbove(uiPos.x, uiPos.y);
		Game.scene().addToFront(menu);
	}


	private static final String VARIANT = "variant";
	private static final String STOCKED = "stocked";
	private static final String GOODS   = "goods";

	private int variant = 0;
	private boolean stocked = false;

	/** 当前货架。铺一次，玩家买走一件就少一件。 */
	private ArrayList<Item> goods = new ArrayList<>();

	public Peddler() {
		this( 0 );
	}

	public Peddler( int variant ) {
		super();
		this.variant = variant;

		properties.add( Property.IMMOVABLE );
	}

	public int variant() {
		return variant;
	}

	public PeddlerCatalog.TradeMode mode() {
		return PeddlerCatalog.mode( variant );
	}

	@Override
	public CharSprite sprite() {
		return new PeddlerSprite( variant );
	}

	@Override
	public String name() {
		return Messages.get( Peddler.class, "name_" + variant );
	}

	@Override
	public String description() {
		return Messages.get( Peddler.class, "desc_" + variant );
	}

	public String greeting() {
		return Messages.get( Peddler.class, "talk_" + variant );
	}

	/** 货架内容。首次访问时铺货。 */
	public ArrayList<Item> goods() {
		stock();
		return goods;
	}

	/** 买走一件后从货架上移除。 */
	public void removeGood( Item item ) {
		if (item == null) return;
		goods.remove( item );
	}

	/**
	 * 首次铺货。对照上游 BarterMan 的 init，货品是固定清单，不是随机抽类别。
	 */
	public void stock() {
		if (stocked) return;
		stocked = true;

		goods = PeddlerCatalog.goods( variant );
	}

	@Override
	protected boolean act() {
		// 每 tick 转身朝向英雄，和上游 BarterMan.act() 一致
		if (sprite != null && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			sprite.turnTo( pos, Dungeon.hero.pos );
		}
		spend( TICK );
		return true;
	}

	@Override
	public void damage( DamageInfo info ) {
		// 测试用 NPC：不会死，挨打也只是站着不动（不再挨打就跑/消失）
	}

	/** 与英雄换位（对话选项「换位」） */
	public void swapWithHero() {
		Hero hero = Dungeon.hero;
		if (hero == null) return;

		// 换位＝把游商放到英雄原来的格子上。那格必须能站、没被别的单位占、也不能是陷阱格，
		// 否则相当于借「换位」把 NPC 塞进墙里、或者白吃一次陷阱。
		int target = hero.pos;
		if (hero.rooted || !Dungeon.level.passable[target]
				|| Dungeon.level.traps.get( target ) != null
				|| com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( target ) != hero) {
			GLog.w( Messages.get( Peddler.class, "swap_blocked" ) );
			return;
		}

		int myPos = pos;
		pos = target;
		hero.pos = myPos;

		if (sprite != null) {
			sprite.place( pos );
		}
		if (hero.sprite != null) {
			hero.sprite.place( hero.pos );
		}
		Dungeon.observe();
		GLog.i( Messages.get( Peddler.class, "swap_done" ) );
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero) {
			return true;
		}

		final Peddler self = this;

		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				openDialogue( self );
			}
		} );

		return true;
	}


	@Override
	public boolean reset() {
		return true;
	}

	/**
	 * 收费服务：先验金币，再真的做事，做成了才扣钱。
	 * 三处反馈都要给（没钱 / 没什么可治 / 收钱治好了），否则点了选项毫无动静。
	 */
	private static void buyService( int variant ) {
		final Hero hero = Dungeon.hero;
		if (hero == null) return;

		final int price = PeddlerCatalog.profile( variant ).servicePrice;

		if (Dungeon.gold < price) {
			GLog.w( Messages.get( Peddler.class, "service_poor", price ) );
			return;
		}

		if (!PeddlerCatalog.applyService( variant, hero )) {
			GLog.i( Messages.get( Peddler.class, "service_idle" ) );
			return;
		}

		Dungeon.gold -= price;
		com.shatteredpixel.shatteredpixeldungeon.items.armor.CountessDress.onGoldChanged();
		GLog.p( Messages.get( Peddler.class, "service_done", price ) );
	}
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VARIANT, variant );
		bundle.put( STOCKED, stocked );
		// 货架内容必须一起存：里面既有「还剩什么」，也隐含「已经卖掉了什么」。
		// 只存 stocked 不存 goods，读档后就成了「标记为已铺货 + 空货架」，stock() 永远不会再补货。
		bundle.put( GOODS, goods );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		variant = bundle.getInt( VARIANT );
		stocked = bundle.getBoolean( STOCKED );

		goods = new ArrayList<>();
		if (bundle.contains( GOODS )) {
			// 旧档没有 GOODS 键：这类档要当作「还没铺过货」，把 stocked 放回去让 stock() 重建，
			// 否则会复现「货架永远空」的老 bug。
			for (Bundlable b : bundle.getCollection( GOODS )) {
				if (b instanceof Item) {
					goods.add( (Item)b );
				}
			}
		} else {
			stocked = false;
		}
	}
}
