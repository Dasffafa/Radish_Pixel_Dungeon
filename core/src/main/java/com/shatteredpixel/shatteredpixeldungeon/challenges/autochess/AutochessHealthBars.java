package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶的状态条（血量 + 技能 + 星级）。
 *
 * <p>两个关键点：</p>
 * <ul>
 *   <li>所有 ColorBlock 用 {@code size(w,h)} 设置尺寸（ColorBlock 的 width 字段不参与绘制）</li>
 *   <li>法力由本组件自己累积（每 0.45 秒 +10%），满 100% 直接让 BUFF 放技能并清零，
 *       完全不依赖 Buff 的 act 排程，也不依赖 HUD</li>
 * </ul>
 */
public class AutochessHealthBars extends Component {

	private static final int STAR = 2;
	private static final int STAR_GAP = 3;

	private static final int HP_H = 3;
	private static final int MANA_H = 2;

	private static final int ALLY_BG = 0xFF16210F;
	private static final int ALLY_FILL = 0xFF56C24B;
	private static final int ALLY_HI = 0xFF9BEA8B;
	private static final int ENEMY_BG = 0xFF2A0E0E;
	private static final int ENEMY_FILL = 0xFFD63A34;
	private static final int ENEMY_HI = 0xFFF58C86;
	private static final int MANA_BG = 0xFF0E1A2B;
	private static final int MANA_FILL = 0xFF3A9BE8;
	private static final int BORDER = 0xFF0A0A0A;

	/** 法力节拍：0.45 秒涨 10%，约 4.5 秒放一次技能。 */
	private static final float MANA_TICK = 0.45f;
	private static final float MANA_PER_TICK = 0.10f;

	private static final class Bar {
		ColorBlock border;
		ColorBlock bg;
		ColorBlock fill;
		ColorBlock hi;
		ColorBlock manaBg;
		ColorBlock manaFill;
		ColorBlock manaHi;
		ColorBlock[] stars;
		float mana = 0f;
	}

	private final HashMap<Mob, Bar> bars = new HashMap<>();
	private float manaAcc = 0f;

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

		// 清理离场棋子
		bars.entrySet().removeIf(e -> {
			Mob mob = e.getKey();
			boolean gone = mob.sprite == null || !present.contains(mob) || !AutochessUtil.alive(mob);
			if (gone) destroy(e.getValue());
			return gone;
		});

		if (!fighting) {
			manaAcc = 0f;
			for (Bar b : bars.values()) {
				b.border.visible = false;
				b.bg.visible = false;
				b.fill.visible = false;
				b.hi.visible = false;
				b.manaBg.visible = false;
				b.manaFill.visible = false;
				b.manaHi.visible = false;
			}
			updateStarsOnly(mobs, present, run);
			return;
		}

		// ---------------- 法力节拍（本组件自己驱动） ----------------
		manaAcc += Game.elapsed;
		if (manaAcc >= MANA_TICK) {
			manaAcc = 0f;
			for (Mob mob : mobs) {
				Bar b = bars.get(mob);
				if (b == null) continue;
				b.mana = Math.min(1f, b.mana + MANA_PER_TICK);
				if (b.mana >= 1f) {
					b.mana = 0f;
					AutochessUnitBuff ub = mob.buff(AutochessUnitBuff.class);
					if (ub != null) {
						ub.castNow();
					} else {
						AutochessFoeBuff fb = mob.buff(AutochessFoeBuff.class);
						if (fb != null) fb.castNow();
					}
				}
			}
		}

		for (Mob mob : mobs) {
			if (mob.sprite == null || !mob.sprite.visible) continue;
			boolean ours = mob.buff(AutochessUnitBuff.class) != null
					|| mob.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;

			Bar bar = bars.get(mob);
			if (bar == null) {
				bar = createBar(mob, run);
			}
			if (bar == null) continue;

			float spriteW = mob.sprite.width();
			float cx = mob.sprite.center().x;
			int barW = Math.max(14, Math.round(spriteW * 1.15f));
			float left = cx - barW / 2f;

			float manaY = mob.sprite.y - (MANA_H + HP_H + 2) - 2f;
			float hpY = manaY + MANA_H + 1f;

			boolean ally = mob.buff(AutochessUnitBuff.class) != null;
			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));

			bar.border.visible = true;
			bar.border.x = left - 1;
			bar.border.y = hpY - 1;
			bar.border.size(barW + 2, HP_H + 2);

			bar.bg.visible = true;
			bar.bg.x = left;
			bar.bg.y = hpY;
			bar.bg.size(barW, HP_H);
			bar.bg.color(ally ? ALLY_BG : ENEMY_BG);

			float hpW = Math.max(0f, barW * ratio);
			bar.fill.visible = true;
			bar.fill.x = left;
			bar.fill.y = hpY;
			bar.fill.size(hpW, HP_H);
			bar.fill.color(ally ? ALLY_FILL : ENEMY_FILL);

			bar.hi.visible = hpW >= 2f;
			bar.hi.x = left;
			bar.hi.y = hpY;
			bar.hi.size(Math.max(0f, hpW - 1f), 1);
			bar.hi.color(ally ? ALLY_HI : ENEMY_HI);

			bar.manaBg.visible = true;
			bar.manaBg.x = left;
			bar.manaBg.y = manaY;
			bar.manaBg.size(barW, MANA_H);

			float manaW = Math.max(0f, barW * bar.mana);
			bar.manaFill.visible = true;
			bar.manaFill.x = left;
			bar.manaFill.y = manaY;
			bar.manaFill.size(manaW, MANA_H);

			bar.manaHi.visible = manaW >= 2f;
			bar.manaHi.x = left;
			bar.manaHi.y = manaY;
			bar.manaHi.size(Math.max(0f, manaW - 1f), 1);

			float starW = bar.stars.length * STAR_GAP - 1f;
			for (int k = 0; k < bar.stars.length; k++) {
				bar.stars[k].x = cx - starW / 2f + k * STAR_GAP;
				bar.stars[k].y = manaY - STAR - 1;
				bar.stars[k].visible = true;
			}
		}
	}

	private void updateStarsOnly(Mob[] mobs, java.util.HashSet<Mob> present, AutochessRun run) {
		for (Mob mob : mobs) {
			if (mob.sprite == null || !mob.sprite.visible) continue;
			boolean ours = mob.buff(AutochessUnitBuff.class) != null
					|| mob.buff(AutochessFoeBuff.class) != null;
			if (!ours) continue;
			Bar bar = bars.get(mob);
			if (bar == null) bar = createBar(mob, run);
			if (bar == null) continue;
			float cx = mob.sprite.center().x;
			float starW = bar.stars.length * STAR_GAP - 1f;
			for (int k = 0; k < bar.stars.length; k++) {
				bar.stars[k].x = cx - starW / 2f + k * STAR_GAP;
				bar.stars[k].y = mob.sprite.y - 8f;
				bar.stars[k].visible = true;
			}
		}
	}

	private Bar createBar(Mob mob, AutochessRun run) {
		Bar bar = new Bar();
		bar.border = new ColorBlock(1, 1, BORDER);
		bar.bg = new ColorBlock(1, 1, ALLY_BG);
		bar.fill = new ColorBlock(1, 1, ALLY_FILL);
		bar.hi = new ColorBlock(1, 1, ALLY_HI);
		bar.manaBg = new ColorBlock(1, 1, MANA_BG);
		bar.manaFill = new ColorBlock(1, 1, MANA_FILL);
		bar.manaHi = new ColorBlock(1, 1, 0xFF8CCBFF);
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
		return bar;
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
		manaAcc = 0f;
	}
}
