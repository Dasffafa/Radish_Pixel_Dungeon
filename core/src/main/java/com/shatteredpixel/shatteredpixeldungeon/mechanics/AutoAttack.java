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

	/** 跳过回合（原地等待）时的随机攻击：范围内随机挑一个敌人。 */
	public static void onHeroWait( Hero hero ){
		if (hero == null || !hero.isAlive() || !active()) return;
		java.util.ArrayList<Char> targets = new java.util.ArrayList<>();
		int reach = heroReach( hero );
		for (Mob m : Dungeon.level.mobs){
			if (!m.isAlive()) continue;
			if (m.alignment != Char.Alignment.ENEMY) continue;
			if (!Dungeon.level.heroFOV[m.pos]) continue;
			if (Dungeon.level.distance( hero.pos, m.pos ) <= reach) targets.add( m );
		}
		if (targets.isEmpty()) return;
		Char target = targets.get( com.watabou.utils.Random.Int( targets.size() - 1 ) );
		if (hero.sprite != null) hero.sprite.attack( target.pos );
		hero.attack( target );
	}

	/** 这个物品能不能手动瞄准：挑战里投掷武器不能选目标，药水照旧。 */
	public static boolean blocksThrowAim( Object item ){
		if (!active() || item == null) return false;
		if (!(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)) return false;
		return true;
	}

	/** 起步那一刻够得着的目标：退着走时也照打（这一步内出的手）。 */
	public static Char targetBeforeMove( Hero hero ){
		if (hero == null || !hero.isAlive() || !active()) return null;
		return nearest( hero, heroReach( hero ) );
	}

	/**
	 * 英雄走完一格后调一次：优先打现在够得着的；若往后退出了范围，就打起步时盯着的那个。
	 * 这样"远离怪物方向走"也照样能还手。
	 */
	public static void onHeroTurn( Hero hero, Char preTarget ){
		if (hero == null || !hero.isAlive() || !active()) return;
		int reach = heroReach( hero );
		Char target = nearest( hero, reach );
		if (target == null && preTarget != null && preTarget.isAlive()
				&& Dungeon.level.distance( hero.pos, preTarget.pos ) <= reach + 1){
			target = preTarget;
		}
		if (target == null) return;
		//先摆出攻击动作，再结算伤害
		if (hero.sprite != null) hero.sprite.attack( target.pos );
		hero.attack( target );
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
			if (mob.sprite != null) mob.sprite.attack( hero.pos );
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
			//只打敌对生物：NPC 与盟友不碰
			if (m.alignment != Char.Alignment.ENEMY) continue;
			//视野里看得见才打；贴身的即使视野还没刷新也算
			if (!Dungeon.level.heroFOV[m.pos] && Dungeon.level.distance( self.pos, m.pos ) > 1) continue;
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
