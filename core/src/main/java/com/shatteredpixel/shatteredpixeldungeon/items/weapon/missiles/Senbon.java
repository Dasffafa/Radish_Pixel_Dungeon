package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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
 * 千本：一次扇形甩出多枚细针。
 * 每枚针各自结算伤害，沿直线飞行直到撞上墙壁或敌人，撞上即消失，不会落地。
 * 物品本身留在背包里，每次投掷扣 1 点耐久，耐久耗尽后整束消失。
 */
public class Senbon extends MissileWeapon {

	/** 每次发射的针数 */
	private static final int SHOTS = 12;

	/** 扇形总夹角（弧度） */
	private static final double SPREAD = Math.toRadians(40);

	/** 单枚伤害占武器伤害的比例 */
	private static final float DAMAGE_FACTOR = 0.18f;

	/** 针贴图缩放，看起来更小 */
	private static final float VISUAL_SCALE = 0.6f;

	/** 总可用次数（耐久） */
	public static final float USES = 30f;

	{
		image = ItemSpriteSheet.SENBON;

		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.2f;

		tier = 1;
		sticky = false;
		stackable = false;

		durability = USES;
	}

	@Override
	public int max(int lvl) {
		return 3 + lvl;
	}

	@Override
	public float durabilityPerUse() {
		return 1f;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public void cast(final Hero user, final int dst) {
		final int cell = throwPos(user, dst);

		user.sprite.zap(cell);
		user.busy();
		throwSound();

		final float delay = castDelay(user, dst);

		int w = Dungeon.level.width();
		int fx = user.pos % w;
		int fy = user.pos / w;
		int tx = cell % w;
		int ty = cell / w;
		double baseAngle = Math.atan2(ty - fy, tx - fx);

		for (int i = 0; i < SHOTS; i++) {
			//在扇形范围内均匀分布，再加一点点随机抖动，避免完全对称
			double offset = SHOTS == 1 ? 0 : -SPREAD / 2 + SPREAD * i / (SHOTS - 1d);
			offset += (Random.Float() - 0.5f) * (SPREAD / SHOTS) * 0.6;
			fireOne(user, baseAngle + offset);
		}

		decrementDurability();
		if (durabilityLeft() <= 0) {
			detach(user.belongings.backpack);
		}

		user.spendAndNext(delay);
		Item.updateQuickslot();
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

		//各自结算伤害（同步结算，避免飞行回调时目标已移动/死亡导致漏伤害）
		if (target != null && target.isAlive()) {
			int dmg = Math.max(1, Math.round(user.damageRoll() * DAMAGE_FACTOR));
			target.damage(DamageInfo.physicalNoArmor(dmg, this));
		}

		final int impactCell = impact;
		final boolean hitChar = target != null;

		MissileSprite ms = (MissileSprite) user.sprite.parent.recycle(MissileSprite.class);
		ms.reset(user.sprite, impactCell, this, new Callback() {
			@Override
			public void call() {
				//纯视觉：撞上东西时的火花
				CellEmitter.center(impactCell).burst(
						Speck.factory(hitChar ? Speck.RED_LIGHT : Speck.WOOL),
						hitChar ? 4 : 2);
			}
		});
		ms.scale.set(VISUAL_SCALE, VISUAL_SCALE);
	}

	@Override
	public String info() {
		if (durabilityLeft() > 0) {
			return super.info() + "\n\n" + Messages.get(this, "uses_left", (int) durabilityLeft());
		}
		return super.info();
	}
}