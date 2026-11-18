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
			mob.sprite.parent.add(mark.stars);
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
		final BitmapText stars;

		Mark(Mob mob, boolean ally, int star) {
			this.mob = mob;

			arrow = new BitmapText(PixelScene.pixelFont);
			arrow.text("v");
			arrow.hardlight(COLOR_ALLY);
			arrow.scale.set(0.8f);
			arrow.visible = ally;

			stars = new BitmapText(PixelScene.pixelFont);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < Math.max(1, star); i++) sb.append("*");
			stars.text(sb.toString());
			stars.hardlight(COLOR_STAR);
			stars.scale.set(0.7f);
		}

		void update(AutochessRun run) {
			if (mob == null || !mob.isAlive() || mob.sprite == null) {
				arrow.visible = false;
				stars.visible = false;
				return;
			}
			boolean visible = mob.sprite.visible;
			stars.visible = visible;
			if (arrow.visible) {
				arrow.visible = visible;
				arrow.x = mob.sprite.x + mob.sprite.width() / 2f - 2f;
				arrow.y = mob.sprite.y - 8f;
				PixelScene.align(arrow);
			}
			stars.x = mob.sprite.x + mob.sprite.width() / 2f - 2f;
			stars.y = mob.sprite.y - (arrow.visible ? 15f : 8f);
			PixelScene.align(stars);
		}

		void destroy() {
			arrow.killAndErase();
			stars.killAndErase();
		}
	}
}
