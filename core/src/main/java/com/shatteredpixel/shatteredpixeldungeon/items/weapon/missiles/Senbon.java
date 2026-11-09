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
		throwSound();

		final float delay = castDelay(user, dst);

		int w = Dungeon.level.width();
		int fx = user.pos % w;
		int fy = user.pos / w;
		double baseAngle = Math.atan2((cell / w) - fy, (cell % w) - fx);

		ShotScheduler scheduler = new ShotScheduler(this, user, baseAngle, delay);
		user.sprite.parent.add(scheduler);

		decrementDurability();
		if (durabilityLeft() <= 0) {
			detach(user.belongings.backpack);
		}
		Item.updateQuickslot();
	}

	//逐帧推进的发射调度器，不依赖 Actor 队列，避免英雄动画阻塞时回合卡死
	private static class ShotScheduler extends com.watabou.noosa.Group {
		private final Senbon weapon;
		private final Hero hero;
		private final double baseAngle;
		private final float delay;
		private int index = 0;
		private float acc = 0f;

		ShotScheduler(Senbon weapon, Hero hero, double baseAngle, float delay) {
			this.weapon = weapon;
			this.hero = hero;
			this.baseAngle = baseAngle;
			this.delay = delay;
		}

		@Override
		public void update() {
			super.update();
			if (hero == null || !hero.isAlive()) {
				killAndErase();
				return;
			}
			acc += com.watabou.noosa.Game.elapsed;
			while (acc >= SHOT_INTERVAL && index < SHOTS) {
				acc -= SHOT_INTERVAL;
				double offset = SHOTS == 1 ? 0 : -SPREAD / 2 + SPREAD * index / (SHOTS - 1d);
				offset += (Random.Float() - 0.5f) * (SPREAD / SHOTS) * 0.6;
				weapon.fireOne(hero, baseAngle + offset);
				index++;
			}
			if (index >= SHOTS) {
				hero.spendAndNext(delay);
				killAndErase();
			}
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