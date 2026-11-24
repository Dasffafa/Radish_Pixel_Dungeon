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

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;

/**
 * A part's header in the item window, drawn the way Warped draws it: a five-pixel fold arrow (down
 * while the section is open, right while it is folded), the title in its colour, and a rule out to
 * the window's edge. It only draws; the window lays a transparent button over it for the press.
 */
public class SectionHeader extends Group {

	public static final int HEIGHT = 9;

	private final RenderedTextBlock arrow;
	private final RenderedTextBlock label;
	private final RenderedTextBlock rule;
	private final int color;
	private int fullWidth;

	/** Whether the section under this header is showing (the owner sets this before placing). */
	public boolean open = true;

	public SectionHeader( String text, int color, int width ){
		super();
		this.color = color;

		arrow = PixelScene.renderTextBlock( "\u25bc", 6 );
		arrow.hardlight( color );
		add( arrow );

		label = PixelScene.renderTextBlock( text, 6 );
		label.hardlight( color );
		add( label );

		rule = PixelScene.renderTextBlock( "", 6 );
		rule.hardlight( color );
		rule.alpha( 0.55f );
		add( rule );

		fullWidth = width;
		place( 0, 0 );
	}

	private void placeArrow( float y ){
		arrow.text( open ? "\u25bc" : "\u25b6" );
		arrow.hardlight( color );
		arrow.setPos( 1, y + (HEIGHT - arrow.height()) / 2f );
	}

	private void placeRule( float y ){
		float from = label.right() + 3;
		int room = Math.max( 0, (int)(fullWidth - from) );
		StringBuilder dashes = new StringBuilder();
		while (dashes.length() < 100) dashes.append( "\u2500" );
		rule.text( dashes.toString() );
		float perChar = Math.max( 1f, rule.width() / 100f );
		int n = Math.max( 1, (int)(room / perChar) );
		StringBuilder fit = new StringBuilder();
		for (int i = 0; i < n; i++) fit.append( "\u2500" );
		rule.text( fit.toString() );
		rule.setPos( from, y + (HEIGHT - rule.height()) / 2f );
	}

	/** Puts the whole header at (x, y). */
	public void place( float x, float y ){
		placeArrow( y );
		label.setPos( x + 9, y + (HEIGHT - label.height()) / 2f );
		placeRule( y );
	}
}
