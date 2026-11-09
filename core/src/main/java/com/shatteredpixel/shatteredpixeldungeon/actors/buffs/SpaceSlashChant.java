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

	{
		actPriority = 1;
	}

	@Override
	public boolean act() {
		if (!(target instanceof Hero)) {
			detach();
			return true;
		}
		Hero hero = (Hero) target;

		stage++;
		if (stage == 1) {
			GLog.p(Messages.get(this, "dragon_scale"));
		} else if (stage == 2) {
			GLog.p(Messages.get(this, "repulsion"));
		} else {
			GLog.p(Messages.get(this, "meteor"));
			fire(hero);
			detach();
			return true;
		}

		spend(TICK);
		return true;
	}

	private void fire(Hero hero) {
		int w = Dungeon.level.width();
		float dx, dy;

		Char nearest = null;
		float best = Float.MAX_VALUE;
		for (Char ch : Actor.chars()) {
			if (ch == hero || !ch.isAlive()) {
				continue;
			}
			float d = Dungeon.level.distance(hero.pos, ch.pos);
			if (d < best) {
				best = d;
				nearest = ch;
			}
		}
		if (nearest != null) {
			dx = (nearest.pos % w) - (hero.pos % w);
			dy = (nearest.pos / w) - (hero.pos / w);
		} else {
			dx = hero.sprite.flipHorizontal ? -1 : 1;
			dy = 0;
		}
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len == 0) {
			dx = 1; dy = 0; len = 1;
		}
		dx /= len;
		dy /= len;

		int range = Math.max(Dungeon.level.width(), Dungeon.level.height());
		int dmg = 40 + hero.lvl * 3;

		for (Char ch : Actor.chars()) {
			if (ch == hero || !ch.isAlive()) {
				continue;
			}
			float vx = (ch.pos % w) - (hero.pos % w);
			float vy = (ch.pos / w) - (hero.pos / w);
			float proj = vx * dx + vy * dy;
			float perp = Math.abs(vx * dy - vy * dx);
			if (proj > 0 && proj <= range && perp <= 1.2f) {
				ch.damage(DamageInfo.physical(dmg, hero));
			}
		}

		SukunaSlashEffect.showGiant(hero, dx, dy, range);
	}
}