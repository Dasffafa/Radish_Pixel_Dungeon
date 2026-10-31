/*
 * Radish Pixel Dungeon
 *
 * 原型机精灵：
 *   未激活 = dm_proto.png 的帧 0（停机躺倒，只有 1 帧，看起来完全不动）
 *   激活   = 换成对应型号怪物的原始贴图与动画
 *            model 100 → dm100.png（DM-100 的贴图）
 *            model 175 → dm200.png（DM-175 用 DM-200 的机甲外观）
 *
 * 注意：精灵类由 Char.sprite 懒建且常驻，中途改 spriteClass 是无效的，
 * 所以两种形态都在这一个精灵类里重建四条 Animation（见 AGENTS.md §6）。
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.watabou.noosa.TextureFilm;

public class DMPrototypeSprite extends MobSprite {

	private Animation inactiveAnim;

	private int model = 100;

	public DMPrototypeSprite() {
		super();

		texture( Assets.Sprites.DM_PROTO );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		// 停机：单帧、极慢，视觉上完全静止
		inactiveAnim = new Animation( 1, true );
		inactiveAnim.frames( frames, 0 );

		idle = inactiveAnim;
		run = inactiveAnim;
		attack = inactiveAnim;
		die = inactiveAnim;

		play( idle );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		//读档时精灵是后建的，restoreFromBundle 里拿不到 sprite，
		//已激活的机体必须在 link 时换成对应型号的原始贴图
		if (ch instanceof WardenPrototype) {
			WardenPrototype p = (WardenPrototype) ch;
			model = p.model;
			if (!p.isInactive()) {
				activate();
			}
		}
	}

	/** 激活后换成对应型号怪物的原始贴图与动画 */
	public void activate() {

		if (model == 175) {
			texture( Assets.Sprites.DM200 );
			TextureFilm frames = new TextureFilm( texture, 21, 18 );

			idle = new Animation( 10, true );
			idle.frames( frames, 0, 1 );

			run = new Animation( 10, true );
			run.frames( frames, 2, 3 );

			attack = new Animation( 15, false );
			attack.frames( frames, 4, 5, 6 );

			die = new Animation( 8, false );
			die.frames( frames, 9, 10, 11 );

		} else {
			texture( Assets.Sprites.DM100 );
			TextureFilm frames = new TextureFilm( texture, 16, 14 );

			idle = new Animation( 1, true );
			idle.frames( frames, 0, 1 );

			run = new Animation( 12, true );
			run.frames( frames, 6, 7, 8, 9 );

			attack = new Animation( 12, false );
			attack.frames( frames, 2, 3, 4, 0 );

			die = new Animation( 12, false );
			die.frames( frames, 10, 11, 12, 13, 14, 15 );
		}

		play( idle );
	}

	@Override
	public int blood() {
		return 0xFFFFFF88;
	}
}
