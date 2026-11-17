package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Point;

/**
 * 摇杆回合制：正常回合/饥饿规则不变，摇杆只负责选择下一步方向。
 * 每次推动只走一格，必须等英雄行动结束才能继续。
 */
public class RealtimeController extends Group {

	private final Joystick moveStick;
	private final Joystick aimStick;
	private final Image aimArrow;
	private final RenderedTextBlock debug;

	private float moveX, moveY, aimX, aimY;
	private int activePointer = -1;
	private boolean activeLeft;
	private float pollStartX, pollStartY;
	private float stepCooldown;
	private float aimAngle;

	public RealtimeController() {
		moveStick = new Joystick(42, 0xFFFFFF);
		aimStick = new Joystick(42, 0xFFFFFF);
		aimArrow = arrow();
		aimArrow.camera = Camera.main;
		aimArrow.visible = false;
		add(moveStick);
		add(aimStick);
		add(aimArrow);

		debug = PixelScene.renderTextBlock(7);
		debug.camera = PixelScene.uiCamera;
		debug.hardlight(0xFFFF66);
		add(debug);
		layout();
	}

	private void layout() {
		float w = PixelScene.uiCamera.width;
		float h = PixelScene.uiCamera.height;
		moveStick.setBounds(8, h - 76, 24);
		aimStick.setBounds(w - 32, h - 76, 24);
	}

	@Override
	public void update() {
		super.update();
		if (Dungeon.hero == null || Dungeon.level == null) return;

		pollTouches();
		if (stepCooldown > 0) stepCooldown -= Game.elapsed;

		aimAngle = (float) Math.atan2(aimY, aimX);

		if (Dungeon.hero.sprite != null) {
			float am = (float) Math.sqrt(aimX * aimX + aimY * aimY);
			aimArrow.visible = am > 0.05f;
			if (aimArrow.visible) {
				aimArrow.angle = aimAngle * 180f / (float) Math.PI;
				aimArrow.setPos(Dungeon.hero.sprite.x + Dungeon.hero.sprite.width() / 2f
								+ (float) Math.cos(aimAngle) * 18f,
						Dungeon.hero.sprite.y + Dungeon.hero.sprite.height() / 2f
								+ (float) Math.sin(aimAngle) * 18f);
			}
		}

		if (debug != null) {
			debug.text(String.format(java.util.Locale.US,
					"touch %b side %s  L %.2f,%.2f  R %.2f,%.2f\npos %d  ready %b",
					Gdx.input.isTouched(), activePointer < 0 ? "-" : (activeLeft ? "L" : "R"),
					moveX, moveY, aimX, aimY, Dungeon.hero.pos, Dungeon.hero.ready));
			debug.setPos(8, 30);
		}

		if (stepCooldown > 0 || !Dungeon.hero.ready) return;

		float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
		if (len < 0.45f) return;

		int dx = Math.round(moveX);
		int dy = Math.round(moveY);
		if (dx == 0 && dy == 0) {
			if (Math.abs(moveX) > Math.abs(moveY)) dx = moveX > 0 ? 1 : -1;
			else dy = moveY > 0 ? 1 : -1;
		}
		int target = Dungeon.hero.pos + dx + dy * Dungeon.level.width();
		if (target < 0 || target >= Dungeon.level.length()) return;

		if (Dungeon.level.pit[target]) {
			if (!com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.jumpConfirmed) {
				com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.heroJump(Dungeon.hero);
			} else {
				com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.heroFall(target);
			}
			stepCooldown = 0.35f;
			return;
		}

		if (Dungeon.level.passable[target]
				&& com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(target) == null
				&& Dungeon.level.heaps.get(target) == null
				&& Dungeon.level.getTransition(target) == null
				&& Dungeon.level.map[target] != Terrain.DOOR) {
			Dungeon.hero.move(target, false);
			Dungeon.hero.spend(1f);
			Dungeon.observe();
			GameScene.updateFog();
			stepCooldown = 0.22f;
		} else if (Dungeon.hero.handle(target)) {
			stepCooldown = 0.22f;
		}
	}

	private void pollTouches() {
		if (activePointer >= 0) {
			if (!Gdx.input.isTouched(activePointer)) {
				resetStick();
			} else {
				updateStick(activePointer);
			}
			return;
		}
		for (int i = 0; i < 2; i++) {
			if (Gdx.input.isTouched(i)) {
				float tx = Gdx.input.getX(i);
				float ty = Gdx.input.getY(i);
				activePointer = i;
				Point lc = moveStick.centerScreen();
				Point rc = aimStick.centerScreen();
				float dl = (tx - lc.x) * (tx - lc.x) + (ty - lc.y) * (ty - lc.y);
				float dr = (tx - rc.x) * (tx - rc.x) + (ty - rc.y) * (ty - rc.y);
				float hit = Math.min(Game.width, Game.height) * 0.13f;
				if (dl > hit * hit && dr > hit * hit) {
					// 不在任何一个摇杆的触控范围内，不要把别的点击当摇杆
					return;
				}
				activeLeft = dl <= dr;
				pollStartX = activeLeft ? lc.x : rc.x;
				pollStartY = activeLeft ? lc.y : rc.y;
				return;
			}
		}
	}

	private void updateStick(int pointer) {
		setAxes(Gdx.input.getX(pointer) - pollStartX, Gdx.input.getY(pointer) - pollStartY, activeLeft);
	}

	private void resetStick() {
		activePointer = -1;
		moveX = moveY = aimX = aimY = 0;
		moveStick.setAxes(0, 0);
		aimStick.setAxes(0, 0);
	}

	private void setAxes(float dx, float dy, boolean left) {
		float max = Math.min(Game.width, Game.height) * 0.085f;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len > max) {
			dx = dx / len * max;
			dy = dy / len * max;
		}
		float nx = dx / max;
		float ny = dy / max;
		if (Math.abs(nx) < 0.05f) nx = 0;
		if (Math.abs(ny) < 0.05f) ny = 0;
		if (left) {
			moveX = nx;
			moveY = ny;
			moveStick.setAxes(nx, ny);
		} else {
			aimX = nx;
			aimY = ny;
			aimStick.setAxes(nx, ny);
		}
	}

	private Image arrow() {
		Pixmap p = new Pixmap(12, 12, Pixmap.Format.RGBA8888);
		p.setColor(com.badlogic.gdx.graphics.Color.WHITE);
		p.fillTriangle(0, 0, 0, 12, 12, 6);
		SmartTexture t = new SmartTexture(p, Texture.LINEAR, Texture.CLAMP, false);
		Image image = new Image(t);
		image.origin.set(6, 6);
		return image;
	}

	public float aimAngle() {
		return aimAngle;
	}
}
