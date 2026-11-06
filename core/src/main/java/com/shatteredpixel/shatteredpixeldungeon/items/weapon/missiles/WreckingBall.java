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
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 破坏球。
 *
 * 两种用法：
 * - 投掷：直接飞出去，落地后立刻在墙壁和怪物之间随机弹射
 * - 放下：停在脚边，需要靠近后按住动作按钮蓄力，再击飞
 */
public class WreckingBall extends MissileWeapon {

	public static final String AC_PLACE = "PLACE";

	/** 破坏球能投出的最大格数 */
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
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_PLACE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (AC_PLACE.equals(action)) {
			place(hero);
			return;
		}
		super.execute(hero, action);
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

	/** 投掷：落地即开始弹射 */
	@Override
	protected void onThrow(int cell) {
		Hero hero = Dungeon.hero;
		int dirX = 1;
		int dirY = 0;
		if (hero != null) {
			int w = Dungeon.level.width();
			dirX = Integer.signum((cell % w) - (hero.pos % w));
			dirY = Integer.signum((cell / w) - (hero.pos / w));
			if (dirX == 0 && dirY == 0) {
				dirX = 1;
			}
		}

		WreckingBallMob ball = spawnBall(cell);
		if (ball != null) {
			ball.launchThrown(hero, dirX, dirY);
		}
	}

	/** 放下：停在脚边，等待蓄力 */
	private void place(Hero hero) {
		int cell = -1;
		for (int i : PathFinder.NEIGHBOURS8) {
			int c = hero.pos + i;
			if (Dungeon.level.insideMap(c) && Dungeon.level.passable[c] && Actor.findChar(c) == null) {
				cell = c;
				break;
			}
		}
		if (cell == -1) {
			GLog.w(Messages.get(this, "no_space"));
			return;
		}

		if (spawnBall(cell) == null) {
			return;
		}

		Sample.INSTANCE.play(Assets.Sounds.MISS);
		hero.sprite.operate(hero.pos);
		detachAll(hero.belongings.backpack);
		hero.spendAndNext(1f);
		Item.updateQuickslot();
	}

	private WreckingBallMob spawnBall(int cell) {
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
			} else {
				return null;
			}
		}

		WreckingBallMob ball = new WreckingBallMob();
		ball.pos = cell;
		GameScene.add(ball);

		if (Dungeon.hero != null) {
			Buff.affect(Dungeon.hero, WreckingBallControl.class).setBall(ball);
		}
		return ball;
	}

	@Override
	public int value() {
		return 120 * quantity;
	}
}
