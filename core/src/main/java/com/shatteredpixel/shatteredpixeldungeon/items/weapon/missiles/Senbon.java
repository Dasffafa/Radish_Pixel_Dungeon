package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.utils.Callback;

/**
 * 千本：一次射出多枚细针，针与针之间有短暂间隔并带一定夹角。
 * 针不会落地，用完即消失（不可回收）。
 */
public class Senbon extends MissileWeapon {

	/** 每次发射的针数 */
	private static final int SHOTS = 5;

	/** 相邻两针的发射间隔（秒） */
	private static final float SHOT_INTERVAL = 0.07f;

	/** 扇形总夹角（弧度） */
	private static final double SPREAD = Math.toRadians(24);

	{
		image = ItemSpriteSheet.SENBON;

		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.2f;

		tier = 1;
		baseUses = 1;
		sticky = false;
		stackable = true;
		quantity = 30;
	}

	@Override
	public int max(int lvl) {
		return 3 + lvl;
	}

	/** 千本不可回收，投出即消耗 */
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

		//消耗一发
		detach(user.belongings.backpack);

		int w = Dungeon.level.width();
		int fx = user.pos % w;
		int fy = user.pos / w;
		int tx = cell % w;
		int ty = cell / w;
		double baseAngle = Math.atan2(ty - fy, tx - fx);

		user.sprite.parent.add(new SenbonVolley(user, baseAngle, new Senbon()));

		user.spendAndNext(delay);
		Item.updateQuickslot();
	}

	/** 分批发射的调度器 */
	public static class SenbonVolley extends Gizmo {

		private final Hero hero;
		private final double baseAngle;
		private final Senbon visual;

		private int fired = 0;
		private float time = 0f;

		public SenbonVolley(Hero hero, double baseAngle, Senbon visual) {
			super();
			this.hero = hero;
			this.baseAngle = baseAngle;
			this.visual = visual;
		}

		@Override
		public void update() {
			if (hero == null || !hero.isAlive() || hero.sprite == null || hero.sprite.parent == null) {
				kill();
				return;
			}

			time += Game.elapsed;

			while (fired < SHOTS && time >= fired * SHOT_INTERVAL) {
				fireOne(fired);
				fired++;
			}

			if (fired >= SHOTS) {
				kill();
			}
		}

		private void fireOne(int index) {
			double offset = SHOTS == 1 ? 0 : -SPREAD / 2 + SPREAD * index / (SHOTS - 1d);
			double angle = baseAngle + offset;

			int w = Dungeon.level.width();
			int h = Dungeon.level.height();
			int fx = hero.pos % w;
			int fy = hero.pos / w;

			double dx = Math.cos(angle);
			double dy = Math.sin(angle);

			double xReach = dx > 0 ? (w - 1 - fx) / dx : dx < 0 ? -fx / dx : Double.MAX_VALUE;
			double yReach = dy > 0 ? (h - 1 - fy) / dy : dy < 0 ? -fy / dy : Double.MAX_VALUE;
			double reach = Math.min(xReach, yReach);

			int endX = Math.max(0, Math.min(w - 1, fx + (int) Math.round(dx * reach)));
			int endY = Math.max(0, Math.min(h - 1, fy + (int) Math.round(dy * reach)));

			Ballistica line = new Ballistica(hero.pos, endX + endY * w, Ballistica.STOP_SOLID);
			int hitCell = line.collisionPos != null ? line.collisionPos : endX + endY * w;

			for (Integer c : line.subPath(0, line.dist)) {
				if (c == hero.pos) {
					continue;
				}
				Char ch = Actor.findChar(c);
				if (ch != null && ch != hero && ch.alignment != hero.alignment && ch.HP > 0) {
					hitCell = c;
					break;
				}
			}

			final int impact = hitCell;

			((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).reset(
					hero.sprite, impact, visual, new Callback() {
						@Override
						public void call() {
							Char target = Actor.findChar(impact);
							if (target != null && target != hero
									&& target.alignment != hero.alignment && target.isAlive()) {
								int dmg = Math.max(1, Math.round(hero.damageRoll() * 0.3f));
								target.damage(DamageInfo.physicalNoArmor(dmg, SenbonVolley.this));
							}
						}
					});
		}
	}
}