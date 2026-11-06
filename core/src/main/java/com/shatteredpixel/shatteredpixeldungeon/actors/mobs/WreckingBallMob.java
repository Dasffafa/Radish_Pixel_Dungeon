/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.WreckingBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WreckingBallSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 落地后的破坏球。
 *
 * 放下时静置待命，靠近后可按住蓄力再击飞；
 * 投掷时直接飞出，之后每次碰撞都随机改变方向继续弹射。
 */
public class WreckingBallMob extends Mob {

	public static final int MAX_CHARGE = 10;

	/** 投掷后的初始飞行步数 */
	private static final int THROWN_ENERGY = 8;

	private static final int[] DX = {1, -1, 0, 0, 1, 1, -1, -1};
	private static final int[] DY = {0, 0, 1, -1, 1, -1, 1, -1};

	{
		spriteClass = WreckingBallSprite.class;

		alignment = Alignment.NEUTRAL;
		HP = HT = 1;
		EXP = 0;
		properties.add(Property.INORGANIC);
		state = WANDERING;
		actPriority = MOB_PRIO + 1;
	}

	/** 最近一次蓄力层数（仅用于展示） */
	public int charge = 0;

	private boolean launched = false;
	private int dx = 1;
	private int dy = 0;
	private int energy = 0;
	private float power = 1f;

	public boolean isLaunched() {
		return launched;
	}

	@Override
	protected boolean act() {
		if (!isAlive()) {
			//已死亡但仍在调度表里时必须移除并消耗时间，否则回合循环会空转
			Actor.remove(this);
			spend(TICK);
			return true;
		}

		if (!launched) {
			//静置待命，蓄力由玩家按住动作按钮提供
			spend(TICK);
			return true;
		}

		step();
		//step() 里可能已经落地死亡并移出调度表，此时不能再对已移除的 actor 计时
		if (isAlive()) {
			spend(TICK);
		}
		return true;
	}

	/** 投掷落地：直接沿投掷方向开始弹射 */
	public void launchThrown(Hero hero, int dirX, int dirY) {
		if (launched || !isAlive()) {
			return;
		}

		charge = 0;
		dx = dirX;
		dy = dirY;
		if (dx == 0 && dy == 0) {
			dx = 1;
		}

		energy = THROWN_ENERGY;
		power = 1.2f;
		launched = true;

		if (sprite != null) {
			((WreckingBallSprite) sprite).spin();
		}
		GLog.i(Messages.get(this, "thrown"));
	}

	/** 蓄力后由英雄击飞，朝远离英雄的方向 */
	public void launch(Hero hero, int charges) {
		if (launched || !isAlive()) {
			return;
		}

		charge = Math.max(0, Math.min(MAX_CHARGE, charges));

		int w = Dungeon.level.width();
		int hx = hero.pos % w;
		int hy = hero.pos / w;
		int bx = pos % w;
		int by = pos / w;

		dx = Integer.signum(bx - hx);
		dy = Integer.signum(by - hy);
		if (dx == 0 && dy == 0) {
			dx = 1;
		}

		energy = 2 + charge;
		power = 0.4f + 0.12f * charge;
		launched = true;

		if (sprite != null) {
			((WreckingBallSprite) sprite).spin();
		}
		GLog.i(Messages.get(this, "launch", charge));
	}

	/** 由英雄触发，把球收回并落回脚边 */
	public void recall(Hero hero) {
		if (!isAlive()) {
			return;
		}
		launched = false;
		Dungeon.level.drop(new WreckingBall(), hero.pos).sprite.drop();
		GLog.i(Messages.get(this, "recall"));
		die(this);
	}

	private void step() {
		if (energy <= 0) {
			land();
			return;
		}

		int w = Dungeon.level.width();
		int next = pos + dy * w + dx;

		if (!Dungeon.level.insideMap(next) || Dungeon.level.solid[next] || !Dungeon.level.passable[next]) {
			bounce();
			energy--;
			return;
		}

		Char ch = Actor.findChar(next);
		if (ch != null) {
			hit(ch);
			bounce();
			energy--;
			return;
		}

		//先让精灵播放位移动画，再更新坐标，否则球看起来只会原地转圈
		moveSprite(pos, next);
		move(next);
		energy--;

		if (energy <= 0) {
			land();
		}
	}

	/** 撞击后随机换一个方向继续弹射，优先不原路返回 */
	private void bounce() {
		java.util.ArrayList<Integer> candidates = new java.util.ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (DX[i] == -dx && DY[i] == -dy) {
				continue;
			}
			if (canStep(DX[i], DY[i])) {
				candidates.add(i);
			}
		}

		if (candidates.isEmpty()) {
			//四周只有来时的方向可走，就原路弹回
			for (int i = 0; i < 8; i++) {
				if (canStep(DX[i], DY[i])) {
					candidates.add(i);
				}
			}
		}

		if (candidates.isEmpty()) {
			dx = -dx;
			dy = -dy;
			return;
		}

		int pick = candidates.get(Random.Int(candidates.size()));
		dx = DX[pick];
		dy = DY[pick];
	}

	private boolean canStep(int stepX, int stepY) {
		int cell = pos + stepY * Dungeon.level.width() + stepX;
		return Dungeon.level.insideMap(cell)
				&& !Dungeon.level.solid[cell]
				&& Dungeon.level.passable[cell];
	}

	private void hit(Char ch) {
		int dmg = Math.max(1, Math.round(baseDamage() * power));

		if (ch == Dungeon.hero) {
			//弹回自己身上会伤到自己
			ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, Dungeon.hero));
			GLog.w(Messages.get(this, "self_hit", dmg));
		} else if (Dungeon.hero != null && ch.alignment != Dungeon.hero.alignment) {
			ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, Dungeon.hero));
		}
	}

	private int baseDamage() {
		if (Dungeon.hero == null) {
			return 1;
		}
		KindOfWeapon weapon = Dungeon.hero.belongings.attackingWeapon();
		if (weapon != null) {
			return weapon.damageRoll(Dungeon.hero);
		}
		return Dungeon.hero.damageRoll();
	}

	private void land() {
		if (!isAlive()) {
			return;
		}
		launched = false;
		Dungeon.level.drop(new WreckingBall(), pos).sprite.drop();
		die(this);
	}

	@Override
	protected boolean getCloser(int target) {
		return false;
	}

	@Override
	protected boolean getFurther(int target) {
		return false;
	}

	private static final String B_CHARGE = "charge";
	private static final String B_LAUNCHED = "launched";
	private static final String B_DX = "dx";
	private static final String B_DY = "dy";
	private static final String B_ENERGY = "energy";
	private static final String B_POWER = "power";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(B_CHARGE, charge);
		bundle.put(B_LAUNCHED, launched);
		bundle.put(B_DX, dx);
		bundle.put(B_DY, dy);
		bundle.put(B_ENERGY, energy);
		bundle.put(B_POWER, power);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		charge = bundle.getInt(B_CHARGE);
		launched = bundle.getBoolean(B_LAUNCHED);
		dx = bundle.getInt(B_DX);
		dy = bundle.getInt(B_DY);
		energy = bundle.getInt(B_ENERGY);
		power = bundle.getFloat(B_POWER);
	}
}
