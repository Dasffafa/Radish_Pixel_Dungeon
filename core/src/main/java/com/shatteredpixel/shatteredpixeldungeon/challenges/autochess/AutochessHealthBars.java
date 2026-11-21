package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶的血条（LoL 风格：受伤变短 + 浅色残影，回血变长）。
 *
 * <pre>
 *      ■ ■ ■        ← 星级
 *     ───────       ← 技能条：随法力增长，放完技能浅色残影缩回 0
 *    ┏━━━━━━━━━┓
 *    ┃██████░ ░┃    ← 血条：深色实心 + 浅色「受伤残影」，没有颜色渐变
 *    ┗━━━━━━━━━┛
 * </pre>
 *
 * <p>我方绿色、敌方红色，颜色固定，不随血量变化。</p>
 */
public class AutochessHealthBars extends Component {

	private static final int BAR_W = 40;
	private static final int BAR_H = 3;

	/** 分段刻度：每 1/5 一道。 */
	private static final int SEGMENTS = 5;

	private static final int STAR = 3;
	private static final int STAR_GAP = 4;

	/** 残影追赶速度（每秒掉的百分比）。 */
	private static final float TRAIL_SPEED = 2.5f;

	private static final int ALLY_FILL = 0xFF4FD24F;
	private static final int ALLY_TRAIL = 0xFFBAF0BA;
	private static final int ENEMY_FILL = 0xFFD83333;
	private static final int ENEMY_TRAIL = 0xFFF5A8A8;

	private static final int MANA_FILL = 0xFF3FA9F5;
	private static final int MANA_TRAIL = 0xFFB9DFFB;

	private static final class Bar {
		ColorBlock border;
		ColorBlock bg;
		ColorBlock fill;
		ColorBlock trail;
		ColorBlock manaBg;
		ColorBlock manaFill;
		ColorBlock manaTrail;
		ColorBlock[] ticks;
		float trailRatio = 1f;
		float manaTrailRatio = 0f;
		float lastRatio = -1f;
		float lastMana = -1f;
		float loggedRatio = -1f;
	}

	private final HashMap<Mob, Bar> bars = new HashMap<>();
	private final HashMap<Mob, ColorBlock[]> stars = new HashMap<>();

