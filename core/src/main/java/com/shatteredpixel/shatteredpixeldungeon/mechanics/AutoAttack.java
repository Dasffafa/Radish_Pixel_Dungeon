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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.mechanics;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

/**
 * 挑战「自动攻击」：玩家与怪物每个回合都自动向范围里的对手出手，移动也不耽误攻击。
 * 远程怪物还多一条：贴得太近就往后退，边退边打。
 */
public final class AutoAttack {

	private AutoAttack(){}

	public static boolean active(){
		return Dungeon.isChallenged( Challenges.AUTOMATIC_ATTACK );
	}

	/** 英雄一回合动作结束后调一次：范围内最近的敌人，顺手打一下。 */
	public static void onHeroTurn( Hero hero ){
		if (hero == null || !hero.isAlive() || !active()) return;
		Char target = nearest( hero, heroReach( hero ) );
		if (target != null) hero.attack( target );
	}

	/** 怪物每次行动后调一次：能打到就打，远程怪贴脸就后退。 */
	public static void onMobTurn( Mob mob ){
		if (mob == null || !mob.isAlive() || !active()) return;
		Char hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) return;
		//双方都看得见彼此才动手（用英雄视野近似）
		if (!Dungeon.level.heroFOV[mob.pos]) return;

		int reach = mobReach( mob );
		int dist = Dungeon.level.distance( mob.pos, hero.pos );
		if (dist <= reach){
			mob.attack( hero );
		}
		if (isRanged( mob ) && dist <= 3){
			stepAway( mob, hero.pos );
		}
	}

	/** 武器够得着的距离：长柄、鞭子会更远。 */
	private static int heroReach( Hero hero ){
		KindOfWeapon w = hero.belongings.attackingWeapon();
		if (w instanceof Weapon){
			return Math.max( 1, ((Weapon)w).reachFactor( hero ) );
		}
		return 1;
	}

	/** 怪物的出手距离：远程怪按 4 格算，其余贴身。 */
	private static int mobReach( Mob mob ){
		return isRanged( mob ) ? 4 : 1;
	}

	/** 远程怪：会射东西的那几类，按类名认。 */
	private static boolean isRanged( Mob mob ){
		String n = mob.getClass().getSimpleName().toLowerCase();
		return n.contains( "shaman" ) || n.contains( "eye" ) || n.contains( "warlock" )
				|| n.contains( "scorcher" ) || n.contains( "geomancer" ) || n.contains( "archer" )
				|| n.contains( "hunter" ) || n.contains( "caster" ) || n.contains( "mage" )
				|| n.contains( "crossbow" ) || n.contains( "rider" ) || n.contains( "drone" );
	}

	private static Char nearest( Char self, int reach ){
		if (Dungeon.level == null) return null;
		Char best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Mob m : Dungeon.level.mobs){
			if (m == self || !m.isAlive()) continue;
			if (!Dungeon.level.heroFOV[m.pos] || m.alignment == self.alignment) continue;
			int d = Dungeon.level.distance( self.pos, m.pos );
			if (d > reach) continue;
			if (d < bestDist){
				bestDist = d;
				best = m;
			}
		}
		return best;
	}

	/** 往远离目标的一侧挪一格，挪不动就算了。 */
	private static void stepAway( Mob mob, int fromPos ){
		int best = -1;
		int bestDist = Dungeon.level.distance( mob.pos, fromPos );
		for (int dir : new int[]{ mob.pos - 1, mob.pos + 1, mob.pos - Dungeon.level.width(), mob.pos + Dungeon.level.width() }){
			if (dir < 0 || dir >= Dungeon.level.length()) continue;
			if (!Dungeon.level.passable[dir] || Actor.findChar( dir ) != null) continue;
			int d = Dungeon.level.distance( dir, fromPos );
			if (d > bestDist){
				bestDist = d;
				best = dir;
			}
		}
		if (best != -1) mob.move( best );
	}
}
