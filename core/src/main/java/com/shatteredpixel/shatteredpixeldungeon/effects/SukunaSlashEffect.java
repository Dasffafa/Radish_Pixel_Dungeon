package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Group;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * 宿傩利爪的斩击特效：每次攻击随机挑选一种红色斩击表现。
 */
public class SukunaSlashEffect extends SwordSlashEffect {

	public SukunaSlashEffect(float r, float g, float b, float scaleFactor) {
		super();
		tint(r, g, b, 1f);
		scale.set(scaleFactor);
	}

	/** 命中时随机播放一种斩击 */
	public static void show(Char attacker, Char defender) {
		if (attacker == null || defender == null
				|| !(attacker.sprite instanceof CharSprite)
				|| !(defender.sprite instanceof CharSprite)
				|| defender.sprite.parent == null) {
			return;
		}

		PointF from = ((CharSprite) attacker.sprite).destinationCenter();
		PointF to = ((CharSprite) defender.sprite).destinationCenter();
		float angle = (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
		Group parent = defender.sprite.parent;

		switch (Random.Int(4)) {
			case 0:
				// 单道血红斩击
				place(parent, to, 0.65f, angle, 2.0f, 0.35f, 0.35f);
				break;
			case 1:
				// 放大的暗红重斩
				place(parent, to, 0.95f, angle + Random.Float(-12f, 12f), 1.7f, 0.15f, 0.2f);
				break;
			case 2:
				// 交叉双斩
				place(parent, to, 0.7f, angle + 32f, 2.0f, 0.3f, 0.3f);
				place(parent, to, 0.7f, angle - 32f, 1.8f, 0.2f, 0.25f);
				break;
			default:
				// 三道爪痕
				place(parent, to, 0.6f, angle + 40f, 2.0f, 0.5f, 0.45f);
				place(parent, to, 0.7f, angle, 2.2f, 0.3f, 0.3f);
				place(parent, to, 0.6f, angle - 40f, 2.0f, 0.5f, 0.45f);
				break;
		}
	}

	private static void place(Group parent, PointF center, float scaleFactor,
							  float angleDeg, float r, float g, float b) {
		SukunaSlashEffect e = new SukunaSlashEffect(r, g, b, scaleFactor);
		e.origin.set(e.width / 2f, e.height / 2f);
		e.x = center.x - e.width / 2f;
		e.y = center.y - e.height / 2f;
		e.angle = angleDeg;
		parent.add(e);
	}
}