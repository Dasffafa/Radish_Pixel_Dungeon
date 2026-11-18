package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;

/**
 * 竞技场的自动开火：每过一个「射击间隔」，自动朝最近的可见怪物打一发。
 *
 * <p>近战手感靠走过去撞，这里补的是「边走边射」——用英雄身上的法杖原样发射，
 * 但不走 {@link Wand#wandUsed()} 那条充能结算，所以不消耗充能，纯属挑战模式特权。</p>
 *
 * <p>没有法杖时就打一发内置能量弹，伤害按当前武器的一半算，保证任何英雄都射得出来。</p>
 */
public class ArenaAutoFire {

	/** 自动索敌半径（格）。 */
	public static final int RANGE = 8;

	/** 尝试开火；打到人返回 true，没有目标或打不出来返回 false。 */
	public static boolean fire(Hero hero) {
		if (hero == null || Dungeon.level == null || !hero.isAlive()) {
			return false;
		}
		Mob target = nearestTarget(hero);
		if (target == null) {
			return false;
		}
		Wand wand = pickWand(hero);
		if (wand != null && cast(hero, wand, target)) {
			return true;
		}
		return fallbackBolt(hero, target);
	}

	/** 最近的、看得见的敌对怪物。 */
	private static Mob nearestTarget(Hero hero) {
		if (Dungeon.level == null) {
			return null;
		}
		Mob best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (!mob.isAlive() || mob.alignment != Char.Alignment.ENEMY) {
				continue;
			}
			int dist = Dungeon.level.distance(hero.pos, mob.pos);
			if (dist > RANGE || dist >= bestDist) {
				continue;
			}
			// 看不见就别隔墙自动开火，否则会一直放空炮
			if (mob.pos >= hero.fieldOfView.length || !hero.fieldOfView[mob.pos]) {
				continue;
			}
			best = mob;
			bestDist = dist;
		}
		return best;
	}

	/** 优先用杖里嵌的法杖，其次背包里任意一根法杖。 */
	private static Wand pickWand(Hero hero) {
		if (hero.belongings == null) {
			return null;
		}
		if (hero.belongings.weapon() instanceof MagesStaff) {
			Wand embedded = ((MagesStaff) hero.belongings.weapon()).wand;
			if (embedded != null) {
				return embedded;
			}
		}
		return hero.belongings.getItem(Wand.class);
	}

	private static boolean cast(Hero hero, Wand wand, Mob target) {
		if (hero.sprite == null || hero.sprite.parent == null) {
			return false;
		}

		final Ballistica bolt = new Ballistica(hero.pos, target.pos, wand.collisionProperties(target.pos));
		final Char prevUser = Wand.curUser;
		final Item prevItem = Wand.curItem;

		Wand.curUser = hero;
		Wand.curItem = wand;

		try {
			wand.fx(bolt, new Callback() {
				@Override
				public void call() {
					try {
						wand.onZap(bolt);
					} catch (Exception ignored) {
						// 个别法杖依赖完整施法流程，失败就当作这发没打出去
					} finally {
						Wand.curUser = prevUser;
						Wand.curItem = prevItem;
					}
				}
			});
		} catch (Exception e) {
			Wand.curUser = prevUser;
			Wand.curItem = prevItem;
			return false;
		}
		return true;
	}

	/** 没杖就甩一发能量弹，伤害减半，算是保底输出。 */
	private static boolean fallbackBolt(Hero hero, final Mob target) {
		if (hero.sprite == null || hero.sprite.parent == null) {
			return false;
		}
		final DamageInfo info = new DamageInfo(
				Math.max(1, hero.damageRoll() / 2), DamageType.MAGICAL, hero);

		MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.MAGIC_MISSILE,
				hero.sprite, target.pos, new Callback() {
					@Override
					public void call() {
						Char ch = Actor.findChar(target.pos);
						if (ch != null && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()) {
							ch.damage(info);
						}
					}
				});
		return true;
	}
}
