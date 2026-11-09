/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Group;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

/**
 * 斩舰刀的白色刀光 / 空间撕裂（素材从 32x32 GIF 抠出，青底已转透明）。
 *
 * <ul>
 *   <li>{@link #showOn(Char, boolean)} 第二参 false：只播「刀光」7 帧（每次攻击）。</li>
 *   <li>true：播整段 8 帧（1 空拍 + 7 帧空间撕裂），用于斩杀。</li>
 * </ul>
 *
 * <p>素材本身已经是左上→右下，和刀口方向一致，所以<b>不做任何旋转</b>：同一把刀，永远同一个方向。
 * 精灵按防守者中心对齐，播完自毁。</p>
 */
public class KillBoatSlashEffect extends MovieClip {

	/** 游戏内是否启用（测试工具可切换）。 */
	public static boolean enabled = true;

	/** 帧尺寸与帧率，跟素材保持一致。 */
	private static final int FRAME = 32;
	private static final int FPS = 10;

	/** 缩放：x 拉长保长度，y 压扁让刀光变细（也把素材约 45° 的斜度压到接近刀口的 30°）。 */
	private static final float SCALE_X = 1.9f;
	private static final float SCALE_Y = 1.15f;

	/** 片段时长（秒）—— 斩杀演出要等这一段放完再切。 */
	public static final float SLASH_TIME = 7 / (float) FPS;     // 0.7s
	public static final float TEAR_TIME = 8 / (float) FPS;      // 0.8s
	public static final float KILL_CLIP_TIME = SLASH_TIME + TEAR_TIME;

	private static TextureFilm slashFilm;
	private static TextureFilm tearFilm;
	private static Animation slashAnim;
	private static Animation tearAnim;

	public KillBoatSlashEffect() {
		this(false);
	}

	private KillBoatSlashEffect(boolean tear) {
		super(tear ? Assets.Effects.KILL_BOAT_TEAR : Assets.Effects.KILL_BOAT_SLASH);

		if (tear) {
			if (tearFilm == null) {
				tearFilm = new TextureFilm(texture, FRAME, FRAME);
				tearAnim = new Animation(FPS, false);
				tearAnim.frames(tearFilm, 0, 1, 2, 3, 4, 5, 6, 7);
			}
			play(tearAnim);
		} else {
			if (slashFilm == null) {
				slashFilm = new TextureFilm(texture, FRAME, FRAME);
				slashAnim = new Animation(FPS, false);
				slashAnim.frames(slashFilm, 0, 1, 2, 3, 4, 5, 6);
			}
			play(slashAnim);
		}

		scale.set(SCALE_X, SCALE_Y);
		origin.set(width / 2f, height / 2f);
		listener = a -> killAndErase();
	}

	/** 在 target 身上播放；tear=true 时播「空拍 + 空间撕裂」那一段。 */
	public static void showOn(Char target, boolean tear) {
		if (!enabled || target == null || target.sprite == null) {
			return;
		}
		CharSprite s = target.sprite;
		if (s.parent == null) {
			return;
		}
		PointF c = s.destinationCenter();
		preview(s.parent, c, tear);
	}

	public static void preview(Group parent, PointF center, boolean tear) {
		if (parent == null || center == null) {
			return;
		}
		KillBoatSlashEffect e = new KillBoatSlashEffect(tear);
		e.scale.set(SCALE_X, SCALE_Y);
		e.origin.set(e.width / 2f, e.height / 2f);
		e.x = center.x - e.width / 2f;
		e.y = center.y - e.height / 2f;
		parent.add(e);
	}
}
