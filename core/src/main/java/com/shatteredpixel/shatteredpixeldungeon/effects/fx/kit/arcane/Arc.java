package com.shatteredpixel.shatteredpixeldungeon.effects.fx.kit.arcane;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.kit.FxParticle;
import com.watabou.noosa.particles.Emitter;

/**
 * Warped's arcane particle factory. Its own version also carries light and ring helpers, which
 * belong to systems not ported here; the trails only need a fresh particle at a point.
 */
public final class Arc {

	private Arc(){}

	public static FxParticle make( Emitter e, float x, float y ){
		FxParticle p = (FxParticle) e.recycle( FxParticle.class );
		p.reset( x, y );
		return p;
	}
}
