package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 棋子头顶信息：星级常显，血条/法力条只在战斗显示。
 *
 * <p>星级和血条分开管理：星级一直是同一批方块，血条在备战阶段整体销毁，
 * 不会出现「战斗结束还挂着一根绿条」的残留。</p>
 */
public class AutochessHealthBars extends Component {

	private static final int BAR_W = 12;
	private static final int BAR_H = 2;
	private static final int STAR = 3;
	private static final int STAR_GAP = 4;

	private static final float FLASH_TIME = 0.28f;

	private static final class Bar {
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

		// 备战：血条整批销毁（避免任何残留）
		if (!fighting) {
			clearBars();
		}

		Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		for (Mob m : mobs) present.add(m);

		// 清理已离场棋子
		stars.entrySet().removeIf(e -> {
			Mob mob = e.getKey();
			boolean gone = mob.sprite == null || !present.contains(mob) || !AutochessUtil.alive(mob);
			if (gone) {
				for (ColorBlock c : e.getValue()) c.killAndErase();
			}
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

			// ---------------- 星级（常显） ----------------
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

			float cx = mob.sprite.center().x;
			float headTop = mob.sprite.y;
			float starW = starArr.length * STAR_GAP - 1f;
			for (int k = 0; k < starArr.length; k++) {
				starArr[k].x = cx - starW / 2f + k * STAR_GAP;
				starArr[k].y = headTop - 11f;
				starArr[k].visible = true;
			}

			if (!fighting) continue;

			// ---------------- 血条 / 法力条（仅战斗） ----------------
			Bar bar = bars.get(mob);
			if (bar == null) {
				bar = new Bar();
				bar.bg = new ColorBlock(BAR_W, BAR_H, 0xFF551111);
				bar.fill = new ColorBlock(BAR_W, BAR_H, 0xFF66DD66);
				bar.flash = new ColorBlock(BAR_W, BAR_H, 0xFFFFFFFF);
				bar.mana = new ColorBlock(BAR_W, 1, 0xFF66CCFF);
				add(bar.bg);
				add(bar.fill);
				add(bar.flash);
				add(bar.mana);
				bars.put(mob, bar);
			}

			float top = headTop - 6f;
			bar.bg.x = cx - BAR_W / 2f;
			bar.bg.y = top;
			bar.fill.x = bar.bg.x;
			bar.fill.y = top;
			bar.mana.x = bar.bg.x;
			bar.mana.y = top + BAR_H + 1;

			float ratio = Math.max(0f, Math.min(1f, mob.HP / (float) Math.max(1, mob.HT)));
			bar.fill.width = Math.max(1f, BAR_W * ratio);
			if (ratio > 0.6f) bar.fill.color(0xFF66DD66);
			else if (ratio > 0.3f) bar.fill.color(0xFFDDCC44);
			else bar.fill.color(0xFFDD4444);

			// 掉血闪白
			if (bar.lastHp >= 0 && mob.HP < bar.lastHp) {
				bar.flashTimer = FLASH_TIME;
			}
			bar.lastHp = mob.HP;
			if (bar.flashTimer > 0f) {
				bar.flashTimer -= Game.elapsed;
				bar.flash.x = bar.bg.x;
				bar.flash.y = top;
				bar.flash.width = BAR_W;
				bar.flash.visible = true;
				bar.flash.alpha(Math.max(0f, bar.flashTimer / FLASH_TIME) * 0.85f);
			} else {
				bar.flash.visible = false;
			}

			AutochessUnitBuff allyBuff = mob.buff(AutochessUnitBuff.class);
			AutochessFoeBuff foeBuff = mob.buff(AutochessFoeBuff.class);
			float manaRatio = allyBuff != null ? allyBuff.manaRatio()
					: (foeBuff != null ? foeBuff.manaRatio() : 0f);
			manaRatio = Math.max(0f, Math.min(1f, manaRatio));
			bar.mana.width = Math.max(0f, BAR_W * manaRatio);
			bar.mana.color(manaRatio >= 0.999f ? 0xFFFFFFFF : 0xFF66CCFF);
		}
	}

	private void destroy(Bar bar) {
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
