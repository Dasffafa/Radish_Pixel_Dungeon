package com.shatteredpixel.shatteredpixeldungeon.levels.features;

/** 门的行为类型，由 LevelTransition 与 Level.doorGates 持有。 */
public enum GateType {

	TWO_WAY,
	ONE_WAY,
	SINGLE_USE,
	BOMB_ONLY,
	CYCLE_ABC,
	CONDITIONAL

}