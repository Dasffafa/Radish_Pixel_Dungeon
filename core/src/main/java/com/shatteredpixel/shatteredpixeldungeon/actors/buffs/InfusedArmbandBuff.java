package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

/**
 * 灌能臂缠（次要护甲）的临时增伤：使用法杖造成伤害后 10 回合内，伤害 +bonus。
 */
public class InfusedArmbandBuff extends FlavourBuff {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private int bonus = 1;

	public int bonus() {
		return bonus;
	}

	public static void apply( Hero hero, int bonus, float duration ) {
		InfusedArmbandBuff b = Buff.affect( hero, InfusedArmbandBuff.class, duration );
		b.bonus = Math.max( b.bonus, bonus );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "bonus", bonus );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		bonus = bundle.getInt( "bonus" );
	}
}
