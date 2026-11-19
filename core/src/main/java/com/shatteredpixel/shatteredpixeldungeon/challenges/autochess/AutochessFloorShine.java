package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

/**
 * 我方地板流光（云顶风的「舞台灯扫过」）。
 *
 * <p>每隔几秒，一道柔和的光带从左到右扫过我方半场，
 * 光带本身做成中间亮、两边淡的竖条，并略带倾斜，看起来像灯光。</p>
 */
public class AutochessFloorShine extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	/** 光带宽度（像素）。 */
	private static final int BEAM_W = 80;
	/** 一次扫过的时长（秒）。 */
	private static final float SWEEP_TIME = 3.2f;
	/** 两次之间的间隔范围。 */
	private static final float GAP_MIN = 7f;
	private static final float GAP_MAX = 13f;
	/** 光带最大亮度。 */
	private static final float MAX_ALPHA = 0.30f;

	private final ColorBlock[] columns;
	private final int startX;
	private final int fieldW;
	private final int fieldY;
	private final int fieldH;

	private float timer;
	private float progress = -1f;
	private boolean sweeping = false;

	public AutochessFloorShine() {
		super();

		startX = 1 * CELL;
		fieldW = (ChessBoardLevel.PLAYER_MAX_X) * CELL;
		fieldY = ChessBoardLevel.FIELD_MIN_Y * CELL;
		fieldH = (ChessBoardLevel.FIELD_MAX_Y - ChessBoardLevel.FIELD_MIN_Y + 1) * CELL;

		columns = new ColorBlock[fieldW];
		for (int i = 0; i < fieldW; i++) {
			// 竖直光带：整列同高，扫过时是一条竖直的光
			ColorBlock c = new ColorBlock(1, fieldH, 0xFFFFFFFF);
			c.x = startX + i;
			c.y = fieldY;
			c.visible = false;
			add(c);
			columns[i] = c;
		}

		timer = 4f + (float) Math.random() * 4f;
	}

	@Override
	public void update() {
		super.update();

		if (!sweeping) {
			timer -= Game.elapsed;
			if (timer <= 0f) {
				sweeping = true;
				progress = 0f;
				for (ColorBlock c : columns) c.visible = true;
			}
			return;
		}

		progress += Game.elapsed;
		float p = progress / SWEEP_TIME;
		if (p >= 1f) {
			sweeping = false;
			progress = -1f;
			timer = GAP_MIN + (float) Math.random() * (GAP_MAX - GAP_MIN);
			for (ColorBlock c : columns) c.visible = false;
			return;
		}

		// 光带中心从左外侧扫到右外侧
		float beamCenter = -BEAM_W + p * (fieldW + BEAM_W * 2f);
		float fade = (float) Math.sin(Math.PI * p);     // 两端淡入淡出

		for (int i = 0; i < columns.length; i++) {
			float dist = Math.abs(i - beamCenter);
			float k = Math.max(0f, 1f - dist / (BEAM_W * 0.5f));
			if (k <= 0f) {
				columns[i].visible = false;
				continue;
			}
			columns[i].visible = true;
			// 中间最亮，边缘淡出
			columns[i].alpha(MAX_ALPHA * fade * k * k);
		}
	}
}
