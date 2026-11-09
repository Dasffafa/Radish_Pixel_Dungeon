package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Callback;

/** 火·フーガ：射出一支火焰箭，命中后爆炸并点燃周围敌人。 */
public class Fuga extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public String icon() {
		return HeroIcon.SHOCKWAVE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[0];
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null || target == hero.pos) {
			return;
		}
		armor.charge -= chargeUse(hero);
		Item.updateQuickslot();

		Ballistica bolt = new Ballistica(hero.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
		int cell = bolt.collisionPos;

		MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE, hero.sprite, cell, new Callback() {
			@Override
			public void call() {
				com.shatteredpixel.shatteredpixeldungeon.effects.PixelExplosionEffect.showAtCell(cell);
				com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(cell).burst(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(com.shatteredpixel.shatteredpixeldungeon.effects.Speck.LIGHT), 14);

				int w = Dungeon.level.width();
				int cx = cell % w;
				int cy = cell / w;
				int blastRadius = 4;
				boolean terrainAffected = false;
				for (int dy = -blastRadius; dy <= blastRadius; dy++) {
					for (int dx = -blastRadius; dx <= blastRadius; dx++) {
						if (dx * dx + dy * dy > blastRadius * blastRadius) {
							continue;
						}
						int x = cx + dx;
						int y = cy + dy;
						if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) {
							continue;
						}
						int c = y * w + x;
						if (Dungeon.level.flamable[c]) {
							Dungeon.level.destroy(c);
							com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(c);
							terrainAffected = true;
						}
						com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.seed(c, 6,
								com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire.class);
					}
				}
				if (terrainAffected) {
					Dungeon.observe();
				}

				int dmg = 20 + hero.lvl * 2;
				int radius = 4;
				for (Char ch : Actor.chars()) {
					if (ch == hero || !ch.isAlive()) {
						continue;
					}
					if (Dungeon.level.distance(cell, ch.pos) <= radius) {
						ch.damage(DamageInfo.fire(dmg, hero));
						Buff.affect(ch, Burning.class);
					}
				}
			}
		});
	}
}