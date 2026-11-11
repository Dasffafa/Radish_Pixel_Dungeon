/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/** 次元撕裂者的裂隙粒子：白 → 紫 → 深紫，三种发射形态。 */
public class RiftParticle extends PixelParticle {

	/** 命中时的小簇飞散。 */
	public static final Emitter.Factory HIT = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((RiftParticle) emitter.recycle( RiftParticle.class )).reset( x, y, 0 );
		}
	};

	/** 传送起点向外炸开。 */
	public static final Emitter.Factory SCATTER = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((RiftParticle) emitter.recycle( RiftParticle.class )).reset( x, y, 1 );
		}
		@Override
		public boolean lightMode() { return true; }
	};

	/** 传送终点向内收束。 */
	public static final Emitter.Factory IMPLODE = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((RiftParticle) emitter.recycle( RiftParticle.class )).reset( x, y, 2 );
		}
		@Override
		public boolean lightMode() { return true; }
	};

	private int mode;

	public RiftParticle() {
		super();
		lifespan = 0.5f;
	}

	public void reset( float x, float y, int mode ) {
		revive();
		this.x = x;
		this.y = y;
		this.mode = mode;
		left = lifespan;
		if (mode == 0){
			speed.set( Random.Float( -6, 6 ), Random.Float( -6, 6 ) );
		} else if (mode == 1){
			speed.polar( Random.Float( PointF.PI2 ), Random.Float( 18, 34 ) );
		} else {
			float angle = Random.Float( PointF.PI2 );
			speed.polar( angle, Random.Float( 20, 36 ) );
			this.x += Math.cos( angle ) * 8;
			this.y += Math.sin( angle ) * 8;
		}
	}

	@Override
	public void update() {
		super.update();
		float am = left / lifespan;
		size( mode == 0 ? 3 - am * 2 : 5 - am * 4 );
		color( ColorMath.interpolate( 0x220044, 0xEEAAFF, am ) );
	}
}
