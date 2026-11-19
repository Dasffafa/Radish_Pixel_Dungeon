package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶血条：战斗时每只棋子头上一条细血条。
 *
 * <p>我方绿色、敌方红色；血量越低颜色越暗。备战阶段不显示（那时都是满血）。</p>
 */
public class AutochessHealthBars extends Component {

	private static final int BAR_W = 14;
	private static final int BAR_H = 2;

	private static final class Bar {
		ColorBlock bg;
		ColorBlock fill;
		ColorBlock mana;         // 法力条，画在血条下方
		ColorBlock[] stars;      // 1~3 颗星，画在血条正上方
	}

	private final HashMap<Mob, Bar> bars = new HashMap<>();

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null || Dungeon.level == null) {
			hideAll();
			return;
		}
		boolean hpVisible = run.state() == AutochessRun.State.FIGHT;

		// 清掉已经死掉/消失的棋子（也包括已经不在场上、只剩残影的）
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) present.add(m);
		bars.entrySet().removeIf(e -> {
			Mob mob = e.getKey();
			boolean gone = !mob.isAlive() || mob.sprite == null || !mob.sprite.visible
					|| !present.contains(mob);
			if (gone) {
				e.getValue().bg.killAndErase();
				e.getValue().fill.killAndErase();
				if (e.getValue().stars != null) {
					for (ColorBlock star : e.getValue().stars) star.killAndErase();
				}
			}
			return gone;
		});

		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.sprite == null) continue;
			boolean ours = mob.buff(AutochessUnitBuff.class) != null
					|| mob.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;

			Bar bar = bars.get(mob);
			if (bar == null) {
				bar = new Bar();
				bar.bg = new ColorBlock(BAR_W, BAR_H, 0xCC100000);
				bar.fill = new ColorBlock(BAR_W, BAR_H, 0xFF66DD66);
				bar.mana = new ColorBlock(BAR_W, 1, 0xFF66CCFF);
				add(bar.bg);
				add(bar.fill);
				add(bar.mana);

				int starCount = Math.max(1, Math.min(3, run.starOf(mob)));
				bar.stars = new ColorBlock[starCount];
				for (int k = 0; k < starCount; k++) {
					ColorBlock star = new ColorBlock(3, 3, 0xFFFFD766);
					add(star);
					bar.stars[k] = star;
				}
				bars.put(mob, bar);
			}

			float x = mob.sprite.x + (mob.sprite.width() - BAR_W) / 2f;
			float y = mob.sprite.y - 5f;
			bar.bg.x = x;
			bar.bg.y = y;
			bar.fill.x = x;
			bar.fill.y = y;
			bar.bg.visible = hpVisible;
			bar.fill.visible = hpVisible;
			bar.mana.visible = hpVisible;
			bar.mana.x = x;
			bar.mana.y = y + BAR_H + 1;

			// 星级：一行小方块，居中排在血条正上方
			float starY = y - 5f;
			float totalW = bar.stars.length * 4f - 1f;
			float sx0 = mob.sprite.x + (mob.sprite.width() - totalW) / 2f;
			for (int k = 0; k < bar.stars.length; k++) {
				bar.stars[k].x = sx0 + k * 4f;
				bar.stars[k].y = starY;
				bar.stars[k].visible = true;
			}

			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));
			bar.fill.width = Math.max(1f, BAR_W * ratio);
			boolean ally = mob.alignment == Char.Alignment.ALLY
					|| mob.buff(AutochessUnitBuff.class) != null;
			AutochessUnitBuff allyBuff = mob.buff(AutochessUnitBuff.class);
			AutochessFoeBuff foeBuff = mob.buff(AutochessFoeBuff.class);
			float manaRatio = allyBuff != null ? allyBuff.manaRatio() : (foeBuff != null ? foeBuff.manaRatio() : 0f);
			bar.mana.width = Math.max(0f, BAR_W * Math.min(1f, manaRatio));
			bar.mana.color(manaRatio >= 0.999f ? 0xFFFFFFFF : 0xFF66CCFF);

			if (ally) {
				bar.fill.color(ratio > 0.5f ? 0xFF66DD66 : (ratio > 0.25f ? 0xFFDDCC44 : 0xFFDD5555));
			} else {
				bar.fill.color(ratio > 0.5f ? 0xFFDD5555 : (ratio > 0.25f ? 0xFFBB3322 : 0xFF772211));
			}
		}
	}

	private void hideAll() {
		for (Map.Entry<Mob, Bar> e : bars.entrySet()) {
			e.getValue().bg.killAndErase();
			e.getValue().fill.killAndErase();
			if (e.getValue().stars != null) {
				for (ColorBlock star : e.getValue().stars) star.killAndErase();
			}
		}
		bars.clear();
	}
}
