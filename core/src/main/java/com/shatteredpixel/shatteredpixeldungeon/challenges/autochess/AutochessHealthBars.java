package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶的血条 + 技能条 + 星级。
 *
 * <p>位置完全照抄原版 CharHealthIndicator 的算法：
 * {@code x = sprite.x + sprite.width()/6, y = sprite.y - 2, width = sprite.width()*4/6}，
 * 所以永远贴着棋子、不会错位。</p>
 *
 * <p>血条：长度 = 宽度 × HP%（掉 10% 就短 10%），我方绿、敌方红；
 * 技能条：长度 = 宽度 × 法力%，在血条正上方一条细蓝线，放完技能归零。</p>
 */
public class AutochessHealthBars extends Component {

	private static final int STAR = 3;
	private static final int STAR_GAP = 4;

	private static final int BAR_H = 3;      // 血条高度
	private static final int MANA_H = 1;     // 技能条高度
	private static final int SEGMENTS = 5;   // 血条分段刻度

	private static final int ALLY_BG = 0xFF12300F;
	private static final int ALLY_FILL = 0xFF4FD24F;
	private static final int ENEMY_BG = 0xFF3A0A0A;
	private static final int ENEMY_FILL = 0xFFE03030;
	private static final int MANA_BG = 0xFF14243A;
	private static final int MANA_FILL = 0xFF3FA9F5;
	private static final int TICK = 0xFF101010;

	private static final class Bar {
		ColorBlock bg;
		ColorBlock fill;
		ColorBlock manaBg;
		ColorBlock mana;
		ColorBlock[] ticks;
		ColorBlock[] stars;
	}

	private final HashMap<Mob, Bar> bars = new HashMap<>();

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null || Dungeon.level == null) {
			clearEverything();
			return;
		}
		boolean fighting = run.state() == AutochessRun.State.FIGHT;

		Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		for (Mob m : mobs) present.add(m);

		bars.entrySet().removeIf(e -> {
			Mob mob = e.getKey();
			boolean gone = mob.sprite == null || !present.contains(mob) || !AutochessUtil.alive(mob);
			if (gone) destroy(e.getValue());
			return gone;
		});

		for (Mob mob : mobs) {
			if (mob.sprite == null || !mob.sprite.visible) continue;
			boolean ours = mob.buff(AutochessUnitBuff.class) != null
					|| mob.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;

			Bar bar = bars.get(mob);
			if (bar == null) {
				bar = new Bar();
				bar.bg = new ColorBlock(1, BAR_H, ALLY_BG);
				bar.fill = new ColorBlock(1, BAR_H, ALLY_FILL);
				bar.manaBg = new ColorBlock(1, MANA_H, MANA_BG);
				bar.mana = new ColorBlock(1, MANA_H, MANA_FILL);
				add(bar.bg);
				add(bar.fill);
				add(bar.manaBg);
				add(bar.mana);

				bar.ticks = new ColorBlock[SEGMENTS - 1];
				for (int t = 0; t < SEGMENTS - 1; t++) {
					ColorBlock tick = new ColorBlock(1, BAR_H, TICK);
					add(tick);
					bar.ticks[t] = tick;
				}

				int starCount = Math.max(1, Math.min(3, run.starOf(mob)));
				bar.stars = new ColorBlock[starCount];
				for (int k = 0; k < starCount; k++) {
					ColorBlock star = new ColorBlock(STAR, STAR, 0xFFFFD766);
					add(star);
					bar.stars[k] = star;
				}
				bars.put(mob, bar);
			}

			// 位置：跟原版血条一致的精灵相对坐标
			float spriteW = mob.sprite.width();
			int barW = Math.max(8, Math.round(spriteW * (4 / 6f)));
			float barX = mob.sprite.x + spriteW / 6f;
			float barY = mob.sprite.y - 4f;

			boolean ally = mob.buff(AutochessUnitBuff.class) != null;
			bar.bg.visible = fighting;
			bar.fill.visible = fighting;
			bar.manaBg.visible = fighting;
			bar.mana.visible = fighting;
			for (ColorBlock t : bar.ticks) t.visible = fighting;

			bar.bg.x = barX;
			bar.bg.y = barY;
			bar.bg.width = barW;
			bar.bg.color(ally ? ALLY_BG : ENEMY_BG);

			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));
			bar.fill.x = barX;
			bar.fill.y = barY;
			bar.fill.width = Math.max(0f, barW * ratio);
			bar.fill.color(ally ? ALLY_FILL : ENEMY_FILL);

			for (int t = 0; t < bar.ticks.length; t++) {
				bar.ticks[t].x = barX + (barW - 1) * (t + 1) / (float) SEGMENTS;
				bar.ticks[t].y = barY;
			}

			// 技能条：血条正上方 1px 细蓝线
			AutochessUnitBuff allyBuff = mob.buff(AutochessUnitBuff.class);
			AutochessFoeBuff foeBuff = mob.buff(AutochessFoeBuff.class);
			float mana = allyBuff != null ? allyBuff.manaRatio()
					: (foeBuff != null ? foeBuff.manaRatio() : 0f);
			mana = Math.max(0f, Math.min(1f, mana));
			bar.manaBg.x = barX;
			bar.manaBg.y = barY - MANA_H - 1;
			bar.manaBg.width = barW;
			bar.mana.x = barX;
			bar.mana.y = barY - MANA_H - 1;
			bar.mana.width = Math.max(0f, barW * mana);
			bar.mana.color(mana >= 0.999f ? 0xFFFFFFFF : MANA_FILL);

			// 星级：技能条上方
			float starW = bar.stars.length * STAR_GAP - 1f;
			for (int k = 0; k < bar.stars.length; k++) {
				bar.stars[k].x = barX + barW / 2f - starW / 2f + k * STAR_GAP;
				bar.stars[k].y = barY - 10f;
				bar.stars[k].visible = true;
			}
		}
	}

	private void destroy(Bar bar) {
		bar.bg.killAndErase();
		bar.fill.killAndErase();
		bar.manaBg.killAndErase();
		bar.mana.killAndErase();
		for (ColorBlock t : bar.ticks) t.killAndErase();
		for (ColorBlock s : bar.stars) s.killAndErase();
	}

	private void clearEverything() {
		for (Map.Entry<Mob, Bar> e : bars.entrySet()) destroy(e.getValue());
		bars.clear();
	}
}
