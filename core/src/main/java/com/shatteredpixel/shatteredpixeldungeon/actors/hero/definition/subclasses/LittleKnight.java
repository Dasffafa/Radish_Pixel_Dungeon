package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.events.HitCheckEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BladeShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EndGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatshield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RoundShield;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class LittleKnight extends HeroSubClass {

	public LittleKnight(){
		super("LITTLE_KNIGHT", HeroIcon.LITTLE_KNIGHT);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.WONT_LOSE, Talent.WET_ENCHANT, Talent.LEFT_BOW_RAPID };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.SHIELD_POKE, Talent.KNIGHT_SPIRIT };
	}

	@Override
	public void onAttackProc( Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy ) {
		int pts = hero.pointsInTalent( Talent.KNIGHT_SPIRIT );
		if (pts > 0 && hit && wasEnemy && enemy != null && enemy.isAlive()) {
			Buff.affect( hero, KnightSpirit.class ).set( enemy, pts );
		}
	}

	/** 盾戳战术：小骑士拿着盾牌类武器近战时，攻击变为必定伏击的戳刺。 */
	public static boolean shieldPoke( Char ch ) {
		if (!(ch instanceof Hero) || ch != Dungeon.hero) return false;
		Hero hero = (Hero) ch;
		if (hero.subClass != HeroSubClasses.LITTLE_KNIGHT || !hero.hasTalent( Talent.SHIELD_POKE )) return false;
		KindOfWeapon w = hero.belongings.attackingWeapon();
		return w instanceof RoundShield || w instanceof Greatshield || w instanceof BladeShield || w instanceof EndGuard;
	}

	/** 戳刺伤害 15~20/25/30/35。 */
	public static int pokeDamage( Hero hero ) {
		return Random.NormalIntRange( 15, 15 + 5 * hero.pointsInTalent( Talent.SHIELD_POKE ) );
	}

	/** 伏击伤害 +5/6/7/8。 */
	public static int ambushBonus( Char attacker ) {
		if (!(attacker instanceof Hero) || ((Hero) attacker).subClass != HeroSubClasses.LITTLE_KNIGHT) return 0;
		int pts = ((Hero) attacker).pointsInTalent( Talent.SHIELD_POKE );
		return pts > 0 ? 4 + pts : 0;
	}

	/** +4 之前戳刺不继承附魔、不吃攻速加成。 */
	public static boolean pokeIsPlain( Char ch ) {
		return shieldPoke( ch ) && ((Hero) ch).pointsInTalent( Talent.SHIELD_POKE ) < 4;
	}

	// ---- 骑士精神：从命中的敌人身上剥夺 25% 的伤害、格挡、命中与闪避 ----

	public static final float SPIRIT_SHARE = 0.25f;

	// 敌人的属性有时会反过来读英雄的属性，防止互相递归
	private static boolean reading = false;

	/** 正被英雄剥夺属性的那名敌人；没有则为 null。 */
	public static Char spiritSource( Char ch ) {
		if (!(ch instanceof Hero) || reading) return null;
		KnightSpirit s = ch.buff( KnightSpirit.class );
		return s == null ? null : s.source();
	}

	/** ch 是否正被英雄剥夺属性（它的伤害、格挡、命中、闪避都只剩 75%）。 */
	public static boolean isSpiritVictim( Char ch ) {
		return ch != null && Dungeon.hero != null && spiritSource( Dungeon.hero ) == ch;
	}

	/** 英雄这次伤害骰额外加上敌人一次伤害骰的 25%：英雄伤害范围扩大敌人范围的 25%。 */
	public static int spiritDamage( Hero hero ) {
		Char src = spiritSource( hero );
		if (src == null) return 0;
		reading = true;
		try { return Math.round( src.damageRoll() * SPIRIT_SHARE ); } finally { reading = false; }
	}

	/** 同上，格挡（护甲减伤）。 */
	public static int spiritArmor( Hero hero ) {
		Char src = spiritSource( hero );
		if (src == null) return 0;
		reading = true;
		try { return Math.round( src.drRoll() * SPIRIT_SHARE ); } finally { reading = false; }
	}

	/** 命中、闪避是单个数值：直接取敌人当前数值的 25%。 */
	public static int spiritAccuracy( Hero hero ) {
		Char src = spiritSource( hero );
		if (src == null) return 0;
		reading = true;
		try { return Math.round( src.attackSkill( hero ) * SPIRIT_SHARE ); } finally { reading = false; }
	}

	public static int spiritEvasion( Hero hero ) {
		Char src = spiritSource( hero );
		if (src == null) return 0;
		reading = true;
		try { return Math.round( src.defenseSkill( hero ) * SPIRIT_SHARE ); } finally { reading = false; }
	}

	/** 被剥夺的敌人：命中、闪避只剩 75%。 */
	@SubscribeEvent(event = HitCheckEvent.class, priority = 100)
	public static void onHitCheck( HitCheckEvent event ) {
		if (isSpiritVictim( event.getAttacker() )) {
			event.setAccuracy( event.getAccuracy() * (1f - SPIRIT_SHARE) );
		}
		if (isSpiritVictim( event.getDefender() )) {
			event.setEvasion( event.getEvasion() * (1f - SPIRIT_SHARE) );
		}
	}

	/**
	 * 骑士精神：持续 1~4 回合；+4 时只要那名敌人还在攻击范围内就一直持续。
	 * 只记敌人是谁，数值每次结算时按敌人当前的属性现取。
	 */
	public static class KnightSpirit extends Buff {

		{
			type = buffType.POSITIVE;
		}

		private int sourceId = -1;

		void set( Char enemy, int pts ) {
			sourceId = enemy.id();
			timeToNow();
			spend( pts );
		}

		Char source() {
			Actor a = sourceId == -1 ? null : Actor.findById( sourceId );
			return a instanceof Char && ((Char) a).isAlive() ? (Char) a : null;
		}

		@Override
		public boolean act() {
			Char src = source();
			boolean keep = target instanceof Hero && ((Hero) target).pointsInTalent( Talent.KNIGHT_SPIRIT ) >= 4
					&& src != null && ((Hero) target).canAttack( src );
			if (!keep) {
				detach();
				return true;
			}
			spend( TICK );
			return true;
		}

		@Override
		public String icon() {
			return BuffIndicator.BLESS;
		}

		@Override
		public String desc() {
			Char src = source();
			return Messages.get( this, "desc", src == null ? "-" : src.name() );
		}

		private static final String SRC = "source";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( SRC, sourceId );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			sourceId = bundle.getInt( SRC );
		}
	}
}
