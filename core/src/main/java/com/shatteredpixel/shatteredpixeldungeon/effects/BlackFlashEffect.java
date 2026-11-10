package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/** 黑闪：从目标身上向四周爆开的黑色闪电。 */
public class BlackFlashEffect extends Group {

	private static final float DURATION = 0.4f;

	private final java.util.ArrayList<Image> bolts = new java.util.ArrayList<>();
	private float life = DURATION;

	public static void show(Char target) {
		if (target == null || target.sprite == null || target.sprite.parent == null) {
			return;
		}
		BlackFlashEffect effect = new BlackFlashEffect();
		effect.build(target.sprite.center());
		target.sprite.parent.addToFront(effect);

		target.sprite.flash();
		Camera.main.shake(3f, 0.15f);
		CellEmitter.center(target.pos).burst(Speck.factory(Speck.SMOKE), 10);
	}

	private void build(PointF center) {
		int count = Random.Int(6, 10);
		for (int i = 0; i < count; i++) {
			float angle = Random.Float(0, 360);
			float x = center.x;
			float y = center.y;

			int segments = Random.Int(2, 4);
			for (int s = 0; s < segments; s++) {
				float len = Random.Float(10f, 20f);
				angle += Random.Float(-35f, 35f);

				Image bolt = new Image(Assets.Effects.BLACK_BOLT);
				float sx = len / bolt.width;
				bolt.origin.set(bolt.width / 2f, bolt.height / 2f);
				bolt.scale.set(sx, Random.Float(0.7f, 1.2f));
				bolt.angle = angle;
				bolt.x = x - bolt.width / 2f;
				bolt.y = y - bolt.height / 2f;
				add(bolt);
				bolts.add(bolt);

				double rad = Math.toRadians(angle);
				x += (float) (Math.cos(rad) * len);
				y += (float) (Math.sin(rad) * len);
			}
		}
	}

	@Override
	public void update() {
		super.update();
		life -= Game.elapsed;
		if (life <= 0) {
			killAndErase();
			return;
		}
		float a = Math.min(1f, life / (DURATION * 0.5f));
		for (Image bolt : bolts) {
			bolt.alpha(a);
		}
	}
}
