/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 卷轴效果：区域揭示
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 「区域揭示」效果：每 20 回合把英雄周围一片区域标进地图（含暗门发现）。
 *
 * <p>原版 {@link RevealedArea} 只在 pos/depth/branchId 都对得上时才揭示 3x3，
 * 直接挂一个默认值的 buff 什么都不会发生，所以这里单独实现一个周期版本。</p>
 */
public class DiyReveal extends Buff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private static final float INTERVAL = 20f;
	private static final int RADIUS = 7;

	/** 触发次数（测试/诊断用）。 */
	public transient int activations = 0;

	@Override
	public boolean act() {
		activations++;
		if (Dungeon.hero != null && Dungeon.level != null) {
			revealAround(Dungeon.hero.pos, RADIUS);
		}
		spend(INTERVAL);
		return true;
	}

	/** 标记 radius 内可探索的地图格，顺便把暗门发现出来。 */
	private static void revealAround(int center, int radius) {
		Level level = Dungeon.level;
		if (level == null) return;

		int width = level.width();
		int cx = center % width;
		int cy = center / width;

		boolean[] mapped = level.mapped;
		boolean[] discoverable = level.discoverable;
		int[] map = level.map;

		int revealed = 0;
		for (int i = 0; i < level.length(); i++) {
			if (!discoverable[i]) continue;
			int x = i % width;
			int y = i / width;
			if (Math.abs(x - cx) + Math.abs(y - cy) > radius) continue;

			boolean changed = false;
			if (!mapped[i]) {
				mapped[i] = true;
				changed = true;
			}
			if ((Terrain.flags[map[i]] & Terrain.SECRET) != 0) {
				level.discover(i);
				if (Dungeon.level.heroFOV[i]) {
					GameScene.discoverTile(i, map[i]);
				}
				changed = true;
			}
			if (changed) revealed++;
		}

		if (revealed > 0) {
			GameScene.updateFog();
			GLog.i("区域揭示：揭开 %d 格", revealed);
		}
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}
}
