/*
 * Radish Pixel Dungeon
 *
 * 奶蛋生物：装备「奶蛋」时生成在地面上。
 * 纯装饰：不移动、不攻击、无经验、不参与随机生成。
 * 不可交互：不能与英雄交换位置，英雄也不能从它所在格子穿过。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NaidanEggSprite;

public class NaidanEgg extends Mob {

	{
		spriteClass = NaidanEggSprite.class;

		HP = HT = 20;
		defenseSkill = 0;
		EXP = 0;

		alignment = Alignment.NEUTRAL;
		state = PASSIVE;

		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	/** 完全被动：只消耗时间，不做任何 AI 行为。 */
	@Override
	protected boolean act() {
		spend( TICK );
		return true;
	}

	/** 拒绝一切交换位置（英雄点击/撞上时默认会互换站位），也就挡住了穿越。 */
	@Override
	public boolean interact( Char c ) {
		return true;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void beckon( int cell ) {
		// 不会被呼唤移动
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill( Char target ) {
		return 0;
	}
}
