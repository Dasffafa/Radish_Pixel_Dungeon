/*
 * Radish Pixel Dungeon
 *
 * 典狱长（Warden）精灵
 *
 * 贴图 warden.png 的结构（256 色检查过列投影）：
 *   帧宽 12、帧高 16；每行手绘内容为 10 帧，但贴图宽 200px，
 *   按 12px 切成 TextureFilm 后每行是 16 格（第 10..15 格为空，行 3 整行空）。
 *   行 0 = 红眼形态（带红色拖尾）—— 用作死亡/解体
 *   行 1 = 红眼形态（带橙色拖尾）—— 半血后的形态
 *   行 2 = 常态（黑眼）
 * 因此索引必须按「行号 * 16 + 帧序号」来算，不能按每行 10 帧直接累加。
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.watabou.noosa.TextureFilm;

public class WardenSprite extends MobSprite {

	private Animation normalIdle, normalRun, normalAttack;
	private Animation rageIdle, rageRun, rageAttack, rageDie;

	private boolean enraged = false;

	public WardenSprite() {
		super();

		texture( Assets.Sprites.WARDEN );

		TextureFilm frames = new TextureFilm( texture, 12, 15 );

		//行 2（32..41）= 常态
		normalIdle   = new Animation( 2, true );
		normalIdle.frames( frames, 32, 33 );

		normalRun    = new Animation( 10, true );
		normalRun.frames( frames, 32, 33, 34, 35, 36 );

		normalAttack = new Animation( 12, false );
		normalAttack.frames( frames, 36, 37, 38, 39, 40, 41, 32 );

		//行 1（16..25）= 红眼形态
		rageIdle   = new Animation( 2, true );
		rageIdle.frames( frames, 16, 17 );

		rageRun    = new Animation( 10, true );
		rageRun.frames( frames, 16, 17, 18, 19, 20 );

		rageAttack = new Animation( 12, false );
		rageAttack.frames( frames, 20, 21, 22, 23, 24, 25, 16 );

		//行 0（0..9）= 红眼解体帧，用作死亡动画
		rageDie    = new Animation( 12, false );
		rageDie.frames( frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 );

		idle = normalIdle;
		run = normalRun;
		attack = normalAttack;
		die = rageDie;

		play( idle );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		//读档时精灵是后建的，restoreFromBundle 里拿不到 sprite，
		//所以红眼形态必须在 link 时按 Boss 的真实状态补上
		if (ch instanceof Warden && ((Warden) ch).isEnraged()) {
			setEnraged( true );
		}
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
