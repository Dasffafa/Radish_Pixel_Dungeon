package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CurseLeapCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.SlashLinesEffect;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;

/** 后跃斩：主动向后退一格，并对相邻敌人斩击。 */
public class CurseLeap {

	public static int cost(Hero hero) {
		return hero.pointsInTalent(Talent.SUKUNA_LOW_HP) >= 2 ? 20 : 30;
	}

	public static float cooldown(Hero hero) {
		return hero.pointsInTalent(Talent.SUKUNA_LOW_HP) >= 2 ? 5f : 10f;
	}

	public static Char adjacentEnemy(Hero hero) {
		for (int i : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			int c = hero.pos + i;
			if (c < 0 || c >= Dungeon.level.length()) {
				continue;
			}
			Char ch = Actor.findChar(c);
			if (ch != null && ch != hero && ch.alignment != Char.Alignment.ALLY) {
				return ch;
			}
		}
		return null;
	}

	public static boolean canUse(Hero hero) {
		if (hero.pointsInTalent(Talent.SUKUNA_LOW_HP) <= 0) {
			return false;
		}
		CursedEnergy ce = hero.buff(CursedEnergy.class);
		if (ce == null) {
			ce = Buff.affect(hero, CursedEnergy.class);
		}

		CurseLeapCooldown cd = hero.buff(CurseLeapCooldown.class);
		if (cd != null && cd.turns() <= 0) {
			cd.detach();
			cd = null;
		}

		return ce.energy() >= cost(hero)
				&& cd == null
				&& adjacentEnemy(hero) != null;
	}

	public static void use(Hero hero) {
		if (hero.pointsInTalent(Talent.SUKUNA_LOW_HP) <= 0) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("尚未学会后跃斩。");
			return;
		}
		CursedEnergy energy = hero.buff(CursedEnergy.class);
		if (energy == null) {
			energy = Buff.affect(hero, CursedEnergy.class);
		}
		if (energy.energy() < cost(hero)) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("咒力不足（需要 " + cost(hero) + "）。");
			return;
		}
		CurseLeapCooldown cdCheck = hero.buff(CurseLeapCooldown.class);
		if (cdCheck != null && cdCheck.turns() > 0) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("后跃斩冷却中，剩余 "
					+ cdCheck.turns() + " 回合。");
			return;
		}
		if (adjacentEnemy(hero) == null) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("周围没有敌人。");
			return;
		}
		if (!canUse(hero)) {
			return;
		}
		Char enemy = adjacentEnemy(hero);
		if (enemy == null) {
			return;
		}
		int dest = destination(hero, enemy.pos);
		if (dest < 0) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("周围没有可以后跃的安全位置。");
			return;
		}

		CursedEnergy ce = hero.buff(CursedEnergy.class);
		ce.spend(cost(hero));
		Buff.affect(hero, CurseLeapCooldown.class)
				.set((int) cooldown(hero));

		final int from = hero.pos;
		final int to = dest;

		//闪现：原地与落点各爆一圈光，瞬间位移
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(from).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.LIGHT), 10);

		//直接改写坐标并刷新精灵，确保是真位移而不是原地动画
		hero.pos = to;
		if (hero.sprite != null) {
			hero.sprite.place(to);
			hero.sprite.idle();
		}
		Dungeon.level.occupyCell(hero);
		Dungeon.observe();
		GameScene.updateFog();

		hero.sprite.flash();
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(to).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.LIGHT), 10);

		//伤害按一次普通攻击计算
		if (enemy.isAlive()) {
			//与普通攻击一致：武器骰 → 攻击结算（附魔/天赋）→ 命中
			int dmg = Math.max(1, hero.damageRoll());
			dmg = hero.attackProc(enemy, dmg);
			SlashLinesEffect.show(enemy, 4);
			enemy.damage(DamageInfo.physical(dmg, hero));
		}

		hero.spendAndNext(1f);
		hero.ready();
	}

	private static int destination(Hero hero, int enemyPos) {
		//优先闪现到敌人正后方 2 格
		int away = hero.pos - enemyPos;
		int far = hero.pos + away * 2;
		if (canLeapTo(hero, far, enemyPos)) {
			return far;
		}

		//其次退一格
		int behind = hero.pos + away;
		if (canLeapTo(hero, behind, enemyPos)) {
			return behind;
		}

		//否则在周围挑一个「离敌人最远、且方向背离敌人」的安全格
		int w = Dungeon.level.width();
		int awayX = (hero.pos % w) - (enemyPos % w);
		int awayY = (hero.pos / w) - (enemyPos / w);

		int best = -1;
		int bestScore = Integer.MIN_VALUE;
		for (int i : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			int c = hero.pos + i;
			if (!canLeapTo(hero, c, enemyPos)) {
				continue;
			}
			int dist = Dungeon.level.distance(c, enemyPos);
			int dx = (c % w) - (hero.pos % w);
			int dy = (c / w) - (hero.pos / w);
			int align = dx * awayX + dy * awayY;

			int score = dist * 10 + align;
			if (score > bestScore) {
				bestScore = score;
				best = c;
			}
		}
		return best;
	}

	private static boolean canLeapTo(Hero hero, int cell, int enemyPos) {
		if (cell < 0 || cell >= Dungeon.level.length() || cell == hero.pos || cell == enemyPos) {
			return false;
		}
		if (!Dungeon.level.passable[cell] || Dungeon.level.solid[cell]) {
			return false;
		}
		return Actor.findChar(cell) == null;
	}
}
