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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.kit;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

/**
 * A flat ring spreading out on the ground. Warped's FxRing animates by swapping frames and reads
 * them as UV rects, which comes out as a flat line in this build; this one picks a single frame
 * from the same sheet and simply grows, which draws reliably.
 */
public class GroundRing extends Image {

	private float age, life, peak, cx, cy;
	private int fromW, toW;
	private int color;

	public GroundRing( Group parent, float x, float y, int fromW, int toW, float life, int color, float alpha ){
		super( Assets.Effects.GROUND_RING );

		this.cx = x;
		this.cy = y;
		this.fromW = Math.max( 5, fromW );
		this.toW = Math.max( this.fromW + 2, toW );
		this.life = Math.max( 0.05f, life );
		this.peak = alpha;
		this.color = color;


		hardlight( ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f );
		am = alpha;
		updateSize( 0f );

		parent.add( this );
	}

	private void updateSize( float p ){
		float w = fromW + (toW - fromW) * p;
		width = Math.round( w );
		height = Math.max( 2, Math.round( w / 2f ) );
		x = Math.round( cx - width / 2f );
		y = Math.round( cy - height / 2f );
	}

	@Override
	public void update(){
		super.update();
		age += com.watabou.noosa.Game.elapsed;
		float p = age / life;
		if (p >= 1f){
			killAndErase();
			return;
		}
		updateSize( p );
		am = peak * (1f - p);
	}
}
