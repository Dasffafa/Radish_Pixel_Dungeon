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
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 次元跳跃模式：开启后攻击未触发传送则扣充能强制传送。 */
public class RiftJump extends FlavourBuff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	@Override
	public String icon() {
		return BuffIndicator.LEVITATION;
	}

	@Override
	public String toString() {
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( this, "name" );
	}
}
