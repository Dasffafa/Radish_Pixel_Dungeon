package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Reflection;

/**
 * 自走棋用的棋子头像：临时造一只怪，拿它的精灵当图标。
 *
 * <p>和 {@code WndInfoMob} 的做法一样——怪物精灵本身就是贴着整张帧图的 Movie，
 * 直接塞进 UI 里就会画当前帧，不需要额外切图。</p>
 */
public class AutochessIcons {

	/** 按类名造一个头像精灵；类名不认识或造不出来返回 null。 */
	public static CharSprite spriteFor(String className) {
		AutochessPool.Def def = AutochessPool.ofName(className);
		if (def == null) {
			return null;
		}
		return spriteFor(def);
	}

	public static CharSprite spriteFor(AutochessPool.Def def) {
		if (def == null) {
			return null;
		}
		Mob mob;
		try {
			mob = Reflection.newInstance(def.cls);
		} catch (Exception e) {
			return null;
		}
		if (mob == null) {
			return null;
		}
		CharSprite sprite;
		try {
			sprite = mob.sprite();
		} catch (Exception e) {
			return null;
		}
		if (sprite == null) {
			return null;
		}
		return sprite;
	}

	/** 把精灵压到图标大小并摆到指定位置。 */
	public static void place(CharSprite sprite, float x, float y, float size) {
		if (sprite == null) {
			return;
		}
		float scale = size / Math.max(sprite.width(), sprite.height());
		sprite.scale.set(scale);
		sprite.x = x + (size - sprite.width() * scale) / 2f;
		sprite.y = y + (size - sprite.height() * scale) / 2f;
	}
}
