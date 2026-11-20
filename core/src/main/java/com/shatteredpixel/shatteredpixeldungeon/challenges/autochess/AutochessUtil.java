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
}
