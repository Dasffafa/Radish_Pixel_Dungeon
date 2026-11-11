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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RiftJump;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RiftWindow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RiftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** 次元撕裂者：三阶弯刀，命中概率随机传送目标或英雄；武技「次元跳跃」为开关式强制传送。 */
public class DimensionalRipper extends Scimitar {

	public static final float PROC_CHANCE_BASE = 1/8f;
	public static final float PROC_CHANCE_MAX = 1/2f;
	public static final float PROC_CHANCE_DECAY = 7/8f;

	{
		image = ItemSpriteSheet.DIMENSIONAL_RIPPER;
		tier = 3;
		DLY = 0.8f;
	}

	public static float procChance( int lvl ) {
		float gap = (PROC_CHANCE_MAX - PROC_CHANCE_BASE) * (float) Math.pow( PROC_CHANCE_DECAY, lvl );
		return Math.max( 0f, PROC_CHANCE_MAX - gap );
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc( attacker, defender, damage );
		if (damage > 0) hitFx( defender );

		boolean teleported = false;
		if (attacker.buff( MagicImmune.class ) == null
				&& defender.isAlive() && damage > 0
				&& Random.Float() < procChance( buffedLvl() ) * Weapon.Enchantment.genericProcChanceMultiplier( attacker )){
			Char warped;
			if (Random.Int( 2 ) == 0){
				warped = warp( defender ) ? defender : null;
			} else {
				warped = warp( attacker ) ? attacker : null;
			}
			if (warped != null && attacker == Dungeon.hero){
				Buff.affect( Dungeon.hero, RiftWindow.class ).open( defender );
			}
			teleported = warped != null;
		}

		if (!teleported && attacker == Dungeon.hero && defender.isAlive() && damage > 0
				&& Dungeon.hero.buff( RiftJump.class ) != null){
			riftJumpFallback( Dungeon.hero, defender );
		}
		return damage;
	}

	private static boolean warp( Char ch ){
		if (ch == null
				|| Char.hasProp( ch, Char.Property.IMMOVABLE )
				|| ch.isImmune( ScrollOfTeleportation.class )){
			return false;
		}
		int oldPos = ch.pos;
		if (!ScrollOfTeleportation.teleportChar( ch )) return false;
		teleportFx( oldPos, ch.pos );
		if (ch instanceof Mob){
			Mob mob = (Mob) ch;
			if (mob.state == mob.HUNTING) mob.state = mob.WANDERING;
		}
		return true;
	}

	public static final int HIT_PARTICLES = 6;
	public static final int WARP_PARTICLES = 8;

	private static void hitFx( Char defender ){
		if (defender == null || defender.sprite == null) return;
		Emitter emitter = defender.sprite.centerEmitter();
		if (emitter != null) emitter.burst( RiftParticle.HIT, HIT_PARTICLES );
	}

	public static void teleportFx( int fromPos, int toPos ){
		if (Dungeon.level == null || Dungeon.level.heroFOV == null) return;
		if (Dungeon.level.heroFOV[fromPos]) CellEmitter.get( fromPos ).burst( RiftParticle.SCATTER, WARP_PARTICLES );
		if (Dungeon.level.heroFOV[toPos]) CellEmitter.center( toPos ).burst( RiftParticle.IMPLODE, WARP_PARTICLES );
	}

	@Override
	public String statsInfo() {
		return Messages.get( this, "stats_desc", Math.round( procChance( buffedLvl() ) * 100 ) );
	}

	// ====================== 武技「次元跳跃」 ======================

	@Override
	public String targetingPrompt() { return null; }

	@Override
	protected int baseChargeUse(Hero hero, Char target){ return 0; }

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (hero.buff( RiftJump.class ) != null){
			Buff.detach( hero, RiftJump.class );
		} else {
			Buff.affect( hero, RiftJump.class );
		}
		hero.sprite.operate( hero.pos );
		hero.next();
	}

	@Override
	public String abilityInfo() { return Messages.get( this, "ability_desc" ); }

	public String upgradeAbilityStat(int level){ return null; }

	private void riftJumpFallback( Hero hero, Char defender ){
		Charger charger = hero.buff( Charger.class );
		if (charger == null || (charger.charges < 1 && charger.partialCharge < 1f)){
			Buff.detach( hero, RiftJump.class );
			return;
		}
		Char warped = warp( defender ) ? defender : (warp( hero ) ? hero : null);
		if (warped == null) return;
		if (charger.charges > 0) charger.charges--;
		else charger.partialCharge -= 1f;
		Buff.affect( hero, RiftWindow.class ).open( defender );
		hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get( RiftJump.class, "warped" ) );
	}

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
		duelistAbility( hero, hero.pos );
		updateQuickslot();
	}
}
