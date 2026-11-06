/*
 * Radish Pixel Dungeon
 *
 * 奶蛋生物贴图：单帧 16x16，直接使用 Assets.Sprites.NAIDAN。
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class NaidanEggSprite extends MobSprite {

	public NaidanEggSprite() {
		super();

		texture( Assets.Sprites.NAIDAN );

		// 只有一张 16x16 的图，所有动作都取第 0 帧
		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 0 );

		run = new Animation( 2, true );
		run.frames( frames, 0 );

		attack = new Animation( 2, false );
		attack.frames( frames, 0 );

		die = new Animation( 2, false );
		die.frames( frames, 0 );

		play( idle );
	}
}
