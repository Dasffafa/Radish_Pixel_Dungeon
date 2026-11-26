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
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.MagicWarrior;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemInfoText;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.SectionHeader;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RoundedFrame;
import com.shatteredpixel.shatteredpixeldungeon.ui.UITheme;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndInfoItem extends Window {
	
	private static final float GAP	= 2;
	private static final float DICE_PAD = 3;
	private static final int DICE_ICON = 28;

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;

	//only one WndInfoItem can appear at a time
	private static WndInfoItem INSTANCE;

	public WndInfoItem( Heap heap ) {

		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

		if (heap.type == Heap.Type.HEAP) {
			fillFields( heap.peek() );

		} else {
			fillFields( heap );

		}
	}
	
	public WndInfoItem( Item item ) {
		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;
		
		fillFields( item );
	}

	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}

	private void fillFields(Heap heap ) {
		if (UITheme.isDiceMage()) {
			layoutDiceHeap(heap);
			return;
		}
		
		IconTitle titlebar = new IconTitle( heap );
		titlebar.color( TITLE_COLOR );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( heap.info(), 6 );

		layoutFields(titlebar, txtInfo);
	}
	
	private void fillFields( Item item ) {
		if (UITheme.isDiceMage()) {
			layoutDiceItem(item);
			maybeAddCurseButton(item);
			return;
		}
		
		int color = TITLE_COLOR;
		if (item.levelKnown && item.level() > 0) {
			color = ItemSlot.UPGRADED;
		} else if (item.levelKnown && item.level() < 0) {
			color = ItemSlot.DEGRADED;
		}

		IconTitle titlebar = new IconTitle( item );
		titlebar.color( color );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( item.displayInfo(), 6 );

		layoutItem(titlebar, txtInfo, item);
		maybeAddCurseButton( item );
	}

	/** 魔武士：在物品详情下方提供手动转化诅咒按钮。 */
	private void maybeAddCurseButton( Item item ) {
		if (!(item instanceof EquipableItem) || Dungeon.hero == null) {
			return;
		}
		EquipableItem eq = (EquipableItem) item;
		if (Dungeon.hero.subClass != HeroSubClasses.MAGIC_WARRIOR) {
			return;
		}
		if (!eq.cursed || Dungeon.hero.buff( MagicWarrior.CursePurgeCooldown.class ) != null) {
			return;
		}

		RedButton btn = new RedButton( Messages.get( HeroSubClass.class, "magicwarrior_curse_convert" ) ) {
			@Override
			protected void onClick() {
				MagicWarrior.purgeCurseOnEquip( Dungeon.hero, item );
				hide();
				GameScene.show( new WndInfoItem( item ) );
			}
		};
		btn.setRect( 0, height + GAP, width, 16 );
		add( btn );
		resize( (int) width, (int) (btn.bottom() + 2) );
	}

	private void layoutFields(IconTitle title, RenderedTextBlock info){		int width = WIDTH_MIN;

		info.maxWidth(width);

		//window can go out of the screen on landscape, so widen it as appropriate
		while (PixelScene.landscape()
				&& info.height() > 100
				&& width < WIDTH_MAX){
			width += 20;
			info.maxWidth(width);
		}

		title.setRect( 0, 0, width, 0 );
		add( title );

		info.setPos(title.left(), title.bottom() + GAP);
		add( info );

		resize( width, (int)(info.bottom() + 2) );
	}

	private void layoutDiceHeap(Heap heap) {
		int width = PixelScene.landscape() ? 150 : 132;
		chrome.hardlight(DiceMageUI.DARK);

		IconTitle titlebar = new IconTitle(heap);
		titlebar.color(DiceMageUI.GOLD);
		layoutDiceFields(titlebar, "[LOOT]", heap.info(), width, DiceMageUI.GREY_LINE);
	}

	private void layoutDiceItem(Item item) {
		int width = PixelScene.landscape() ? 150 : 132;
		chrome.hardlight(DiceMageUI.DARK);
		int lineColor = DiceMageUI.itemLineColor(item, item.isEquipped(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero));
		ItemSlot icon = new ItemSlot(item);
		icon.setRect(DICE_PAD, DICE_PAD, DICE_ICON, DICE_ICON);
		RoundedFrame iconFrame = UITheme.roundedFrame(DiceMageUI.BLACK, lineColor);
		iconFrame.setRect(0, 0, DICE_ICON + DICE_PAD * 2, DICE_ICON + DICE_PAD * 2);
		add(iconFrame);
		add(icon);

		RenderedTextBlock title = PixelScene.renderTextBlock("[ITEM] " + item.name(), 8);
		title.hardlight(lineColor);
		title.maxWidth(width - DICE_ICON - (int)DICE_PAD * 4);
		title.setPos(DICE_ICON + DICE_PAD * 3, DICE_PAD);
		add(title);

		RenderedTextBlock tags = PixelScene.renderTextBlock(itemTags(item), 6);
		tags.hardlight(DiceMageUI.CREAM);
		tags.maxWidth(width - DICE_ICON - (int)DICE_PAD * 4);
		tags.setPos(title.left(), title.bottom() + 1);
		add(tags);

		float topHeight = Math.max(iconFrame.bottom(), tags.bottom()) + DICE_PAD;
		layoutDiceInfoBody(item.displayInfo(), width, topHeight, lineColor);
	}

	private void layoutDiceFields(IconTitle titlebar, String label, String body, int width, int lineColor) {
		RoundedFrame top = UITheme.roundedFrame(DiceMageUI.BLACK, lineColor);
		top.setRect(0, 0, width, 34);
		add(top);

		RenderedTextBlock labelText = PixelScene.renderTextBlock(label, 8);
		labelText.hardlight(DiceMageUI.GOLD);
		labelText.setPos(DICE_PAD, DICE_PAD);
		add(labelText);

		titlebar.setRect(DICE_PAD, labelText.bottom() + 1, width - DICE_PAD * 2, 0);
		add(titlebar);

		layoutDiceInfoBody(body, width, Math.max(34, titlebar.bottom() + DICE_PAD), lineColor);
	}

	private void layoutDiceInfoBody(String body, int width, float top, int lineColor) {
		RenderedTextBlock info = PixelScene.renderTextBlock(body, 6);
		info.hardlight(DiceMageUI.CREAM);
		info.maxWidth((int)(width - DICE_PAD * 4));
		info.setPos(DICE_PAD * 2, top + DICE_PAD * 2);

		RoundedFrame bodyFrame = UITheme.roundedFrame(DiceMageUI.PANEL, lineColor);
		bodyFrame.setRect(0, top + DICE_PAD, width, info.height() + DICE_PAD * 4);
		add(bodyFrame);
		add(info);

		resize(width, (int)(bodyFrame.bottom() + DICE_PAD));
	}

	private String itemTags(Item item) {
		String tags = "[card]";
		if (item.levelKnown && item.level() > 0) {
			tags += " +" + item.level();
		} else if (item.levelKnown && item.level() < 0) {
			tags += " " + item.level();
		}
		if (item.cursedKnown) {
			tags += item.cursed ? " cursed" : " clean";
		}
		if (!item.isIdentified()) {
			tags += " unknown";
		}
		return tags;
	}

	//the folding sections: description, enchantment, type
	private IconTitle secTitle;
	//an enchanted item's name breathes (its colour, its light waxing and waning)
	private IconTitle pulseTitle;
	private boolean pulses;
	private int pulseColor = 0xE8D9A8;
	private SectionHeader descHeader, enchantHeader, typeHeader;
	private RenderedTextBlock secInfo, enchantBody;
	private int secWidth;

	private void layoutItem( IconTitle title, RenderedTextBlock info, Item item ){
		secTitle = title;
		pulseTitle = title;
		pulses = ItemInfoText.enchantOf( item ) != null;
		secInfo = info;
		sectionItem = item;
		secWidth = WIDTH_MIN;
		info.maxWidth( secWidth );
		while (PixelScene.landscape() && info.height() > 100 && secWidth < WIDTH_MAX){
			secWidth += 20;
			info.maxWidth( secWidth );
		}

		descHeader = new SectionHeader( "\u63cf\u8ff0", 0xE8D9A8, secWidth );

		enchantHeader = null;
		enchantBody = null;
		Object ench = ItemInfoText.enchantKnown( item )
				? ItemInfoText.enchantOf( item ) : null;
		if (ItemInfoText.isGear( item )){
			int rColor = ench != null
					? ItemInfoText.enchantColor( item )
					: 0xE8D9A8;
			if (rColor == 0) rColor = 0xE8D9A8;
			pulseColor = rColor;

			//the header already names the type, so the body does not repeat it
			String famText = ItemInfoText.familyText( item );
			String famName = ItemInfoText.familyTitle( item );
			if (famText.startsWith( famName + "\uff1a" )){
				famText = famText.substring( famName.length() + 1 );
			}
			typeBody = PixelScene.renderTextBlock( famText, 6 );
			typeBody.maxWidth( secWidth );

			typeHeader = new SectionHeader(
					"\u7c7b\u578b\uff1a" + ItemInfoText.familyTitle( item ),
					rColor, secWidth );
			add( typeHeader );
			add( typeBody );

			if (ench != null){
				//the header only says which section it is: name and text both fold away below it
				boolean cursed = ench instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment
						&& ((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment) ench).curse();
				enchantHeader = new SectionHeader(
						cursed ? "\u8bc5\u5492" : "\u9644\u9b54", rColor, secWidth );
				enchantBody = PixelScene.renderTextBlock(
						"\u00b7 " + com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(
								ItemInfoText.enchantWord( item ) )
								+ "\n" + com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( ench, "desc" ), 6 );
				enchantBody.maxWidth( secWidth );
				add( enchantBody );
			}
		}

		add( secTitle );
		add( descHeader );
		add( secInfo );
		if (enchantHeader != null) add( enchantHeader );
		layoutSections();
	}

	/** Lays the sections out; folded ones take only their header's height. */
	//invisible buttons laid over each section header: pressing one folds that section

	private boolean descOpen = true, enchantOpen = true, typeOpen = true;
	private com.shatteredpixel.shatteredpixeldungeon.ui.Button typeHit;
	private RenderedTextBlock typeBody;
	private com.watabou.noosa.ColorBlock descRule;
	private com.shatteredpixel.shatteredpixeldungeon.ui.Button descHit, enchantHit;
	private Item sectionItem;

	private void layoutSections(){
		float y = 0;
		secTitle.setRect( 0, 0, secWidth, 0 );
		secTitle.setPos( 0, 0 );
		y = secTitle.bottom() + 2;

		descHeader.open = descOpen;
		descHeader.place( 0, y );
		if (descHit == null){
			descHit = new com.shatteredpixel.shatteredpixeldungeon.ui.Button(){
				@Override
				protected void onClick(){
					descOpen = !descOpen;
					layoutSections();
				}
			};
			add( descHit );
		}
		descHit.setRect( 0, y, secWidth, SectionHeader.HEIGHT );
		y += SectionHeader.HEIGHT;
		secInfo.visible = descOpen;
		if (descOpen){
			secInfo.setPos( 0, y );
			y = secInfo.bottom();
		}
		y += GAP;

		if (enchantHeader != null){
			enchantHeader.open = enchantOpen;
			enchantHeader.place( 0, y );
			if (enchantHit == null){
				enchantHit = new com.shatteredpixel.shatteredpixeldungeon.ui.Button(){
					@Override
					protected void onClick(){
						enchantOpen = !enchantOpen;
						layoutSections();
					}
				};
				add( enchantHit );
			}
			enchantHit.setRect( 0, y, secWidth, SectionHeader.HEIGHT );
			y += SectionHeader.HEIGHT;
			if (enchantBody != null){
				enchantBody.visible = enchantOpen;
				if (enchantOpen){
					enchantBody.setPos( 0, y );
					y = enchantBody.bottom();
				}
			}
			y += GAP;
		}

		if (typeHeader != null){
			typeHeader.open = typeOpen;
			typeHeader.place( 0, y );
			if (typeHit == null){
				typeHit = new com.shatteredpixel.shatteredpixeldungeon.ui.Button(){
					@Override
					protected void onClick(){
						typeOpen = !typeOpen;
						layoutSections();
					}
				};
				add( typeHit );
			}
			typeHit.setRect( 0, y, secWidth, SectionHeader.HEIGHT );
			y += SectionHeader.HEIGHT;
			if (typeBody != null){
				typeBody.visible = typeOpen;
				if (typeOpen){
					typeBody.setPos( 0, y );
					y = typeBody.bottom();
				}
			}
			y += GAP;
		}

		float oldHeight = height;
		resize( secWidth, (int)(y + 2) );
		onRelayout( oldHeight );

	}

	/** Called after the window's height changed (a section was folded). */
	protected void onRelayout( float oldHeight ){
	}

	@Override
	public void update(){
		super.update();
		//breathing: an enchanted item's name dims and brightens, in its enchantment's colour
		if (pulses && pulseTitle != null && pulseTitle.tfLabel != null){
			float wave = ItemInfoText.pulse();
			pulseTitle.tfLabel.alpha( 0.45f + 0.55f * wave );
			//and a little brighter at the top of the breath, not only more opaque
			pulseTitle.tfLabel.hardlight(
					ItemInfoText
							.lighten( pulseColor, 0.40f * wave ) );
		}
	}
}
