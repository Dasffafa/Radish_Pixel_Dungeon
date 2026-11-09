package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.badlogic.gdx.Gdx;
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
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * 放在地上的破坏球。
 *
 * 蓄力时英雄身旁会出现扇形与方向箭头，玩家移动指针即可旋转箭头选择发射方向；
 * 蓄力值决定初速度、伤害与飞行距离。松手按箭头方向击飞。
 */
public class WreckingBallControl extends Buff implements ActionIndicator.Action {

	public static final int MAX_CHARGE = 10;

	/** 蓄满所需时间，刻意放慢方便瞄准 */
	private static final float MAX_HOLD_TIME = 1.5f;
	private static final float CYCLE = MAX_HOLD_TIME * 2f;
	private static final float MISFIRE_WINDOW = 1.5f;

	private WreckingBall ball;
	private int cell = -1;

	private float progress = 0f;
	private float aimAngle = 0f;
	private boolean fired = false;
	private float misfireAt = -1f;

	private BitmapText chargeText;
	private Image arc;
	private Image arrow;

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
			//必须照常消耗时间并返回 true，否则回合循环会停下
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
		aimAngle = defaultAimAngle();

		if (target != null && target.sprite != null) {
			arc = new Image(Assets.Sprites.CHARGE_ARC);
			arc.origin.set(0, 32);
			arc.alpha(0.55f);
			arc.hardlight(0x9aa2b1);
			arc.camera = target.sprite.camera();
			target.sprite.parent.add(arc);

			arrow = new Image(Assets.Sprites.CHARGE_ARROW);
			arrow.origin.set(1, 8);
			arrow.alpha(0.85f);
			arrow.hardlight(0xffffff);
			arrow.camera = target.sprite.camera();
			target.sprite.parent.add(arrow);
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

		//跟随指针方向
		updateAimFromPointer();

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
		aimAngle = defaultAimAngle();
		fire();
	}

	private void fire() {
		fired = true;
		int charges = Math.round(progress * MAX_CHARGE);

		if (ballPlacedHere()) {
			Heap heap = Dungeon.level.heaps.get(cell);
			heap.remove(ball);

			int[] dir = angleToDir(aimAngle);
			WreckingBall.launchFromGround((Hero) target, ball, cell, charges, dir[0], dir[1]);
		}

		cleanupVisuals();
	}

	/** 指针方向 -> 箭头角度；指针离英雄太近时保持原方向 */
	private void updateAimFromPointer() {
		if (target == null || target.sprite == null) {
			return;
		}
		PointF screen = new PointF(Gdx.input.getX(), Gdx.input.getY());
		PointF world = Camera.main.screenToCamera((int) screen.x, (int) screen.y);
		PointF heroCenter = target.sprite.center();

		float dx = world.x - heroCenter.x;
		float dy = world.y - heroCenter.y;
		if (dx * dx + dy * dy < 100f) {
			return;
		}
		aimAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
	}

	/** 默认朝向地上的球 */
	private float defaultAimAngle() {
		if (target == null || target.sprite == null || cell < 0) {
			return 0f;
		}
		PointF heroCenter = target.sprite.center();
		PointF ballCenter = DungeonTilemap.tileCenterToWorld(cell);
		return (float) Math.toDegrees(Math.atan2(ballCenter.y - heroCenter.y, ballCenter.x - heroCenter.x));
	}

	/** 连续角度 -> 八方向 */
	private int[] angleToDir(float angle) {
		double rad = Math.toRadians(angle);
		int dx = (int) Math.round(Math.cos(rad));
		int dy = (int) Math.round(Math.sin(rad));
		if (dx == 0 && dy == 0) {
			dx = 1;
		}
		return new int[]{dx, dy};
	}

	private void updateVisuals() {
		updateChargeText();

		if (target == null || target.sprite == null) {
			return;
		}
		PointF center = target.sprite.center();

		if (arc != null) {
			arc.x = center.x;
			arc.y = center.y - 32;
			float scale = 0.25f + 0.75f * progress;
			arc.scale.set(scale, scale);
			arc.angle = aimAngle;
			arc.visible = progress > 0.02f;
		}

		if (arrow != null) {
			//箭头从英雄中心向外伸，长度随蓄力变化
			//原点在箭头根部，直接放在英雄中心
			arrow.x = center.x - 1f;
			arrow.y = center.y - 8f;
			arrow.angle = aimAngle;
			float s = 0.8f + 0.5f * progress;
			arrow.scale.set(s, s);
			arrow.visible = true;
		}
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
		if (arrow != null) {
			arrow.killAndErase();
			arrow.destroy();
			arrow = null;
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