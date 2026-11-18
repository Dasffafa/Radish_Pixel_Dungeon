package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 在己方棋子头顶画一个小蓝箭头（"v"），用来区分敌我。
 *
 * <p>实现方式参考伤害飘字：文字挂在棋子精灵所在的 Group 里、用世界坐标定位，
 * 这样一定画在棋子上面，镜头移动也跟着走。</p>
 */
public class AutochessUnitMarks extends Component {

	private static final int COLOR = 0x66DDFF;

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
			rebuild();
		}

		for (Mark mark : marks) {
			mark.update();
		}
	}

	private void rebuild() {
		clearMarks();
		if (Dungeon.level == null) return;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment != Char.Alignment.ALLY) continue;
			if (mob.sprite == null || mob.sprite.parent == null) continue;
			Mark mark = new Mark(mob);
			mob.sprite.parent.add(mark.label);
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

	/** 头顶的 "v" 标记，跟着精灵走。 */
	private static class Mark {
		final Mob mob;
		final BitmapText label;

		Mark(Mob mob) {
			this.mob = mob;
			label = new BitmapText(PixelScene.pixelFont);
			label.text("v");
			label.hardlight(COLOR);
			label.scale.set(0.8f);
		}

		void update() {
			if (mob == null || !mob.isAlive() || mob.sprite == null) {
				label.visible = false;
				return;
			}
			label.visible = mob.sprite.visible;
			label.x = mob.sprite.x + mob.sprite.width() / 2f - 2f;
			label.y = mob.sprite.y - 7f;
			PixelScene.align(label);
		}

		void destroy() {
			label.killAndErase();
		}
	}
}
