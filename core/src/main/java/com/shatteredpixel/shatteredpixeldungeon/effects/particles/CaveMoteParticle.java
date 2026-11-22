package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;

/** 洞穴飞尘粒子（简版）：洞窟里飘的一点亮尘。 */
public class CaveMoteParticle extends PixelParticle.Shrinking {

	public static final Emitter.Factory EMBERS = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ){
			CaveMoteParticle p = (CaveMoteParticle) emitter.recycle( CaveMoteParticle.class );
			p.reset( x, y );
		}
		@Override
		public boolean lightMode(){ return true; }
	};

	public CaveMoteParticle(){
		super();
		color( 0xFFDDAA );
		lifespan = 0.8f;
		speed.set( 0, -12 );
	}

	public void reset( float x, float y ){
		revive();
		this.x = x;
		this.y = y;
		left = lifespan;
		size = 2;
	}
}
