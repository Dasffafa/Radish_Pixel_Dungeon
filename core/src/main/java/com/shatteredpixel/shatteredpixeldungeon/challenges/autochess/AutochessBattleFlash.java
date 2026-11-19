package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

/**
 * 开战特效：双方阵地各闪一下再开打。
 *
 * <p>备战转到战斗的那一瞬间，我方半场闪冷蓝、敌方半场闪红，
 * 中间再闪一条白线，0.55 秒内淡出。</p>
 */
public class AutochessBattleFlash extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	private static final float FLASH_TIME = 0.55f;

	private final ColorBlock playerHalf;
	private final ColorBlock enemyHalf;
	private final ColorBlock midLine;

	private float flash = 0f;
	/** 只有在 startFight() 真正开打时才会被置位，避免状态抖动误触发。 */
	private static boolean pending = false;

	public static void request() {
		pending = true;
	}

	public AutochessBattleFlash() {
		super();

		float fieldTop = ChessBoardLevel.FIELD_MIN_Y * (float) CELL;
		float fieldH = (ChessBoardLevel.FIELD_MAX_Y - ChessBoardLevel.FIELD_MIN_Y + 1) * (float) CELL;
		int midX = ChessBoardLevel.SIZE / 2;

		float pw = midX * (float) CELL;
		float ew = (ChessBoardLevel.SIZE - 1) * (float) CELL - pw;

		playerHalf = new ColorBlock(pw, fieldH, 0x66AAD4FF);
		playerHalf.x = CELL;
		playerHalf.y = fieldTop;
		playerHalf.visible = false;
		add(playerHalf);

		enemyHalf = new ColorBlock(ew, fieldH, 0x66FF6666);
		enemyHalf.x = pw + midX * (float) CELL;
		enemyHalf.y = fieldTop;
		enemyHalf.visible = false;
		add(enemyHalf);

		midLine = new ColorBlock(2, fieldH, 0xCCFFFFFF);
		midLine.x = midX * (float) CELL;
		midLine.y = fieldTop;
		midLine.visible = false;
		add(midLine);
	}

	@Override
	public void update() {
		super.update();

		if (pending) {
			pending = false;
			flash = FLASH_TIME;
		}

		if (flash <= 0f) {
			playerHalf.visible = false;
			enemyHalf.visible = false;
			midLine.visible = false;
			return;
		}

		flash -= Game.elapsed;
		float k = Math.max(0f, flash / FLASH_TIME);
		// 起手最亮，然后平方衰减，看起来像「闪一下」
		float a = k * k;

		playerHalf.visible = true;
		playerHalf.alpha(a);
		enemyHalf.visible = true;
		enemyHalf.alpha(a);
		midLine.visible = true;
		midLine.alpha(Math.min(1f, a * 1.6f));
	}
}
