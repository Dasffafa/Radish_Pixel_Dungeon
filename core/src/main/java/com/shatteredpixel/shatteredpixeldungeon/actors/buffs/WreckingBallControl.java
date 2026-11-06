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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WreckingBallMob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * 破坏球落地后挂在英雄身上的控制条。
 *
 * 按住动作按钮蓄力：扇形指示器在英雄身旁张开并随时间放大，
 * 蓄满后回落，形成 0→满→0 的循环；完整走过一轮后，会在随机时刻自行脱手发射。
 * 松开时按当前蓄力层数击飞，并消耗与蓄力相匹配的回合数。
 */
public class WreckingBallControl extends Buff implements ActionIndicator.Action {

	/** 满蓄力层数 */
	public static final int MAX_CHARGE = 10;

	/** 从 0 蓄到满所需时间（秒） */
	private static final float MAX_HOLD_TIME = 0.6f;

	/** 一个完整的 0→满→0 周期（秒） */
	private static final float CYCLE = MAX_HOLD_TIME * 2f;

	/** 走过一轮后，随机脱手的额外等待上限（秒） */
	private static final float MISFIRE_WINDOW = 1.2f;

	private WreckingBallMob ball;

	/** 当前蓄力进度 0~1 */
	private float progress = 0f;

	/** 本轮是否已经发射（防止松手时重复触发） */
	private boolean fired = false;

	/** 随机脱手的时间点，-1 表示尚未排定 */
	private float misfireAt = -1f;

	private BitmapText chargeText;
	private Image arc;

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
			cleanupVisuals();
			detach();
			//注意：这里必须照常消耗时间并返回 true。
			//返回 false 会让 Actor 的回合循环直接停下，表现为"卡死回合"。
			spend(TICK);
			return true;
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
		cleanupVisuals();
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
		return ball != null && ball.isAlive() && !ball.isLaunched() && adjacentToBall();
	}

	private boolean adjacentToBall() {
		return target != null && ball != null && Dungeon.level.adjacent(target.pos, ball.pos);
	}

	@Override
	public void onHoldStart() {
		fired = false;
		misfireAt = -1f;
		progress = 0f;

		if (target != null && target.sprite != null) {
			arc = new Image(Assets.Sprites.CHARGE_ARC);
			arc.origin.set(0, 32);
			arc.alpha(0.55f);
			arc.hardlight(0x9aa2b1);
			arc.camera = target.sprite.camera();
			target.sprite.parent.add(arc);
		}

		if (chargeText != null) {
			chargeText.visible = true;
		}
		updateVisuals();
	}

	@Override
	public void onHoldTick(float heldTime) {
		if (fired) {
			return;
		}

		//0→满→0 往复
		float phase = heldTime % CYCLE;
		progress = phase <= MAX_HOLD_TIME
				? phase / MAX_HOLD_TIME
				: (CYCLE - phase) / MAX_HOLD_TIME;

		//至少走完一轮后，随机时刻自行脱手
		if (heldTime >= CYCLE && misfireAt < 0f) {
			misfireAt = CYCLE + Random.Float() * MISFIRE_WINDOW;
		}
		if (misfireAt > 0f && heldTime >= misfireAt && Dungeon.hero != null && Dungeon.hero.ready) {
			fire();
			return;
		}

		updateVisuals();
	}

	@Override
	public void onHoldRelease(float heldTime) {
		if (fired) {
			cleanupVisuals();
			return;
		}

		float phase = heldTime % CYCLE;
		progress = phase <= MAX_HOLD_TIME
				? phase / MAX_HOLD_TIME
				: (CYCLE - phase) / MAX_HOLD_TIME;

		fire();
	}

	@Override
	public void doAction() {
		if (ball == null || !ball.isAlive()) {
			detach();
			return;
		}
		if (!(target instanceof Hero)) {
			return;
		}
		Hero hero = (Hero) target;

		if (ball.isLaunched()) {
			ball.recall(hero);
			return;
		}

		if (!adjacentToBall()) {
			GLog.w(Messages.get(this, "too_far"));
			return;
		}

		//轻点视为零蓄力击飞
		progress = 0f;
		fire();
	}

	private void fire() {
		fired = true;
		int charges = Math.round(progress * MAX_CHARGE);

		if (ball != null && ball.isAlive() && target instanceof Hero) {
			Hero hero = (Hero) target;
			ball.launch(hero, charges);
			//蓄力也要花时间：基础 0.5 回合，每层再 +0.1
			if (hero.ready) {
				hero.spendAndNext(0.5f + 0.1f * charges);
			}
			ActionIndicator.refresh();
		}

		cleanupVisuals();
	}

	private void updateVisuals() {
		updateChargeText();

		if (arc != null && target != null && target.sprite != null) {
			PointF center = target.sprite.center();
			arc.x = center.x;
			arc.y = center.y - 32;
			float scale = 0.25f + 0.75f * progress;
			arc.scale.set(scale, scale);
			arc.angle = facingAngle();
			arc.visible = progress > 0.02f;
		}
	}

	/** 扇形朝向：英雄指向破坏球的方向 */
	private float facingAngle() {
		if (ball == null || ball.sprite == null || target == null || target.sprite == null) {
			return 0f;
		}
		PointF heroCenter = target.sprite.center();
		PointF ballCenter = ball.sprite.center();
		return (float) Math.toDegrees(Math.atan2(ballCenter.y - heroCenter.y, ballCenter.x - heroCenter.x));
	}

	private void updateChargeText() {
		if (chargeText != null) {
			chargeText.text(Integer.toString(Math.round(progress * MAX_CHARGE)));
			chargeText.measure();
		}
	}

	private void cleanupVisuals() {
		if (arc != null) {
			arc.killAndErase();
			arc.destroy();
			arc = null;
		}
		if (chargeText != null) {
			chargeText.visible = false;
		}
		progress = 0f;
	}

	@Override
	public String desc() {
		if (ball == null || !ball.isAlive()) {
			return Messages.get(this, "detached");
		}
		return Messages.get(this, "desc", MAX_CHARGE);
	}
}
