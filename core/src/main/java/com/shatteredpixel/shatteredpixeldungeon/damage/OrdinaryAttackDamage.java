package com.shatteredpixel.shatteredpixeldungeon.damage;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.FatedDraw;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Torturer;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Radish;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Bloodblade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CelestialSphere;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GiantKiller;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.LongStick;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Seekingspear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class OrdinaryAttackDamage {

	private OrdinaryAttackDamage() {
	}

	public static DamageInfo build(Char attacker, Char defender, int baseDamage, boolean critical,
								   float criticalMultiplier, float damageMultiplier, float damageBonus) {
		DamageInfo info = new DamageInfo(baseDamage,
				ignoresArmor(attacker, defender) ? DamageType.PHYSICAL_NO_ARMOR : DamageType.PHYSICAL,
				attacker, sourceItem(attacker), attacker);
		info.setCritical(critical, criticalMultiplier);
		info.addDirectMultModifier(damageMultiplier, "attack multiplier");

		applyOutgoingModifiers(attacker, defender, info);
		applyPreFinalModifiers(attacker, defender, info);
		info.addPreFinalAddModifier(damageBonus, "attack bonus");
		applyFinalModifiers(attacker, defender, info);

		return info;
	}

	public static int rollDefenseReduction(Char attacker, Char defender) {
		return rollDefenseReduction(attacker, defender, false);
	}

	public static int rollDefenseReduction(Char attacker, Char defender, boolean includeBarkskin) {
		int dr = Math.round(defender.drRoll() * AscensionChallenge.statModifier(defender));

		if (defender instanceof Hero) {
			FatedDraw.FatedDrawTracker trackerD = ((Hero) defender).buff(FatedDraw.FatedDrawTracker.class);
			if (trackerD != null && trackerD.remainingChecks > 0) {
				trackerD.consume("defense_block");
			}
		}

		if (attacker instanceof Hero) {
			Hero h = (Hero) attacker;
			if (h.belongings.weapon() instanceof MissileWeapon
					&& h.subClass == HeroSubClass.SNIPER
					&& !Dungeon.level.adjacent(h.pos, defender.pos)) {
				dr = 0;
			}

			if (h.pointsInTalent(Talent.LAND_HEART) >= 3 && nearbyGrass(attacker.pos)) {
				dr = 0;
			}
		}

		if (includeBarkskin) {
			Barkskin bark = defender.buff(Barkskin.class);
			if (bark != null) {
				dr += Random.NormalIntRange(0, bark.level());
			}
		}

		return dr;
	}

	public static DamageRoll rollBaseDamage(Char attacker) {
		Preparation prep = attacker.buff(Preparation.class);
		float damage;
		if (prep != null) {
			damage = prep.damageRoll(attacker);
			if (attacker == hero) {
				if (hero.hasTalent(Talent.BOUNTY_HUNTER)) {
					Buff.affect(hero, Talent.BountyHunterTracker.class, 0.0f);
				}
				if (hero.hasTalent(Talent.POWER_RECYCLE)) {
					Buff.affect(attacker, Talent.PowerRecycleTracker.class, 0.0f);
				}
			}
		} else {
			damage = attacker.damageRoll();
			if (attacker instanceof Hero) {
				FatedDraw.FatedDrawTracker trackerA = ((Hero) attacker).buff(FatedDraw.FatedDrawTracker.class);
				if (trackerA != null && trackerA.remainingChecks > 0) {
					trackerA.consume("attack_damage");
				}
			}
			if (attacker == hero && hero.hasTalent(Talent.POWER_RECYCLE)
					&& hero.pointsInTalent(Talent.POWER_RECYCLE) == 4
					&& Random.Int(2) == 0) {
				Buff.affect(attacker, Talent.PowerRecycleTracker.class, 0.0f);
			}
		}
		return new DamageRoll(damage, prep);
	}

	public static CriticalRoll rollCritical(Char attacker, Char defender, float baseDamage) {
		boolean critical = false;
		boolean surprise = defender instanceof Mob && ((Mob) defender).surprisedBy(attacker);
		float chance = attacker.baseCritSkill();
		float multiplier = attacker.baseCritDamage();
		float damage = baseDamage;

		if (attacker == hero) {
			if (hero.belongings.weapon() instanceof LongStick) {
				chance += hero.defenseSkill(hero);
			} else if (hero.belongings.weapon() instanceof Bloodblade) {
				chance += ((Bloodblade) hero.belongings.weapon).sac;
			} else if (hero.belongings.weapon() instanceof GiantKiller) {
				critical = ((GiantKiller) hero.belongings.weapon).isMustCrit;
			} else if (hero.belongings.weapon() instanceof Seekingspear) {
				Seekingspear ss = (Seekingspear) hero.belongings.weapon;
				multiplier += 0.3f + 0.05f * ss.buffedLvl();
				if (surprise) {
					chance += 25f;
				}
			} else if (hero.belongings.weapon() instanceof MissileWeapon) {
				Talent.HoldBreathTracker hb = attacker.buff(Talent.HoldBreathTracker.class);
				if (hb != null) {
					chance += hb.crit_b;
					multiplier += hb.cd_b;
				}
			}

			Radish.GlobalCritChance globalCritChance = hero.buff(Radish.GlobalCritChance.class);

			if (hero.hasTalent(Talent.DEATHBLOW)) {
				chance += 15f;
			}
			if (globalCritChance != null) {
				chance += globalCritChance.critChance;
			}
		}

		if (!(attacker.buff(Calm.class) != null || attacker.buff(CriticalAttack.class) != null)) {
			multiplier = Math.min(multiplier, attacker.critDamageCap());
		}
		if (attacker.buff(Scythe.scytheSac.class) != null) {
			chance += 10f;
			multiplier += 0.1f;
		}
		if (attacker instanceof Hero && hero.hasTalent(Talent.DEATHBLOW) && surprise
				&& hero.pointsInTalent(Talent.DEATHBLOW) >= 2) {
			multiplier += 0.25f;
			if (hero.pointsInTalent(Talent.DEATHBLOW) == 3) {
				damage *= 1.15f;
			}
		}

		if (attacker.buff(RingOfTenacity.Tenacity.class) != null) {
			chance = 0;
		}

		if (Random.Float() * 100 < chance || critical
				|| (attacker.rawCritDamage() >= 3 && attacker instanceof Hero && hero.buff(CriticalAttack.class) != null)) {
			critical = true;
		}

		return new CriticalRoll(damage, critical, multiplier);
	}

	public static void applyOutgoingModifiers(Char attacker, Char defender, DamageInfo info) {
		AscensionChallenge.modifyOutgoingAttackDamage(attacker, info);
		for (Buff buff : attacker.buffs()) {
			buff.modifyOutgoingAttackDamage(attacker, defender, info);
		}
	}

	public static void applyPreFinalModifiers(Char attacker, Char defender, DamageInfo info) {
		for (Buff buff : attacker.buffs()) {
			buff.modifyPreFinalOutgoingAttackDamage(attacker, defender, info);
		}
	}

	public static void applyFinalModifiers(Char attacker, Char defender, DamageInfo info) {
		for (Buff buff : attacker.buffs()) {
			buff.modifyFinalOutgoingAttackDamage(attacker, defender, info);
		}
		for (Buff buff : defender.buffs()) {
			buff.modifyIncomingAttackDamage(attacker, defender, info);
		}
	}

	public static void applyPlateArmor(Char defender, DamageInfo info) {
		if (defender instanceof Hero && ((Hero) defender).belongings.armor() instanceof PlateArmor) {
			int before = info.getDamage();
			info.addFinalAddModifier(((PlateArmor) ((Hero) defender).belongings.armor()).damageReduce(before) - before, "plate armor");
		}
	}

	/**
	 * 攻击后处理：defenseProc → 粘性 → 弱点 → attackProc → 诗。
	 * 护甲 DR 已移入 DamagePipeline 的「应用护甲」阶段。
	 */
	public static int foldPostProcessing(Char attacker, Char defender, DamageInfo info) {
		int effectiveDamage = defender.defenseProc(attacker, info.getDamage());

		if (defender.buff(Viscosity.ViscosityTracker.class) != null) {
			effectiveDamage = defender.buff(Viscosity.ViscosityTracker.class).deferDamage(effectiveDamage);
			defender.buff(Viscosity.ViscosityTracker.class).detach();
		}

		if (defender.buff(Vulnerable.class) != null) {
			effectiveDamage *= 1.33f;
		}

		effectiveDamage = attacker.attackProc(defender, effectiveDamage);

		if (attacker == hero) {
			PoemBuff poem = hero.buff(PoemBuff.class);
			if (poem != null) {
				effectiveDamage = poem.applyFinalDamage(hero, defender, effectiveDamage);
			}
		}

		info.addFinalAddModifier(effectiveDamage - info.getDamage(), "attack post-processing");
		return effectiveDamage;
	}

	private static boolean ignoresArmor(Char attacker, Char defender) {
		return attacker == Dungeon.hero
				&& Dungeon.hero.subClass == HeroSubClass.SNIPER
				&& !Dungeon.level.adjacent(Dungeon.hero.pos, defender.pos)
				&& Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon;
	}

	public static boolean ignoresDefenseRoll(Char attacker) {
		boolean srcIsAHeroWieldingCS = attacker instanceof Hero
				&& ((Hero) attacker).belongings.attackingWeapon() instanceof CelestialSphere;
		boolean srcIsAStatueWieldingCS = attacker instanceof Statue
				&& ((Statue) attacker).weapon instanceof CelestialSphere;
		return attacker instanceof Torturer || srcIsAHeroWieldingCS || srcIsAStatueWieldingCS;
	}

	private static Item sourceItem(Char attacker) {
		if (attacker instanceof Hero) {
			return ((Hero) attacker).belongings.attackingWeapon();
		}
		if (attacker instanceof Statue) {
			return ((Statue) attacker).weapon;
		}
		return null;
	}

	private static boolean nearbyGrass(int pos) {
		Point c = Dungeon.level.cellToPoint(pos);
		for (int y = Math.max(0, c.y - 1); y <= Math.min(Dungeon.level.height() - 1, c.y + 1); y++) {
			int left = Math.max(0, c.x - 1);
			int right = Math.min(Dungeon.level.width() - 1, c.x + 1);
			for (int curr = left + y * Dungeon.level.width(); curr <= right + y * Dungeon.level.width(); curr++) {
				if (Dungeon.level.map[curr] == Terrain.FURROWED_GRASS || Dungeon.level.map[curr] == Terrain.HIGH_GRASS) {
					return true;
				}
			}
		}
		return false;
	}

	public static class DamageRoll {
		public final float damage;
		public final Preparation preparation;

		private DamageRoll(float damage, Preparation preparation) {
			this.damage = damage;
			this.preparation = preparation;
		}
	}

	public static class CriticalRoll {
		public final float damage;
		public final boolean critical;
		public final float multiplier;

		private CriticalRoll(float damage, boolean critical, float multiplier) {
			this.damage = damage;
			this.critical = critical;
			this.multiplier = multiplier;
		}
	}
}
