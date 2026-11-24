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
 * A titled section divider, the way Warped's item window reads: a small triangle, the title, and a
 * hairline running out to the right edge. Used for the description and rarity blocks.
 */
public class SectionHeader extends Group {

	public static final int HEIGHT = 9;

	private final RenderedTextBlock label;
	private final ColorBlock line;
	private final ColorBlock mark;

	private final int width;

	public SectionHeader( String text, int color, int width ){
		super();
		this.width = width;

		mark = new ColorBlock( 4, 4, color );
		mark.x = 0;
		mark.y = 2;

		label = PixelScene.renderTextBlock( text, 6 );
		label.hardlight( color );
		label.setPos( 6, 0 );

		line = new ColorBlock( 1, 1, color );
		line.alpha( 0.35f );
		line.x = label.right() + 3;
		line.y = 3;
		line.size( Math.max( 1, width - line.x ), 1 );

		add( mark );
		add( label );
		add( line );
	}

	/** Puts the whole header at (x, y); a Group has no position of its own. */
	public void place( float x, float y ){
		mark.x = x;
		mark.y = y + 2;
		label.setPos( x + 6, y );
		line.x = x + label.right() + 3;
		line.y = y + 3;
		line.size( Math.max( 1, width - (line.x - x) ), 1 );
	}
}
