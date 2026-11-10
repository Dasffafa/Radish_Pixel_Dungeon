package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/** 宿傩专属武器，随角色等级成长而非卷轴强化，无法卸下。 */
public class SukunaClaws extends MeleeWeapon {

	public static final String AC_SWITCH = "SWITCH";

	private boolean ranged = false;

	{
		image = ItemSpriteSheet.SUKUNA_CLAWS;
		tier = 1;
	}

	public boolean isRanged() {
		return ranged;
	}

	public void setRanged(boolean value) {
		ranged = value;
	}

	public void toggleStance() {
		ranged = !ranged;
		GLog.i(Messages.get(this, ranged ? "to_ranged" : "to_melee"));
	}

	public int clawLevel() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return 0;
		}
		return Math.min(15, hero.lvl / 2);
	}

	public int reach() {
		if (!ranged) {
			return 1;
		}
		int base = 2 + clawLevel();
		Hero hero = Dungeon.hero;
		if (hero != null && hero.subClass == HeroSubClasses.QIANREN) {
			base += 2;
		}
		if (hero != null) {
			base += hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SUKUNA_REACH);
			base += hero.pointsInTalent(
					com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.QIANREN_REACH);
			base += hero.pointsInTalent(
					com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SUKUNA_CLEAVE);
		}
		return base;
	}

	@Override
	public int min(int lvl) {
		return 3 + clawLevel();
	}

	@Override
	public int max(int lvl) {
		return 5 + clawLevel();
	}

	@Override
	public int level() {
		return clawLevel();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int STRReq(int lvl) {
		return 10;
	}

	@Override
	public java.util.ArrayList<String> actions(Hero hero) {
		java.util.ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SWITCH);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_SWITCH.equals(action)) {
			ranged = !ranged;
			GLog.i(Messages.get(this, ranged ? "to_ranged" : "to_melee"));
			com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();
			hero.ready();
		}
	}

	@Override
	public int damageRoll(Char owner) {
		//走标准近战结算，正常获得力量溢出加成
		int dmg = super.damageRoll(owner);
		if (owner instanceof Hero) {
			Hero h = (Hero) owner;
			if (owner.HP >= owner.HT) {
				dmg = Math.round(dmg * (1f + 0.05f * h.pointsInTalent(
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.BLOODTHIRST)));
			}
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodlust lust =
					h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodlust.class);
			if (lust != null) {
				dmg = Math.round(dmg * (1f + 0.03f * lust.stacks()
						* h.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.BLOODLUST)));
			}
			dmg = Math.round(dmg * (1f + 0.06f * h.pointsInTalent(
					com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SUKUNA_CLAW_MASTERY)));
			if (owner.HP < owner.HT * 0.3f) {
				dmg = Math.round(dmg * (1.15f + 0.08f * h.pointsInTalent(
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SUKUNA_LOW_HP)));
			}
		}
		return dmg;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		GLog.w(Messages.get(this, "bound"));
		return false;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		Hero hero = attacker instanceof Hero ? (Hero) attacker : null;
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedEnergy energy =
				hero == null ? null : hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedEnergy.class);

		boolean slashing = ranged;
		if (slashing && energy != null) {
			if (energy.energy() < 10) {
				//咒力不足，自动切回近战
				ranged = false;
				slashing = false;
				GLog.w(Messages.get(this, "no_energy"));
			} else {
				energy.spend(10);
				com.shatteredpixel.shatteredpixeldungeon.effects.SukunaSlashEffect.show(attacker, defender);
				if (energy.energy() <= 0) {
					ranged = false;
					GLog.w(Messages.get(this, "no_energy"));
				}
				com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();
			}
		}

		int result = super.proc(attacker, defender, damage);
		boolean condemn = hero != null && hero.subClass == HeroSubClasses.DUANZUI;

		if (hero != null && defender.isAlive() && hero.pointsInTalent(
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.COMBAT_INSTINCT) > 0
				&& Random.Float() < 0.08f * hero.pointsInTalent(
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.COMBAT_INSTINCT)) {
			defender.damage(DamageInfo.physical(Math.round(damage * 0.5f), attacker));
		}

		if (condemn && defender.isAlive()) {
			float bonus = 0.5f;
			if (hero != null) {
				bonus += 0.15f * hero.pointsInTalent(
						com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.DUANZUI_EXECUTE);
				if (defender.HP >= defender.HT) {
					bonus += 0.12f * hero.pointsInTalent(
							com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.DUANZUI_JUDGE);
				}
				if (defender.HP < defender.HT * 0.3f) {
					bonus += 0.25f * hero.pointsInTalent(
							com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.DUANZUI_LINE);
				}
			}
			defender.damage(DamageInfo.physical(Math.round(damage * bonus), attacker));
		}

		//千刃专精：横扫范围内所有目标
		if (hero != null && hero.subClass == HeroSubClasses.QIANREN && attacker.isAlive()) {
			for (Char ch : Actor.chars()) {
				if (ch == attacker || ch == defender || !ch.isAlive()) {
					continue;
				}
				if (Dungeon.level.distance(attacker.pos, ch.pos) <= reach()) {
					int dmg = Random.NormalIntRange(min(0), max(0));
					if (hero != null) {
						dmg += 2 * hero.pointsInTalent(
								com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.QIANREN_STORM);
					}
					if (condemn) {
						dmg = Math.round(dmg * 0.5f);
					} else if (hero != null) {
						dmg = Math.round(dmg * (1f + 0.05f * hero.pointsInTalent(
								com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.QIANREN_ECHO)
								+ 0.05f * hero.pointsInTalent(
								com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SUKUNA_CLEAVE)));
					}
					ch.damage(DamageInfo.physical(dmg, attacker));
				}
			}
		}

		//近战形态 5% 概率触发黑闪
		if (!slashing && hero != null && defender.isAlive() && Random.Float() < 0.05f) {
			int base = Math.max(1, result);
			int blackFlash = (int) Math.round(Math.pow(base, 2.5));
			showBlackFlash(attacker, defender);
			if (energy != null) {
				energy.restoreAll();
			}
			GLog.p(Messages.get(this, "black_flash"));
			return blackFlash;
		}

		return result;
	}

	private void showBlackFlash(Char attacker, Char defender) {
		com.shatteredpixel.shatteredpixeldungeon.effects.BlackFlashEffect.show(defender);
	}

	private static final String RANGED = "ranged";

	@Override
	public void storeInBundle(com.watabou.utils.Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(RANGED, ranged);
	}

	@Override
	public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
		super.restoreFromBundle(bundle);
		ranged = bundle.getBoolean(RANGED);
	}
}