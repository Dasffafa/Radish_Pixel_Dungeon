package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

/** 闪现残影：复制角色当前帧，染成暗紫色后淡出。 */
public class Afterimage extends Image {

	private static final float DURATION = 0.35f;

	private float life = DURATION;

	public Afterimage(CharSprite source) {
		super(source.texture);
		frame(source.frame());
		x = source.x;
		y = source.y;
		scale.set(source.scale.x, source.scale.y);
		origin.set(source.origin.x, source.origin.y);
		angle = source.angle;
		hardlight(0.5f, 0.25f, 0.75f);
		alpha(0.75f);
	}

	@Override
	public void update() {
		super.update();
		life -= Game.elapsed;
		if (life <= 0) {
			killAndErase();
		} else {
			alpha(0.75f * life / DURATION);
		}
	}
}
