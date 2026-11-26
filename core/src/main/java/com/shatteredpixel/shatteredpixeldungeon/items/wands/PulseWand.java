/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2026 TheCatist
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.engineer.EngineerPassive;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.engineer.EngineerRules;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.RangeMap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TrapHack;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 脉冲法杖：工程师的初始法杖。
 *
 * 放在 items.wands 包里是因为它要改充能器的充能因数，那个字段只在包内可见。
 */
public class PulseWand extends Wand {

	public static final String AC_DISARM = "disarm";

	{
		image = ItemSpriteSheet.PULSE;

		unique = true;
		bones = false;
	}

	@Override
	public java.util.ArrayList<String> actions(Hero hero) {
		java.util.ArrayList<String> actions = super.actions(hero);
		actions.add(AC_DISARM);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_DISARM)) {
			if ((Dungeon.level.map[hero.pos] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.INACTIVE_TRAP
					|| (Dungeon.level.map[hero.pos] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.PEDESTAL && hero.hasTalent(Talent.APART_ANYTHING)))
					&& Dungeon.depth != 15) {
				com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(hero.pos, com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
				Dungeon.level.traps.remove(hero.pos);
				int quantity = 1;
				for (int i : com.watabou.utils.PathFinder.NEIGHBOURS4) {
					if (Dungeon.level.map[hero.pos + i] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.INACTIVE_TRAP) {
						com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(hero.pos + i, com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
						Dungeon.level.traps.remove(hero.pos + i);
						quantity++;
					}
				}
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(hero.pos);
				if (hero.subClass != HeroSubClasses.CRAFTSMAN) quantity = Math.min(quantity, 2);
				com.shatteredpixel.shatteredpixeldungeon.items.Gold gold = new com.shatteredpixel.shatteredpixeldungeon.items.Gold(quantity * 5);
				if (!gold.collect()) Dungeon.level.drop(gold, hero.pos).sprite.drop();
				hero.sprite.operate(hero.pos);
				com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.UNLOCK);
				hero.spendAndNext(2f);
				if (hero.hasTalent(Talent.GENERAL_DISARM)) {
					Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower.class)
							.set(1 + hero.pointsInTalent(Talent.GENERAL_DISARM), 3);
				}
			} else if (hero.hasTalent(Talent.DISMANTLE_URGE)) {
				int doorCell = findAdjacentDoor(hero.pos);
				Char mech = hero.pointsInTalent(Talent.DISMANTLE_URGE) >= 2 ? findAdjacentMech(hero.pos) : null;
				if (mech != null) {
					Buff.affect(mech, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class, 999999f);
					com.shatteredpixel.shatteredpixeldungeon.actors.hero.engineer.EngineerLoot.give(hero, 1, 0, mech.pos);
					hero.sprite.operate(mech.pos);
					com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.UNLOCK);
					hero.spendAndNext(2f);
				} else if (doorCell != -1) {
					com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(doorCell, com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
					com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(doorCell);
					Dungeon.observe();
					com.shatteredpixel.shatteredpixeldungeon.actors.hero.engineer.EngineerLoot.give(hero, 1, 0, doorCell);
					hero.sprite.operate(doorCell);
					com.watabou.noosa.audio.Sample.INSTANCE.play(com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.UNLOCK);
					hero.spendAndNext(3f);
				} else {
					com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(this, "no_trap"));
				}
			} else {
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(this, "no_trap"));
			}
		}
	}

	private int findAdjacentDoor(int pos) {
		if (Dungeon.level.map[pos] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR
				|| Dungeon.level.map[pos] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.OPEN_DOOR) {
			return pos;
		}
		for (int i : com.watabou.utils.PathFinder.NEIGHBOURS4) {
			int c = pos + i;
			if (Dungeon.level.map[c] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR
					|| Dungeon.level.map[c] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.OPEN_DOOR) {
				return c;
			}
		}
		return -1;
	}

	private Char findAdjacentMech(int pos) {
		for (int i : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			Char ch = Actor.findChar(pos + i);
			if (ch != null && ch.alignment == Char.Alignment.ENEMY && Char.hasProp(ch, Char.Property.INORGANIC)) {
				return ch;
			}
		}
		return null;
	}

	@Override
	public void onZap( Ballistica attack ) {
		Hero hero = curUser;
		int cell = attack.collisionPos;
		int[] cells = hero.subClass == HeroSubClasses.HACKER
				? RangeMap.centeredRect( cell, 1, 1 ) : new int[]{ cell };
		for (int hit : cells) {
			if (Dungeon.level.solid[hit]) continue;
			Char enemy = Actor.findChar( hit );
			if (enemy != null && enemy.alignment == Char.Alignment.ENEMY) {
				EngineerRules.applyPulse( hero, enemy, level() );
			}
			Trap trap = Dungeon.level.traps.get( hit );
			if (trap != null) {
				if (trap.active) TrapHack.hack( trap );
				else TrapHack.offerRepair( hero, trap );
			}
		}
		int shielding = 4 * hero.pointsInTalent( Talent.PULSE_ENERGY );
		if (shielding > 0) Buff.affect( hero, Barrier.class ).setShield( shielding );
	}

	@Override
	public int initialCharges() {
		return 1;
	}

	@Override
	public int level() {
		return Dungeon.hero == null ? 0 : Dungeon.hero.lvl / 5;
	}

	@Override
	public void updateLevel() {
		maxCharges = Dungeon.hero != null && Dungeon.hero.pointsInTalent( Talent.WAND_ENGINEERING ) >= 3 ? 2 : 1;
		curCharges = Math.min( curCharges, maxCharges );
	}

	@Override
	public void charge( Char owner ) {
		super.charge( owner );
		charger.setScaleFactor( 0.75f );
	}

	@Override
	public void charge( Char owner, float scale ) {
		charge( owner );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public void onHit( MagesStaff staff, Char attacker, Char defender, int damage ) {
	}

	/** 点天赋时调用（Talent.onTalentUpgraded）：「杖构工学」+3 把充能上限提到 2。 */
	public static void onTalentUpgraded( Hero hero, Talent talent ) {
		if (talent == Talent.WAND_ENGINEERING) {
			PulseWand wand = hero.belongings.getItem( PulseWand.class );
			if (wand != null) wand.updateLevel();
		}
	}

	/** 工程师身上没有脉冲法杖、也没发过时补发一根（旧存档）。由常驻状态每回合调用，要足够便宜。 */
	public static void ensureGranted( Hero hero ) {
		EngineerPassive passive = hero.buff( EngineerPassive.class );
		if (passive == null || passive.pulseWandGranted) return;
		if (hero.belongings.getItem( PulseWand.class ) == null) {
			PulseWand wand = new PulseWand();
			wand.identify();
			if (!wand.collect()) return;
		}
		passive.pulseWandGranted = true;
		Buff.detach( hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Pulse.class );
	}
}
