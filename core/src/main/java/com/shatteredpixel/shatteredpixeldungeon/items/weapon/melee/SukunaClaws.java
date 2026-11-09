package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.SwordSlashEffect;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 宿傩的专属武器：利爪。
 *
 * 无法用升级卷轴升级，而是随宿傩的等级成长（每 2 级 +1 武器等级，上限 15）。
 * 基础伤害 2-4，每级 +2 伤害与 +1 攻击距离。
 * 8 级之后可以攻击范围内所有目标。
 * 无法卸下，且只有宿傩开局携带。
 */
public class SukunaClaws extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SUKUNA_CLAWS;
		tier = 0;
	}

	/** 当前武器等级：随角色等级成长，每 2 级 +1，上限 15 */
	public int clawLevel() {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return 0;
		}
		return Math.min(15, hero.lvl / 2);
	}

	/** 攻击距离（格） */
	public int reach() {
		return 2 + clawLevel();
	}

	@Override
	public int min(int lvl) {
		return 2 + 2 * clawLevel();
	}

	@Override
	public int max(int lvl) {
		return 4 + 2 * clawLevel();
	}

	/** 不需要任何力量即可使用 */
	@Override
	public int STRReq(int lvl) {
		return 0;
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
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		GLog.w(Messages.get(this, "bound"));
		return false;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		//红色斩击特效飞向目标
		SwordSlashEffect.showOn(attacker, defender);

		int result = super.proc(attacker, defender, damage);

		//8 级之后：横扫范围内所有敌人
		if (clawLevel() >= 8 && attacker != null && attacker.isAlive()) {
			for (Char ch : Actor.chars()) {
				if (ch == attacker || ch == defender || !ch.isAlive()) {
					continue;
				}
				if (Dungeon.level.distance(attacker.pos, ch.pos) <= reach()) {
					int dmg = Random.NormalIntRange(min(0), max(0));
					ch.damage(com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo.physical(dmg, attacker));
				}
			}
		}
		return result;
	}
}