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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.watabou.noosa.Game;

/**
 * The buff's icon, rising off the head of whoever just gained it and fading out. Warped shows the
 * same thing through its blow presenter; this keeps it to the moment a buff attaches.
 */
public class BuffPop extends BuffIcon {

	private static final float LIFE = 0.9f;
	private static final float RISE = 14f;

	private float age;

	public BuffPop( Buff buff ){
		super( buff, true );
		buff.tintIcon( this );
		scale.set( 0.75f );
		alpha( 1f );
	}

	@Override
	public void update(){
		super.update();
		age += Game.elapsed;
		if (age >= LIFE){
			killAndErase();
			return;
		}
		y -= RISE * Game.elapsed;
		alpha( 1f - age / LIFE );
	}
}
