/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.particles.Emitter;

//The eye of a tornado created by the Potion of Tornadoes. Every turn it drags
//all enemies within a 9x9 area one tile closer to its center.
public class Tornado extends Blob {

	//the cell where the potion landed; only this cell draws a cyclone
	private int centerCell = -1;

	@Override
	protected void evolve() {
		//this blob stays put at each of its centers and simply decays by 1 each turn
		int width = Dungeon.level.width();
		for (int cell = 0; cell < cur.length; cell++) {
			if (cur[cell] <= 0) {
				continue;
			}
			off[cell] = cur[cell] - 1;
			volume += off[cell];

			if (centerCell < 0 || cur[cell] > cur[centerCell]) {
				centerCell = cell;
			}

			if (Dungeon.level.insideMap(cell)) {
				pullToward(cell, width);
			}
		}
	}

	private void pullToward(int center, int width) {
		int cx = center % width;
		int cy = center / width;

		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (mob.alignment != Char.Alignment.ENEMY
					|| !mob.isAlive()
					|| Char.hasProp(mob, Char.Property.IMMOVABLE)) {
				continue;
			}

			int mx = mob.pos % width;
			int my = mob.pos / width;
			if (Math.max(Math.abs(mx - cx), Math.abs(my - cy)) > 4) {
				continue;
			}

			int sx = Integer.signum(cx - mx);
			int sy = Integer.signum(cy - my);
			int[] candidates = {
					(mx + sx) + (my + sy) * width,
					(mx + sx) + my * width,
					mx + (my + sy) * width
			};

			for (int target : candidates) {
				if (target == mob.pos) {
					continue;
				}
				if (Dungeon.level.insideMap(target)
						&& Dungeon.level.passable[target]
						&& !Dungeon.level.pit[target]
						&& Actor.findChar(target) == null) {
					mob.sprite.move(mob.pos, target);
					mob.move(target, false);
					break;
				}
			}
		}
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);
		//initialize the eye right away so the first frame draws at the right cell
		if (centerCell < 0) {
			for (int cell = 0; cell < cur.length; cell++) {
				if (cur[cell] > 0 && (centerCell < 0 || cur[cell] > cur[centerCell])) {
					centerCell = cell;
				}
			}
		}
		emitter.start(CYCLONE, 0, 1);
	}

	//only the eye of the storm draws a cyclone
	private final Emitter.Factory CYCLONE = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			if (centerCell < 0 || cur[centerCell] <= 0) {
				return;
			}
			float cx = (centerCell % Dungeon.level.width()) + 0.5f;
			float cy = (centerCell / Dungeon.level.width()) + 0.5f;
			CycloneParticle p = (CycloneParticle)emitter.recycle( CycloneParticle.class );
			p.reset( cx * DungeonTilemap.SIZE, cy * DungeonTilemap.SIZE );
		}
	};

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
