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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DimensionalRipper;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 空间裂隙窗口：传送成功后 3 回合内可追击到目标身旁并攻击一次。 */
public class RiftWindow extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public static final float DURATION = 3f;

	private Char target;
	private int targetPos = -1;

	/** 打开窗口（或刷新剩余时间），记录这次被攻击的敌人。 */
	public void open( Char target ){
		this.target = target;
		this.targetPos = target == null ? -1 : target.pos;
		postpone( DURATION );
		ActionIndicator.setAction( this );
		GLog.p( Messages.get( DimensionalRipper.class, "window_open" ) );
	}

	@Override
	public String icon() {
		return BuffIndicator.LEVITATION;
	}

	@Override
	public String actionName() {
		return Messages.get( DimensionalRipper.class, "space_tear" );
	}

	@Override
	public int indicatorColor() {
		return 0xAA55FF;
	}

	@Override
	public void doAction() {
		Hero hero = Dungeon.hero;
		if (hero == null || target == null || !target.isAlive()){
			detach();
			return;
		}
		int cell = freeCellNear( target.pos );
		if (cell < 0){
			GLog.w( Messages.get( DimensionalRipper.class, "no_space" ) );
			return;
		}
		ScrollOfTeleportation.appear( hero, cell );
		AttackIndicator.target( target );
		hero.sprite.attack( target.pos, new com.watabou.utils.Callback() {
			@Override
			public void call() {
				hero.attack( target );
				hero.spendAndNext( hero.attackDelay() );
			}
		});
		detach();
	}

	private int freeCellNear( int pos ){
		int w = Dungeon.level.width();
		int[] dirs = { -w-1, -w, -w+1, -1, 1, w-1, w, w+1 };
		for (int d : dirs){
			int c = pos + d;
			if (c >= 0 && c < Dungeon.level.passable.length
					&& Dungeon.level.passable[c] && Actor.findChar(c) == null){
				return c;
			}
		}
		return -1;
	}

	@Override
	public boolean act() {
		detach();
		return true;
	}

	@Override
	public void detach() {
		ActionIndicator.clearAction( this );
		target = null;
		super.detach();
	}

	@Override
	public String toString() {
		return Messages.get( this, "name" );
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc" );
	}
}
