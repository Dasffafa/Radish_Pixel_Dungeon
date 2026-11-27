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
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

/**
 * 挑战「自动攻击」：玩家与怪物每个回合都自动向范围里的对手出手，移动也不耽误攻击。
 * 远程怪物还多一条：贴得太近就往后退，边退边打。
 */
public final class AutoAttack {

	//同一次行动里 Mob.act 可能被反复调用（状态返回 false 时），用时间戳保证一回合只打一次
	private static final java.util.WeakHashMap<Mob, Float> lastMobAttack = new java.util.WeakHashMap<>();

	private AutoAttack(){}

	public static boolean active(){
		return Dungeon.isChallenged( Challenges.AUTOMATIC_ATTACK );
	}

	/** 上一回合同一时刻是否已经随机出手过（点自己与等待按钮都会走到，避免一回合打两下）。 */
	private static float lastWaitTime = -1f;

	/** 跳过回合（原地等待）时的随机攻击：范围内随机挑一个敌人。 */
	public static void onHeroWait( Hero hero ){
		if (hero == null || !hero.isAlive() || !active()) return;
		if (Actor.now() == lastWaitTime) return;
		lastWaitTime = Actor.now();
		//先随机打一下贴脸的，再照常把弓和投掷物放出去
		waitMelee( hero );
		rangedStrikes( hero );
	}

	/** 原地等待时的那一下随机近战。 */
	private static void waitMelee( Hero hero ){
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



	/** 放一个飞行物出去，落地时结算伤害；没有精灵图时退回直接命中。 */
	private static void fling( Hero hero, Char target,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon missile ){
		//先放飞行物（纯视觉），伤害当场结算：之前等它飞到再算，回调没触发时等于没打
		if (hero.sprite != null && hero.sprite.parent != null && target.sprite != null){
			com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite ms =
					(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite)
							hero.sprite.parent.recycle( com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite.class );
			ms.reset( hero.sprite, target.sprite, missile, null );
		} else if (hero.sprite != null){
			hero.sprite.zap( target.pos );
		}
		missile.autoThrow( hero, target.pos );
	}

	/**
	 * 远程出手：弓（若装备且力够）射一箭，背包里每件力够的投掷物各投一次。
	 * 投掷物本身没有距离限制，只要目标在视野里就算够得着。
	 */
	private static void rangedStrikes( Hero hero ){
		//女猎手的弓不能装备，一直放在背包里：按物品找，不按武器槽找
		com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow =
				hero.belongings.getItem( com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.class );
		if (bow == null){
			KindOfWeapon w = hero.belongings.weapon();
			if (w instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow){
				bow = (com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow) w;
			}
		}
		if (bow != null){
			Char target = visibleEnemy( hero );
			if (target != null){
				com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon arrow =
						bow.knockArrow();
				fling( hero, target, arrow );

			}
		}

		//背包里所有投掷物（含收纳袋里的），力够的才能用
		java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon>
				missiles = new java.util.ArrayList<>();
		for (com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon m
				: hero.belongings.getAllItems( com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon.class )){
			if (m.quantity() > 0 && m.STRReq() <= hero.STR()){
				missiles.add( m );
			}
		}
		for (com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon m : missiles){
			Char target = visibleEnemy( hero );
			if (target == null) break;
			//从背包里取一件再直接命中：cast 是异步的，一次行动里要投出全部就得同步结算
			Item one = m.detach( hero.belongings.backpack );
			if (!(one instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon))
					continue;
			fling( hero, target, (com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon) one );
		}
		staffStrikes( hero );
	}

	/** 法杖：背包里每根还有能量的都朝目标放一次（没能量就跳过）。 */
	private static void staffStrikes( Hero hero ){
		java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand> wands =
				new java.util.ArrayList<>( hero.belongings.getAllItems( com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.class ) );
		//法师的法杖（MagesStaff）里嵌着一根魔杖，也算一根
		for (com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff
				: hero.belongings.getAllItems( com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff.class )){
			if (staff.wand != null && !wands.contains( staff.wand )){
				wands.add( staff.wand );
			}
		}
		for (com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand : wands){
			Char target = visibleEnemy( hero );
			if (target == null) break;
			zapWith( hero, target, wand );
		}
	}

	/**
	 * 用这根法杖打一下：能量照扣，但不推进回合。
	 * 走的是法杖自己的 onZap，所以各法杖的特殊效果都在。
	 */
	private static void zapWith( Hero hero, Char target,
			com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand ){
		//没能量就安静跳过，不再刷"法杖抖动"的提示
		if (!wand.autoHasCharge()) return;
		Item.curUser = hero;
		Item.curItem = wand;
		if (hero.sprite != null) hero.sprite.zap( target.pos );
		final int cell = target.pos;
		final com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand w = wand;
		final com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica shot =
				new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(
						hero.pos, cell, wand.collisionProperties( cell ) );
		com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.autoCastFree = true;
		wand.fx( shot, new com.watabou.utils.Callback(){
			@Override
			public void call(){
				Item.curUser = hero;
				Item.curItem = w;
				w.onZap( shot );
				//走 wandUsed：照扣能量、触发施法天赋，但由 autoCastFree 跳过回合消耗
				w.wandUsed();
				com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.autoCastFree = false;
			}
		} );
	}

	/** 视野里最近的敌人（投掷物没有距离限制）。 */
	private static Char visibleEnemy( Hero hero ){
		Char best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Mob m : Dungeon.level.mobs){
			if (!m.isAlive() || m.alignment != Char.Alignment.ENEMY) continue;
			if (!Dungeon.level.heroFOV[m.pos]) continue;
			int d = Dungeon.level.distance( hero.pos, m.pos );
			if (d < 1) continue;
			if (d < bestDist){ bestDist = d; best = m; }
		}
		return best;
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
		if (target != null){
			//先摆出攻击动作，再结算伤害
			if (hero.sprite != null) hero.sprite.attack( target.pos );
			hero.attack( target );
		}

		//远程照打：弓与背包里所有力够的投掷物
		rangedStrikes( hero );
	}

	/** 怪物每次行动后调一次：能打到就打，远程怪贴脸就后退。 */
	public static void onMobMoved( Mob mob ){
		if (mob == null || !mob.isAlive() || !active()) return;
		float now = Actor.now();
		Float last = lastMobAttack.get( mob );
		if (last != null && Math.abs( now - last ) < 0.0001f) return;
		lastMobAttack.put( mob, now );
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
