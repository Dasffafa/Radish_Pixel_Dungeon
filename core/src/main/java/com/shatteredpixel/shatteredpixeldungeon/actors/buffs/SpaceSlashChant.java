package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.SukunaSlashEffect;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class SpaceSlashChant extends Buff {

	private int stage = 0;
	private float dirX = 1f;
	private float dirY = 0f;

	public float startedAt = 0f;

	{
		actPriority = 1;
	}

	public void setDirection(Hero hero, int target) {
		int w = Dungeon.level.width();
		float dx = (target % w) - (hero.pos % w);
		float dy = (target / w) - (hero.pos / w);
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len == 0) {
			dirX = 1f;
			dirY = 0f;
		} else {
			dirX = dx / len;
			dirY = dy / len;
		}
	}

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		spend(TICK);
		return true;
	}

	/** 玩家点一次按钮蓄力一次，第三段之后斩出 */
	public void charge() {
		if (!(target instanceof Hero)) {
			detach();
			return;
		}
		Hero hero = (Hero) target;

		stage++;
		if (stage == 1) {
			GLog.p(Messages.get(this, "dragon_scale"));
		} else if (stage == 2) {
			GLog.p(Messages.get(this, "repulsion"));
		} else if (stage == 3) {
			GLog.p(Messages.get(this, "meteor"));
		} else {
			fire(hero);
			detach();
		}
	}

	private void fire(Hero hero) {
		int w = Dungeon.level.width();
		int range = Math.max(Dungeon.level.width(), Dungeon.level.height());
		int dmg = 40 + hero.lvl * 3;
		dmg = Math.round(dmg * (1f + 0.10f * hero.pointsInTalent(
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SPACE_SLASH_POWER)));

		for (Char ch : Actor.chars()) {
			if (ch == hero || !ch.isAlive()) {
				continue;
			}
			float vx = (ch.pos % w) - (hero.pos % w);
			float vy = (ch.pos / w) - (hero.pos / w);
			float proj = vx * dirX + vy * dirY;
			float perp = Math.abs(vx * dirY - vy * dirX);
			if (proj > 0 && proj <= range && perp <= 2.2f) {
				ch.damage(DamageInfo.physical(dmg, hero));
			}
		}

		SukunaSlashEffect.showGiant(hero, dirX, dirY, range);
	}
}