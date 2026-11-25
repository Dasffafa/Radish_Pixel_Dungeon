/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

/**
 * A part's header in the item window, the way Warped draws it: a fold arrow (down while open,
 * right while folded), the title in its colour, and a rule out to the window's edge. It only
 * draws; the window lays a transparent button over it for the press.
 *
 * The arrow and rule are artwork rather than text glyphs: the game font has no ▼ or ─ on Android,
 * so text turned into question marks there.
 */
public class SectionHeader extends Group {

	public static final int HEIGHT = 9;

	private final Image arrow;
	private final RenderedTextBlock label;
	private final Image rule;
	private final int color;

	private int fullWidth;
	private Image icon;

	/** Whether the section under this header is showing (the owner sets this before placing). */
	public boolean open = true;

	public SectionHeader( String text, int color, int width ){
		super();
		this.color = color;
		this.fullWidth = width;

		arrow = new Image( Assets.Sprites.SECTION_ARROW );
		arrow.hardlight( color );
		add( arrow );

		label = PixelScene.renderTextBlock( text, 6 );
		label.hardlight( color );
		add( label );

		rule = new Image( Assets.Sprites.SECTION_RULE );
		rule.hardlight( color );
		rule.alpha( 0.35f );
		add( rule );

		place( 0, 0 );
	}

	/** Shows a 16px-cell picture (half scale) between the arrow and the title. */
	public void setIcon( int cell ){
		if (icon == null){
			icon = new Image( Assets.Sprites.RARITY_ICONS );
			icon.scale.set( 0.5f );
			add( icon );
		}
		icon.frame( cell * 16, 0, 16, 16 );
		icon.visible = true;
	}

	/** Where the title ends, so the window can draw a rule out from there. */
	public float labelRight(){
		return label.right();
	}

	/** Puts the whole header at (x, y). */
	public void place( float x, float y ){
		arrow.frame( open ? 0 : 8, 0, 5, 5 );
		arrow.x = x + 1;
		arrow.y = y + (HEIGHT - 5) / 2f;

		if (icon != null && icon.visible){
			icon.x = x + 9;
			icon.y = y + (HEIGHT - 8) / 2f;
			label.setPos( x + 19, y + (HEIGHT - label.height()) / 2f );
		} else {
			label.setPos( x + 9, y + (HEIGHT - label.height()) / 2f );
		}

		float from = label.right() + 3;
		int room = Math.max( 0, (int)(fullWidth - (from - x)) );
		rule.scale.set( Math.max( 1, room ), 1f );
		rule.x = from;
		rule.y = y + HEIGHT / 2f;
	}
}
