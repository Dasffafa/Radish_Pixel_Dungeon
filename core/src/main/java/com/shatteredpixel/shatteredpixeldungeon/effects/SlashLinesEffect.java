package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/** 斩击特效：直接在目标身上划出数道红色斩线，不再从施术者飞出。 */
public class SlashLinesEffect extends Group {

	private static final float DURATION = 0.25f;

	private final java.util.ArrayList<Image> lines = new java.util.ArrayList<>();
	private float life = DURATION;

	public static void show(Char target) {
		show(target, Random.Int(2, 4));
	}

	public static void show(Char target, int count) {
		if (target == null || target.sprite == null || target.sprite.parent == null) {
			return;
		}
		SlashLinesEffect effect = new SlashLinesEffect();
		PointF center = target.sprite.center();
		effect.build(count, center);
		target.sprite.parent.addToFront(effect);
	}

	private void build(int count, PointF center) {
		for (int i = 0; i < count; i++) {
			Image line = new Image(Assets.Effects.SLASH_LINE);
			float length = Random.Float(16f, 26f);
			line.scale.set(length / line.width, Random.Float(0.8f, 1.3f));
			line.angle = Random.Float(0, 180);
			line.x = center.x - length / 2f + Random.Float(-5f, 5f);
			line.y = center.y - 3f + Random.Float(-8f, 8f);
			add(line);
			lines.add(line);
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
		float a = Math.min(1f, life / (DURATION * 0.6f));
		for (Image line : lines) {
			line.alpha(a);
		}
	}
}