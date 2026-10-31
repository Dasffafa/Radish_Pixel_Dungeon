/*
 * Radish Pixel Dungeon
 *
 * 原型机（WardenPrototype）—— 典狱长 Boss 战里的 DM-100 / DM-175 未激活机体。
 *
 * 状态机：
 *   未激活（NEUTRAL，躺倒不动）
 *     ├─ 被典狱长靠近激活 → 变成典狱长的友军（ENEMY，攻击玩家）
 *     ├─ 被玩家用简易控制器激活 → 进入倒数，若干回合后变成玩家友军（ALLY）
 *     └─ 被电击符石命中 → 立刻变成玩家友军（ALLY）
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DMPrototypeSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class WardenPrototype extends Mob {

	//100 = DM-100 原型机（弱），175 = DM-175 原型机（强）
	public int model = 100;

	private static final String MODEL      = "model";
	private static final String STATE      = "proto_state";
	private static final String PROGRESS   = "proto_progress";

	//INACTIVE：未激活；W_ALLY：已归典狱长；H_ALLY：已归玩家
	public static final int INACTIVE = 0;
	public static final int W_ALLY   = 1;
	public static final int H_ALLY   = 2;

	private int protoState = INACTIVE;
	private int progress   = 0;   // 玩家侧激活还剩的回合

	{
		spriteClass = DMPrototypeSprite.class;
		alignment = Alignment.NEUTRAL;

		properties.add(Property.INORGANIC);
		properties.add(Property.ELECTRIC);
	}

	public WardenPrototype() {
		this( 100 );
	}

	public WardenPrototype( int model ) {
		this.model = model;
		if (model == 175) {
			HP = HT = 34;
			EXP = 8;
			maxLvl = 18;
		} else {
			HP = HT = 22;
			EXP = 5;
			maxLvl = 15;
		}
	}

	@Override
	public int damageRoll() {
		return Char.combatRoll( model == 175 ? 4 : 2, model == 175 ? 10 : 7 );
	}

	@Override
	public int attackSkill( Char target ) {
		return model == 175 ? 16 : 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Char.combatRoll( model == 175 ? 2 : 1, model == 175 ? 6 : 4 );
	}

	@Override
	public String name() {
		return Messages.get( this, model == 175 ? "name_175" : "name_100" );
	}

	@Override
	public String description() {
		return Messages.get( this, "desc" );
	}

	public boolean isInactive() {
		return protoState == INACTIVE && isAlive();
	}

	public boolean isWardenAlly() {
		return protoState == W_ALLY;
	}

	public boolean isHeroAlly() {
		return protoState == H_ALLY;
	}

	/** 典狱长开始激活需要的回合数 */
	public int activationTurns() {
		return model == 175 ? 4 : 2;
	}

	/** 玩家用简易控制器激活需要的回合数 */
	public int playerActivationTurns() {
		return model == 175 ? 6 : 3;
	}

	/** 典狱长把原型机激活成自己的友军 */
	public void activateByWarden() {
		if (protoState != INACTIVE) return;
		protoState = W_ALLY;
		alignment = Alignment.ENEMY;
		if (sprite != null) {
			sprite.showStatus( 0xFFCC22, Messages.get( this, "activated_warden" ) );
			((DMPrototypeSprite) sprite).activate();
		}
	}

	/** 电击符石：立刻归玩家 */
	public void activateByHero() {
		if (protoState != INACTIVE) return;
		protoState = H_ALLY;
		progress = 0;
		alignment = Alignment.ALLY;
		if (sprite != null) {
			sprite.showStatus( 0x22CCFF, Messages.get( this, "activated_hero" ) );
			((DMPrototypeSprite) sprite).activate();
		}
	}

	/** 简易控制器：倒数若干回合后归玩家 */
	public void beginHeroActivation() {
		if (protoState != INACTIVE) return;
		progress = playerActivationTurns();
		GLog.w( Messages.get( this, "hacking_start" ) );
	}

	@Override
	protected boolean act() {

		if (protoState == INACTIVE) {
			if (progress > 0) {
				progress--;
				if (progress <= 0) {
					activateByHero();
				}
			}
			//未激活的机体不动、不攻击
			spend( TICK );
			return true;
		}

		return super.act();
	}

	@Override
	public void damage( DamageInfo info ) {
		//未激活的机体被打一下就会醒过来并归到玩家对面（避免玩家白嫖）
		//判据用阵营而不是"攻击者是不是英雄本人"，否则宠物/召唤物打它仍然能白嫖
		Char attacker = info.getAttacker();
		if (protoState == INACTIVE && isAlive() && attacker != null && attacker.alignment == Alignment.ALLY) {
			protoState = W_ALLY;
			alignment = Alignment.ENEMY;
			//视觉上要立刻站起来并给个提示，否则玩家只看到一台躺着的机器突然开始打人
			if (sprite != null) {
				sprite.showStatus( 0xFFCC22, Messages.get( this, "activated_warden" ) );
				if (sprite instanceof DMPrototypeSprite) {
					((DMPrototypeSprite) sprite).activate();
				}
			}
		}
		super.damage( info );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MODEL, model );
		bundle.put( STATE, protoState );
		bundle.put( PROGRESS, progress );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		model = bundle.getInt( MODEL );
		protoState = bundle.getInt( STATE );
		progress = bundle.getInt( PROGRESS );
		if (protoState == W_ALLY) alignment = Alignment.ENEMY;
		else if (protoState == H_ALLY) alignment = Alignment.ALLY;
		else alignment = Alignment.NEUTRAL;
	}
}
