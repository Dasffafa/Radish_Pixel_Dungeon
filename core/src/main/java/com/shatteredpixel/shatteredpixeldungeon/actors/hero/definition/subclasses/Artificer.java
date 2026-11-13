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

	public static boolean readyModify( Hero hero, com.shatteredpixel.shatteredpixeldungeon.items.Item gear, Char ally ) {
		if (hero == null || ally == null || ally == hero || gear == null
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
		// 同一目标只能改装一次，避免重复叠加生命上限
		if (ally.buff( ReadyMod.class ) != null) {
			return false;
		}

		boolean isWeapon = gear instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
		boolean isArmor = gear instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
		if (!isWeapon && !isArmor) {
			return false;
		}
		int tier = isWeapon
				? Math.min(5, Math.max(1, gear.level() + 1))
				: ((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor) gear).tier;
		int level = gear.level();

		int hp = (tier + level) * (pts >= 3 ? 8 : pts >= 2 ? 6 : 4);
		int def = (tier + level) * 2;

		// 只消耗玩家选中的那一件装备
		ReadyMod rm = Buff.affect( ally, ReadyMod.class );
		rm.gearClass = gear.getClass().getName();
		rm.apply( hp, def );
		gear.detach( hero.belongings.backpack );
		return true;
	}

	public static class ReadyMod extends Buff {

		public ReadyMod() {
			type = buffType.POSITIVE;
			announced = false;
		}

		public int bonusHp = 0;
		public int bonusDef = 0;
		public String gearClass = "";

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
			// 友方死亡后，这件装备有 50% 概率掉回地上
			if (target != null && !gearClass.isEmpty() && com.watabou.utils.Random.Float() < 0.5f) {
				try {
					com.shatteredpixel.shatteredpixeldungeon.items.Item gear =
							(com.shatteredpixel.shatteredpixeldungeon.items.Item) Class
									.forName( gearClass ).newInstance();
					com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.drop( gear, target.pos );
				} catch (Throwable ignored) {
				}
			}
			super.detach();
		}

		@Override
		public String icon() {
			return BuffIndicator.UPGRADE;
		}

		private static final String HP = "bonus_hp";
		private static final String DEF = "bonus_def";
		private static final String GEAR = "gear_class";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( HP, bonusHp );
			bundle.put( DEF, bonusDef );
			bundle.put( GEAR, gearClass );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			bonusHp = bundle.getInt( HP );
			bonusDef = bundle.getInt( DEF );
			gearClass = bundle.getString( GEAR );
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

