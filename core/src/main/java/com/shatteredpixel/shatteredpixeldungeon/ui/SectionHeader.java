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
 * A titled section divider that can be pressed to fold its text away, the way Warped's item window
 * reads: a triangle, the title, a hairline out to the edge, and the section's text under it. The
 * triangle points down while the text shows and right while it is folded; the owner lays the
 * window out again when it changes.
 */
public class SectionHeader extends Button {

	public static final int HEIGHT = 9;

	private final RenderedTextBlock label;
	private final ColorBlock line;
	private final ColorBlock mark;

	/** Whether the section under this header is showing. */
	public boolean open = true;

	private Runnable onToggle;

	public SectionHeader( String text, int color, int width ){
		super();

		mark = new ColorBlock( 4, 4, color );
		mark.x = 0;
		mark.y = 2;

		label = PixelScene.renderTextBlock( text, 6 );
		label.hardlight( color );
		label.setPos( 6, 0 );

		line = new ColorBlock( 1, 1, color );
		line.alpha( 0.35f );
		line.y = 3;

		add( mark );
		add( label );
		add( line );

		setSize( width, HEIGHT );
		placeLine( 0, width );
	}

	public void setOnToggle( Runnable r ){
		onToggle = r;
	}

	@Override
	protected void onClick(){
		open = !open;
		if (onToggle != null) onToggle.run();
	}

	private void placeLine( float x, int width ){
		line.x = x + label.right() + 3;
		line.size( Math.max( 1, width - (line.x - x) ), 1 );
	}

	/** Puts the whole header at (x, y). */
	public void place( float x, float y ){
		mark.x = x;
		mark.y = y + 2;
		label.setPos( x + 6, y );
		line.y = y + 3;
		placeLine( x, (int)width );
	}
}
