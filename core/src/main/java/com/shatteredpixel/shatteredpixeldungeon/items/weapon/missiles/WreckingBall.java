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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WreckingBallControl;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 破坏球。
 *
 * 投掷出去后像普通投掷物一样飞行；撞到墙壁或敌人就随机换个方向再"投"一次，
 * 每次飞行距离递减，直到停下为止。整段飞行结束后英雄的回合才结束。
 */
public class WreckingBall extends MissileWeapon {

	/** 投掷时能瞄准的最大格数 */
	public static final int MAX_THROW_DISTANCE = 2;

	/** 第一段弹射的飞行距离 */
	private static final int THROWN_RANGE = 4;

	private static final int[] DX = {1, -1, 0, 0, 1, 1, -1, -1};
	private static final int[] DY = {0, 0, 1, -1, 1, -1, 1, -1};

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
		//只能瞄准很短的距离
		Ballistica bolt = new Ballistica(user.pos, dst, Ballistica.PROJECTILE);
		if (bolt.dist > MAX_THROW_DISTANCE) {
			return bolt.path.get(MAX_THROW_DISTANCE);
		}
		return super.throwPos(user, dst);
	}

	@Override
	public void execute(Hero hero, String action) {
		if (AC_DROP.equals(action)) {
			place(hero);
			return;
		}
		super.execute(hero, action);
	}

	/** 放下：像普通物品一样落在脚下，之后可以站到旁边蓄力 */
	private void place(Hero hero) {
		GameScene.cancel();
		curUser = hero;
		curItem = this;

		Item item = detach(hero.belongings.backpack);
		if (item == null) {
			return;
		}

		Heap heap = Dungeon.level.drop(item, hero.pos);
		heap.sprite.drop();

		Buff.affect(hero, WreckingBallControl.class).setBall((WreckingBall) item, hero.pos);

		hero.spendAndNext(1f);
		Item.updateQuickslot();
	}

	/** 蓄力后从地面起飞 */
	public static void launchFromGround(Hero user, WreckingBall ball, int cell, int charges) {
		if (user == null || ball == null || cell < 0) {
			return;
		}
		int w = Dungeon.level.width();
		int dx = Integer.signum((cell % w) - (user.pos % w));
		int dy = Integer.signum((cell / w) - (user.pos / w));
		if (dx == 0 && dy == 0) {
			dx = 1;
		}
		float delay = 0.5f + 0.1f * charges;
		user.busy();
		fly(user, ball, cell, dx, dy, 1 + charges / 2, delay);
	}

	@Override
	public void cast(final Hero user, final int dst) {
		final int cell = throwPos(user, dst);

		user.sprite.zap(cell);
		user.busy();
		throwSound();

		final float delay = castDelay(user, dst);

		((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).reset(
				user.sprite, cell, this, new Callback() {
					@Override
					public void call() {
						curUser = user;
						Item i = WreckingBall.this.detach(user.belongings.backpack);
						if (i == null) {
							user.spendAndNext(delay);
							return;
						}
						WreckingBall ball = (WreckingBall) i;

						int w = Dungeon.level.width();
						int dx = Integer.signum((cell % w) - (user.pos % w));
						int dy = Integer.signum((cell / w) - (user.pos / w));
						if (dx == 0 && dy == 0) {
							dx = 1;
						}

						fly(user, ball, cell, dx, dy, THROWN_RANGE, delay);
					}
				});
	}

	/** 一段飞行：从 from 沿方向飞最多 range 格，撞到东西就随机换向并把距离减一 */
	private static void fly(final Hero user, final WreckingBall ball,
	                        final int from, final int dirX, final int dirY,
	                        final int range, final float delay) {
		if (range <= 0 || user == null || !user.isAlive()) {
			finish(user, ball, from, delay);
			return;
		}

		int w = Dungeon.level.width();
		int cur = from;
		Char target = null;

		for (int step = 1; step <= range; step++) {
			int next = cur + dirY * w + dirX;
			if (!Dungeon.level.insideMap(next)
					|| Dungeon.level.solid[next]
					|| !Dungeon.level.passable[next]) {
				break;
			}
			Char ch = Actor.findChar(next);
			if (ch != null) {
				target = ch;
				cur = next;
				break;
			}
			cur = next;
		}

		if (cur == from) {
			//起点就被挡住，直接换个方向（距离必须递减，避免死循环）
			int[] dir = randomDir(from, dirX, dirY);
			if (dir == null) {
				finish(user, ball, from, delay);
			} else {
				fly(user, ball, from, dir[0], dir[1], range - 1, delay);
			}
			return;
		}

		final int landing = cur;
		final Char hit = target;

		((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).reset(
				from, landing, ball, new Callback() {
					@Override
					public void call() {
						if (hit != null && hit.isAlive() && hit != user) {
							int dmg = Math.max(1, Math.round(ball.rollDamage(user) * 1.2f));
							hit.damage(new DamageInfo(dmg, DamageType.PHYSICAL, user));
						}

						int[] dir;
						if (hit != null) {
							//撞到东西：随机换方向
							dir = randomDir(landing, dirX, dirY);
						} else {
							//没撞到就继续沿原方向，只是距离变短
							dir = new int[]{dirX, dirY};
						}

						if (dir == null) {
							finish(user, ball, landing, delay);
						} else {
							fly(user, ball, landing, dir[0], dir[1], range - 1, delay);
						}
					}
				});
	}

	private int rollDamage(Hero user) {
		int base = damageRoll(user);
		return Math.max(1, base);
	}

	/** 随机挑一个能走的方向，优先不原路返回 */
	private static int[] randomDir(int cell, int fromDx, int fromDy) {
		java.util.ArrayList<int[]> options = new java.util.ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (DX[i] == -fromDx && DY[i] == -fromDy) {
				continue;
			}
			if (stepFree(cell, DX[i], DY[i])) {
				options.add(new int[]{DX[i], DY[i]});
			}
		}
		if (options.isEmpty()) {
			for (int i = 0; i < 8; i++) {
				if (stepFree(cell, DX[i], DY[i])) {
					options.add(new int[]{DX[i], DY[i]});
				}
			}
		}
		if (options.isEmpty()) {
			return null;
		}
		return options.get(Random.Int(options.size()));
	}

	private static boolean stepFree(int cell, int dx, int dy) {
		int next = cell + dy * Dungeon.level.width() + dx;
		return Dungeon.level.insideMap(next)
				&& !Dungeon.level.solid[next]
				&& Dungeon.level.passable[next]
				&& Actor.findChar(next) == null;
	}

	private static void finish(Hero user, WreckingBall ball, int cell, float delay) {
		if (ball != null && cell >= 0 && Dungeon.level.insideMap(cell)) {
			Dungeon.level.drop(ball, cell).sprite.drop();
			if (user != null && user.isAlive() && Dungeon.level.heroFOV[cell]) {
				GLog.i(Messages.get(WreckingBall.class, "landed"));
			}
		}
		if (user != null) {
			user.spendAndNext(delay);
			Item.updateQuickslot();
		}
	}
}
