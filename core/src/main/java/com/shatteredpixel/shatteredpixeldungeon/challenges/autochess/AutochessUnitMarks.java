package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 棋子头顶标记：己方画蓝色箭头，双方的星级用金色 "*" 数量表示。
 *
 * <p>文字挂在棋子精灵所在的 Group 里、用世界坐标定位，和伤害飘字同一套路。</p>
 */
public class AutochessUnitMarks extends Component {

	private static final int COLOR_ALLY = 0x66DDFF;
	private static final int COLOR_STAR = 0xFFD766;

	private final ArrayList<Mark> marks = new ArrayList<>();
	private int builtVersion = -1;

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null) {
			clearMarks();
			return;
		}
		if (run.unitVersion != builtVersion) {
			builtVersion = run.unitVersion;
			rebuild(run);
		}

		for (Mark mark : marks) {
			mark.update(run);
		}
	}

	private void rebuild(AutochessRun run) {
		clearMarks();
		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.sprite == null || mob.sprite.parent == null) continue;
			boolean ally = mob.alignment == com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;
			Mark mark = new Mark(mob, ally, run.starOf(mob));
			mob.sprite.parent.add(mark.arrow);
			for (com.watabou.noosa.Image star : mark.stars) mob.sprite.parent.add(star);
			marks.add(mark);
		}
	}

	private void clearMarks() {
		for (Mark mark : marks) {
			mark.destroy();
		}
		marks.clear();
	}

	@Override
	public void destroy() {
		clearMarks();
		super.destroy();
	}

	private static class Mark {
		final Mob mob;
		final BitmapText arrow;
		final com.watabou.noosa.ColorBlock[] stars;

		Mark(Mob mob, boolean ally, int star) {
			this.mob = mob;

			arrow = new BitmapText(PixelScene.pixelFont);
			arrow.text("v");
			arrow.hardlight(COLOR_ALLY);
			arrow.scale.set(0.8f);
			arrow.visible = ally;

			// 星级用小方块拼的菱形（纯色块，不依赖贴图/字体，手机也不会变问号）
			int count = Math.max(1, Math.min(3, star));
			stars = new com.watabou.noosa.ColorBlock[count];
			for (int i = 0; i < count; i++) {
				stars[i] = new com.watabou.noosa.ColorBlock(4, 2, COLOR_STAR);
			}
		}

		void update(AutochessRun run) {
			if (mob == null || !mob.isAlive() || mob.sprite == null) {
				arrow.visible = false;
				for (com.watabou.noosa.ColorBlock star : stars) star.visible = false;
				return;
			}
			boolean visible = mob.sprite.visible;
			if (arrow.visible) {
				arrow.visible = visible;
				arrow.x = mob.sprite.x + mob.sprite.width() / 2f - 2f;
				arrow.y = mob.sprite.y - 8f;
				PixelScene.align(arrow);
			}
			float baseX = mob.sprite.x + mob.sprite.width() / 2f
					- (stars.length * 6f) / 2f;
			float baseY = mob.sprite.y - (arrow.visible ? 15f : 8f);
			for (int i = 0; i < stars.length; i++) {
				stars[i].visible = visible;
				stars[i].x = baseX + i * 6f;
				stars[i].y = baseY;
				PixelScene.align(stars[i]);
			}
		}

		void destroy() {
			arrow.killAndErase();
			for (com.watabou.noosa.ColorBlock star : stars) star.killAndErase();
		}
	}
}
