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

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Random;

//A looping 3-frame cyclone drawn over a tornado blob cell.
public class CycloneParticle extends MovieClip {

	private static TextureFilm film;
	private static Animation anim;

	public CycloneParticle() {
		super( Assets.Effects.CYCLONE );

		if (film == null) {
			film = new TextureFilm( texture, 16, 16 );
		}
		if (anim == null) {
			anim = new MovieClip.Animation( 8, true );
			anim.frames( film, 0, 1, 2 );
		}
		play( anim );
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x - width / 2f;
		this.y = y - height / 2f;
		//每帧随机起点，避免所有粒子完全同步
		frame( film.get( Random.Int( 3 ) ) );
	}

	@Override
	public void update() {
		super.update();
	}
}
