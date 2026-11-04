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

/**
 * 落地后的破坏球。停驻时每回合积攒 1 点蓄力（上限 {@link #MAX_CHARGE}），
 * 被击飞后沿当前方向移动，遇到墙壁或角色则反弹，并把当前武器伤害乘以蓄力系数施加给目标。
 */
public class WreckingBallMob extends Mob {

	public static final int MAX_CHARGE = 10;

	{
		spriteClass = WreckingBallSprite.class;

		alignment = Alignment.NEUTRAL;
		HP = HT = 1;
		EXP = 0;
		properties.add(Property.INORGANIC);
		state = WANDERING;
		actPriority = MOB_PRIO + 1;
	}

	/** 当前蓄力点数 */
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
			return true;
		}

		if (!launched) {
			if (charge < MAX_CHARGE) {
				charge++;
			}
			spend(TICK);
			return true;
		}

		step();
		spend(TICK);
		return true;
	}

	/** 由英雄触发，朝远离英雄的方向击飞 */
	public void launch(Hero hero) {
		if (launched || !isAlive()) {
			return;
		}

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
			reflect();
			energy--;
			return;
		}

		Char ch = Actor.findChar(next);
		if (ch != null) {
			hit(ch);
			reflect();
			energy--;
			return;
		}

		move(next);
		energy--;

		if (energy <= 0) {
			land();
		}
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

	/** 撞墙或撞人后的反弹：优先水平翻转，其次垂直翻转，都不可行则原路返回 */
	private void reflect() {
		int w = Dungeon.level.width();
		int flipX = pos + dy * w - dx;
		int flipY = pos - dy * w + dx;

		if (canEnter(flipX)) {
			dx = -dx;
		} else if (canEnter(flipY)) {
			dy = -dy;
		} else {
			dx = -dx;
			dy = -dy;
		}
	}

	private boolean canEnter(int cell) {
		if (!Dungeon.level.insideMap(cell)) {
			return false;
		}
		return !Dungeon.level.solid[cell] && Dungeon.level.passable[cell] && Actor.findChar(cell) == null;
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

	private static final String CHARGE = "charge";
	private static final String LAUNCHED = "launched";
	private static final String DX = "dx";
	private static final String DY = "dy";
	private static final String ENERGY = "energy";
	private static final String POWER = "power";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHARGE, charge);
		bundle.put(LAUNCHED, launched);
		bundle.put(DX, dx);
		bundle.put(DY, dy);
		bundle.put(ENERGY, energy);
		bundle.put(POWER, power);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		charge = bundle.getInt(CHARGE);
		launched = bundle.getBoolean(LAUNCHED);
		dx = bundle.getInt(DX);
		dy = bundle.getInt(DY);
		energy = bundle.getInt(ENERGY);
		power = bundle.getFloat(POWER);
	}
}
