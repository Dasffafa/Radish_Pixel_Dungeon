/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.rarity;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greataxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Mace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The gear quality, Warped's cut down to what this repo has: an item's rarity and the family of
 * weapon (or armour) it belongs to. Warped's own Quality rolls perks, keeps a reroll wheel and reads
 * its whole enchantment catalogue; none of that is here yet - this is the floor the item window's
 * quality line and the gear display stand on.
 */
public final class Quality {

	/** What kind of gear it is: Warped's families, without its guns, bows and instrument set. */
	public enum Family {
		SWORD, AXE, SPEAR, MACE, DAGGER, STAFF, ARMOR, SHIELD, RING, WAND, OTHER
	}

	private final Item item;
	private final Rarity rarity;
	private final Family family;

	private Quality( Item item, Rarity rarity, Family family ){
		this.item = item;
		this.rarity = rarity;
		this.family = family;
	}

	//one rolled quality per item instance: the same gear always reads the same
	private static final Map<Item, Quality> ROLLED = new IdentityHashMap<>();

	/** The quality of an item: gear rolls a rarity once and keeps it; anything else is COMMON. */
	public static Quality of( Item item ){
		if (item == null) return new Quality( null, Rarity.COMMON, Family.OTHER );
		if (!(item instanceof Weapon) && !(item instanceof Armor)){
			return new Quality( item, Rarity.COMMON, Family.OTHER );
		}
		Quality q = ROLLED.get( item );
		if (q == null){
			//seeded by the instance's own identity, so an item does not change its rarity as it is
			//looked at; deeper gear rolls better tiers
			java.util.Random seeded = new java.util.Random(
					((long)System.identityHashCode( item ) << 7) ^ item.getClass().getName().hashCode() );
			int depth = com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth;
			Rarity r = rollWith( seeded, Math.max( 1, depth ) );
			q = new Quality( item, r, familyOf( item ) );
			ROLLED.put( item, q );
		}
		return q;
	}

	/** Rarity.roll(depth), with a seeded source so it is stable per item. */
	private static Rarity rollWith( java.util.Random rnd, int depth ){
		//weights as Rarity's own shallow/deep tables, by depth
		float total = 0f;
		float[] w = new float[Rarity.values().length];
		for (Rarity r : Rarity.values()){
			w[r.ordinal()] = r.weight( depth );
			total += w[r.ordinal()];
		}
		float pick = rnd.nextFloat() * total;
		for (Rarity r : Rarity.values()){
			pick -= w[r.ordinal()];
			if (pick <= 0f) return r;
		}
		return Rarity.COMMON;
	}

	public static Family familyOf( Item item ){
		if (item instanceof Armor) return Family.ARMOR;
		if (item instanceof Sword) return Family.SWORD;
		if (item instanceof Greataxe) return Family.AXE;
		if (item instanceof Spear) return Family.SPEAR;
		if (item instanceof Mace) return Family.MACE;
		if (item instanceof Dagger) return Family.DAGGER;
		if (item instanceof MeleeWeapon){
			//the repo's melee set is large and not all of it sits under one base class: read the name
			String n = item.getClass().getSimpleName().toLowerCase();
			if (n.contains( "axe" )) return Family.AXE;
			if (n.contains( "spear" ) || n.contains( "glaive" ) || n.contains( "lance" )) return Family.SPEAR;
			if (n.contains( "mace" ) || n.contains( "hammer" ) || n.contains( "flail" )) return Family.MACE;
			if (n.contains( "dagger" ) || n.contains( "dirk" ) || n.contains( "blade" ) && n.contains( "assassin" )) return Family.DAGGER;
			if (n.contains( "staff" )) return Family.STAFF;
			return Family.SWORD;
		}
		if (item instanceof Weapon) return Family.OTHER;
		return Family.OTHER;
	}

	public Rarity rarity(){
		return rarity;
	}

	public Family family(){
		return family;
	}

	public Item item(){
		return item;
	}

	/** Its weapon's upgrade level (armour's too); 0 for anything else. */
	public int level(){
		if (item instanceof Weapon) return ((Weapon)item).level();
		if (item instanceof Armor) return ((Armor)item).level();
		return 0;
	}

	/** The lines this item shows: its family's, as many as its rarity allows, chosen by the item
	 *  itself so the same gear always reads the same. */
	public List<RarityLine> lines(){
		List<RarityLine> pool = RarityLine.forFamily( family );
		List<RarityLine> out = new ArrayList<>();
		int want = Math.min( rarity.lines, pool.size() );
		if (want <= 0) return out;
		//per instance, not per class: otherwise every sword of a kind would read the same line
		int seed = (System.identityHashCode( item ) ^ (item.getClass().getName().hashCode() * 31) ^ 0x5EED) & 0x7fffffff;
		for (int i = 0; i < want; i++){
			int k = (seed / (i + 1) + i * 7) % pool.size();
			RarityLine line = pool.get( k );
			if (!out.contains( line )) out.add( line );
		}
		return out;
	}

	/** Records a kill with this item; every five of them is a masterwork level. Saved on the item. */
	public static void noteKill( Item item ){
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon){
			((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)item).masterworkKills++;
		}
	}

	/** How many kills it has to its name. */
	public static int masterworkKills( Item item ){
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon){
			return ((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)item).masterworkKills;
		}
		return 0;
	}

	/** Its masterwork level, 0..5 (a level every five kills). */
	public static int masterworkLevel( Item item ){
		return Math.min( 5, masterworkKills( item ) / 5 );
	}

	/** The rarity's own grant, lifted by its masterwork level (each +15% of the lines). */
	public static float potencyOf( Item item ){
		return of( item ).rarity().potency() * (1f + 0.15f * masterworkLevel( item ) );
	}

	/** The family a SLAYER line hunts (NONE when the item does not carry it). */
	public SlayerFamily slayer(){
		if (!has( RarityLine.SLAYER )) return SlayerFamily.NONE;
		SlayerFamily[] all = { SlayerFamily.VERMIN, SlayerFamily.BEAST, SlayerFamily.UNDEAD,
				SlayerFamily.DEMON, SlayerFamily.HUMANOID };
		int seed = (System.identityHashCode( item ) ^ 0x51A7E) & 0x7fffffff;
		return all[seed % all.length];
	}

	/** How strongly one of its lines reads, 0..1: its rarity's roll, fixed per item and line. */
	public float strength( RarityLine line ){
		if (!has( line )) return 0f;
		int seed = (item.getClass().getName().hashCode() ^ line.ordinal() * 0x9E3779B9);
		java.util.Random rnd = new java.util.Random( seed );
		return rarity.minRoll + (rarity.maxRoll - rarity.minRoll) * rnd.nextFloat();
	}

	/** Whether it carries that quality line. */
	public boolean has( RarityLine line ){
		return line != null && lines().contains( line );
	}

	/** A short label for the window: the rarity's own title. */
	public String title(){
		return rarity.title();
	}

	/** The vestige of a weapon's spent enchantment, or null: none here. */
	public static Weapon.Enchantment vestigeOf( Weapon w ){
		return null;
	}
}
