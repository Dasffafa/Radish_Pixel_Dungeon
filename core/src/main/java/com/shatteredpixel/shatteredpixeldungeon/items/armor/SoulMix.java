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

	{ image = ItemSpriteSheet.ARMOR_SOULMIX; }

	public SoulMix() { super( 5 ); }

	@Override public int DRMax(int lvl){ return 10 + 5 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 1 + lvl; }

	/** 护甲自身的额外等级；武器侧在 Weapon.buffedLvl() 里加同一份。 */
	@Override public int buffedLvl(){ return super.buffedLvl() + stacks( Dungeon.hero ); }

	@Override protected ArmorBuff buff(){ return new SoulMixBuff(); }

	public static int maxStacks( int lvl ){ return 1 + Math.round( 0.33f * lvl ); }

	@Override
	public String statsInfo() {
		return Messages.get( this, "stats_desc", maxStacks( level() ), DURATION );
	}

	/** 英雄身上混灵的当前层数。 */
	public static int stacks( Hero hero ){
		if (hero == null) return 0;
		SoulMixBuff b = hero.buff( SoulMixBuff.class );
		return b == null || b.turnsLeft <= 0 ? 0 : b.stacks;
	}

	/** 供 Weapon.buffedLvl() 调用：只对英雄当前装备的武器生效。 */
	public static int bonusLevel( Hero hero, Weapon weapon ){
		if (hero == null || weapon == null || !weapon.isEquipped( hero )) return 0;
		return stacks( hero );
	}

	public class SoulMixBuff extends ArmorBuff {
		public int stacks = 0;
		public int turnsLeft = 0;

		public void gain(){
			stacks = Math.min( maxStacks( SoulMix.this.level() ), stacks + 1 );
			turnsLeft = DURATION;
		}

		@Override
		public boolean act(){
			if (turnsLeft > 0 && --turnsLeft <= 0) stacks = 0;
			spend( TICK );
			return true;
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
			stacks = bundle.getInt( STACKS );
			// 老存档没有回数字段：仍有层数就补一个完整持续期，避免读档后加成直接消失。
			turnsLeft = bundle.contains( TURNS ) ? bundle.getInt( TURNS ) : (stacks > 0 ? DURATION : 0);
		}
	}

	@SubscribeEvent(event = AttackHitEvent.class, priority = 0)
	public static void onKill( AttackHitEvent event ){
		if (!(event.getAttacker() instanceof Hero)) return;
		Char defender = event.getDefender();
		if (defender == null || defender.isAlive()
				|| defender.alignment != Char.Alignment.ENEMY) return;
		Hero hero = (Hero) event.getAttacker();
		SoulMixBuff b = hero.buff( SoulMixBuff.class );
		if (b != null) b.gain();
	}
}
