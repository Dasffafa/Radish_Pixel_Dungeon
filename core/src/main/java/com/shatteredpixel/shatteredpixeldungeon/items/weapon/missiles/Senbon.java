package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.utils.Callback;

/**
 * 千本：一次扇形甩出多枚细针，针不会落地。
 * 物品本身留在背包里，每次投掷扣 1 点耐久，耐久耗尽后整束消失。
 */
public class Senbon extends MissileWeapon {

	/** 每次发射的针数 */
	private static final int SHOTS = 5;

	/** 扇形总夹角（弧度） */
	private static final double SPREAD = Math.toRadians(24);

	/** 针贴图缩放，看起来更小 */
	private static final float VISUAL_SCALE = 0.6f;

	{
		image = ItemSpriteSheet.SENBON;

		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.2f;

		tier = 1;
		sticky = false;
		stackable = false;

		//整束可用次数
		durability = USES;
	}

	/** 总可用次数（耐久） */
	public static final float USES = 30f;

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

		//扇形同时射出
		for (int i = 0; i < SHOTS; i++) {
			double offset = SHOTS == 1 ? 0 : -SPREAD / 2 + SPREAD * i / (SHOTS - 1d);
			fireOne(user, baseAngle + offset);
		}

		//物品留在背包，只扣耐久
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

		double xReach = dx > 0 ? (w - 1 - fx) / dx : dx < 0 ? -fx / dx : Double.MAX_VALUE;
		double yReach = dy > 0 ? (h - 1 - fy) / dy : dy < 0 ? -fy / dy : Double.MAX_VALUE;
		double reach = Math.min(xReach, yReach);

		int endX = Math.max(0, Math.min(w - 1, fx + (int) Math.round(dx * reach)));
		int endY = Math.max(0, Math.min(h - 1, fy + (int) Math.round(dy * reach)));

		Ballistica line = new Ballistica(user.pos, endX + endY * w, Ballistica.STOP_SOLID);
		int hitCell = line.collisionPos != null ? line.collisionPos : endX + endY * w;

		for (Integer c : line.subPath(0, line.dist)) {
			if (c == user.pos) {
				continue;
			}
			Char ch = Actor.findChar(c);
			if (ch != null && ch != user && ch.alignment != user.alignment && ch.HP > 0) {
				hitCell = c;
				break;
			}
		}

		final int impact = hitCell;
		final Hero hero = user;

		MissileSprite ms = (MissileSprite) user.sprite.parent.recycle(MissileSprite.class);
		ms.reset(user.sprite, impact, this, new Callback() {
			@Override
			public void call() {
				Char target = Actor.findChar(impact);
				if (target != null && target != hero
						&& target.alignment != hero.alignment && target.isAlive()) {
					int dmg = Math.max(1, Math.round(hero.damageRoll() * 0.3f));
					target.damage(DamageInfo.physicalNoArmor(dmg, Senbon.this));
				}
			}
		});
		//针看起来小一点
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