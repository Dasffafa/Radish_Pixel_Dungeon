package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.SukunaSlashEffect;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/** 宿傩专属武器，随角色等级成长而非卷轴强化，无法卸下。 */
public class SukunaClaws extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SUKUNA_CLAWS;
		tier = 0;
	}

	public int clawLevel() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return 0;
		}
		return Math.min(15, hero.lvl / 2);
	}

	public int reach() {
		int base = 2 + clawLevel();
		Hero hero = Dungeon.hero;
		if (hero != null && hero.subClass == HeroSubClasses.QIANREN) {
			base += 2;
		}
		return base;
	}

	@Override
	public int min(int lvl) {
		return 2 + 2 * clawLevel();
	}

	@Override
	public int max(int lvl) {
		return 4 + 2 * clawLevel();
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
		return 0;
	}

	//不吃力量溢出加成，保持固定成长曲线
	@Override
	public int damageRoll(Char owner) {
		int dmg = augment.damageFactor(Random.NormalIntRange(min(0), max(0)));
		if (owner.HP < owner.HT * 0.3f) {
			dmg = Math.round(dmg * 1.15f);
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
		SukunaSlashEffect.show(attacker, defender);

		int result = super.proc(attacker, defender, damage);

		Hero hero = attacker instanceof Hero ? (Hero) attacker : null;
		boolean condemn = hero != null && hero.subClass == HeroSubClasses.DUANZUI;

		if (condemn && defender.isAlive()) {
			defender.damage(DamageInfo.physical(Math.round(damage * 0.5f), attacker));
		}

		//8 级起横扫范围内所有目标
		if (clawLevel() >= 8 && attacker.isAlive()) {
			for (Char ch : Actor.chars()) {
				if (ch == attacker || ch == defender || !ch.isAlive()) {
					continue;
				}
				if (Dungeon.level.distance(attacker.pos, ch.pos) <= reach()) {
					com.shatteredpixel.shatteredpixeldungeon.effects.SukunaSlashEffect.show(attacker, ch);
					int dmg = Random.NormalIntRange(min(0), max(0));
					if (condemn) {
						dmg = Math.round(dmg * 0.5f);
					}
					ch.damage(DamageInfo.physical(dmg, attacker));
				}
			}
		}

		if (hero != null && !defender.isAlive()) {
			int heal = hero.subClass == HeroSubClasses.HUISHENG ? 2 : 1;
			hero.HP = Math.min(hero.HT, hero.HP + heal);
		}

		return result;
	}
}