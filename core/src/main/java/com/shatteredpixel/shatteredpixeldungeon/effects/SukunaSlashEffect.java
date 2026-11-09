package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * 宿傩利爪的飞行斩击：从宿傩飞出，掠过目标位置后消散。
 */
public class SukunaSlashEffect extends MovieClip {

	private static TextureFilm film;
	private static Animation anim;
	private static Animation loopAnim;

	private PointF start;
	private PointF end;
	private float life = 0f;
	private float duration;

	public SukunaSlashEffect(PointF from, PointF to, float angleDeg,
							 float scaleFactor, float duration,
							 float r, float g, float b) {
		super(Assets.Effects.SUKUNA_SLASH);

		if (film == null) {
			film = new TextureFilm(texture, 64, 47);
		}
		if (anim == null) {
			anim = new Animation(30, false);
			anim.frames(film, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		}
		play(anim);

		this.start = from;
		this.end = to;
		this.duration = duration;

		scale.set(scaleFactor);
		origin.set(width / 2f, height / 2f);
		x = from.x - width / 2f;
		y = from.y - height / 2f;
		this.angle = angleDeg;
		tint(r, g, b, 1f);
	}

	@Override
	public void update() {
		super.update();
		life += Game.elapsed;
		float t = Math.min(1f, life / duration);
		//缓出，越接近目标越快
		float ease = 1f - (1f - t) * (1f - t);
		x = (start.x + (end.x - start.x) * ease) - width / 2f;
		y = (start.y + (end.y - start.y) * ease) - height / 2f;
		if (t >= 1f) {
			killAndErase();
		}
	}

	/** 命中时随机播放一种飞行斩击 */
	//空间斩：横穿整张地图的巨型斩击，可穿墙
	void useLoopAnim() {
		if (loopAnim == null) {
			loopAnim = new Animation(16, true);
			loopAnim.frames(film, 2, 3, 4, 5, 6, 7);
		}
		play(loopAnim);
	}

	public static void showGiant(Char attacker, float dx, float dy, int cells) {
		if (!(attacker.sprite instanceof CharSprite) || attacker.sprite.parent == null) {
			return;
		}
		PointF from = ((CharSprite) attacker.sprite).destinationCenter();
		PointF to = new PointF(from.x + dx * cells * 16f, from.y + dy * cells * 16f);
		float angle = (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
		SukunaSlashEffect e = new SukunaSlashEffect(from, to, angle, 6.0f, 2.0f, 2.0f, 0.25f, 0.3f);
		attacker.sprite.parent.add(e);
	}

	public static void show(Char attacker, Char defender) {
		if (attacker == null || defender == null
				|| !(attacker.sprite instanceof CharSprite)
				|| !(defender.sprite instanceof CharSprite)
				|| defender.sprite.parent == null) {
			return;
		}

		PointF from = ((CharSprite) attacker.sprite).destinationCenter();
		PointF to = ((CharSprite) defender.sprite).destinationCenter();
		com.watabou.noosa.Group parent = defender.sprite.parent;
		float angle = (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));

		//攻击距离越远，斩击越大
		int dist = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level != null
				? com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.distance(attacker.pos, defender.pos) : 1;
		float mul = Math.min(2.4f, 1f + 0.07f * Math.max(0, dist - 1));
		//距离越远，飞行时间略长，避免瞬移感
		float durMul = Math.min(1.8f, 0.75f + 0.25f * mul);

		switch (Random.Int(4)) {
			case 0:
				//细长斩，方向微偏
				place(parent, from, to, angle + Random.Float(-20f, 20f),
						(0.55f + Random.Float(-0.08f, 0.12f)) * mul, 0.14f * durMul, 2.0f, 0.35f, 0.35f);
				break;
			case 1:
				//重斩，角度更开
				place(parent, from, to, angle + Random.Float(-35f, 35f),
						(0.8f + Random.Float(-0.1f, 0.15f)) * mul, 0.22f * durMul, 1.6f, 0.12f, 0.16f);
				break;
			case 2:
				//交叉双斩
				float crossAngle = Random.Float(30f, 50f);
				place(parent, from, to, angle + crossAngle, 0.5f * mul, 0.13f * durMul, 2.1f, 0.4f, 0.36f);
				place(parent, from, to, angle - crossAngle, 0.5f * mul, 0.16f * durMul, 1.7f, 0.2f, 0.24f);
				break;
			default:
				//三道爪痕
				float spread = Random.Float(10f, 18f);
				for (int i = -1; i <= 1; i++) {
					float off = i * Random.Float(4f, 7f);
					place(parent, offset(from, angle, off), offset(to, angle, off),
							angle + i * spread + Random.Float(-6f, 6f),
							(0.45f + Random.Float(-0.05f, 0.08f)) * mul, (0.14f + Math.abs(i) * 0.03f) * durMul,
							2.2f, 0.45f - Math.abs(i) * 0.1f, 0.4f - Math.abs(i) * 0.08f);
				}
				break;
		}

		CellEmitter.get(defender.pos).burst(Speck.factory(Speck.STAR), 4);
	}

	private static PointF offset(PointF p, float angleDeg, float dist) {
		double rad = Math.toRadians(angleDeg + 90f);
		return new PointF(p.x + (float) Math.cos(rad) * dist,
				p.y + (float) Math.sin(rad) * dist);
	}

	private static void place(com.watabou.noosa.Group parent, PointF from, PointF to,
							  float angleDeg, float scaleFactor, float duration,
							  float r, float g, float b) {
		SukunaSlashEffect e = new SukunaSlashEffect(from, to, angleDeg, scaleFactor, duration, r, g, b);
		parent.add(e);
	}
}