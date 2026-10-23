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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

//Sprite for the ice blocks created by the Potion of Crystal Ice.
//Uses a dedicated 16x16 ice tile instead of the crystal spire art.
public class IceBlockSprite extends MobSprite {

	{
		perspectiveRaise = 4 / 16f; //4 pixels

		shadowWidth     = 0.6f;
		shadowHeight    = 0.6f;
		shadowOffset    = 0.6f;
	}

	public IceBlockSprite(){
		super();

		texture( Assets.Sprites.ICE_BLOCK );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		//single frame; frame 1 is a collapsed version used when melting
		idle = new Animation( 1, true );
		idle.frames( frames, 0 );

		run = idle.clone();
		attack = idle.clone();
		zap = idle.clone();

		die = new Animation( 12, false );
		die.frames( frames, 0, 1 );

		play( idle );
	}

	@Override
	public int blood() {
		return 0xFF8EE3FF;
	}

	@Override
	public void turnTo( int from, int to ) {
		//do nothing, the block is symmetric
	}
}
