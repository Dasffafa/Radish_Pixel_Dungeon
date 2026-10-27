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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Peddler;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PeddlerCatalog;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PeddlerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 游商货架窗口。
 *
 * 对照上游 _upstream/darkest .../npcs/BarterMan.kt 里的 inner class WndTrade : WndShop()：
 * 顶部标题栏 → 分隔线 → 5 列商品格 → 每格下方黄色价签（钱不够时变红）。
 * 价格走标准商人价（PeddlerCatalog.price → Item.value()），不是另外编一个公式。
 */
public class WndPeddlerShop extends Window {

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	private static final int GAP = 2;
	private static final int COLS = 5;
	private static final int BUTTON_SIZE = 24;
	private static final int LABEL_HEIGHT = 10;
	/** 商品区最多露出的行数，超出的部分靠滚动条看（货品多时防止窗口顶出屏幕）。 */
	private static final int MAX_VISIBLE_ROWS = 4;

	private final Peddler peddler;
	private final Hero hero;

	/** 商品区在窗口内的顶边与可见高度，以及滚动容器本身。挂进场景后才排版。 */
	private float scrollTop = 0;
	private float scrollVisibleHeight = 0;
	private ScrollPane scroll = null;
	private boolean scrollSized = false;

	public WndPeddlerShop( Peddler peddler ) {
		super();

		this.peddler = peddler;
		// 直接把 hero 抓成字段，避免后续渲染线程里 Dungeon.hero 变化
		this.hero = Dungeon.hero;

		Image icon = new PeddlerSprite( peddler.variant() );
		IconTitle titlebar = new IconTitle( icon, Messages.titleCase( peddler.name() ) );
		titlebar.setRect( 0, 0, WIDTH_MAX, 0 );
		add( titlebar );

		RenderedTextBlock tfHint = PixelScene.renderTextBlock(
				Messages.get( WndPeddlerShop.class, "hint", Dungeon.gold ), 6 );
		tfHint.maxWidth( WIDTH_MAX );
		tfHint.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( tfHint );

		float pos = tfHint.bottom() + GAP + 2;

		ArrayList<Item> goods = peddler.goods();

		if (goods.isEmpty()) {
			RenderedTextBlock tfEmpty = PixelScene.renderTextBlock(
					Messages.get( WndPeddlerShop.class, "empty" ), 6 );
			tfEmpty.maxWidth( WIDTH_MAX );
			tfEmpty.setPos( titlebar.left(), pos );
			add( tfEmpty );
			pos = tfEmpty.bottom() + GAP;

		} else {
			int count = goods.size();
			float slotW = (WIDTH_MAX - GAP * (COLS - 1)) / (float) COLS;

			// 货品多了以后整窗会顶出屏幕：把商品区塞进 ScrollPane（照 WndBag 的做法），
			// 标题栏与金币提示保持固定，只有商品格滚动。
			int rows = (count + COLS - 1) / COLS;
			float rowH = BUTTON_SIZE + LABEL_HEIGHT + GAP * 2;

			ScrollPane scroll;
			Component content = new Component();
			content.setSize( WIDTH_MAX, rows * rowH );
			scroll = new ScrollPane( content );
			add( scroll );

			// 可见区高度：在窗口真正挂进场景后才知道（camera() 为空时不能排版）
			scrollTop = pos;
			scrollVisibleHeight = Math.min( rows * rowH, MAX_VISIBLE_ROWS * rowH );
			this.scroll = scroll;

			for (int i = 0; i < count; i++) {
				final Item item = goods.get( i );
				int col = i % COLS;
				int row = i / COLS;

				float bx = col * (slotW + GAP);
				float by = row * rowH;

				// 格子用 InventorySlot：和按 B 打开的背包是同一个类，
				// 底色、四边描线、圆角框全部一致，不再自己画一套 ItemButton。
				InventorySlot btn = new InventorySlot( item ) {
					@Override
					protected void onClick() {
						buy( item );
					}

					// 与玩家背包完全一致的预览逻辑：
					// WndBag.placeItem 里长按就是 Game.scene().addToFront(new WndInfoItem(item))，
					// 这里走同一条链路，单击仍是直接购买（对应背包里单击即选中）。
					@Override
					protected boolean onLongClick() {
						Game.scene().addToFront( new WndInfoItem( item ) );
						return true;
					}
				};
				btn.setRect( bx, by, slotW, BUTTON_SIZE );
				content.add( btn );

				int price = PeddlerCatalog.price( item );
				boolean affordable = Dungeon.gold >= price;

				RenderedTextBlock tfPrice = PixelScene.renderTextBlock( price + "G", 6 );
				// 钱不够变红：和上游 showPrice 的 red 分支一致
				if (!affordable) tfPrice.hardlight( 0xFF4400 );
				tfPrice.maxWidth( (int) slotW );
				tfPrice.setPos( bx + (slotW - tfPrice.width()) / 2f, by + BUTTON_SIZE + 1 );
				content.add( tfPrice );
			}

			pos += scrollVisibleHeight;
		}

		float width = WIDTH_MAX;
		if (titlebar.width() + 2 > width) width = titlebar.width() + 2;
		if (tfHint.width() + 2 > width) width = tfHint.width() + 2;
		if (width < WIDTH_MIN) width = WIDTH_MIN;

		resize( (int) width, (int) pos );
	}

	@Override
	public void update() {
		super.update();
		// 窗口挂进场景后才给 ScrollPane 排版（它的 layout 需要 camera()，照 WndBag 的做法）
		if (!scrollSized && scroll != null && camera() != null) {
			scroll.setRect( 0, scrollTop, WIDTH_MAX, scrollVisibleHeight );
			scrollSized = true;
		}
	}

	/**
	 * 买下一件货。金币不足就只提示、不扣钱。
	 */
	private void buy( Item item ) {
		if (item == null) return;

		int price = PeddlerCatalog.price( item );
		if (Dungeon.gold < price) {
			GLog.w( Messages.get( WndPeddlerShop.class, "no_gold", price ) );
			return;
		}

		Dungeon.gold -= price;
		GLog.i( Messages.get( WndPeddlerShop.class, "bought", item.title(), price ) );

		// 从货架移除，避免重复买同一件
		peddler.removeGood( item );

		if (!item.doPickUp( hero )) {
			Dungeon.level.drop( item, hero.pos );
		}

		// 重新打开窗口刷新货架与金币显示
		final Peddler p = peddler;
		hide();
		Game.runOnRenderThread( new com.watabou.utils.Callback() {
			@Override
			public void call() {
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show( new WndPeddlerShop( p ) );
			}
		} );
	}
}
