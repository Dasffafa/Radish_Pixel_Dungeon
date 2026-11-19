package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.levels.ChessBoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Random;

/**
 * 棋盘外的「夜间舞台」（动态版）。
 *
 * <p>夜色 + 星星，并且是动的：星星会缓慢闪烁、轻轻漂浮，
 * 偶尔有一颗流星划过，棋盘边缘的冷光也会像呼吸一样明暗变化。</p>
 */
public class AutochessArenaOverlay extends Component {

	private static final int CELL = DungeonTilemap.SIZE;
	private static final int MARGIN = 320;
	private static final int STAR_COUNT = 260;

	private static final class Star {
		ColorBlock block;
		float x, y;
		float baseAlpha;
		float phase;
		float blinkSpeed;
		float floatSpeed;
		float floatAmp;
	}

	private final ArrayList<Star> stars = new ArrayList<>();
	private final ArrayList<ColorBlock> glow = new ArrayList<>();

	/** 流星 */
	private ColorBlock meteor;
	private ColorBlock meteorTail;
	private float meteorTimer = 6f;
	private float meteorProgress = 0f;
	private boolean meteorActive = false;
	private float meteorX, meteorY, meteorVX, meteorVY;

	private float time = 0f;

	public AutochessArenaOverlay() {
		super();

		int bw = ChessBoardLevel.SIZE * CELL;
		int bh = ChessBoardLevel.HEIGHT * CELL;

		int night = 0xE00A0D14;

		// 夜色（四个方向，不覆盖棋盘）
		addBand(-MARGIN, -MARGIN, MARGIN, bh + MARGIN * 2, night);
		addBand(bw, -MARGIN, MARGIN, bh + MARGIN * 2, night);
		addBand(0, -MARGIN, bw, MARGIN, night);
		addBand(0, bh, bw, MARGIN, night);

		// 棋盘边缘冷光（呼吸）
		addGlow(-2, -2, 2, bh + 4);
		addGlow(bw, -2, 2, bh + 4);
		addGlow(0, -2, bw, 2);
		addGlow(0, bh, bw, 2);

		// 星星
		Random rnd = new Random(20261120L);
		for (int i = 0; i < STAR_COUNT; i++) {
			float sx = rnd.nextInt(bw + MARGIN * 2) - MARGIN;
			float sy = rnd.nextInt(bh + MARGIN * 2) - MARGIN;
			if (sx >= -2 && sx <= bw + 2 && sy >= -2 && sy <= bh + 2) continue;

			Star s = new Star();
			int v = 90 + rnd.nextInt(120);
			int a = 70 + rnd.nextInt(120);
			s.block = new ColorBlock(1, 1, (a << 24) | (v << 16) | (v << 8) | v);
			s.block.x = Math.round(sx);
			s.block.y = Math.round(sy);
			add(s.block);

			s.x = s.block.x;
			s.y = s.block.y;
			s.baseAlpha = (a / 255f);
			s.phase = rnd.nextFloat() * 6.2832f;
			s.blinkSpeed = 0.8f + rnd.nextFloat() * 2.4f;
			s.floatSpeed = 0.2f + rnd.nextFloat() * 0.6f;
			s.floatAmp = 1f + rnd.nextFloat() * 3f;
			stars.add(s);

			// 少量星星加个横向十字，闪烁时更有层次
			if (rnd.nextInt(7) == 0) {
				Star cross = new Star();
				cross.block = new ColorBlock(3, 1, (a / 2 << 24) | (v << 16) | (v << 8) | v);
				cross.block.x = s.x - 1;
				cross.block.y = s.y;
				add(cross.block);
				cross.x = cross.block.x;
				cross.y = cross.block.y;
				cross.baseAlpha = s.baseAlpha * 0.6f;
				cross.phase = s.phase;
				cross.blinkSpeed = s.blinkSpeed;
				cross.floatSpeed = s.floatSpeed;
				cross.floatAmp = s.floatAmp;
				stars.add(cross);
			}
		}

		// 流星
		meteor = new ColorBlock(3, 1, 0xFFFFFFFF);
		meteor.visible = false;
		add(meteor);
		meteorTail = new ColorBlock(10, 1, 0x88FFFFFF);
		meteorTail.visible = false;
		add(meteorTail);
	}

	private void addBand(float x, float y, float w, float h, int color) {
		ColorBlock block = new ColorBlock(w, h, color);
		block.x = x;
		block.y = y;
		add(block);
	}

	private void addGlow(float x, float y, float w, float h) {
		ColorBlock block = new ColorBlock(w, h, 0x1E4A6FA8);
		block.x = x;
		block.y = y;
		add(block);
		glow.add(block);
	}

	@Override
	public void update() {
		super.update();
		time += Game.elapsed;

		// 星星闪烁 + 轻微上下浮动
		for (Star s : stars) {
			float twinkle = 0.55f + 0.45f * (float) Math.sin(time * s.blinkSpeed + s.phase);
			s.block.alpha(Math.max(0.08f, Math.min(1f, s.baseAlpha * twinkle)));
			s.block.y = s.y + (float) Math.sin(time * s.floatSpeed + s.phase) * s.floatAmp;
		}

		// 棋盘边缘冷光呼吸
		float breathe = 0.6f + 0.4f * (float) Math.sin(time * 1.1f);
		for (ColorBlock g : glow) {
			g.alpha(breathe);
		}

		// 流星：每隔一段时间从左上往右下划一颗
		if (!meteorActive) {
			meteorTimer -= Game.elapsed;
			if (meteorTimer <= 0f) {
				startMeteor();
			}
		} else {
			meteorProgress += Game.elapsed;
			float t = meteorProgress;
			meteor.x = meteorX + meteorVX * t;
			meteor.y = meteorY + meteorVY * t;
			meteorTail.x = meteor.x - meteorVX * 8f;
			meteorTail.y = meteor.y - meteorVY * 8f;
			meteor.alpha(Math.min(1f, 1.4f - t * 0.5f));
			meteorTail.alpha(Math.min(0.7f, 0.9f - t * 0.35f));

			if (t > 2.2f) {
				meteorActive = false;
				meteor.visible = false;
				meteorTail.visible = false;
				meteorTimer = 5f + (float) Math.random() * 6f;
			}
		}
	}

	private void startMeteor() {
		int bw = ChessBoardLevel.SIZE * CELL;
		int bh = ChessBoardLevel.HEIGHT * CELL;

		meteorActive = true;
		meteorProgress = 0f;
		meteorX = -MARGIN * 0.6f + (float) Math.random() * (bw + MARGIN * 0.2f);
		meteorY = -MARGIN * 0.5f + (float) Math.random() * (bh * 0.35f);
		meteorVX = 90f + (float) Math.random() * 70f;
		meteorVY = 45f + (float) Math.random() * 40f;

		// 避开棋盘区域：从棋盘上方掠过就够好看了
		if (meteorX > bw * 0.75f) {
			meteorVX = -meteorVX;
		}

		meteor.visible = true;
		meteorTail.visible = true;
		meteor.x = meteorX;
		meteor.y = meteorY;
	}
}
