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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WreckingBallMob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

/**
 * 破坏球落地后挂在英雄身上的控制条：显示蓄力层数，并提供一个动作按钮用于击飞或收回。
 */
public class WreckingBallControl extends Buff implements ActionIndicator.Action {

	private WreckingBallMob ball;

	{
		announced = false;
		type = buffType.POSITIVE;
	}

	public void setBall(WreckingBallMob ball) {
		this.ball = ball;
		ActionIndicator.refresh();
	}

	public WreckingBallMob ball() {
		return ball;
	}

	@Override
	public boolean act() {
		if (ball == null || !ball.isAlive()) {
			detach();
			return false;
		}
		spend(TICK);
		return true;
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			ActionIndicator.setAction(this);
			return true;
		}
		return false;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	@Override
	public String actionName() {
		if (ball != null && ball.isLaunched()) {
			return Messages.get(this, "recall");
		}
		return Messages.get(this, "launch");
	}

	@Override
	public ItemSprite primaryVisual() {
		return new ItemSprite(ItemSpriteSheet.WRECKING_BALL);
	}

	@Override
	public int indicatorColor() {
		return 0x9aa2b1;
	}

	@Override
	public void doAction() {
		if (ball == null || !ball.isAlive()) {
			detach();
			return;
		}
		if (target instanceof Hero) {
			if (ball.isLaunched()) {
				ball.recall((Hero) target);
			} else {
				ball.launch((Hero) target);
			}
		}
	}

	@Override
	public String desc() {
		if (ball == null || !ball.isAlive()) {
			return Messages.get(this, "detached");
		}
		return Messages.get(this, "desc", ball.charge, WreckingBallMob.MAX_CHARGE);
	}
}
