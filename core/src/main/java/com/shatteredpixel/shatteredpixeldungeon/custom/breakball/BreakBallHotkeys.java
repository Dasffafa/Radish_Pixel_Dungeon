package com.shatteredpixel.shatteredpixeldungeon.custom.breakball;

import com.badlogic.gdx.Input;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BreakBallEntity;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.BreakBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Group;
import com.watabou.utils.Signal;

public class BreakBallHotkeys extends Group {

	private final Signal.Listener<KeyEvent> keyListener = new Signal.Listener<KeyEvent>() {
		@Override
		public boolean onSignal( KeyEvent event ) {
			if ( !event.pressed ) return false;
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

	public BreakBallHotkeys() {
		KeyEvent.addKeyListener( keyListener );
	}

	@Override
	public void destroy() {
		KeyEvent.removeKeyListener( keyListener );
		super.destroy();
	}

	private void recall() {
		if ( Dungeon.hero == null || !Dungeon.hero.ready ) return;
		BreakBallEntity entity = BreakBall.findEntity();
		if ( entity == null ) return;
		if ( BreakBallFlight.isFlying() ) {
			GLog.w( Messages.get( BreakBall.class, "flight_active" ) );
			return;
		}
		if ( BreakBall.recallEntity( entity ) ) {
			Dungeon.hero.spendAndNext( 1f );
		}
	}

	private void quickFire() {
		if ( Dungeon.hero == null || !Dungeon.hero.ready ) return;
		if ( BreakBall.findEntity() == null && Dungeon.hero.belongings.getItem( BreakBall.class ) == null ) return;
		if ( BreakBallFlight.isFlying() ) {
			GLog.w( Messages.get( BreakBall.class, "flight_active" ) );
			return;
		}
		if ( BreakBall.quickFire() ) {
			Dungeon.hero.spendAndNext( 1f );
		} else {
			GLog.w( Messages.get( BreakBall.class, "miss" ) );
		}
	}
}
