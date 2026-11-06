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

/**
 * 破坏球。
 *
 * 投掷：飞出去后撞到墙或敌人就按反射角弹开，每弹一次距离缩短，停稳后结束回合。
 * 放下：普通物品落地，站旁边按住动作按钮蓄力，箭头方向即发射方向。
 */
public class WreckingBall extends MissileWeapon {

	public static final int MAX_THROW_DISTANCE = 2;

	private static final int THROWN_RANGE = 6;

	/** 最多反射次数 */
	public static final int MAX_BOUNCES = 3;

	/** 普通投掷时的反射次数 */
	private static final int THROWN_BOUNCES = 3;

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

						//每次投出消耗一次耐久
						ball.decrementDurability();

						int w = Dungeon.level.width();
						int dx = Integer.signum((cell % w) - (user.pos % w));
						int dy = Integer.signum((cell / w) - (user.pos / w));
						if (dx == 0 && dy == 0) {
							dx = 1;
						}

						fly(user, ball, cell, dx, dy, THROWN_RANGE, 1.2f, delay, THROWN_BOUNCES);
					}
				});
	}

	/** 蓄力后从地面起飞：方向由箭头决定，蓄力决定距离/伤害/反射次数 */
	public static void launchFromGround(Hero user, WreckingBall ball, int cell, int charges, int dirX, int dirY) {
		if (user == null || ball == null || cell < 0) {
			return;
		}
		if (dirX == 0 && dirY == 0) {
			dirX = 1;
		}

		int c = Math.max(0, Math.min(WreckingBallControl.MAX_CHARGE, charges));
		int range = 4 + c;
		float power = 0.6f + 0.12f * c;
		float delay = 0.5f + 0.1f * c;
		int bounces = bouncesForCharge(c);

		//每次击飞也消耗一次耐久
		ball.decrementDurability();

		user.busy();
		fly(user, ball, cell, dirX, dirY, range, power, delay, bounces);
	}

	/** 蓄力分三段，每段 +1 次反射，最多 3 次 */
	public static int bouncesForCharge(int charges) {
		if (charges <= 0) {
			return 0;
		}
		int perSegment = Math.max(1, WreckingBallControl.MAX_CHARGE / 3);
		return Math.min(MAX_BOUNCES, (charges + perSegment - 1) / perSegment);
	}

	private static void fly(final Hero user, final WreckingBall ball,
	                        final int from, final int dirX, final int dirY,
	                        final int range, final float power, final float delay,
	                        final int bouncesLeft) {
		if (user == null || !user.isAlive() || range <= 0) {
			finish(user, ball, from, delay);
			return;
		}

		int w = Dungeon.level.width();
		int cur = from;
		Char target = null;
		boolean blocked = false;

		for (int step = 1; step <= range; step++) {
			int next = cur + dirY * w + dirX;
			if (!Dungeon.level.insideMap(next)
					|| Dungeon.level.solid[next]
					|| !Dungeon.level.passable[next]) {
				blocked = true;
				break;
			}
			Char ch = Actor.findChar(next);
			if (ch != null) {
				target = ch;
				cur = next;
				blocked = true;
				break;
			}
			cur = next;
		}

		if (cur == from) {
			//起点就被挡住：还有反射次数就换向，否则就地停下
			if (bouncesLeft > 0) {
				int[] dir = reflect(from, dirX, dirY);
				fly(user, ball, from, dir[0], dir[1], range, power, delay, bouncesLeft - 1);
			} else {
				finish(user, ball, from, delay);
			}
			return;
		}

		final int landing = cur;
		final Char hit = target;
		final boolean collided = blocked;

		((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).reset(
				from, landing, ball, new Callback() {
					@Override
					public void call() {
						if (hit != null && hit.isAlive() && hit != user) {
							int dmg = Math.max(1, Math.round(ball.damageRoll(user) * power));
							hit.damage(new DamageInfo(dmg, DamageType.PHYSICAL, user));
						}

						if (collided && bouncesLeft > 0) {
							//撞到墙壁或敌人才反射
							int[] dir = reflect(landing, dirX, dirY);
							fly(user, ball, landing, dir[0], dir[1], range, power, delay, bouncesLeft - 1);
						} else {
							//没撞到东西（飞满距离）或者反射次数用完，落地
							finish(user, ball, landing, delay);
						}
					}
				});
	}

	/** 镜面反射：按被挡住的轴翻转方向分量 */
	private static int[] reflect(int from, int dirX, int dirY) {
		int w = Dungeon.level.width();
		boolean xFree = isFree(from + dirX);
		boolean yFree = isFree(from + dirY * w);

		if (!xFree && !yFree) {
			return new int[]{-dirX, -dirY};
		}
		if (!xFree) {
			return new int[]{-dirX, dirY};
		}
		if (!yFree) {
			return new int[]{dirX, -dirY};
		}
		//对角被角色挡住等情况，直接原路弹回
		return new int[]{-dirX, -dirY};
	}

	private static boolean isFree(int cell) {
		return Dungeon.level.insideMap(cell)
				&& !Dungeon.level.solid[cell]
				&& Dungeon.level.passable[cell];
	}

	private static void finish(Hero user, WreckingBall ball, int cell, float delay) {
		//耐久耗尽时物品已损毁，不再落地
		if (ball != null && ball.durabilityLeft() > 0 && cell >= 0 && Dungeon.level.insideMap(cell)) {
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