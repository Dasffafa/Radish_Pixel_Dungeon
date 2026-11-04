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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WreckingBallControl;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WreckingBallMob;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 破坏球。
 *
 * 只能投出很短的距离，落地后停在原地蓄力；蓄力完成后可以用动作按钮把它击飞出去，
 * 球会在墙壁与敌人之间反复弹射，撞击敌人造成基于当前武器伤害与蓄力系数的伤害，
 * 弹回英雄身上则会伤到英雄自己。
 */
public class WreckingBall extends MissileWeapon {

	/** 破坏球能飞出的最大格数 */
	public static final int MAX_THROW_DISTANCE = 2;

	{
		image = ItemSpriteSheet.WRECKING_BALL;

		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.8f;

		tier = 6;
		baseUses = 10;
		sticky = false;
	}

	@Override
	public int max(int lvl) {
		return 4 * tier + tier * lvl;
	}

	@Override
	public int throwPos(Hero user, int dst) {
		//只能飞很短的距离
		Ballistica bolt = new Ballistica(user.pos, dst, Ballistica.PROJECTILE);
		if (bolt.dist > MAX_THROW_DISTANCE) {
			return bolt.path.get(MAX_THROW_DISTANCE);
		}
		return super.throwPos(user, dst);
	}

	@Override
	protected void onThrow(int cell) {
		if (Actor.findChar(cell) != null) {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8) {
				int c = cell + i;
				if (Dungeon.level.insideMap(c) && Dungeon.level.passable[c] && Actor.findChar(c) == null) {
					candidates.add(c);
				}
			}
			if (!candidates.isEmpty()) {
				cell = Random.element(candidates);
			}
		}

		WreckingBallMob ball = new WreckingBallMob();
		ball.pos = cell;
		GameScene.add(ball);

		if (Dungeon.hero != null) {
			Buff.affect(Dungeon.hero, WreckingBallControl.class).setBall(ball);
		}

		//物品本身不落地：落地的是球，物品已经化作球
	}

	@Override
	public int value() {
		return 120 * quantity;
	}
}
