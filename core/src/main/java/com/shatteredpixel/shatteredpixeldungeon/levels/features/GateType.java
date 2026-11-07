package com.shatteredpixel.shatteredpixeldungeon.levels.features;

/**
 * 逻辑门的类型。每种门的行为与文本说明都集中在这里，
 * 由 LevelTransition 持有，英雄交互与格子信息面板统一读取。
 */
public enum GateType {

	/** 普通双向门（默认） */
	TWO_WAY,
	/** 单向门：只能从另一侧激活 */
	ONE_WAY,
	/** 一次性门：全流程只能用一次，用过后两侧都失效 */
	SINGLE_USE,
	/** 炸药门：必须被爆炸炸开才能通过 */
	BOMB_ONLY,
	/** 循环门：按 A->B->C->A 的顺序循环切换目的地 */
	CYCLE_ABC,
	/** 条件门：满足条件（当前为层数要求）才会出现并可通行 */
	CONDITIONAL

}