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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ButterflyField;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** 圣宣：四阶长鞭，4 倍速、射程 8，命中撒黑白蝴蝶；武技「哀悼」铺扇形蝴蝶场地。
 *  伤害类型框架已剥离，按方案 A 走普通物理口径，仅保留粒子观感。 */
public class HolyDecree extends Whip {

	{
		image = ItemSpriteSheet.HOLY_DECREE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;

		tier = 4;
		DLY = 0.25f;
		RCH = 8;
	}

	@Override
	public int min(int lvl) { return 1 + lvl; }

	@Override
	public int max(int lvl) { return 5 + lvl; }

	// 本仓库 Speck 没有蝴蝶专用帧，用白色星点与灰黑尘点近似黑白双色。
	public static final Emitter.Factory BUTTERFLIES = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			Speck p = (Speck) emitter.recycle( Speck.class );
			p.reset( 0, x, y, Random.Int(2) == 0 ? Speck.DUST : Speck.STAR );
		}
	};

	@Override
	public Emitter emitter() {
		Emitter e = new Emitter();
		e.pos( 8, 8 );
		e.fillTarget = false;
		e.pour( BUTTERFLIES, 0.6f );
		return e;
	}

	public static void emitButterflies( Char target ){
		if (target != null && target.sprite != null){
			target.sprite.centerEmitter().start( BUTTERFLIES, 0.15f, Random.IntRange( 4, 6 ) );
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (damage > 0) emitButterflies( defender );
		return super.proc( attacker, defender, damage );
	}

	// ====================== 武技「哀悼」 ======================

	public static final float CONE_RADIUS = 8f;
	public static final float CONE_DEGREES = 60f;

	public static int mourningDuration( int lvl ){ return 8 + 2*lvl; }

	@Override
	public String targetingPrompt() { return Messages.get( this, "prompt" ); }

	@Override
	protected int baseChargeUse(Hero hero, Char target){ return 2; }

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (target == null) return;
		final int dst = target;
		if (!Dungeon.level.heroFOV[dst]){
			GLog.w( Messages.get( this, "ability_no_target" ) );
			return;
		}
		if (Dungeon.level.trueDistance( hero.pos, dst ) > CONE_RADIUS){
			GLog.w( Messages.get( this, "ability_target_range" ) );
			return;
		}

		Ballistica dir = new Ballistica( hero.pos, dst, Ballistica.WONT_STOP );
		int farCell = dir.path.get( Math.min( dir.path.size()-1, Math.round( CONE_RADIUS ) ) );
		final Ballistica aim = new Ballistica( hero.pos, farCell, Ballistica.STOP_SOLID );

		hero.sprite.attack( dst, new Callback() {
			@Override
			public void call() {
				ConeAOE cone = new ConeAOE( aim, CONE_RADIUS, CONE_DEGREES, Ballistica.STOP_SOLID );
				beforeAbilityUsed( hero, null );
				ButterflyField.spread( hero.pos, cone.cells, mourningDuration( buffedLvl() ), hero.sprite.parent );
				afterAbilityUsed( hero );
				hero.spendAndNext( Actor.TICK );
			}
		});
	}

	@Override
	public String abilityInfo() { return Messages.get( this, "ability_desc", mourningDuration( buffedLvl() ) ); }

	public String upgradeAbilityStat(int level){ return Integer.toString( mourningDuration( level ) ); }

	// 本仓库 MeleeWeapon 的武技调用被注释掉了，这里在武器自身接管动作入口，不动公共基类。
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
