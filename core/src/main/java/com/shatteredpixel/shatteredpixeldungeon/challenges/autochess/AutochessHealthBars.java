package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶的状态条（LoL 风格，干净版）。
 *
 * <pre>
 *      ■ ■            ← 星级
 *     ▔▔▔▔▔           ← 技能条（蓝）
 *    ┏━━━━━━━┓
 *    ┃▓▓▓▓▓  ┃        ← 血条（我方绿 / 敌方红）
 *    ┗━━━━━━━┛
 * </pre>
 *
 * <p>长度严格按百分比：血条 = 宽度 × HP%，技能条 = 宽度 × 法力%。</p>
 */
public class AutochessHealthBars extends Component {

	private static final int STAR = 2;
	private static final int STAR_GAP = 3;

	private static final int HP_H = 3;
	private static final int MANA_H = 2;

	// 我方
	private static final int ALLY_BG = 0xFF16210F;
	private static final int ALLY_FILL = 0xFF56C24B;
	private static final int ALLY_HI = 0xFF9BEA8B;
	// 敌方
	private static final int ENEMY_BG = 0xFF2A0E0E;
	private static final int ENEMY_FILL = 0xFFD63A34;
	private static final int ENEMY_HI = 0xFFF58C86;
	// 技能条
	private static final int MANA_BG = 0xFF0E1A2B;
	private static final int MANA_FILL = 0xFF3A9BE8;
	private static final int MANA_HI = 0xFF8CCBFF;

	private static final int BORDER = 0xFF0A0A0A;

	private static final class Bar {
		ColorBlock border;
		ColorBlock bg;
		ColorBlock fill;
		ColorBlock hi;
		ColorBlock manaBg;
		ColorBlock manaFill;
		ColorBlock manaHi;
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
				bar.border = new ColorBlock(1, 1, BORDER);
				bar.bg = new ColorBlock(1, 1, ALLY_BG);
				bar.fill = new ColorBlock(1, 1, ALLY_FILL);
				bar.hi = new ColorBlock(1, 1, ALLY_HI);
				bar.manaBg = new ColorBlock(1, 1, MANA_BG);
				bar.manaFill = new ColorBlock(1, 1, MANA_FILL);
				bar.manaHi = new ColorBlock(1, 1, MANA_HI);

				add(bar.border);
				add(bar.bg);
				add(bar.fill);
				add(bar.hi);
				add(bar.manaBg);
				add(bar.manaFill);
				add(bar.manaHi);

				int starCount = Math.max(1, Math.min(3, run.starOf(mob)));
				bar.stars = new ColorBlock[starCount];
				for (int k = 0; k < starCount; k++) {
					ColorBlock star = new ColorBlock(STAR, STAR, 0xFFFFD766);
					add(star);
					bar.stars[k] = star;
				}
				bars.put(mob, bar);
			}

			// ---------- 布局（以精灵中心对齐） ----------
			float spriteW = mob.sprite.width();
			float cx = mob.sprite.center().x;
			int barW = Math.max(14, Math.round(spriteW * 1.15f));
			float left = cx - barW / 2f;

			float manaY = mob.sprite.y - (MANA_H + HP_H + 1) - 3f;
			float hpY = manaY + MANA_H + 1f;

			boolean ally = mob.buff(AutochessUnitBuff.class) != null;
			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));
			AutochessUnitBuff allyBuff = mob.buff(AutochessUnitBuff.class);
			AutochessFoeBuff foeBuff = mob.buff(AutochessFoeBuff.class);
			float mana = allyBuff != null ? allyBuff.manaRatio()
					: (foeBuff != null ? foeBuff.manaRatio() : 0f);
			mana = Math.max(0f, Math.min(1f, mana));

			// 边框（比血条大 1px）
			bar.border.visible = fighting;
			bar.border.x = left - 1;
			bar.border.y = hpY - 1;
			bar.border.size(barW + 2, HP_H + 2);

			bar.bg.visible = fighting;
			bar.bg.x = left;
			bar.bg.y = hpY;
			bar.bg.size(barW, HP_H);
			bar.bg.color(ally ? ALLY_BG : ENEMY_BG);

			float hpW = Math.max(0f, barW * ratio);
			bar.fill.visible = fighting;
			bar.fill.x = left;
			bar.fill.y = hpY;
			bar.fill.size(hpW, HP_H);
			bar.fill.color(ally ? ALLY_FILL : ENEMY_FILL);

			// 顶部 1px 高光，看起来更像联盟血条
			bar.hi.visible = fighting && hpW >= 2f;
			bar.hi.x = left;
			bar.hi.y = hpY;
			bar.hi.size(Math.max(0f, hpW - 1f), 1);
			bar.hi.color(ally ? ALLY_HI : ENEMY_HI);

			// 技能条
			bar.manaBg.visible = fighting;
			bar.manaBg.x = left;
			bar.manaBg.y = manaY;
			bar.manaBg.size(barW, MANA_H);

			float manaW = Math.max(0f, barW * mana);
			bar.manaFill.visible = fighting;
			bar.manaFill.x = left;
			bar.manaFill.y = manaY;
			bar.manaFill.size(manaW, MANA_H);
			bar.manaFill.color(mana >= 0.999f ? 0xFFFFFFFF : MANA_FILL);

			bar.manaHi.visible = fighting && manaW >= 2f;
			bar.manaHi.x = left;
			bar.manaHi.y = manaY;
			bar.manaHi.size(Math.max(0f, manaW - 1f), 1);
			bar.manaHi.color(mana >= 0.999f ? 0xFFFFFFFF : MANA_HI);

			// 星级：技能条上方
			float starW = bar.stars.length * STAR_GAP - 1f;
			for (int k = 0; k < bar.stars.length; k++) {
				bar.stars[k].x = cx - starW / 2f + k * STAR_GAP;
				bar.stars[k].y = manaY - STAR - 1;
				bar.stars[k].visible = true;
			}
		}
	}

	private void destroy(Bar bar) {
		bar.border.killAndErase();
		bar.bg.killAndErase();
		bar.fill.killAndErase();
		bar.hi.killAndErase();
		bar.manaBg.killAndErase();
		bar.manaFill.killAndErase();
		bar.manaHi.killAndErase();
		for (ColorBlock s : bar.stars) s.killAndErase();
	}

	private void clearEverything() {
		for (Map.Entry<Mob, Bar> e : bars.entrySet()) destroy(e.getValue());
		bars.clear();
	}
}
