package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶的血条（LoL 风格）。
 *
 * <pre>
 *      ■ ■ ■          ← 星级
 *      ▓▓▓▓▓▓▓▓       ← 技能条：随法力累积变长，放完技能回到初始状态
 *     ┏━━━━━━━━━┓
 *     ┃███████  ┃     ← 血条：联盟式黑框 + 纯色填充，掉血缩短
 *     ┗━━━━━━━━━┛
 * </pre>
 *
 * <p>我方绿色（残血转黄/红），敌方红色系。</p>
 */
public class AutochessHealthBars extends Component {

	private static final int BAR_W = 16;
	private static final int BAR_H = 4;

	private static final int STAR = 3;
	private static final int STAR_GAP = 4;

	private static final float FLASH_TIME = 0.28f;

	private static final class Bar {
		ColorBlock border;
		ColorBlock bg;
		ColorBlock fill;
		ColorBlock flash;
		ColorBlock mana;
		int lastHp = -1;
		float flashTimer = 0f;
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
				starArr[k].y = headTop - 20f;
				starArr[k].visible = true;
			}

			if (!fighting) continue;

			// ---------------- 血条 / 技能条 ----------------
			Bar bar = bars.get(mob);
			if (bar == null) {
				bar = new Bar();
				bar.border = new ColorBlock(BAR_W + 2, BAR_H + 2, 0xFF000000);
				bar.bg = new ColorBlock(BAR_W, BAR_H, 0xFF3A3A3A);
				bar.fill = new ColorBlock(BAR_W, BAR_H, 0xFF44CC44);
				bar.flash = new ColorBlock(BAR_W, BAR_H, 0xFFFFFFFF);
				bar.mana = new ColorBlock(BAR_W, 2, 0xFF3FA9F5);
				add(bar.border);
				add(bar.bg);
				add(bar.fill);
				add(bar.flash);
				add(bar.mana);
				bars.put(mob, bar);
			}

			float top = headTop - 8f;
			float left = cx - BAR_W / 2f;
			bar.border.x = left - 1;
			bar.border.y = top - 1;
			bar.bg.x = left;
			bar.bg.y = top;
			bar.fill.x = left;
			bar.fill.y = top;

			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));
			bar.fill.width = Math.max(0f, BAR_W * ratio);

			boolean ally = mob.buff(AutochessUnitBuff.class) != null;
			if (ally) {
				// 我方：绿 → 黄 → 红（残血警示）
				if (ratio > 0.6f) bar.fill.color(0xFF44CC44);
				else if (ratio > 0.3f) bar.fill.color(0xFFDDCC22);
				else bar.fill.color(0xFFDD3333);
			} else {
				// 敌方：红色系
				if (ratio > 0.5f) bar.fill.color(0xFFE03030);
				else if (ratio > 0.25f) bar.fill.color(0xFFB01818);
				else bar.fill.color(0xFF7A0A0A);
			}

			// 掉血闪白
			if (bar.lastHp >= 0 && mob.HP < bar.lastHp) bar.flashTimer = FLASH_TIME;
			bar.lastHp = mob.HP;
			if (bar.flashTimer > 0f) {
				bar.flashTimer -= Game.elapsed;
				bar.flash.x = left;
				bar.flash.y = top;
				bar.flash.width = Math.max(0f, BAR_W * ratio);
				bar.flash.visible = true;
				bar.flash.alpha(Math.max(0f, bar.flashTimer / FLASH_TIME) * 0.85f);
			} else {
				bar.flash.visible = false;
			}

			// 技能条：随法力累积变长，放完技能归零
			AutochessUnitBuff allyBuff = mob.buff(AutochessUnitBuff.class);
			AutochessFoeBuff foeBuff = mob.buff(AutochessFoeBuff.class);
			float manaRatio = allyBuff != null ? allyBuff.manaRatio()
					: (foeBuff != null ? foeBuff.manaRatio() : 0f);
			manaRatio = Math.max(0f, Math.min(1f, manaRatio));

			bar.mana.x = left;
			bar.mana.y = top - 4f;
			bar.mana.width = BAR_W * manaRatio;
			bar.mana.visible = manaRatio > 0.02f;
			bar.mana.color(manaRatio >= 0.999f ? 0xFFFFFFFF : 0xFF3FA9F5);
		}
	}

	private void destroy(Bar bar) {
		bar.border.killAndErase();
		bar.bg.killAndErase();
		bar.fill.killAndErase();
		bar.flash.killAndErase();
		bar.mana.killAndErase();
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
