package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.WreckingBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * 放在地上的破坏球：按住动作按钮蓄力，松手击飞。
 */
public class WreckingBallControl extends Buff implements ActionIndicator.Action {

	public static final int MAX_CHARGE = 10;

	private static final float MAX_HOLD_TIME = 0.6f;
	private static final float CYCLE = MAX_HOLD_TIME * 2f;
	private static final float MISFIRE_WINDOW = 1.2f;

	private WreckingBall ball;
	private int cell = -1;

	private float progress = 0f;
	private boolean fired = false;
	private float misfireAt = -1f;

	private BitmapText chargeText;
	private Image arc;

	{
		announced = false;
		type = buffType.POSITIVE;
	}

	public void setBall(WreckingBall ball, int cell) {
		this.ball = ball;
		this.cell = cell;
		ActionIndicator.refresh();
	}

	@Override
	public boolean act() {
		if (!ballPlacedHere()) {
			cleanupVisuals();
			detach();
			//必须照常消耗时间并返回 true，返回 false 会让回合循环停下
			spend(TICK);
			return true;
		}
		spend(TICK);
		return true;
	}

	private boolean ballPlacedHere() {
		if (ball == null || cell < 0 || Dungeon.level == null) {
			return false;
		}
		Heap heap = Dungeon.level.heaps.get(cell);
		return heap != null && heap.items != null && heap.items.contains(ball);
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
		return ballPlacedHere() && target != null && Dungeon.level.distance(target.pos, cell) <= 1;
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

		float phase = heldTime % CYCLE;
		progress = phase <= MAX_HOLD_TIME ? phase / MAX_HOLD_TIME : (CYCLE - phase) / MAX_HOLD_TIME;

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
		progress = phase <= MAX_HOLD_TIME ? phase / MAX_HOLD_TIME : (CYCLE - phase) / MAX_HOLD_TIME;

		fire();
	}

	@Override
	public void doAction() {
		if (!ballPlacedHere()) {
			detach();
			return;
		}
		if (!(target instanceof Hero)) {
			return;
		}
		if (Dungeon.level.distance(target.pos, cell) > 1) {
			GLog.w(Messages.get(this, "too_far"));
			return;
		}
		progress = 0f;
		fire();
	}

	private void fire() {
		fired = true;
		int charges = Math.round(progress * MAX_CHARGE);

		if (ballPlacedHere()) {
			Heap heap = Dungeon.level.heaps.get(cell);
			heap.remove(ball);
			WreckingBall.launchFromGround((Hero) target, ball, cell, charges);
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

	private float facingAngle() {
		if (target == null || target.sprite == null || cell < 0) {
			return 0f;
		}
		PointF heroCenter = target.sprite.center();
		PointF ballCenter = DungeonTilemap.tileCenterToWorld(cell);
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
		return Messages.get(this, "desc", MAX_CHARGE);
	}
}