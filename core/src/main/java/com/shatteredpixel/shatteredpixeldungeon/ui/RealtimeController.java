package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

/** 第一阶段：双摇杆、360°连续移动，不消耗回合。 */
public class RealtimeController extends Group implements Signal.Listener<PointerEvent> {

	private final Joystick moveStick;
	private final Joystick aimStick;
	private final Image aimArrow;
	private final RenderedTextBlock debug;

	private float cellX, cellY;
	private int lastCell = -1;
	private float aimAngle;

	private float moveX, moveY, aimX, aimY;
	private boolean initialized;
	private int activePointer = -1;
	private boolean activeLeft;
	private float pollStartX, pollStartY;

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
		PointerEvent.addPointerListener(this);
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

		if (!initialized) {
			initialized = true;
			lastCell = Dungeon.hero.pos;
			cellX = lastCell % Dungeon.level.width() + 0.5f;
			cellY = lastCell / Dungeon.level.width() + 0.5f;
		}

		pollTouches();

		float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
		float ax = moveX, ay = moveY;
		if (len > 1f) {
			ax /= len;
			ay /= len;
		}
		float speed = 3.2f;
		float dx = ax * speed * Game.elapsed;
		float dy = ay * speed * Game.elapsed;

		if (dx != 0 || dy != 0) {
			com.badlogic.gdx.Gdx.app.log("RTMove", "mx=" + moveX + " my=" + moveY
					+ " cx=" + cellX + " cy=" + cellY + " pos=" + Dungeon.hero.pos);
			float oldX = cellX;
			cellX = tryMove(cellX + dx, cellY, cellX);
			cellY = tryMove(cellX, cellY + dy, cellY);
			if (oldX != cellX || cellY != lastCell / Dungeon.level.width() + 0.5f) {
				// moved
			}
		}

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
			float sx = cellX * DungeonTilemap.SIZE - Dungeon.hero.sprite.width() / 2f;
			float sy = (cellY + 0.5f) * DungeonTilemap.SIZE - Dungeon.hero.sprite.height()
					- DungeonTilemap.SIZE * 6f / 16f;
			Dungeon.hero.sprite.point(new PointF(PixelScene.align(Camera.main, sx), PixelScene.align(Camera.main, sy)));
			if (Game.scene() instanceof GameScene) {
				Camera.main.panFollow(Dungeon.hero.sprite, 12f);
			}
		}

		if (debug != null) {
			debug.text(String.format(java.util.Locale.US,
					"touch %b side %s  L %.2f,%.2f  R %.2f,%.2f\ncell %.2f,%.2f  pos %d",
					Gdx.input.isTouched(), activePointer < 0 ? "-" : (activeLeft ? "L" : "R"),
					moveX, moveY, aimX, aimY, cellX, cellY, Dungeon.hero.pos));
			debug.setPos(8, 30);
		}

		int cell = (int) Math.floor(cellY) * Dungeon.level.width() + (int) Math.floor(cellX);
		if (cell != Dungeon.hero.pos && Dungeon.level.passable[cell]) {
			Dungeon.hero.pos = cell;
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

	@Override
	public boolean onSignal(PointerEvent event) {
		return false;
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
				activePointer = i;
				float tx = Gdx.input.getX(i);
				float ty = Gdx.input.getY(i);
				com.watabou.utils.Point lc = moveStick.centerScreen();
				com.watabou.utils.Point rc = aimStick.centerScreen();
				float dl = (tx - lc.x) * (tx - lc.x) + (ty - lc.y) * (ty - lc.y);
				float dr = (tx - rc.x) * (tx - rc.x) + (ty - rc.y) * (ty - rc.y);
				activeLeft = dl <= dr;
				pollStartX = tx;
				pollStartY = ty;
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
		float max = Math.min(Game.width, Game.height) * 0.18f;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len > max) {
			dx = dx / len * max;
			dy = dy / len * max;
		}
		float nx = dx / max;
		float ny = dy / max;
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

	private float tryMove(float newX, float newY, float fallback) {
		int x = (int) Math.floor(newX);
		int y = (int) Math.floor(newY);
		if (x < 0 || y < 0 || x >= Dungeon.level.width() || y >= Dungeon.level.height()) return fallback;
		int cell = x + y * Dungeon.level.width();
		return Dungeon.level.passable[cell] ? newX : fallback;
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
