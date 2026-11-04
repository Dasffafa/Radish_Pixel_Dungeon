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
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;

/**
 * 破坏球落地后挂在英雄身上的控制条。
 *
 * 按住动作按钮蓄力（约 {@link #MAX_HOLD_TIME} 秒充满），松手把球击飞出去；
 * 球飞行期间再点一下即可把它收回。
 */
public class WreckingBallControl extends Buff implements ActionIndicator.Action {

	/** 按住多久算满蓄力（秒） */
	public static final float MAX_HOLD_TIME = 1.0f;

	private WreckingBallMob ball;

	/** 0~1 的蓄力进度 */
	private float holdProgress = 0f;

	private BitmapText chargeText;

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
	public Visual primaryVisual() {
		return new ItemSprite(ItemSpriteSheet.WRECKING_BALL);
	}

	@Override
	public Visual secondaryVisual() {
		chargeText = new BitmapText(PixelScene.pixelFont);
		chargeText.hardlight(0x1e6ad1);
		chargeText.visible = false;
		updateChargeText();
		return chargeText;
	}

	@Override
	public int indicatorColor() {
		return 0x9aa2b1;
	}

	@Override
	public boolean holdToCharge() {
		return ball != null && ball.isAlive() && !ball.isLaunched();
	}

	@Override
	public void onHoldStart() {
		holdProgress = 0f;
		if (chargeText != null) {
			chargeText.visible = true;
		}
		updateChargeText();
	}

	@Override
	public void onHoldTick(float heldTime) {
		holdProgress = Math.min(1f, heldTime / MAX_HOLD_TIME);
		updateChargeText();
	}

	@Override
	public void onHoldRelease(float heldTime) {
		holdProgress = Math.min(1f, heldTime / MAX_HOLD_TIME);
		if (ball != null && ball.isAlive() && target instanceof Hero) {
			ball.launch((Hero) target, Math.round(holdProgress * WreckingBallMob.MAX_CHARGE));
			ActionIndicator.refresh();
		}
		holdProgress = 0f;
		if (chargeText != null) {
			chargeText.visible = false;
		}
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
				//轻点视为零蓄力击飞
				ball.launch((Hero) target, 0);
				ActionIndicator.refresh();
			}
		}
	}

	private void updateChargeText() {
		if (chargeText != null) {
			chargeText.text(Integer.toString(Math.round(holdProgress * WreckingBallMob.MAX_CHARGE)));
			chargeText.measure();
		}
	}

	@Override
	public String desc() {
		if (ball == null || !ball.isAlive()) {
			return Messages.get(this, "detached");
		}
		return Messages.get(this, "desc", WreckingBallMob.MAX_CHARGE);
	}
}
