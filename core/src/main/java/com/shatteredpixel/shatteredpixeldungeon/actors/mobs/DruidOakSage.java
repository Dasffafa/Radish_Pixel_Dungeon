package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DruidOakSageSprite;
import com.watabou.utils.Bundle;

/** 德鲁伊召唤物：橡木智者（无敌支援）。 */
public class DruidOakSage extends Mob {

	{
		spriteClass = DruidOakSageSprite.class;
		// 中立：敌人选目标时只在友方单位和英雄里挑，中立单位不会被当成攻击目标（原版的羊、莲花也是这样避开火力的）。
		// 下面的光环按「目标是不是友方」发放，与它自己的阵营无关。
		alignment = Alignment.NEUTRAL;
		state = WANDERING;
	}

	public int lifespan = 200;

	@Override
	public boolean isInvulnerable( Class damageType ) {
		return true;
	}

	// 橡木智者不受任何伤害，也从来没有设过生命值，HP 和 HT 都是 0；而底层用「HP 大于 0」判断一个单位是否还在场上。
	// 用放大镜查看它时因此被当成已经死掉的单位，只提示「你不清楚那里有什么」，名字和说明都看不到。
	// 这里只让「是否在场」按实际情况回答。不要改成给它补生命值：有了生命值，英雄点它就会和它换位，它就不再是原地不动的了。
	@Override
	public boolean isActive() {
		return Actor.chars().contains( this );
	}

	// 变成中立之后，英雄这边「对视野内所有非友方生效」的效果（恐惧、盛怒、魅惑之类）也会扫到它。
	// 它不该被吓跑、激怒或魅惑，所以和原版的羊、莲花一样不接受任何状态。
	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	protected boolean act() {
		if (--lifespan <= 0) {
			die( null );
			return true;
		}
		int pts = Dungeon.hero != null
				? Dungeon.hero.pointsInTalent( com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.NATURE_BOND )
				: 0;
		int radius = 2 + pts;
		int shield = 2 + Math.round( pts * 0.5f );
		// 护盾只补到当前楼层数为止，楼层的取法与自然能量上限里的「当前楼层」相同
		int cap = Dungeon.depth;
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Alignment.ALLY && ch != this
					&& Dungeon.level.distance( pos, ch.pos ) <= radius) {
				// 按目标身上所有来源的护盾合计来算。已经到上限就完全不碰它：
				// 不再加，不削减别的来源给的护盾，也不打断那些护盾自己的衰减
				int room = cap - ch.shielding();
				if (room <= 0) {
					continue;
				}
				Barrier barrier = Buff.affect( ch, Barrier.class );
				int had = barrier.shielding();
				barrier.incShield( Math.min( shield, room ) );
				// 英雄获得护盾的量可能被别的效果放大，补完超过上限时，只把这一次多出来的部分收回
				int over = ch.shielding() - cap;
				if (over > 0) {
					barrier.decShield( Math.min( over, barrier.shielding() - had ) );
				}
			}
		}
		Hero h = Dungeon.hero;
		if (h != null && h.isAlive() && Dungeon.level.distance( pos, h.pos ) <= radius) {
			Buff.affect( h, Haste.class, 1f );
		}
		return super.act();
	}

	// 说明文字写明橡木智者不会移动：追击、游荡、逃跑时都留在原地
	@Override
	protected boolean getCloser( int target ) {
		return false;
	}

	@Override
	protected boolean getFurther( int target ) {
		return false;
	}

	// 它的伤害和命中都是 0，出手打不出伤害，却要等一整段攻击动画放完才轮到别人行动。
	// 身边有敌人时每回合都会卡上一秒左右，所以干脆不让它出手。
	@Override
	protected boolean canAttack( Char enemy ) {
		return false;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill( Char target ) {
		return 0;
	}

	private static final String LIFESPAN = "lifespan";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LIFESPAN, lifespan );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		lifespan = bundle.getInt( LIFESPAN );
	}
}
