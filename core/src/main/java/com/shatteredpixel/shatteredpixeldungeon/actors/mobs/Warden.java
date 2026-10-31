/*
 * Radish Pixel Dungeon
 *
 * 典狱长（Warden）—— 地牢第 10 层 Boss，与天狗（Tengu）替换出现。
 *
 * 设计要点（来自需求）：
 *   - 120 生命、攻击 4~12、防御 2~5、命中 15、闪避 12、经验 20、可获经验上限 20 级
 *   - 半血后切换红眼形态（仅视觉）
 *   - 场上固定存在未激活的 DM-100 / DM-175 原型机，典狱长靠近后花 2/4 回合把它们
 *     激活成自己的友军；玩家也能用简易控制器或电击符石反过来拉拢
 *   - 自己拥有的友军 >= 3 个之前，不去主动追击玩家，优先激活原型机
 *   - 每失去 30 生命，场上随机补 1 台 DM-100 原型机
 *   - 失去 60 生命后，激活速度加快 1 回合
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WardenSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Warden extends Mob {

	{
		spriteClass = WardenSprite.class;

		HP = HT = 120;
		EXP = 20;
		maxLvl = 20;

		defenseSkill = 12;

		properties.add(Property.BOSS);
		properties.add(Property.INORGANIC);
	}

	private static final String ENRAGED      = "enraged";
	private static final String NEXT_SPAWN   = "next_spawn";
	private static final String SPEED_BONUS  = "speed_bonus";
	private static final String ACTIVATING   = "activating_cell";
	private static final String ACTIVATING_TURNS = "activating_turns";
	private static final String AWAKE        = "awake";

	private boolean enraged      = false;
	private int     nextSpawnHp  = 30;   // 下一次补生成原型机的生命阈值
	private int     speedBonus   = 0;    // 失去 60 生命后 +1

	//玩家进入视野前，典狱长保持静止（不激活原型机、不移动）
	private boolean awake = false;

	//当前正在激活的原型机
	private WardenPrototype activating = null;
	private int activatingTurnsLeft = 0;

	@Override
	public int attackSkill( Char target ) {
		return 15;
	}

	@Override
	public int damageRoll() {
		return Char.combatRoll( 4, 12 );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Char.combatRoll( 2, 5 );
	}

	@Override
	public String name() {
		return Messages.get( this, "name" );
	}

	@Override
	public String description() {
		return Messages.get( this, "desc" );
	}

	@Override
	public void notice() {
		super.notice();
		//这里要用带参数的版本：无参版本只判断"场上有任意 Boss"，
		//多 Boss 场景下会把自己漏掉
		if (!BossHealthBar.isAssigned( this )) {
			BossHealthBar.assignBoss( this );
			GLog.p( Messages.get( this, "boss_appear" ) );
		}
	}

	@Override
	protected boolean act() {

		//注意：本类在 super.act()（→Mob.act→Char.act）之前就会调用 getCloser()，
		//而视野数组正是在 Char.act() 里才初始化的，所以必须先自己补一次，
		//否则第一回合走向原型机时 Dungeon.findPassable 会拿到 null 的 vis 直接崩。
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		if (!BossHealthBar.isAssigned( this ) && Dungeon.level.heroFOV[pos]) {
			notice();
		}

		// 玩家还没进门看到它：保持静止，既不移动也不激活原型机
		if (!awake) {
			if (Dungeon.level.heroFOV[pos]) {
				awake = true;
				notice();
			} else {
				spend( TICK );
				return true;
			}
		}

		//正在激活某台原型机：每回合推进一次，期间不动
		if (activating != null) {
			WardenPrototype p = activating;
			if (p.isAlive() && p.isInactive() && Dungeon.level.adjacent( p.pos, pos ) && activatingTurnsLeft > 0) {
				activatingTurnsLeft--;
				if (activatingTurnsLeft <= 0) {
					p.activateByWarden();
					GLog.w( Messages.get( this, "activated" ) );
					activating = null;
				}
				spend( TICK );
				return true;
			}
			//目标没了就地取消
			activating = null;
			activatingTurnsLeft = 0;
		}

		//拥有 3 个以上友军后才主动追击玩家
		if (activatedAllies() >= 3 && state != HUNTING && state != FLEEING) {
			state = HUNTING;
			enemySeen = false;
		}

		//少于 3 个友军时优先去激活原型机
		if (activatedAllies() < 3 && isAlive()) {
			WardenPrototype target = nearestInactivePrototype();
			if (target != null) {
				if (Dungeon.level.adjacent( target.pos, pos )) {
					startActivating( target );
					spend( TICK );
					return true;
				}
				int oldPos = pos;
				if (getCloser( target.pos )) {
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				}
			}
		}

		return super.act();
	}

	private void startActivating( WardenPrototype p ) {
		activating = p;
		activatingTurnsLeft = Math.max( 1, p.activationTurns() - speedBonus );
		GLog.i( Messages.get( this, "activating" ) );
	}

	public int activatedAllies() {
		int count = 0;
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof WardenPrototype && m.alignment == Alignment.ENEMY && ((WardenPrototype) m).isWardenAlly()) {
				count++;
			}
		}
		return count;
	}

	private WardenPrototype nearestInactivePrototype() {
		WardenPrototype best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof WardenPrototype && m.isAlive() && ((WardenPrototype) m).isInactive()) {
				int d = Dungeon.level.distance( pos, m.pos );
				if (d < bestDist) {
					bestDist = d;
					best = (WardenPrototype) m;
				}
			}
		}
		return best;
	}

	@Override
	public void damage( DamageInfo info ) {

		if (!BossHealthBar.isAssigned()) {
			notice();
		}

		int preHP = HP;
		super.damage( info );
		int lost = preHP - HP;
		if (lost <= 0) return;

		//这一击已经把自己打死了：尸体不该再补生成原型机、也不再播形态切换的台词
		if (!isAlive()) return;

		//每失去 30 生命补生成一台 DM-100 原型机
		while ( lostTotal() >= nextSpawnHp ) {
			nextSpawnHp += 30;
			if (Dungeon.level instanceof WardenBossLevel) {
				((WardenBossLevel) Dungeon.level).spawnPrototype( 100 );
			}
		}

		//失去 60 生命后加速
		if (speedBonus == 0 && HT - HP >= 60) {
			speedBonus = 1;
			GLog.w( Messages.get( this, "faster" ) );
		}

		//半血切换红眼形态
		if (!enraged && HP * 2 <= HT) {
			enrage();
		}
	}

	private int lostTotal() {
		return HT - HP;
	}

	private void enrage() {
		enraged = true;
		if (sprite instanceof WardenSprite) {
			((WardenSprite) sprite).setEnraged( true );
		}
		yell( Messages.get( this, "enraged" ) );
	}

	@Override
	public void die( Object cause ) {
		//与天狗（Tengu）保持一致：Boss 被击杀要有横幅 + 击杀音效
		GameScene.bossSlain();
		super.die( cause );
		if (Dungeon.level instanceof WardenBossLevel) {
			((WardenBossLevel) Dungeon.level).onBossDeath();
		}
	}

	/** 半血后的红眼形态（供精灵读档时同步） */
	public boolean isEnraged() {
		return enraged;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ENRAGED, enraged );
		bundle.put( NEXT_SPAWN, nextSpawnHp );
		bundle.put( SPEED_BONUS, speedBonus );
		bundle.put( AWAKE, awake );
		if (activating != null && activating.isAlive()) {
			bundle.put( ACTIVATING, activating.pos );
			bundle.put( ACTIVATING_TURNS, activatingTurnsLeft );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		enraged = bundle.getBoolean( ENRAGED );
		//老档没有这个键时不能读成 0，否则下一次受伤会把历史阈值一次性全部补生成
		nextSpawnHp = bundle.contains( NEXT_SPAWN ) ? bundle.getInt( NEXT_SPAWN ) : 30;
		speedBonus = bundle.getInt( SPEED_BONUS );
		awake = bundle.getBoolean( AWAKE );
		//红眼形态不在这里切：读档阶段 sprite 还没创建，交给 WardenSprite.link()
		activating = null;
		activatingTurnsLeft = 0;
		if (bundle.contains( ACTIVATING )) {
			int cell = bundle.getInt( ACTIVATING );
			for (Mob m : Dungeon.level.mobs) {
				if (m instanceof WardenPrototype && m.pos == cell) {
					activating = (WardenPrototype) m;
					activatingTurnsLeft = bundle.getInt( ACTIVATING_TURNS );
					break;
				}
			}
		}
	}
}
