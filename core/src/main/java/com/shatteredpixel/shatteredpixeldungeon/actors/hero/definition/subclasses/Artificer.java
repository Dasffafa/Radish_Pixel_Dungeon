package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 奇械师：法师的第三转职。
 *
 * 炼金配方能量消耗降低，并获得投掷武器/药水包等新配方；
 * 投掷武器会继承上一次使用的法杖特性。
 */
public class Artificer extends HeroSubClass {

	public Artificer() {
		super("ARTIFICER", HeroIcon.ARTIFICER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.POTION_DISTILLATION, Talent.CIRCUIT_CHAIN, Talent.READY_MODIFICATION };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.WAND_DODGE, Talent.MAGIC_WORKMAN };
	}

	/** 线路串联：武器攻击距离加成（+1/+1/+2）。 */
	public static int attackReachBonus( Char owner ) {
		if (!(owner instanceof Hero)) {
			return 0;
		}
		Hero hero = (Hero) owner;
		if (hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return 0;
		}
		int pts = hero.pointsInTalent( Talent.CIRCUIT_CHAIN );
		if (pts <= 0) {
			return 0;
		}
		return pts >= 3 ? 2 : 1;
	}

	@Override
	public void onAttackProc( Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy ) {
		if (!hit || !wasEnemy || enemy == null) {
			return;
		}
		int pts = hero.pointsInTalent( Talent.CIRCUIT_CHAIN );
		if (pts <= 0) {
			return;
		}
		float cost = pts >= 2 ? 2f : 3f;

		ArrayList<MissileWeapon> missiles = hero.belongings.getAllItems( MissileWeapon.class );
		ArrayList<MissileWeapon> usable = new ArrayList<>();
		for (MissileWeapon m : missiles) {
			if (m != null && m.durabilityLeft() > 0) {
				usable.add( m );
			}
		}
		if (usable.isEmpty()) {
			return;
		}
		MissileWeapon m = Random.element( usable );
		m.repair( -Math.min( cost, m.durabilityLeft() ) );

		// 投掷武器继承上一次使用法杖的特性：附加魔法伤害
		com.shatteredpixel.shatteredpixeldungeon.items.Item w = hero.belongings.attackingWeapon();
		if (w instanceof MissileWeapon) {
			LastWand lw = hero.buff( LastWand.class );
			if (lw != null && !lw.wandClass.isEmpty()) {
				enemy.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo(
						Math.max( 2, 2 + lw.wandLevel ),
						com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL,
						hero, w, this ) );
			}
		}
	}

	// ===== 投掷武器继承上一次法杖的特性 =====

	public static int alchemyDiscount( Hero hero, int cost ) {
		if (hero != null && hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return Math.max( 0, cost - 2 );
		}
		return cost;
	}

	public static void recordWand( Hero hero, com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand ) {
		if (hero == null || wand == null
				|| hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return;
		}
		LastWand lw = Buff.affect( hero, LastWand.class );
		lw.wandClass = wand.getClass().getName();
		lw.wandLevel = wand.level();
	}

	public static class LastWand extends Buff {

		{
			type = buffType.POSITIVE;
			announced = false;
		}

		public String wandClass = "";
		public int wandLevel = 0;

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		private static final String WAND_CLASS = "wand_class";
		private static final String WAND_LEVEL = "wand_level";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( WAND_CLASS, wandClass );
			bundle.put( WAND_LEVEL, wandLevel );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			wandClass = bundle.getString( WAND_CLASS );
			wandLevel = bundle.getInt( WAND_LEVEL );
		}
	}

	// ===== 即用改装：把武器/护甲改装给友方单位 =====

	public static boolean readyModify( Hero hero, Char ally ) {
		if (hero == null || ally == null || ally == hero
				|| hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return false;
		}
		// 友方包括所有非敌对单位（召唤物、宠物、被安抚的中立单位）
		if (ally.alignment == Char.Alignment.ENEMY) {
			return false;
		}
		int pts = hero.pointsInTalent( Talent.READY_MODIFICATION );
		if (pts <= 0) {
			return false;
		}

		int tier = 0, level = 0;
		boolean isWeapon = false;
		com.shatteredpixel.shatteredpixeldungeon.items.Item gear = null;

		// 优先投掷背包里的备用武器
		com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon active =
				hero.belongings.attackingWeapon();
		for (com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon wg
				: new ArrayList<>( hero.belongings.getAllItems( com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.class ) )) {
			if (wg != active && wg != hero.belongings.weapon && wg != hero.belongings.secondWep) {
				gear = wg;
				isWeapon = true;
				tier = Math.min(5, Math.max(1, wg.level() + 1));
				level = wg.level();
				break;
			}
		}
		// 没有备用武器时，允许把当前装备的武器改装出去
		if (gear == null) {
			if (hero.belongings.weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon) {
				com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon wg =
						(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon) hero.belongings.weapon;
				gear = wg;
				isWeapon = true;
				tier = Math.min(5, Math.max(1, wg.level() + 1));
				level = wg.level();
			} else if (hero.belongings.secondWep instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon) {
				com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon wg =
						(com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon) hero.belongings.secondWep;
				gear = wg;
				isWeapon = true;
				tier = Math.min(5, Math.max(1, wg.level() + 1));
				level = wg.level();
			}
		}
		// 再找背包里的备用护甲
		if (gear == null) {
			for (com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor ag
					: new ArrayList<>( hero.belongings.getAllItems( com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.class ) )) {
				if (ag != hero.belongings.armor) {
					gear = ag;
					isWeapon = false;
					tier = ag.tier;
					level = ag.level();
					break;
				}
			}
		}
		// 最后允许把当前装备的护甲改装出去
		if (gear == null && hero.belongings.armor != null) {
			gear = hero.belongings.armor;
			isWeapon = false;
			tier = hero.belongings.armor.tier;
			level = hero.belongings.armor.level();
		}
		if (gear == null) {
			return false;
		}

		int hp = (tier + level) * (pts >= 3 ? 8 : pts >= 2 ? 6 : 4);
		int def = (tier + level) * 2;

		// 从装备槽或背包中移除
		if (gear == hero.belongings.weapon) {
			hero.belongings.weapon = null;
		} else if (gear == hero.belongings.secondWep) {
			hero.belongings.secondWep = null;
		} else if (gear == hero.belongings.armor) {
			hero.belongings.armor = null;
		}
		gear.detach( hero.belongings.backpack );

		// 直接装备到友方单位上，让它真正获得攻击/防御
		if (ally instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob =
					(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) ally;
			if (isWeapon && gear instanceof com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon) {
				mob.mobEquipment.weapon = (com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon) gear;
			} else if (!isWeapon && gear instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor) {
				mob.mobEquipment.armor = (com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor) gear;
			}
			if (gear instanceof com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem) {
				((com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem) gear).activate( ally );
			}
		}

		Buff.affect( ally, ReadyMod.class ).apply( hp, def );
		return true;
	}

	public static class ReadyMod extends Buff {

		public ReadyMod() {
			type = buffType.POSITIVE;
			announced = false;
		}

		public int bonusHp = 0;
		public int bonusDef = 0;

		public void apply( int hp, int def ) {
			unapply();
			bonusHp = hp;
			bonusDef = def;
			if (target != null) {
				target.HT += hp;
				target.HP += hp;
				if (target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).defenseSkill += def;
				}
			}
		}

		private void unapply() {
			if (target != null && (bonusHp != 0 || bonusDef != 0)) {
				target.HT -= bonusHp;
				target.HP = Math.min( target.HP, target.HT );
				if (target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).defenseSkill -= bonusDef;
				}
			}
		}

		@Override
		public void detach() {
			unapply();
			super.detach();
		}

		@Override
		public String icon() {
			return BuffIndicator.UPGRADE;
		}

		private static final String HP = "bonus_hp";
		private static final String DEF = "bonus_def";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( HP, bonusHp );
			bundle.put( DEF, bonusDef );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			bonusHp = bundle.getInt( HP );
			bonusDef = bundle.getInt( DEF );
		}
	}

	// ===== 药剂蒸馏冷却 =====

	public static boolean canDistill( Hero hero ) {
		return hero != null
				&& hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER
				&& hero.pointsInTalent( Talent.POTION_DISTILLATION ) > 0
				&& hero.buff( DistillCooldown.class ) == null;
	}

	public static void startDistillCooldown( Hero hero ) {
		int pts = hero.pointsInTalent( Talent.POTION_DISTILLATION );
		float cd = pts >= 3 ? 150f : (pts >= 2 ? 225f : 300f);
		Buff.affect( hero, DistillCooldown.class, cd );
	}

	public static class DistillCooldown extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}
	}
}

