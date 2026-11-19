package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BreakBallEntity;
import com.watabou.noosa.TextureFilm;

public class BreakBallSprite extends MobSprite {

	public BreakBallSprite() {
		this( Assets.Sprites.BREAK_BALL_1 );
	}

	protected BreakBallSprite( String asset ) {
		super();

		texture( asset );

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

	@Override
	public void link( Char ch ) {
		super.link( ch );
		if ( ch instanceof BreakBallEntity ) {
			scale.set( ((BreakBallEntity) ch).visualScale() );
		}
	}

	public static class Variant1 extends BreakBallSprite {
		public Variant1() {
			super( Assets.Sprites.BREAK_BALL_1 );
		}
	}

	public static class Variant2 extends BreakBallSprite {
		public Variant2() {
			super( Assets.Sprites.BREAK_BALL_2 );
		}
	}

	public static class Egg extends BreakBallSprite {
		public Egg() {
			super( Assets.Sprites.BREAK_BALL_EGG );
		}
	}
}
