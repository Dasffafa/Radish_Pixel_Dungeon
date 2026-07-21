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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 破灭之戒 - 持续伤害戒指
 * 每回合对视野内敌人造成百分比伤害，同时自己也受到百分比伤害
 * 伤害不会致死
 */
public class RingOfDestruction extends Ring {

	// TODO: 需要为破灭之戒创建专属图标，目前使用占位符
	{
		icon = ItemSpriteSheet.Icons.RING_MIGHT; // 临时占位，待替换
	}

	@Override
	protected RingBuff buff() {
		return new Destruction();
	}

	public static float selfDamagePercent(int level) {
		return 0.1f + 0.05f * level;
	}

	public static float enemyDamagePercent(int level) {
		return 2f + 0.75f * level;
	}

	@Override
	public String statsInfo() {
		if (isIdentified()) {
			int level = soloBuffedBonus();
			float selfDmg = selfDamagePercent(level);
			float enemyDmg = enemyDamagePercent(level);
			return Messages.get(this, "stats", selfDmg, enemyDmg);
		} else {
			return Messages.get(this, "typical_stats");
		}
	}

	@Override
	public String upgradeStat1(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level - 3);
		return String.format("%.2f%%", selfDamagePercent(level + 1));
	}

	@Override
	public String upgradeStat2(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level - 3);
		return String.format("%.2f%%", enemyDamagePercent(level + 1));
	}

	public class Destruction extends RingBuff {

		@Override
		public int icon() {
			return 0; // 隐藏状态栏图标
		}

		@Override
		public boolean act() {
			if (target != Dungeon.hero) {
				spend(TICK);
				return true;
			}

			int level = getRingLevel();
			if (level <= 0) {
				spend(TICK);
				return true;
			}

			ArrayList<Mob> visibleEnemies = new ArrayList<>();
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.alignment == Char.Alignment.ENEMY 
						&& Dungeon.level.heroFOV[mob.pos]
						&& mob.isAlive()
						&& !mob.isInvulnerable(getClass())) {
					visibleEnemies.add(mob);
				}
			}

			if (!visibleEnemies.isEmpty()) {
				float enemyDmgPercent = enemyDamagePercent(level);
				for (Mob mob : visibleEnemies) {
					int damage = Math.round(mob.HT * enemyDmgPercent / 100f);
					if (damage > 0) {
						mob.HP = Math.max(1, mob.HP - damage);
					}
				}

				float selfDmgPercent = selfDamagePercent(level);
				int selfDamage = Math.round(Dungeon.hero.HT * selfDmgPercent / 100f);
				if (selfDamage > 0) {
					Dungeon.hero.HP = Math.max(1, Dungeon.hero.HP - selfDamage);
				}
			}

			spend(TICK);
			return true;
		}

		/**
		 * 获取戒指等级（处理诅咒情况）
		 */
		private int getRingLevel() {
			// 从 Ring.getBuffedBonus 获取等级
			return combinedBuffedBonus(Dungeon.hero);
		}
	}
}