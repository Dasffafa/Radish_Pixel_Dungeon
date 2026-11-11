/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;

/** 沉默的代价：四阶近战，十二击蓄势，第十三次命中斩杀或追加伤害；武技「钟响」把一击全额转成延缓伤害。 */
public class SilentPriceBlade extends MeleeWeapon {

	public static final int HITS_TO_CHARGE = 12;
	private static final String HITS = "hits";
	private static final String CHARGED = "charged";

	{
		image = ItemSpriteSheet.SILENT_PRICE_BLADE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;
		tier = 4;
		ACC = 0.9f;
	}

	private int hits = 0;
	private boolean charged = false;

	@Override
	public int min(int lvl) { return 4 + lvl; }

	@Override
	public int max(int lvl) { return 13 + 4*lvl; }

	@Override
	public String image() {
		return charged ? ItemSpriteSheet.SILENT_PRICE_BLADE_CHARGED : ItemSpriteSheet.SILENT_PRICE_BLADE;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (attacker instanceof Hero){
			if (charged){
				charged = false;
				hits = 0;
				updateQuickslot();
				reap( attacker, defender, damage );
			} else if (++hits >= HITS_TO_CHARGE){
				charged = true;
				updateQuickslot();
			}
		}
		return super.proc( attacker, defender, damage );
	}

	private void reap( Char attacker, Char defender, int damage ) {
		if (defender == null || !defender.isAlive()) return;
		boolean boss = Char.hasProp( defender, Char.Property.BOSS )
				|| Char.hasProp( defender, Char.Property.MINIBOSS );
		int hpPercent = Math.round( defender.HP * 100f / defender.HT );

		if (!boss && damage >= hpPercent){
			final Char target = defender;
			final Item cause = this;
			Actor.add( new Actor() {
				{ actPriority = VFX_PRIO; }
				@Override
				protected boolean act() {
					if (target.isAlive()){
						target.HP = 0;
						if (!target.isAlive()) target.die( cause );
					}
					Actor.remove( this );
					return true;
				}
			});
			return;
		}

		int extra = Math.round( damage * defender.HT / 100f );
		if (extra > 0 && !defender.isInvulnerable( attacker.getClass() )){
			defender.damage( new DamageInfo( extra, DamageType.PHYSICAL_NO_ARMOR, attacker, this, this ) );
		}
	}

	// ====================== 武技「钟响」 ======================

	@Override
	public String targetingPrompt() { return Messages.get( this, "prompt" ); }

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		int amount = augment.damageFactor( Math.round( 30f + 4.5f * buffedLvl() ) );
		bellToll( hero, target, amount, this );
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get( this, "ability_desc",
					augment.damageFactor( Math.round( 30f + 4.5f * buffedLvl() ) ), 3 + buffedLvl()/2 );
		} else {
			return Messages.get( this, "typical_ability_desc", 30, 3 );
		}
	}

	public String upgradeAbilityStat(int level) {
		return Integer.toString( augment.damageFactor( Math.round( 30f + 4.5f * level ) ) );
	}

	/** 本仓库 Char 只接受 DamageInfo，无法照原包在 Char.damage 里插钩子；
	 *  改用零侵入兜底：本次不出伤害，把全额直接塞进原版延缓伤害 buff。 */
	private static void bellToll( Hero hero, Integer target, int amount, MeleeWeapon wep ){
		if (target == null) return;
		Char enemy = Actor.findChar( target );
		if (enemy == null || enemy == hero || hero.isCharmedBy( enemy ) || !Dungeon.level.heroFOV[target]){
			GLog.w( Messages.get( wep, "ability_no_target" ) );
			return;
		}
		hero.belongings.abilityWeapon = wep;
		if (!hero.canAttack( enemy )){
			GLog.w( Messages.get( wep, "ability_target_range" ) );
			hero.belongings.abilityWeapon = null;
			return;
		}
		hero.belongings.abilityWeapon = null;

		hero.sprite.attack( enemy.pos, new Callback() {
			@Override
			public void call() {
				wep.beforeAbilityUsed( hero, enemy );
				AttackIndicator.target( enemy );
				Viscosity.DeferedDamage deferred = Buff.affect( enemy, Viscosity.DeferedDamage.class );
				deferred.prolong( amount );
				enemy.sprite.showStatus( com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.WARNING,
						Messages.get( wep, "deferred", amount ) );
				Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );
				Invisibility.dispel();
				// 本仓库 TimeBubble.reset() 固定 7 回合且 left 私有，
				// 借公开的 processTime 把剩余时间收敛到 3 + L/2 回合。
				Swiftthistle.TimeBubble bubble = Buff.affect( hero, Swiftthistle.TimeBubble.class );
				bubble.reset();
				bubble.processTime( 7f - (3 + wep.buffedLvl()/2f) );
				hero.spendAndNext( hero.attackDelay() );
				if (!enemy.isAlive()) MeleeWeapon.onAbilityKill( hero, enemy );
				wep.afterAbilityUsed( hero );
			}
		});
	}

	// ====================== 存档 ======================

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle( bundle );
		bundle.put( HITS, hits );
		bundle.put( CHARGED, charged );
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle( bundle );
		hits = bundle.getInt( HITS );
		charged = bundle.getBoolean( CHARGED );
	}

	/** 保留类以对应文本键；本仓库走零侵入兜底，不挂此标记。 */
	public static class BellTracker extends FlavourBuff {}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero )) actions.add( AC_ABILITY );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (!AC_ABILITY.equals( action ) || !isEquipped( hero )){
			super.execute( hero, action );
			return;
		}
		if (STRReq() > hero.STR()){
			GLog.w( Messages.get( this, "ability_low_str" ) );
			return;
		}
		Charger charger = Buff.affect( hero, Charger.class );
		if (charger.charges + charger.partialCharge < abilityChargeUse( hero, null )){
			GLog.w( Messages.get( this, "ability_no_charge" ) );
			return;
		}
		GameScene.selectCell( new CellSelector.Listener() {
			@Override
			public void onSelect( Integer cell ) {
				if (cell != null) duelistAbility( hero, cell );
			}
			@Override
			public String prompt() { return targetingPrompt(); }
		});
	}
}
