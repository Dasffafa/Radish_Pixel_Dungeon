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
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;

/**
 * A part's header in the item window, the way Warped draws it: a five-pixel fold arrow, the title
 * in its colour, and a rule out to the window's edge. Pressing it folds or unfolds the text under
 * it; the arrow points down while open and right while folded.
 */
public class SectionHeader extends Button {

	public static final int HEIGHT = 9;

	private final ColorBlock[] arrow = new ColorBlock[3];
	private final RenderedTextBlock label;
	private final ColorBlock rule;

	/** Whether the text under this header is showing. */
	public boolean open = true;

	private Runnable onToggle;

	public SectionHeader( String text, int color, int width ){
		super();

		for (int i = 0; i < arrow.length; i++){
			arrow[i] = new ColorBlock( 1, 1, color );
			add( arrow[i] );
		}

		label = PixelScene.renderTextBlock( text, 6 );
		label.hardlight( color );
		add( label );

		rule = new ColorBlock( 1, 1, color );
		rule.alpha( 0.3f );
		add( rule );

		setSize( width, HEIGHT );
		placeArrow( color );
		label.setPos( 9, (HEIGHT - label.height()) / 2f );
		placeRule( width );
	}

	public void setOnToggle( Runnable r ){
		onToggle = r;
	}

	@Override
	protected void onClick(){
		toggle();
	}

	/** Folds or unfolds the section (also what a test calls). */
	public void toggle(){
		open = !open;
		if (onToggle != null) onToggle.run();
	}

	/** Puts the whole header at (x, y); the children sit relative to it. */
	public void place( float x, float y ){
		setPos( x, y );
		placeArrow( -1 );
		label.setPos( 9, (HEIGHT - label.height()) / 2f );
		placeRule( (int)width );
	}

	private void placeArrow( int color ){
		float ax = 1, ay = (HEIGHT - 5) / 2f;
		for (int i = 0; i < arrow.length; i++){
			int len = 5 - 2 * i;
			if (open){
				arrow[i].size( len, 1 );
				arrow[i].x = ax + i;
				arrow[i].y = ay + 1 + i;
			} else {
				arrow[i].size( 1, len );
				arrow[i].x = ax + 1 + i;
				arrow[i].y = ay + i;
			}
		}
	}

	private void placeRule( int width ){
		float from = label.right() + 3;
		rule.size( Math.max( 0, width - from ), 1 );
		rule.x = from;
		rule.y = HEIGHT / 2f;
	}
}
