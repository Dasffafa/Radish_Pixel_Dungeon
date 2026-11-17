package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

/**
 * 回合制 + 格内自由移动：
 * 角色可以在当前格内任意位置连续移动；跨入新格时消耗一个回合。
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
	private float aimAngle;

	private float cellX, cellY;
	private boolean initialized;
	private boolean wasMoving;

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
		moveStick.setBounds(8, h - 82, 30);
		aimStick.setBounds(w - 38, h - 82, 30);
	}

	@Override
	public void update() {
		super.update();
		if (Dungeon.hero == null || Dungeon.level == null) return;

		if (!initialized) {
			initialized = true;
			cellX = Dungeon.hero.pos % Dungeon.level.width() + 0.5f;
			cellY = Dungeon.hero.pos / Dungeon.level.width() + 0.5f;
		}

		pollTouches();
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
					"touch %b side %s  L %.2f,%.2f  R %.2f,%.2f\ncell %.2f,%.2f  pos %d  ready %b",
					Gdx.input.isTouched(), activePointer < 0 ? "-" : (activeLeft ? "L" : "R"),
					moveX, moveY, aimX, aimY, cellX, cellY, Dungeon.hero.pos, Dungeon.hero.ready));
			debug.setPos(8, 30);
		}

		// 行动未结束时暂停移动，保持正常回合节奏
		if (!Dungeon.hero.ready) return;

		float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
		if (len < 0.08f) {
			stopMoving();
			return;
		}

		float speed = 5.2f;
		float dx = moveX * speed * Game.elapsed;
		float dy = moveY * speed * Game.elapsed;

		float nx = cellX + dx;
		float ny = cellY + dy;
		if (canStandPath(cellX, cellY, nx, ny)) {
			cellX = nx;
			cellY = ny;
		} else if (canStandPath(cellX, cellY, nx, cellY)) {
			cellX = nx;
		} else if (canStandPath(cellX, cellY, cellX, ny)) {
			cellY = ny;
		} else {
			stopMoving();
			return;
		}

		startMoving(dx, dy);

		if (Dungeon.hero.sprite != null) {
			float sx = cellX * DungeonTilemap.SIZE - Dungeon.hero.sprite.width() / 2f;
			float sy = (cellY + 0.5f) * DungeonTilemap.SIZE - Dungeon.hero.sprite.height()
					- DungeonTilemap.SIZE * 6f / 16f;
			Dungeon.hero.sprite.point(new PointF(PixelScene.align(Camera.main, sx), PixelScene.align(Camera.main, sy)));
			Camera.main.panFollow(Dungeon.hero.sprite, 12f);
		}

		int cell = (int) Math.floor(cellY) * Dungeon.level.width() + (int) Math.floor(cellX);
		if (cell != Dungeon.hero.pos && Dungeon.level.passable[cell]) {
			Dungeon.hero.pos = cell;
			Dungeon.observe();
			GameScene.updateFog();
			// 跨格才消耗回合；spendAndNext 才会真正推进行动队列
			Dungeon.hero.spendAndNext(1f);
		}
	}

	private void startMoving(float dx, float dy) {
		if (Dungeon.hero.sprite == null) return;
		if (!wasMoving) {
			wasMoving = true;
			Dungeon.hero.sprite.playRun();
		}
		if (Math.abs(dx) > 0.0001f) Dungeon.hero.sprite.flipHorizontal = dx < 0;
	}

	private void stopMoving() {
		if (wasMoving && Dungeon.hero.sprite != null) {
			wasMoving = false;
			Dungeon.hero.sprite.idle();
		}
	}

	private boolean canStandPath(float x0, float y0, float x1, float y1) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		float dist = (float) Math.sqrt(dx * dx + dy * dy);
		int steps = Math.max(1, (int) Math.ceil(dist / 0.12f));
		for (int s = 0; s <= steps; s++) {
			float t = s / (float) steps;
			if (!canStand(x0 + dx * t, y0 + dy * t)) return false;
		}
		return true;
	}

	private boolean canStand(float x, float y) {
		int cx = (int) Math.floor(x);
		int cy = (int) Math.floor(y);
		if (cx < 0 || cy < 0 || cx >= Dungeon.level.width() || cy >= Dungeon.level.height()) return false;
		int cell = cx + cy * Dungeon.level.width();
		return Dungeon.level.passable[cell] && !Dungeon.level.pit[cell];
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
				Point lc = moveStick.centerScreen();
				Point rc = aimStick.centerScreen();
				float dl = (tx - lc.x) * (tx - lc.x) + (ty - lc.y) * (ty - lc.y);
				float dr = (tx - rc.x) * (tx - rc.x) + (ty - rc.y) * (ty - rc.y);
				float hit = Math.min(Game.width, Game.height) * 0.16f;
				if (dl > hit * hit && dr > hit * hit) return;
				activePointer = i;
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
