package com.shatteredpixel.shatteredpixeldungeon.challenges.autochess;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.HashMap;

/**
 * 棋子头顶信息：干净的一条，不叠加。
 *
 * <ul>
 *   <li>备战阶段：只显示星级（1~3 颗小金块）</li>
 *   <li>战斗阶段：只显示血条（16×3）+ 血条下方 1px 法力条</li>
 * </ul>
 *
 * <p>血条掉血时会闪一下白，扣血更明显。</p>
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
		ColorBlock[] stars;
		int lastHp = -1;
		float flashTimer = 0f;
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
		boolean fighting = run.state() == AutochessRun.State.FIGHT;

		// 场上现存棋子
		java.util.HashSet<Mob> present = new java.util.HashSet<>();
		Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
		for (Mob m : mobs) present.add(m);

		// 清理已离场棋子的所有元素
		bars.entrySet().removeIf(e -> {
			Mob mob = e.getKey();
			boolean gone = mob.sprite == null || !mob.sprite.visible
					|| !present.contains(mob) || !AutochessUtil.alive(mob);
			if (gone) destroy(e.getValue());
			return gone;
		});

		for (Mob mob : mobs) {
			boolean ours = mob.buff(AutochessUnitBuff.class) != null
					|| mob.buff(AutochessFoeBuff.class) != null;
			if (mob.sprite == null || !mob.sprite.visible || !ours) {
				// 精灵不可见/不是自走棋棋子：把它的条彻底去掉，避免残留
				Bar stale = bars.remove(mob);
				if (stale != null) destroy(stale);
				continue;
			}

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

				int starCount = Math.max(1, Math.min(3, run.starOf(mob)));
				bar.stars = new ColorBlock[starCount];
				for (int k = 0; k < starCount; k++) {
					ColorBlock star = new ColorBlock(STAR, STAR, 0xFFFFD766);
					add(star);
					bar.stars[k] = star;
				}
				bars.put(mob, bar);
			}

			// 用格子坐标定位：棋子在战斗里不会离开格子，比 sprite 尺寸更稳
			int lw = Dungeon.level.width();
			int lx = mob.pos % lw;
			int ly = mob.pos / lw;
			float cx = lx * 16f + 8f;
			// 用精灵自己的 y 当基准，贴着头顶上方，绝不压住棋子
			float top = (mob.sprite != null ? mob.sprite.y : ly * 16f) - 3f;

			// 星级：只在备战阶段显示，一排居中
			float starW = bar.stars.length * STAR_GAP - 1f;
			for (int k = 0; k < bar.stars.length; k++) {
				bar.stars[k].visible = !fighting;
				bar.stars[k].x = cx - starW / 2f + k * STAR_GAP;
				bar.stars[k].y = top - 5f;
			}

			// 血条 / 法力条：只在战斗显示
			bar.bg.visible = fighting;
			bar.fill.visible = fighting;
			bar.mana.visible = fighting;
			bar.flash.visible = false;
			if (!fighting) {
				bar.lastHp = -1;
				continue;
			}

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
				bar.flashTimer -= com.watabou.noosa.Game.elapsed;
				bar.flash.x = bar.bg.x;
				bar.flash.y = top;
				bar.flash.width = BAR_W;
				bar.flash.visible = true;
				bar.flash.alpha(Math.max(0f, bar.flashTimer / FLASH_TIME) * 0.85f);
			}

			// 法力条
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
		if (bar.stars != null) {
			for (ColorBlock star : bar.stars) star.killAndErase();
		}
	}

	private void hideAll() {
		for (Bar bar : bars.values()) destroy(bar);
		bars.clear();
	}
}
