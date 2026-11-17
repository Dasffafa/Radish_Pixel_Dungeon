package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

/**
 * 单摇杆格内自由移动；跨格消耗回合。
 */
public class RealtimeController extends Group {

	private final Joystick moveStick;
	private final RenderedTextBlock debug;

	private float moveX, moveY;
	private int activePointer = -1;
	private float pollStartX, pollStartY;

	private float cellX, cellY;
	private boolean initialized;
	private boolean wasMoving;

	public RealtimeController() {
		moveStick = new Joystick(42, 0xFFFFFF);
		add(moveStick);

		debug = PixelScene.renderTextBlock(7);
		debug.camera = PixelScene.uiCamera;
		debug.hardlight(0xFFFF66);
		add(debug);
		layout();
	}

	private void layout() {
		float w = PixelScene.uiCamera.width;
		float h = PixelScene.uiCamera.height;
		moveStick.setBounds(w / 2f - 15, h - 82, 30);
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

		if (debug != null) {
			debug.text(String.format(java.util.Locale.US,
					"touch %b  L %.2f,%.2f\ncell %.2f,%.2f  pos %d  ready %b",
					Gdx.input.isTouched(), moveX, moveY,
					cellX, cellY, Dungeon.hero.pos, Dungeon.hero.ready));
			debug.setPos(8, 30);
		}

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

		int door = doorInPath(cellX, cellY, nx, ny);
		if (door != -1) {
			// 撞到门时交给原版交互：开门/解锁，不直接穿过去
			Dungeon.hero.handle(door);
			stopMoving();
			return;
		}

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
			Dungeon.hero.move(cell, true);
			Dungeon.observe();
			GameScene.updateFog();
			Dungeon.hero.spendAndNext(1f);
		}
	}

	private void startMoving(float dx, float dy) {
		if (Dungeon.hero.sprite == null) return;
		wasMoving = true;
		// 游戏每回合可能把英雄切回 idle，这里持续确保处于 run 状态
		Dungeon.hero.sprite.playRun();
		if (Math.abs(dx) > 0.0001f) Dungeon.hero.sprite.flipHorizontal = dx < 0;
	}

	private void stopMoving() {
		if (wasMoving && Dungeon.hero.sprite != null) {
			wasMoving = false;
			Dungeon.hero.sprite.idle();
		}
	}

	private int doorInPath(float x0, float y0, float x1, float y1) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		float dist = (float) Math.sqrt(dx * dx + dy * dy);
		int steps = Math.max(1, (int) Math.ceil(dist / 0.10f));
		for (int s = 0; s <= steps; s++) {
			float t = s / (float) steps;
			int cx = (int) Math.floor(x0 + dx * t);
			int cy = (int) Math.floor(y0 + dy * t);
			if (cx < 0 || cy < 0 || cx >= Dungeon.level.width() || cy >= Dungeon.level.height()) continue;
			int cell = cx + cy * Dungeon.level.width();
			if (Dungeon.level.map[cell] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR
					|| Dungeon.level.map[cell] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_DOOR
					|| Dungeon.level.map[cell] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CRYSTAL_DOOR) {
				return cell;
			}
		}
		return -1;
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
		float r = 0.30f;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				int cx = (int) Math.floor(x + i * r);
				int cy = (int) Math.floor(y + j * r);
				if (cx < 0 || cy < 0 || cx >= Dungeon.level.width() || cy >= Dungeon.level.height()) return false;
				int cell = cx + cy * Dungeon.level.width();
				if (!Dungeon.level.passable[cell] || Dungeon.level.pit[cell]) return false;
			}
		}
		int cx = (int) Math.floor(x);
		int cy = (int) Math.floor(y);
		int cell = cx + cy * Dungeon.level.width();
		return Dungeon.level.passable[cell] && !Dungeon.level.pit[cell];
	}

	private void pollTouches() {
		if (activePointer >= 0) {
			if (!Gdx.input.isTouched(activePointer)) {
				resetStick();
			} else {
				setAxes(Gdx.input.getX(activePointer) - pollStartX,
						Gdx.input.getY(activePointer) - pollStartY);
			}
			return;
		}
		for (int i = 0; i < 2; i++) {
			if (Gdx.input.isTouched(i)) {
				float tx = Gdx.input.getX(i);
				float ty = Gdx.input.getY(i);
				Point center = moveStick.centerScreen();
				float dx = tx - center.x;
				float dy = ty - center.y;
				float hit = Math.min(Game.width, Game.height) * 0.16f;
				if (dx * dx + dy * dy > hit * hit) return;
				activePointer = i;
				pollStartX = center.x;
				pollStartY = center.y;
				return;
			}
		}
	}

	private void resetStick() {
		activePointer = -1;
		moveX = moveY = 0;
		moveStick.setAxes(0, 0);
	}

	private void setAxes(float dx, float dy) {
		float max = Math.min(Game.width, Game.height) * 0.085f;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len > max) {
			dx = dx / len * max;
			dy = dy / len * max;
		}
		moveX = dx / max;
		moveY = dy / max;
		if (Math.abs(moveX) < 0.05f) moveX = 0;
		if (Math.abs(moveY) < 0.05f) moveY = 0;
		moveStick.setAxes(moveX, moveY);
	}
}
