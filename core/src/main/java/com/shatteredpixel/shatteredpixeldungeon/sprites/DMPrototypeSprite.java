/*
 * Radish Pixel Dungeon
 *
 * 原型机精灵：dm_proto.png 里是两帧 16x16
 *   帧 0 = 停机躺倒（未激活）
 *   帧 1 = 侧身站立（已激活）
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class DMPrototypeSprite extends MobSprite {

	private Animation inactiveAnim;
	private Animation activeAnim;

	private boolean active = false;

	public DMPrototypeSprite() {
		super();

		texture( Assets.Sprites.DM_PROTO );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		inactiveAnim = new Animation( 1, true );
		inactiveAnim.frames( frames, 0 );

		activeAnim = new Animation( 2, true );
		activeAnim.frames( frames, 1 );

		idle = inactiveAnim;
		run = activeAnim;
		attack = activeAnim;
		die = inactiveAnim;

		play( idle );
	}

	public void activate() {
		active = true;
		idle = activeAnim;
		run = activeAnim;
		attack = activeAnim;
		play( idle );
	}

	public boolean isActive() {
		return active;
	}
}
