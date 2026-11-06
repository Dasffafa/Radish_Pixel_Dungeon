package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.timing.VirtualActor;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 千本：一次扇形甩出多枚细针，各针按极短间隔依次出手，每针出手时才计算自己的朝向。
 * 每枚针各自结算伤害，沿直线飞行直到撞上墙壁或敌人，撞上即消失，不会落地。
 * 物品本身留在背包里，每次投掷扣 1 点耐久，耐久耗尽后整束消失。
 */
public class Senbon extends MissileWeapon {

	/** 每次发射的针数 */
	private static final int SHOTS = 12;

	/** 相邻两针的发射间隔（秒） */
	private static final float SHOT_INTERVAL = 0.05f;

	/** 扇形总夹角（弧度） */
	private static final double SPREAD = Math.toRadians(40);

	/** 单枚伤害占武器伤害的比例 */
	private static final float DAMAGE_FACTOR = 0.18f;

	/** 针贴图缩放，保留纤细感 */
	private static final float VISUAL_SCALE = 0.6f;

	{
		image = ItemSpriteSheet.SENBON;

		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.2f;

		tier = 1;
		sticky = false;
		stackable = false;

		baseUses = 10;
	}

	@Override
	public int max(int lvl) {
		return 3 + lvl;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public void cast(final Hero user, final int dst) {
		final int cell = throwPos(user, dst);

		user.sprite.zap(cell);
		//整轮扇形打完前阻塞英雄，避免后半程针还在出手时玩家已经行动
		user.playAnimationNoTime(null);
		throwSound();

		final float delay = castDelay(user, dst);

		for (int i = 0; i < SHOTS; i++) {
			//第一枚立即出手，其余按固定间隔依次发射
			if (i == 0) {
				fireShot(user, cell, i, delay);
			} else {
				final int index = i;
				VirtualActor.delay(SHOT_INTERVAL * i, () -> fireShot(user, cell, index, delay));
			}
		}

		decrementDurability();
		if (durabilityLeft() <= 0) {
			detach(user.belongings.backpack);
		}

		Item.updateQuickslot();
	}

	private void fireShot(Hero user, int targetCell, int index, float delay) {
		if (user.sprite != null && user.sprite.parent != null) {
			int w = Dungeon.level.width();
			int fx = user.pos % w;
			int fy = user.pos / w;
			double baseAngle = Math.atan2((targetCell / w) - fy, (targetCell % w) - fx);

			//在扇形范围内均匀分布，再加一点点随机抖动，避免完全对称
			double offset = SHOTS == 1 ? 0 : -SPREAD / 2 + SPREAD * index / (SHOTS - 1d);
			offset += (Random.Float() - 0.5f) * (SPREAD / SHOTS) * 0.6;
			fireOne(user, baseAngle + offset);
		}

		//最后一针出手后才结算回合并解除行动阻塞
		if (index == SHOTS - 1) {
			user.spendAndNext(delay);
			//这里只清标记，不用 finishAnimationNoTime：它会立刻 ready()，让玩家在敌人行动前就能操作
			user.animationBusy = false;
		}
	}

	private void fireOne(Hero user, double angle) {
		int w = Dungeon.level.width();
		int h = Dungeon.level.height();
		int fx = user.pos % w;
		int fy = user.pos / w;

		double dx = Math.cos(angle);
		double dy = Math.sin(angle);

		//沿射线一直延伸到地图边缘
		double xReach = dx > 0 ? (w - 1 - fx) / dx : dx < 0 ? -fx / dx : Double.MAX_VALUE;
		double yReach = dy > 0 ? (h - 1 - fy) / dy : dy < 0 ? -fy / dy : Double.MAX_VALUE;
		double reach = Math.min(xReach, yReach);
		reach = Math.max(0, reach);

		int endX = Math.max(0, Math.min(w - 1, fx + (int) Math.round(dx * reach)));
		int endY = Math.max(0, Math.min(h - 1, fy + (int) Math.round(dy * reach)));

		Ballistica line = new Ballistica(user.pos, endX + endY * w, Ballistica.STOP_SOLID);
		int impact = line.collisionPos != null ? line.collisionPos : endX + endY * w;

		//找到路径上第一个活着的敌人
		Char target = null;
		for (Integer c : line.subPath(0, line.dist)) {
			if (c == user.pos) {
				continue;
			}
			Char ch = Actor.findChar(c);
			if (ch != null && ch != user && ch.alignment != user.alignment && ch.isAlive()) {
				target = ch;
				impact = c;
				break;
			}
		}

		final int impactCell = impact;
		final Char hitTarget = target;
		final Hero hero = user;

		MissileSprite ms = (MissileSprite) user.sprite.parent.recycle(MissileSprite.class);
		ms.reset(user.sprite, impactCell, this, new Callback() {
			@Override
			public void call() {
				//针真正扎到身上时才结算伤害
				if (hitTarget != null && hitTarget.isAlive()) {
					int dmg = Math.max(1, Math.round(hero.damageRoll() * DAMAGE_FACTOR));
					hitTarget.damage(DamageInfo.physical(dmg, hero, Senbon.this));
				}
				CellEmitter.center(impactCell).burst(
						Speck.factory(hitTarget != null ? Speck.RED_LIGHT : Speck.WOOL),
						hitTarget != null ? 4 : 2);
			}
		});
		ms.scale.set(VISUAL_SCALE, VISUAL_SCALE);
	}

}