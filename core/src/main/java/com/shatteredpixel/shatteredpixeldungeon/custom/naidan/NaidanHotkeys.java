/*
 * Radish Pixel Dungeon — 奶蛋快捷键
 *
 * 桌面端快捷操作（各耗一回合，仅在英雄就绪时生效）：
 *  · C = 收回：奶蛋从原地滚向英雄，接触到英雄后消散（路被挡会撞墙反弹继续滚，6 秒兜底强制回收）；
 *  · V = 快速发射：奶蛋从英雄身上沿鼠标方向满力直接射出（不做击球点接触判定）。
 *
 * 监听挂在 GameScene 生命周期上（create 注册 / destroy 注销），
 * 只在地牢场景内响应，菜单界面按 C/V 不会误触发。
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.naidan;

import com.badlogic.gdx.Input;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NaidanEgg;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Naidan;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Group;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Signal;

public class NaidanHotkeys extends Group {

	private final Signal.Listener<KeyEvent> keyListener = new Signal.Listener<KeyEvent>() {
		@Override
		public boolean onSignal( KeyEvent event ) {
			if ( !event.pressed ) return false;
			// 仪器化：C/V 的每一次按下与被吞原因都要留日志（问题排查后随功能收尾一起摘除）
			String key = event.code == Input.Keys.C ? "C" : event.code == Input.Keys.V ? "V" : null;
			if ( key != null ) {
				String why = null;
				if ( Dungeon.hero == null ) why = "hero_null";
				else if ( !Dungeon.hero.ready ) why = "hero_not_ready";
				else if ( findEgg() == null && key.equals( "C" ) ) why = "no_egg_for_recall";
				else if ( !isNaidanEquipped() ) why = "not_equipped";
				else if ( NaidanFlight.isFlying() ) why = "flight_active";
				DeviceCompat.log( "Naidan", "hotkey " + key + " pressed"
						+ ( why != null ? " BLOCKED: " + why : " -> dispatch" ) );
			}
			if ( event.code == Input.Keys.C ) {
				recall();
				return true;
			}
			if ( event.code == Input.Keys.V ) {
				quickFire();
				return true;
			}
			return false;
		}
	};

	public NaidanHotkeys() {
		KeyEvent.addKeyListener( keyListener );
	}

	@Override
	public void destroy() {
		KeyEvent.removeKeyListener( keyListener );
		super.destroy();
	}

	/** C：滚动回收（逻辑在 Naidan.recallEgg，与武器菜单共用） */
	private void recall() {
		NaidanEgg egg = findEgg();
		if ( egg == null || !Dungeon.hero.ready ) return;
		if ( !isNaidanEquipped() ) {
			GLog.w( Messages.get( Naidan.class, "ability_unequipped" ) );
			return;
		}
		if ( NaidanFlight.isFlying() ) {
			GLog.w( Messages.get( Naidan.class, "flight_active" ) );
			return;
		}
		if ( Naidan.recallEgg( egg ) ) {
			Dungeon.hero.spendAndNext( 1f );
		}
	}

	/** V：从英雄身上向鼠标方向满力直射（逻辑在 Naidan.quickFire） */
	private void quickFire() {
		if ( !Dungeon.hero.ready ) return;
		if ( !isNaidanEquipped() ) {
			GLog.w( Messages.get( Naidan.class, "ability_unequipped" ) );
			return;
		}
		if ( NaidanFlight.isFlying() ) {
			GLog.w( Messages.get( Naidan.class, "flight_active" ) );
			return;
		}
		if ( Naidan.quickFire() ) {
			Dungeon.hero.spendAndNext( 1f );
		} else {
			GLog.w( Messages.get( Naidan.class, "miss" ) );
		}
	}

	/** C/V 都要求奶蛋在装备栏（主手）——蛋是武器的衍生生物，卸下武器就没有这个能力 */
	private static boolean isNaidanEquipped() {
		return Dungeon.hero.belongings.weapon() instanceof Naidan;
	}

	private static NaidanEgg findEgg() {
		if ( Dungeon.level == null ) return null;
		for ( Char c : Actor.chars() ) {
			if ( c instanceof NaidanEgg && c.isAlive() ) return (NaidanEgg) c;
		}
		return null;
	}
}
