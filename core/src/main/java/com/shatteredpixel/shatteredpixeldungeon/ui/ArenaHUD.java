package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.challenges.ArenaRun;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ui.Component;

/**
 * 竞技场顶部常驻条：剩余时间 · 第几波 · 击杀数。
 *
 * <p>放在状态栏里会和血条、Buff 条抢位置，所以单独做成一条浮在画面上方中间的小黑板。</p>
 */
public class ArenaHUD extends Component {

	public static final float WIDTH = 104;
	public static final float HEIGHT = 14;

	private static final int COLOR_NORMAL = 0xFFFFCC;
	private static final int COLOR_URGENT = 0xFF6060;

	private RoundedFrame frame;
	private BitmapText label;
	private String shown = "";

	public ArenaHUD() {
		super();
		setSize(WIDTH, HEIGHT);
	}

	@Override
	protected void createChildren() {
		frame = new RoundedFrame(0xCC1A1A1A, 0xFFFF66);
		add(frame);

		label = new BitmapText(PixelScene.pixelFont);
		label.hardlight(COLOR_NORMAL);
		add(label);
	}

	@Override
	protected void layout() {
		frame.setRect(x, y, WIDTH, HEIGHT);
		if (label != null) {
			label.measure();
			label.x = x + (WIDTH - label.width()) / 2f;
			label.y = y + (HEIGHT - label.baseLine()) / 2f;
			PixelScene.align(label);
		}
	}

	@Override
	public void update() {
		super.update();

		String text = M.L(ArenaHUD.class, "format",
				ArenaRun.hudTime(), ArenaRun.hudWave(), ArenaRun.hudKills());
		if (!text.equals(shown)) {
			shown = text;
			label.text(text);
			label.measure();
			label.x = x + (WIDTH - label.width()) / 2f;
			PixelScene.align(label);
		}

		// 最后 30 秒变红提醒
		label.hardlight(ArenaRun.hudSeconds() <= 30 ? COLOR_URGENT : COLOR_NORMAL);
	}
}
