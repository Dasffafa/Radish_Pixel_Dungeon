package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
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

	private float cellX, cellY;
	private int lastCell = -1;
	private float aimAngle;

	private int leftId = -1, rightId = -1;
	private float leftStartX, leftStartY, rightStartX, rightStartY;
	private float moveX, moveY, aimX, aimY;

	public RealtimeController() {
		moveStick = new Joystick(42, 0xFFFFFF);
		aimStick = new Joystick(42, 0xFFFFFF);
		add(moveStick);
		add(aimStick);
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

		if (lastCell != Dungeon.hero.pos) {
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
			float oldX = cellX;
			cellX = tryMove(cellX + dx, cellY, cellX);
			cellY = tryMove(cellX, cellY + dy, cellY);
			if (oldX != cellX || cellY != lastCell / Dungeon.level.width() + 0.5f) {
				// moved
			}
		}

		aimAngle = (float) Math.atan2(aimY, aimX);

		if (Dungeon.hero.sprite != null) {
			float sx = cellX * DungeonTilemap.SIZE - Dungeon.hero.sprite.width() / 2f;
			float sy = (cellY + 0.5f) * DungeonTilemap.SIZE - Dungeon.hero.sprite.height()
					- DungeonTilemap.SIZE * 6f / 16f;
			Dungeon.hero.sprite.point(new PointF(PixelScene.align(Camera.main, sx), PixelScene.align(Camera.main, sy)));
			if (Game.scene() instanceof GameScene) {
				Camera.main.panFollow(Dungeon.hero.sprite, 6f);
			}
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
		if (event == null) return false;
		float screenY = event.current.y;
		if (screenY < Game.height * 0.55f) {
			// 上半屏留给正常 UI/点击
			return false;
		}

		if (event.type == PointerEvent.Type.DOWN) {
			if (event.current.x < Game.width / 2f) {
				if (leftId == -1) {
					leftId = event.id;
					leftStartX = event.current.x;
					leftStartY = event.current.y;
					moveX = moveY = 0;
					return true;
				}
			} else {
				if (rightId == -1) {
					rightId = event.id;
					rightStartX = event.current.x;
					rightStartY = event.current.y;
					aimX = aimY = 0;
					return true;
				}
			}
		} else if (event.type == PointerEvent.Type.HOVER) {
			if (event.id == leftId) {
				setAxes(event.current.x - leftStartX, event.current.y - leftStartY, true);
				return true;
			} else if (event.id == rightId) {
				setAxes(event.current.x - rightStartX, event.current.y - rightStartY, false);
				return true;
			}
		} else if (event.type == PointerEvent.Type.UP) {
			if (event.id == leftId) {
				leftId = -1;
				moveX = moveY = 0;
				moveStick.setAxes(0, 0);
				return true;
			} else if (event.id == rightId) {
				rightId = -1;
				aimX = aimY = 0;
				aimStick.setAxes(0, 0);
				return true;
			}
		}
		return false;
	}


	private boolean leftDown, rightDown;
	private float leftStartX2, leftStartY2, rightStartX2, rightStartY2;

	private void pollTouches() {
		boolean any = false;
		if (Gdx.input.isTouched(0)) {
			any = true;
			handleTouch(0);
		}
		if (Gdx.input.isTouched(1)) {
			any = true;
			handleTouch(1);
		}
		if (!any) {
			leftDown = rightDown = false;
			moveX = moveY = aimX = aimY = 0;
			moveStick.setAxes(0, 0);
			aimStick.setAxes(0, 0);
		}
	}

	private void handleTouch(int index) {
		float x = Gdx.input.getX(index);
		float y = Gdx.input.getY(index);
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		if (y < h * 0.55f) return;
		boolean left = x < w / 2f;
		if (left) {
			if (!leftDown) {
				leftDown = true;
				leftStartX2 = x;
				leftStartY2 = y;
				return;
			}
			setAxes(x - leftStartX2, y - leftStartY2, true);
		} else {
			if (!rightDown) {
				rightDown = true;
				rightStartX2 = x;
				rightStartY2 = y;
				return;
			}
			setAxes(x - rightStartX2, y - rightStartY2, false);
		}
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
		float radius = 0.28f;
		int minX = (int) Math.floor(newX - radius);
		int maxX = (int) Math.floor(newX + radius);
		int minY = (int) Math.floor(newY - radius);
		int maxY = (int) Math.floor(newY + radius);
		int width = Dungeon.level.width();
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				if (x < 0 || y < 0 || x >= width || y >= Dungeon.level.height()) return fallback;
				int cell = x + y * width;
				if (!Dungeon.level.passable[cell]) return fallback;
			}
		}
		return newX;
	}

	public float aimAngle() {
		return aimAngle;
	}
}
