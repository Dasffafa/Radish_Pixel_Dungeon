/*
 * Radish Pixel Dungeon
 *
 * 典狱长（Warden）精灵
 *
 * 贴图 warden.png 的结构（256 色检查过列投影）：
 *   帧宽 12、帧高 16，每行横排 10 帧；
 *   行 0 = 红眼形态（带红色拖尾）动作帧
 *   行 1 = 红眼形态（带橙色拖尾）动作帧
 *   行 2 = 常态（黑眼）
 * 素材在 x>120 处还有一段手绘草稿，这里不使用，只取每行前 10 帧。
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class WardenSprite extends MobSprite {

	private Animation normalIdle, normalRun, normalAttack;
	private Animation rageIdle, rageRun, rageAttack, rageDie;

	private boolean enraged = false;

	public WardenSprite() {
		super();

		texture( Assets.Sprites.WARDEN );

		TextureFilm frames = new TextureFilm( texture, 12, 16 );

		//行 2（20..29）= 常态
		normalIdle   = new Animation( 2, true );
		normalIdle.frames( frames, 20, 21 );

		normalRun    = new Animation( 10, true );
		normalRun.frames( frames, 20, 21, 22, 23, 24 );

		normalAttack = new Animation( 12, false );
		normalAttack.frames( frames, 24, 25, 26, 27, 28, 29, 20 );

		//行 1（10..19）、行 0（0..9）= 红眼形态
		rageIdle   = new Animation( 2, true );
		rageIdle.frames( frames, 10, 11 );

		rageRun    = new Animation( 10, true );
		rageRun.frames( frames, 10, 11, 12, 13, 14 );

		rageAttack = new Animation( 12, false );
		rageAttack.frames( frames, 14, 15, 16, 17, 18, 19, 10 );

		rageDie    = new Animation( 12, false );
		rageDie.frames( frames, 0, 1, 2, 3, 4, 5 );

		idle = normalIdle;
		run = normalRun;
		attack = normalAttack;
		die = rageDie;

		play( idle );
	}

	/** 半血后切红眼形态（贴图换行，不换精灵类） */
	public void setEnraged( boolean value ) {
		if (enraged == value ) {
			return;
		}
		enraged = value;
		if (enraged ) {
			idle = rageIdle;
			run = rageRun;
			attack = rageAttack;
		} else {
			idle = normalIdle;
			run = normalRun;
			attack = normalAttack;
		}
		idle();
	}

	public boolean isEnraged() {
		return enraged;
	}
}
