package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

/**
 * 自走棋里的安全判定工具。
 *
 * <p>部分怪（比如 ClusteredSkeleton）的 isAlive() 有副作用，会往 sprite.parent 上挂特效，
 * 棋子精灵已经被移除时直接 NPE。所有自走棋代码统一走这里。</p>
 */
public class AutochessUtil {

	public static boolean alive(Char c) {
		if (c == null || c.sprite == null) return false;
		try {
			return c.isAlive();
		} catch (Throwable t) {
			return false;
		}
	}

	/** 自走棋回合：找最近的敌人，贴身就打，不贴身就朝他走一步。 */
	public static boolean combatStep(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob self) {
		if (self == null || com.shatteredpixel.shatteredpixeldungeon.Dungeon.level == null) return false;
		com.shatteredpixel.shatteredpixeldungeon.levels.Level level = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;

		com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob foe = null;
		int best = Integer.MAX_VALUE;
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m
				: level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])) {
			if (m == self || !alive(m) || m.alignment == self.alignment) continue;
			int d = level.distance(self.pos, m.pos);
			if (d < best) { best = d; foe = m; }
		}
		if (foe == null) return false;

		if (best <= 1) {
			self.attack(foe);
			return true;
		}

		int w = level.width();
		int sx = self.pos % w, sy = self.pos / w;
		int fx = foe.pos % w, fy = foe.pos / w;
		int dx = Integer.signum(fx - sx);
		int dy = Integer.signum(fy - sy);

		// 一步一步挪（直接改 pos，不走 Char.move，避开边界崩溃）
		int[][] tries = new int[][]{{dx, dy}, {dx, 0}, {0, dy}};
		for (int[] t : tries) {
			int nx = sx + t[0];
			int ny = sy + t[1];
			if (nx <= 0 || ny <= 0 || nx >= level.width() - 1 || ny >= level.height() - 1) continue;
			int np = ny * w + nx;
			if (!level.passable[np]) continue;
			if (com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(np) != null) continue;
			self.pos = np;
			if (self.sprite != null) self.sprite.place(np);
			return true;
		}
		return false;
	}
}
