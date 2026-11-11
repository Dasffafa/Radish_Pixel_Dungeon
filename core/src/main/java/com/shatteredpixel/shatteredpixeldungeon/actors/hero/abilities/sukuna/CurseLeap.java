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
			if (ch != null && ch != hero && ch.alignment == Char.Alignment.ENEMY) {
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
		return ce.energy() >= cost(hero)
				&& hero.buff(CurseLeapCooldown.class) == null
				&& adjacentEnemy(hero) != null;
	}

	public static void use(Hero hero) {
		if (!canUse(hero)) {
			return;
		}
		Char enemy = adjacentEnemy(hero);
		if (enemy == null) {
			return;
		}
		int dest = destination(hero, enemy.pos);
		if (dest < 0) {
			return;
		}

		CursedEnergy ce = hero.buff(CursedEnergy.class);
		ce.spend(cost(hero));
		Buff.affect(hero, CurseLeapCooldown.class, cooldown(hero));

		final int from = hero.pos;
		final int to = dest;
		final Char target = enemy;
		hero.busy();
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(from).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.DUST), 6);
		hero.sprite.jump(from, to, 8f, 0.5f, new Callback() {
			@Override
			public void call() {
				hero.move(to);
				Dungeon.level.occupyCell(hero);
				Dungeon.observe();
				GameScene.updateFog();

				if (target.isAlive()) {
					int dmg = Math.max(1, Math.round(hero.damageRoll()));
					SlashLinesEffect.show(target, 4);
					target.damage(DamageInfo.physical(dmg, hero));
				}
				hero.spendAndNext(1f);
			}
		});
	}

	private static int destination(Hero hero, int enemyPos) {
		int behind = hero.pos + (hero.pos - enemyPos);
		if (canLeapTo(hero, behind, enemyPos)) {
			return behind;
		}
		for (int i : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			int c = hero.pos + i;
			if (canLeapTo(hero, c, enemyPos)) {
				return c;
			}
		}
		return -1;
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