	@Override
	public void update() {
		super.update();

		AutochessRun run = AutochessRun.get();
		if (run == null || Dungeon.level == null) {
			clearEverything();
			return;
		}
		boolean fighting = run.state() == AutochessRun.State.FIGHT;
		if (!fighting) clearBars();

		Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		for (Mob m : mobs) present.add(m);

		stars.entrySet().removeIf(e -> {
			Mob mob = e.getKey();
			boolean gone = mob.sprite == null || !present.contains(mob) || !AutochessUtil.alive(mob);
			if (gone) for (ColorBlock c : e.getValue()) c.killAndErase();
			return gone;
		});
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

			float cx = mob.sprite.center().x;
			float headTop = mob.sprite.y;

			// ---------------- 星级 ----------------
			ColorBlock[] starArr = stars.get(mob);
			if (starArr == null) {
				int count = Math.max(1, Math.min(3, run.starOf(mob)));
				starArr = new ColorBlock[count];
				for (int k = 0; k < count; k++) {
					ColorBlock c = new ColorBlock(STAR, STAR, 0xFFFFD766);
					add(c);
					starArr[k] = c;
				}
				stars.put(mob, starArr);
			}
			float starW = starArr.length * STAR_GAP - 1f;
			for (int k = 0; k < starArr.length; k++) {
				starArr[k].x = cx - starW / 2f + k * STAR_GAP;
				starArr[k].y = headTop - 16f;
				starArr[k].visible = true;
			}

			if (!fighting) continue;

			// ---------------- 血条 / 技能条 ----------------
			Bar bar = bars.get(mob);
			if (bar == null) {
				bar = new Bar();
				bar.border = new ColorBlock(BAR_W + 2, BAR_H + 2, 0xFF000000);
				bar.bg = new ColorBlock(BAR_W, BAR_H, 0xFF3A3A3A);
				bar.trail = new ColorBlock(BAR_W, BAR_H, 0xFFBBBBBB);
				bar.fill = new ColorBlock(BAR_W, BAR_H, 0xFF4FD24F);
				bar.manaBg = new ColorBlock(BAR_W, 2, 0xFF14243A);
				bar.manaTrail = new ColorBlock(BAR_W, 2, MANA_TRAIL);
				bar.manaFill = new ColorBlock(BAR_W, 2, MANA_FILL);
				add(bar.border);
				add(bar.bg);
				add(bar.trail);
				add(bar.fill);
				add(bar.manaBg);
				add(bar.manaTrail);
				add(bar.manaFill);


				bar.ticks = new ColorBlock[SEGMENTS - 1];
				for (int t = 0; t < SEGMENTS - 1; t++) {
					ColorBlock tick = new ColorBlock(1, BAR_H, 0xFF1A1A1A);
					add(tick);
					bar.ticks[t] = tick;
				}
				bars.put(mob, bar);
			}

			float top = headTop - 11f;
			float left = cx - BAR_W / 2f;
			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));

			// 浅色残影：受伤时保留旧长度，再慢慢追上新长度
			if (bar.lastRatio < 0f) {
				bar.trailRatio = ratio;
			} else if (ratio < bar.lastRatio - 0.0001f) {
				bar.trailRatio = Math.max(bar.trailRatio, bar.lastRatio);
			} else if (ratio > bar.lastRatio + 0.0001f) {
				// 回血：长度立刻变长，残影跟上
				bar.trailRatio = Math.max(bar.trailRatio, ratio);
			}
			bar.trailRatio = Math.max(ratio, bar.trailRatio - Game.elapsed * TRAIL_SPEED);
			bar.lastRatio = ratio;

			// debug: print hp/mana percent (arena.debug)
			if (java.lang.Boolean.getBoolean("arena.debug")
					&& Math.abs(ratio - bar.loggedRatio) > 0.04f) {
				bar.loggedRatio = ratio;
				float dbgMana = 0f;
				AutochessUnitBuff ub = mob.buff(AutochessUnitBuff.class);
				AutochessFoeBuff fb = mob.buff(AutochessFoeBuff.class);
				if (ub != null) dbgMana = ub.manaRatio();
				else if (fb != null) dbgMana = fb.manaRatio();
				System.out.println("[BAR] " + mob.getClass().getSimpleName()
						+ " hp=" + Math.round(ratio * 100) + "%"
						+ " barW=" + Math.round(BAR_W * ratio) + "/" + BAR_W
						+ " mana=" + Math.round(dbgMana * 100) + "%"
						+ " manaW=" + Math.round(BAR_W * dbgMana) + "/" + BAR_W);
			}

			boolean ally = mob.buff(AutochessUnitBuff.class) != null;
			int fillColor = ally ? ALLY_FILL : ENEMY_FILL;
			int trailColor = ally ? ALLY_TRAIL : ENEMY_TRAIL;

			bar.border.x = left - 1;
			bar.border.y = top - 1;
			bar.bg.x = left;
			bar.bg.y = top;

			bar.trail.x = left;
			bar.trail.y = top;
			bar.trail.width = Math.max(0f, BAR_W * bar.trailRatio);
			bar.trail.color(trailColor);

			bar.fill.x = left;
			bar.fill.y = top;
			bar.fill.width = Math.max(0f, BAR_W * ratio);
			bar.fill.color(fillColor);

			for (int t = 0; t < bar.ticks.length; t++) {
				bar.ticks[t].x = left + (BAR_W - 1) * (t + 1) / (float) SEGMENTS;
				bar.ticks[t].y = top;
			}

			// ---------------- 技能条 ----------------
			AutochessUnitBuff allyBuff = mob.buff(AutochessUnitBuff.class);
			AutochessFoeBuff foeBuff = mob.buff(AutochessFoeBuff.class);
			float mana = allyBuff != null ? allyBuff.manaRatio()
					: (foeBuff != null ? foeBuff.manaRatio() : 0f);
			mana = Math.max(0f, Math.min(1f, mana));

			// 释放技能（mana 归零）时保留一段浅色残影，再缩回 0
			if (bar.lastMana < 0f) {
				bar.manaTrailRatio = mana;
			} else if (mana < bar.lastMana - 0.0001f) {
				bar.manaTrailRatio = Math.max(bar.manaTrailRatio, bar.lastMana);
			} else {
				bar.manaTrailRatio = Math.max(mana, bar.manaTrailRatio - Game.elapsed * 3.0f);
			}
			bar.lastMana = mana;

			bar.manaBg.x = left;
			bar.manaBg.y = top - 4f;
			bar.manaTrail.x = left;
			bar.manaTrail.y = top - 4f;
			bar.manaTrail.width = Math.max(0f, BAR_W * bar.manaTrailRatio);
			bar.manaFill.x = left;
			bar.manaFill.y = top - 4f;
			bar.manaFill.width = Math.max(0f, BAR_W * mana);
		}
	}

	private void destroy(Bar bar) {
		bar.border.killAndErase();
		bar.bg.killAndErase();
		bar.trail.killAndErase();
		bar.fill.killAndErase();
		bar.manaBg.killAndErase();
		bar.manaTrail.killAndErase();
		bar.manaFill.killAndErase();
		if (bar.ticks != null) for (ColorBlock t : bar.ticks) t.killAndErase();
	}

	private void clearBars() {
		for (Map.Entry<Mob, Bar> e : bars.entrySet()) destroy(e.getValue());
		bars.clear();
	}

	private void clearEverything() {
		clearBars();
		for (ColorBlock[] arr : stars.values()) {
			for (ColorBlock c : arr) c.killAndErase();
		}
		stars.clear();
	}
}
