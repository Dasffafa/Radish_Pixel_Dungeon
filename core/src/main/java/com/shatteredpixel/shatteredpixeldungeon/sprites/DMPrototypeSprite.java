package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;

public class DMPrototypeSprite extends MobSprite {

	private Animation inactiveAnim;
	private int model = 100;

	public DMPrototypeSprite() {
		super();

		texture( Assets.Sprites.DM_PROTO );
		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		// 先只初始化空动画占位，不填帧！
		inactiveAnim = new Animation( 1, true );
		idle = new Animation( 1, true );
		run = new Animation( 1, true );
		attack = new Animation( 1, false );
		die = new Animation( 1, false );
		zap = attack.clone();
		idle.frames( frames, 0 );
		play( idle );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		if (ch instanceof WardenPrototype) {
			model = ((WardenPrototype) ch).model;

			// ✅ 拿到真实model之后，再构建【停机动画】！
			TextureFilm frames = new TextureFilm( texture, 16, 16 );
			inactiveAnim.frames( frames, model == 175 ? 0 : 1 );
			idle = inactiveAnim;

			if (((WardenPrototype) ch).isInactive()) {
				play( idle );
			} else {
				activate();
			}
		}
	}

	/** 激活后换成对应型号怪物的原始贴图与动画 */
	public void activate() {
		if (model == 175) {
			texture( Assets.Sprites.DM175 );
			TextureFilm frames = new TextureFilm( texture, 18, 15 );

			idle = new Animation( 7, true );
			idle.frames( frames, 0, 1, 2, 3 );

			run = new Animation( 9, true );
			run.frames( frames, 4, 5, 6, 7 );

			attack = new Animation( 11, false );
			attack.frames( frames, 8, 9, 10, 11 );

			die = new Animation( 9, false );
			die.frames( frames, 12, 13, 14, 15, 16, 17, 18, 19 );
		} else {
			texture( Assets.Sprites.DM100 );
			TextureFilm frames = new TextureFilm( texture, 16, 16 );

			idle = new Animation( 1, true );
			idle.frames( frames, 0, 1 );

			run = new Animation( 12, true );
			run.frames( frames, 6, 7, 8, 9 );

			attack = new Animation( 12, false );
			attack.frames( frames, 2, 3, 4, 0 );

			die = new Animation( 12, false );
			die.frames( frames, 10, 11, 12, 13, 14, 15 );
		}
		zap = attack.clone();
		play( idle );
	}

	public void zap( int pos ) {
		Char enemy = Actor.findChar(pos);
		//shoot lightning from eye, not sprite center.
		PointF origin = center();
		if (flipHorizontal){
			origin.y -= 6*scale.y;
			origin.x -= 1*scale.x;
		} else {
			origin.y -= 8*scale.y;
			origin.x += 1*scale.x;
		}
		if (enemy != null) {
			parent.add(new Lightning(origin, enemy.sprite.destinationCenter(), (WardenPrototype) ch));
		} else {
			parent.add(new Lightning(origin, pos, (WardenPrototype) ch));
		}
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );

		super.zap( ch.pos );
		flash();
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}

	@Override
	public int blood() {
		return 0xFFFFFF88;
	}
}
