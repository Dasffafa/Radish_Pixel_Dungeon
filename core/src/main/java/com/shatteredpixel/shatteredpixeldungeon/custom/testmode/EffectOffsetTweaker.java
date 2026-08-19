package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

import java.util.ArrayList;

/**
 * 游戏内特效偏移微调工具（test mode）。
 *
 * <p>开启后，当前被登记的特效可用方向键实时平移偏移，HUD 显示当前值；
 * 满意后按 Enter 把数值打印到日志，作为你硬编码进特效的常量。
 * 偏移字段由特效自身通过 {@link OffsetTweakable} 暴露，本工具只负责改与显示。</p>
 *
 * <p>操作：方向键微调；Shift 按住 ×10；Ctrl 按住 ×0.1；+/- 调步长；Enter 打印；Backspace 清零。</p>
 */
public class EffectOffsetTweaker extends TestItem {

	public static boolean active = false;
	public static float step = 1f;
	private static OffsetTweakable target;

	private static TweakOverlay overlay;

	private static final String AC_TWEAK = "tweak";
	private static final String AC_STEP  = "step";

	{
		image = ItemSpriteSheet.WAND_MAGIC_MISSILE;
		defaultAction = AC_TWEAK;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_TWEAK);
		actions.add(AC_STEP);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_TWEAK)) {
			active = !active;
			if (active) {
				if (overlay == null || !overlay.alive) {
					overlay = new TweakOverlay();
					Game.scene().add(overlay);
				}
				GLog.i("特效微调：已开启，等待特效注册…");
			} else {
				GLog.i("特效微调：已关闭");
			}
		}
		if (action.equals(AC_STEP)) {
			if (overlay == null || !overlay.alive) {
				overlay = new TweakOverlay();
				Game.scene().add(overlay);
			}
			Game.scene().addToFront(new StepWindow());
		}
	}

	/** 特效每帧调用以登记自己为微调目标（未开启时为无操作）。 */
	public static void track(OffsetTweakable e) {
		if (!active) return;
		if (target != e) {
			target = e;
			GLog.i("特效微调：目标 = " + e.tweakName());
		}
	}

	/** 当前微调目标。 */
	public static OffsetTweakable target() { return target; }

	/** 步长调整窗口。 */
	private static class StepWindow extends Window {

		public StepWindow() {
			float step = EffectOffsetTweaker.step;

			RenderedTextBlock title = PixelScene.renderTextBlock("步长: " + step, 9);
			title.setPos(4, 4);
			add(title);

			RedButton b;
			float y = 18;
			float w = (140 - 12) / 3f;
			b = new RedButton("0.1") { @Override protected void onClick(){ EffectOffsetTweaker.step = 0.1f; hide(); } };
			b.setRect(4, y, w, 18); add(b);
			b = new RedButton("1")   { @Override protected void onClick(){ EffectOffsetTweaker.step = 1f;   hide(); } };
			b.setRect(4 + w + 2, y, w, 18); add(b);
			b = new RedButton("10")  { @Override protected void onClick(){ EffectOffsetTweaker.step = 10f;  hide(); } };
			b.setRect(4 + 2*w + 4, y, w, 18); add(b);

			resize(140, 18 + 18 + 4);
		}
	}

	/** 每帧读取输入并绘制 HUD 的覆盖层。 */
	private static class TweakOverlay extends Group {

		private final RenderedTextBlock hud;
		private long lastPrint = 0;

		public TweakOverlay() {
			hud = PixelScene.renderTextBlock("", 9);
			hud.setPos(6, 6);
			add(hud);
		}

		@Override
		public void update() {
			super.update();

			if (!active) { remove(); return; }

			OffsetTweakable t = EffectOffsetTweaker.target;
			if (t == null) {
				hud.text("特效微调：等待特效注册…\n特效在 update 里调用 EffectOffsetTweaker.track(this)");
				hud.setPos(6, 6);
				return;
			}

			float s = EffectOffsetTweaker.step;
			if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))   s = EffectOffsetTweaker.step * 10;
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) s = EffectOffsetTweaker.step * 0.1f;

			float dx = 0, dy = 0;
			if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT))  dx -= s;
			if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) dx += s;
			if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP))    dy -= s;
			if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN))  dy += s;
			if (dx != 0 || dy != 0) {
				t.setTweakOffset(t.tweakOffsetX() + dx, t.tweakOffsetY() + dy);
			}

			long now = System.currentTimeMillis();
			if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && now - lastPrint > 500) {
				lastPrint = now;
				GLog.i("特效偏移 " + t.tweakName() + " = (" + t.tweakOffsetX() + ", " + t.tweakOffsetY() + ")");
			}

			if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)) {
				t.setTweakOffset(0, 0);
			}

			hud.text(t.tweakName()
					+ "\noffset = (" + t.tweakOffsetX() + ", " + t.tweakOffsetY() + ")"
					+ "\n步长 " + EffectOffsetTweaker.step
					+ "\n方向键微调 | Shift×10 Ctrl×0.1"
					+ "\nEnter 打印 | Backspace 清零");
			hud.setPos(6, 6);
		}
	}
}
