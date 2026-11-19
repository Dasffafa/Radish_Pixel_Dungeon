package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

public class CriticalShock extends Image {
	private static final float MOVE_OUT_DONE = .1f;
	private static final float STATIC_DONE = .6f;
	private static final float FADE_OUT_DONE = 1f;

	private static final float MOVE_OFFSET = 8f;
	private static final float SCALE_OFFSET = .25f;
	private static final float INIT_ALPHA = .8f;

	private Char target;

	private int color;
	private float passed;
	private float dx, dy;
	private float strength;

	public CriticalShock(Char c) {
		target = c;

		flipHorizontal = c.sprite.flipHorizontal;
		flipVertical = c.sprite.flipVertical;
		copy(c.sprite);
		origin.set(c.sprite.width / 2f, c.sprite.height / 2f);

		color = c.sprite.blood();
		passed = 0;

		tint(color, .5f);
		alpha(INIT_ALPHA);
	}

	public CriticalShock set(float dir, float str) {
		dx = (float) Math.cos(dir);
		dy = (float) Math.sin(dir);
		strength = str;

		return this;
	}

	@Override
	public void update() {
		super.update();

		passed += Game.elapsed;

		if (passed < MOVE_OUT_DONE) {
			if (target == null || target.sprite == null) {
				killAndErase();
				return;
			}
			float pct = passed / MOVE_OUT_DONE;
			float ds = (MOVE_OFFSET * strength) * pct;
			x = target.sprite.x + ds * dx;
			y = target.sprite.y + ds * dy;
			scale.set(1f + (SCALE_OFFSET * strength) * pct);
			alpha(INIT_ALPHA * pct);
		} else if (passed < STATIC_DONE) {
		} else if (passed < FADE_OUT_DONE) {
			alpha((1f - (passed - STATIC_DONE) / (FADE_OUT_DONE - STATIC_DONE)) * INIT_ALPHA);
		} else {
			killAndErase();
		}
	}

	public static void show(Char ch, float dir, float power) {
		if (ch == null || ch.sprite == null) return;
		Group p = ch.sprite.parent;
		if (p == null || !ch.sprite.visible) return;

		CriticalShock cs = new CriticalShock(ch).set(dir, power);
		p.addToBack(cs);
	}
}
