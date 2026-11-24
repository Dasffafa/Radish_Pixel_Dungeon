package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 「萝卜自走棋」挑战 smoke：开新局直接进棋盘，确认挑战生效、模式挂上、能跑若干回合。
 */
public class AutoChess_Smoke implements Scenario {

	@Override
	public String describe() {
		return "自走棋挑战：开局进棋盘、AutochessRun 挂上、能跑一段时间不崩";
	}

	@Override
	public void run( H h ) {
		h.newGame( HeroClasses.WARRIOR, "AUTOTEST", Challenges.AUTO_BATTLER );

		h.check( h.get( () -> Dungeon.isChallenged( Challenges.AUTO_BATTLER ) ), "自走棋挑战已开启" );
		h.check( h.get( () -> {
			for ( String n : Challenges.NAME_IDS ) {
				if ( "hongbao".equals( n ) || "three_minute".equals( n ) ) return false;
			}
			return Challenges.MASKS.length == Challenges.NAME_IDS.length;
		} ), "挑战列表里已经没有红包/三分钟，掩码数量一致" );
		h.check( h.get( () -> Dungeon.level != null
				&& Dungeon.level.getClass().getSimpleName().equals( "ChessBoardLevel" ) ), "进入棋盘关 ChessBoardLevel" );
		h.check( h.get( () -> AutochessRun.active() ), "AutochessRun.active() 为真" );
		h.check( h.get( () -> Dungeon.hero != null && Dungeon.hero.isAlive() ), "英雄活着" );

		h.frames( 120 );
		h.screenshot( "autochess-start" );

		h.check( h.get( () -> AutochessRun.get() != null ), "AutochessRun buff 已挂到英雄身上" );
		h.check( h.get( () -> Dungeon.hero.isAlive() ), "跑 120 帧后英雄还活着" );
	}
}
