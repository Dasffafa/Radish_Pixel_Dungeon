package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.utils.PointF;

/** 第一阶段：双摇杆、360°连续移动，不消耗回合。 */
public class RealtimeController extends Group {

	private final Joystick moveStick;
	private final Joystick aimStick;

	private float cellX, cellY;
	private int lastCell = -1;
	private float aimAngle;

	public RealtimeController() {
		moveStick = new Joystick(42, 0xFFFFFF);
		aimStick = new Joystick(42, 0xFFFFFF);
		add(moveStick);
		add(aimStick);
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

		if (lastCell != Dungeon.hero.pos) {
			lastCell = Dungeon.hero.pos;
			cellX = lastCell % Dungeon.level.width() + 0.5f;
			cellY = lastCell / Dungeon.level.width() + 0.5f;
		}

		float ax = moveStick.getXAxis();
		float ay = moveStick.getYAxis();
		float len = (float) Math.sqrt(ax * ax + ay * ay);
		if (len > 1f) {
			ax /= len;
			ay /= len;
		}

		float speed = 3.2f;
		float dx = ax * speed * Game.elapsed;
		float dy = ay * speed * Game.elapsed;

		if (dx != 0 || dy != 0) {
			cellX = tryMove(cellX + dx, cellY, cellX);
			cellY = tryMove(cellX, cellY + dy, cellY);
		}

		aimAngle = (float) Math.atan2(aimStick.getYAxis(), aimStick.getXAxis());
		if (Math.abs(aimStick.getXAxis()) + Math.abs(aimStick.getYAxis()) < 0.05f) {
			aimAngle = (float) Math.atan2(ay, ax);
		}

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
