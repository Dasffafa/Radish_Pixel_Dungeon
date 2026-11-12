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
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

/** 混灵：五阶护甲，击杀敌人后叠层，每层给护甲与武器 +1 额外等级。 */
public class SoulMix extends Armor {

	private static final String STACKS = "soul_stacks";
	private static final String TURNS = "soul_turns";

	/** 额外等级持续回合数：每次击杀刷新，归零后层数清空。 */
	public static final int DURATION = 10;

	// 层数和剩余回合记在护甲上而不是 SoulMixBuff 里：ArmorBuff 是非静态内部类，不进存档，
	// 读档时 activate() 会重新 new 一个，记在它身上的状态读档即丢。
	private int stacks = 0;
	private int turnsLeft = 0;

	{ image = ItemSpriteSheet.ARMOR_SOULMIX; }

	public SoulMix() { super( 5 ); }

	@Override public int DRMax(int lvl){ return 10 + 5 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 1 + lvl; }

	/** 护甲自身的额外等级；武器侧在 Weapon.buffedLvl() 里加同一份。 */
	@Override public int buffedLvl(){ return super.buffedLvl() + stacks; }

	@Override protected ArmorBuff buff(){ return new SoulMixBuff(); }

	public static int maxStacks( int lvl ){ return 1 + Math.round( 0.33f * lvl ); }

	@Override
	public String statsInfo() {
		return Messages.get( this, "stats_desc", maxStacks( level() ), DURATION );
	}

	/** 供 Weapon.buffedLvl() 调用：只对英雄当前装备的武器生效，层数取英雄穿着的那件混灵。 */
	public static int bonusLevel( Hero hero, Weapon weapon ){
		if (hero == null || weapon == null || !weapon.isEquipped( hero )) return 0;
		Armor armor = hero.belongings.armor();
		return armor instanceof SoulMix ? ((SoulMix) armor).stacks : 0;
	}

	private void gainStack(){
		stacks = Math.min( maxStacks( level() ), stacks + 1 );
		turnsLeft = DURATION;
	}

	private void clearStacks(){
		stacks = 0;
		turnsLeft = 0;
	}

	@Override
	public void activate( Char ch ) {
		// 只有英雄的击杀会叠层。工匠把穿着的护甲改装给友方单位时不经过 doUnequip，残留的层数在这里清掉
		if (!(ch instanceof Hero)) clearStacks();
		super.activate( ch );
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			// 层数只在穿着期间有效，脱下即清空
			clearStacks();
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		super.reset();
		// 留在遗骸里的护甲会进下一局，层数不能跟过去
		clearStacks();
	}

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle( bundle );
		bundle.put( STACKS, stacks );
		bundle.put( TURNS, turnsLeft );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle( bundle );
		// 改动前的存档里没有这两个键
		turnsLeft = bundle.contains( TURNS ) ? bundle.getInt( TURNS ) : 0;
		stacks = turnsLeft > 0 && bundle.contains( STACKS ) ? bundle.getInt( STACKS ) : 0;
	}

	/** 只负责每回合给层数计时，自身不带状态。 */
	public class SoulMixBuff extends ArmorBuff {
		@Override
		public boolean act(){
			if (turnsLeft > 0 && --turnsLeft <= 0) stacks = 0;
			spend( TICK );
			return true;
		}
	}

	@SubscribeEvent(event = AttackHitEvent.class, priority = 0)
	public static void onKill( AttackHitEvent event ){
		if (!(event.getAttacker() instanceof Hero)) return;
		Char defender = event.getDefender();
		if (defender == null || defender.isAlive()
				|| defender.alignment != Char.Alignment.ENEMY) return;
		Armor armor = ((Hero) event.getAttacker()).belongings.armor();
		if (armor instanceof SoulMix) ((SoulMix) armor).gainStack();
	}
}
