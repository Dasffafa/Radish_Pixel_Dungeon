package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

/**
 * 回合切换的镜头轻推：新回合开始时镜头微微压近再回位。
 *
 * <p>不改 zoom（会跟 GameScene 打架），只在 0.25 秒内把镜头往棋盘中心
 * 轻推一点点再回来，配合 HUD 的状态条动画，形成「转场」的感觉。</p>
 */
public class AutochessRoundTransition extends Component {

	private static final float PUSH_TIME = 0.12f;
	private static final float BACK_TIME = 0.22f;
	private static final float PUSH_DIST = 10f;

	private int lastRound = -1;
	private float timer = -1f;
	private boolean returning = false;

	private PointF home = null;
	private PointF pushed = null;

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null || Dungeon.level == null) return;

		if (home == null) {
			float boardPx = ChessBoardLevel.SIZE * (float) DungeonTilemap.SIZE;
			home = new PointF(boardPx / 2f, boardPx / 2f + 26f);
			pushed = new PointF(home.x + PUSH_DIST * 0.35f, home.y + PUSH_DIST);
		}

		if (lastRound < 0) {
			lastRound = run.round();     // 首次进入场景不推镜头
		} else if (run.round() != lastRound) {
			lastRound = run.round();
			if (timer < 0f) {
				timer = PUSH_TIME;
				returning = false;
				Camera.main.panTo(pushed, PUSH_TIME);
			}
		}

		if (timer > 0f) {
			timer -= Game.elapsed;
			if (timer <= 0f) {
				if (!returning) {
					returning = true;
					timer = BACK_TIME;
					Camera.main.panTo(home, BACK_TIME);
				} else {
					timer = -1f;
					returning = false;
				}
			}
		}
	}
}
