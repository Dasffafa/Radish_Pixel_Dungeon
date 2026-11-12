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
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.utils.Random;

import java.util.Objects;

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
			tracker.setup( attacker, weapon, this, level );
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
		private VoidRift enchant;
		private int level;

		void setup( Char attacker, Weapon weapon, VoidRift enchant, int level ){
			this.attacker = attacker;
			this.weapon = weapon;
			this.enchant = enchant;
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
			// 只有怪物会被撕下去；英雄被怪物手里的虚空武器打中时走下面的伤害兜底
			if (target instanceof Mob && !boss && canRip( lvl, cell )){
				// 三参 set 会同步刷新该格的 pit 标记，之后踩上去也会正常坠落。
				Level.set( cell, Terrain.CHASM, lvl );
				GameScene.updateMap( cell );
				CellEmitter.get( cell ).burst( RiftParticle.SCATTER, 12 );
				Chasm.mobFall( (Mob) target );
			} else {
				// 地块撕不开时改走高额真实伤害，不另写进游戏说明。
				int extra = Math.max( 1, Math.round( target.HT * (0.45f + 0.02f * level) ) );
				// 伤害来源记成附魔本身，和其它附魔的追加伤害一致：Mob.die 靠来源是不是
				// 武器 / 附魔来认「武器击杀」，记成这个追踪 buff 就不算了。
				// 这几个字段不入档，读档后恢复出来的追踪器拿不到附魔，才退回用自己。
				Object source = enchant != null ? enchant : this;
				target.damage( new DamageInfo( extra, DamageType.TRUE, attacker, weapon, source ) );
			}
		}

		/** 这一格能不能撕成深渊。撕出来的坑是永久地形，所以撕不开的情况都交给 resolve() 的伤害兜底。 */
		private boolean canRip( Level lvl, int cell ){
			// 和原版落穴陷阱（PitfallTrap）同一条规则：首领层、25 层以后、支线层不开坑。
			// 这些楼层要么没有能掉下去的下一层，要么留下的坑能让英雄跳下去绕过首领战。
			if (Dungeon.bossLevel() || Dungeon.depth > 25
					|| !Objects.equals( Dungeon.branchId, Branches.MAIN )){
				return false;
			}
			// 封锁中的楼层不允许离开（传送类道具同样被 locked 拦住），不能在地上留一个出口
			if (lvl.locked) return false;

			if (lvl.solid[cell] || lvl.avoid[cell]) return false;

			int terr = lvl.map[cell];
			if (terr == Terrain.CHASM
					|| terr == Terrain.ENTRANCE || terr == Terrain.ENTRANCE_SP || terr == Terrain.EXIT
					|| terr == Terrain.UNLOCKED_EXIT || terr == Terrain.LOCKED_EXIT
					|| terr == Terrain.DOOR || terr == Terrain.OPEN_DOOR || terr == Terrain.LOCKED_DOOR
					// 专门垫在自定义贴图下面的地板
					|| terr == Terrain.CUSTOM_DECO_EMPTY){
				return false;
			}

			// 楼层出入口不一定是楼梯地形，也可能不止一格，按过渡区判断才不会漏；撕掉就回不去了
			if (lvl.getTransition( cell ) != null) return false;

			// Level.set 只换地形，格子上的物品堆和植物会原样留在坑上，再也够不着
			if (lvl.heaps.get( cell ) != null || lvl.plants.get( cell ) != null) return false;

			// 被自定义贴图盖住的格子换了地形画面也不会变，会留下一个看不见的坑。
			// 判断方式同 Level.setCellToWater。
			int x = cell % lvl.width();
			int y = cell / lvl.width();
			for (CustomTilemap cust : lvl.customTiles){
				int cx = x - cust.tileX;
				int cy = y - cust.tileY;
				if (cx >= 0 && cy >= 0 && cx < cust.tileW && cy < cust.tileH
						&& cust.image( cx, cy ) != null){
					return false;
				}
			}

			return true;
		}
	}
}
