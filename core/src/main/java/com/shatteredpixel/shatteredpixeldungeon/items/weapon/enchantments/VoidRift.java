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
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RiftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/** 虚空附魔：撕扯空间，攻击后有机会把目标脚下的地块撕成深渊使其坠落。 */
public class VoidRift extends Weapon.Enchantment {

	private static final ItemSprite.Glowing VOID_GLOW = new ItemSprite.Glowing( 0x7A2BE2 );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		if (damage <= 0 || !defender.isAlive() || defender.isImmune( VoidRift.class )){
			return damage;
		}
		int level = Math.max( 0, weapon.buffedLvl() );
		float chance = Math.min( 0.40f, 0.10f + 0.02f * level ) * procChanceMultiplier( attacker );
		if (Random.Float() < chance){
			VoidRiftTracker tracker = Buff.affect( defender, VoidRiftTracker.class );
			tracker.setup( attacker, weapon, level );
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return VOID_GLOW;
	}

	/** 与死神附魔一样延后一拍结算，等这一击的真实伤害落地后再判定地块。 */
	public static class VoidRiftTracker extends Buff {

		{ actPriority = Actor.VFX_PRIO; }

		private Char attacker;
		private Weapon weapon;
		private int level;

		void setup( Char attacker, Weapon weapon, int level ){
			this.attacker = attacker;
			this.weapon = weapon;
			this.level = level;
		}

		@Override
		public boolean act() {
			if (target != null && target.isAlive()) resolve();
			detach();
			return true;
		}

		private void resolve(){
			Level lvl = Dungeon.level;
			if (lvl == null) return;
			int cell = target.pos;
			if (cell < 0 || cell >= lvl.map.length) return;

			boolean boss = Char.hasProp( target, Char.Property.BOSS )
					|| Char.hasProp( target, Char.Property.MINIBOSS );
			if (canRip( lvl, cell ) && !boss && target != Dungeon.hero){
				// 三参 set 会同步刷新该格的 pit 标记，之后踩上去也会正常坠落。
				Level.set( cell, Terrain.CHASM, lvl );
				GameScene.updateMap( cell );
				CellEmitter.get( cell ).burst( RiftParticle.SCATTER, 12 );
				if (target instanceof Mob){
					Chasm.mobFall( (Mob) target );
				} else {
					target.die( VoidRift.class );
				}
			} else {
				// 地块撕不开时改走高额真实伤害，不另写进游戏说明。
				int extra = Math.max( 1, Math.round( target.HT * (0.45f + 0.02f * level) ) );
				target.damage( new DamageInfo( extra, DamageType.TRUE, attacker, weapon, this ) );
			}
		}

		private boolean canRip( Level lvl, int cell ){
			int terr = lvl.map[cell];
			return !lvl.solid[cell] && !lvl.avoid[cell]
					&& terr != Terrain.CHASM
					&& terr != Terrain.ENTRANCE && terr != Terrain.EXIT
					&& terr != Terrain.UNLOCKED_EXIT && terr != Terrain.LOCKED_EXIT
					&& terr != Terrain.DOOR && terr != Terrain.OPEN_DOOR && terr != Terrain.LOCKED_DOOR;
		}
	}
}
