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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamagePipeline;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
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

	// 未鉴定时只给 +0 的数值，否则能从描述里的数字反推出等级
	@Override
	public String statsInfo() {
		if (isIdentified()) {
			return Messages.get( this, "stats_desc", maxStacks( level() ), DURATION );
		} else {
			return Messages.get( this, "typical_stats_desc", maxStacks( 0 ), DURATION );
		}
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
		// 额外等级直接显示在物品栏、快捷栏的等级数字上，层数变了要通知它们重画
		updateQuickslot();
	}

	private void clearStacks(){
		if (stacks > 0) updateQuickslot();
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
			if (turnsLeft > 0 && --turnsLeft <= 0) clearStacks();
			spend( TICK );
			return true;
		}
	}

	/**
	 * 击杀判定挂在最终伤害事件上，而不是 AttackHitEvent：法杖、法术这类不经过 attack() 的伤害
	 * 不会发 AttackHitEvent，只认它的话法师用法杖击杀永远叠不了层。
	 */
	@SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
	public static void onKill( CharFinalDamageEvent event ){
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		Armor armor = hero.belongings.armor();
		if (!(armor instanceof SoulMix)) return;

		Char victim = event.getTarget();
		if (victim.alignment != Char.Alignment.ENEMY || victim.isAlive()) return;

		// 事件自带的 attacker 只有伤害来源对象本身是角色时才有值（法杖伤害的来源是法杖），
		// 凶手要从本次伤害的 DamageInfo 里取
		DamageInfo info = DamagePipeline.activeInfo();
		if (info == null || info.getAttacker() != hero) return;

		((SoulMix) armor).gainStack();
	}
}
