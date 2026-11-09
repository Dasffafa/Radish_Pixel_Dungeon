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

	private PointF start;
	private PointF end;
	private float life = 0f;
	private float duration;

	public SukunaSlashEffect(PointF from, PointF to, float angleDeg,
							 float scaleFactor, float duration,
							 float r, float g, float b) {
		super(Assets.Effects.SWORD_SLASH);

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

		switch (Random.Int(4)) {
			case 0:
				//一道细长血红斩击
				place(parent, from, to, angle, 0.6f, 0.14f, 2.0f, 0.35f, 0.35f);
				break;
			case 1:
				//缓慢的暗红重斩
				place(parent, from, to, angle + Random.Float(-8f, 8f), 0.95f, 0.22f, 1.6f, 0.12f, 0.16f);
				break;
			case 2:
				//两道平行斩痕
				place(parent, offset(from, angle, 4f), offset(to, angle, 4f), angle + 6f, 0.55f, 0.13f, 2.1f, 0.4f, 0.36f);
				place(parent, offset(from, angle, -4f), offset(to, angle, -4f), angle - 6f, 0.55f, 0.17f, 1.7f, 0.2f, 0.24f);
				break;
			default:
				//三道爪痕
				for (int i = -1; i <= 1; i++) {
					float off = i * 5f;
					place(parent, offset(from, angle, off), offset(to, angle, off),
							angle + i * 14f, 0.5f, 0.15f + Math.abs(i) * 0.03f,
							2.2f, 0.45f - Math.abs(i) * 0.1f, 0.4f - Math.abs(i) * 0.08f);
				}
				break;
		}

		//命中火花
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