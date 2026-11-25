/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Warped Pixel Dungeon
 * Copyright (C) 2026 Gabriel Duarte Guerra (gabriwar)
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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.warped;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Repulsion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Dazzling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Displacing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Explosive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Meteor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Seeking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * The table of enchantment looks, Warped's cut down to the enchantments this repo has: each row
 * gives a class its family, element and glow colour, and the row's own details (motif, profile,
 * its sting, whether it is a curse). Warped's table has 78 rows over its own enchantment set; this
 * one covers the ones this repo shares, and anything else simply has no look - its proc plays with
 * no extra art, as before.
 */
public final class EnchantLooks {

	public static final int COUNT = 64;
	public static final int ENCHANTMENTS = 0, GLYPHS = 32, WEAPON_CURSES = 48, ARMOR_CURSES = 58;

	private static final EnchantLook[] BY_CODE = new EnchantLook[COUNT];
	private static final HashMap<Class<?>, EnchantLook> BY_CLASS = new HashMap<>();
	private static final IdentityHashMap<ItemSprite.Glowing, EnchantLook> BY_GLOWING = new IdentityHashMap<>();

	static {
		weaponEnchantments();
		glyphs();
		weaponCurses();
	}

	private EnchantLooks(){}

	public static EnchantLook of( Class<?> c ){
		if (c == null) return null;
		EnchantLook l = BY_CLASS.get( c );
		if (l != null) return l;
		//an enchantment this table never heard of (the fork's own, or a DIY one): give it a look of
		//its own so the window, the aura and the proc rings still have a colour and a family
		l = synthetic( c );
		BY_CLASS.put( c, l );
		return l;
	}

	private static final java.util.HashMap<Class<?>, EnchantLook> SYNTHETIC = new java.util.HashMap<>();

	/** HSV to 0xRRGGBB, by hand: android has no java.awt.Color. */
	private static int hsv( float hue, float sat, float val ){
		float h6 = (hue - (float)Math.floor( hue )) * 6f;
		int i = (int)h6;
		float f = h6 - i;
		float p = val * (1f - sat);
		float q = val * (1f - sat * f);
		float t = val * (1f - sat * (1f - f));
		float r, g, b;
		switch (i % 6){
			case 0: r = val; g = t; b = p; break;
			case 1: r = q; g = val; b = p; break;
			case 2: r = p; g = val; b = t; break;
			case 3: r = p; g = q; b = val; break;
			case 4: r = t; g = p; b = val; break;
			default: r = val; g = p; b = q; break;
		}
		int ri = Math.round( r * 255f ), gi = Math.round( g * 255f ), bi = Math.round( b * 255f );
		return (ri << 16) | (gi << 8) | bi;
	}

	private static EnchantLook synthetic( Class<?> c ){
		EnchantLook cached = SYNTHETIC.get( c );
		if (cached != null) return cached;

		int h = Math.abs( c.getName().hashCode() );
		Family[] fams = Family.values();
		Family fam = fams[h % fams.length];
		int glow = hsv( (h % 360) / 360f, 0.55f, 1f );
		Motif[] motifs = Motif.values();
		Profile[] profiles = Profile.values();

		Element[] els = Element.values();
		Element el = els[h / 5 % els.length];

		EnchantLook l = new EnchantLook( -1, c, fam, el, glow, glow, glow,
				motifs[h / 7 % motifs.length], profiles[h / 11 % profiles.length],
				"", 0f, 1f, false, new int[0], new int[0], false, false );
		SYNTHETIC.put( c, l );
		return l;
	}

	public static EnchantLook byCode( int code ){
		return code < 0 || code >= COUNT ? null : BY_CODE[code];
	}

	public static EnchantLook forGlowing( ItemSprite.Glowing g ){
		return g == null ? null : BY_GLOWING.get( g );
	}

	public static List<EnchantLook> all(){
		List<EnchantLook> out = new ArrayList<>();
		for (EnchantLook l : BY_CODE) if (l != null) out.add( l );
		return out;
	}

	private static void weaponEnchantments(){
		row( 0, Blazing.class,    Family.EMBER,   Element.FIRE,     0xFF8418 ).sound( "flare", 0.35f, 1f ).own();
		row( 1, Chilling.class,   Family.RIME,    Element.FROST,    0xBCE0FF ).sound( "ice", 0.35f, 1f ).own();
		row( 2, Shocking.class,   Family.STORM,   Element.ELECTRIC, 0xA8C8FF ).sound( "lightning", 0.35f, 1f ).own();
		row( 3, Corrupting.class, Family.VENOM,   Element.POISON,   0x6CCC3C );
		row( 4, Grim.class,       Family.REAPER,  Element.DEATH,    0xFF4060 );
		row( 5, Kinetic.class,    Family.FORCE,   Element.STEEL,    0xA8C0E0 );
		row( 6, Elastic.class,    Family.FORCE,   Element.STEEL,    0xE8F0FF );
		row( 7, Blooming.class,   Family.VERDANT, Element.LIFE,     0x58E08C );
		row( 8, Lucky.class,      Family.FORTUNE, Element.GOLD,     0xFFF0A0 );
		row( 9, Vampiric.class,   Family.HEART,   Element.LIFE,     0xFF90C8 );
		row( 10, Blocking.class,  Family.WARD,    Element.ARCANE,   0xC8B0FF );
		row( 11, Meteor.class,    Family.EMBER,   Element.LAVA,     0xFF7A14 );
		row( 12, Seeking.class,   Family.MIRAGE,  Element.ARCANE,   0xB8A0FF );
	}

	/** This repo's glyphs have no family of Warped's, so only the few with an obvious one are given. */
	private static void glyphs(){
		row( GLYPHS + 0, Brimstone.class, Family.EMBER,  Element.LAVA,  0xFF7A14 ).glyph();
		row( GLYPHS + 1, Thorns.class,    Family.CUT,    Element.DEATH, 0xFF4060 ).glyph();
		row( GLYPHS + 2, Viscosity.class, Family.DREAD,  Element.SHADOW,0x6A4A8A ).glyph();
		row( GLYPHS + 3, Repulsion.class, Family.FORCE,  Element.STEEL, 0xE8F0FF ).glyph();
		row( GLYPHS + 4, Potential.class, Family.WARD,   Element.ARCANE,0xC8B0FF ).glyph();
	}

	private static void weaponCurses(){
		row( WEAPON_CURSES + 0, Wayward.class,    Family.MIRAGE,  Element.VOID, 0x8A5AC0 ).curse();
		row( WEAPON_CURSES + 1, Displacing.class, Family.MIRAGE,  Element.VOID, 0x8A5AC0 ).curse();
		row( WEAPON_CURSES + 2, Dazzling.class,   Family.FORTUNE, Element.VOID, 0x8A5AC0 ).curse();
		row( WEAPON_CURSES + 3, Explosive.class,  Family.EMBER,   Element.HELL, 0xF0303C ).curse();
		row( WEAPON_CURSES + 4, Sacrificial.class, Family.DRAIN,  Element.VOID, 0x8A5AC0 ).curse();
	}

	/** A row's runes: the family's item motif frames, or none (the item's own). */
	static int[] rune( Family fam ){
		return null;
	}

	private static Row row( int code, Class<?> of, Family fam, Element el, int glow ){
		return new Row( code, of, fam, el, glow );
	}

	//one row being written
	private static final class Row {
		final int code;
		final Class<?> of;
		final Family fam;
		final Element el;
		final int glow;
		int rim = -1, eye = -1;
		Motif motif = Motif.RISE;
		Profile profile = Profile.PULSE;
		String sound;
		float vol = 0.3f, pitch = 1f;
		boolean own, curse, glyph;
		int[] rune, cycle;

		Row( int code, Class<?> of, Family fam, Element el, int glow ){
			this.code = code;
			this.of = of;
			this.fam = fam;
			this.el = el;
			this.glow = glow;
			rune = EnchantLooks.rune( fam );
			row( this );
		}

		Row rim( int c ){ rim = c; return this; }
		Row eye( int c ){ eye = c; return this; }
		Row motif( Motif m, Profile p ){ motif = m; profile = p; return this; }
		Row sound( String s, float v, float p ){ sound = s; vol = v; pitch = p; return this; }
		Row own(){ own = true; return this; }
		Row curse(){ curse = true; return this; }
		Row glyph(){ glyph = true; return this; }
		Row rune( int[] f ){ rune = f; return this; }
		Row cycle( int... c ){ cycle = c; return this; }
	}

	//each row files itself as it is made
	private static void row( Row r ){
		if (r.code < 0 || r.code >= COUNT || BY_CODE[r.code] != null)
			throw new IllegalStateException( "code " + r.code + " twice or off the table" );
		if (BY_CLASS.containsKey( r.of )) throw new IllegalStateException( r.of + " twice" );
		int edge = r.rim != -1 ? r.rim : (r.curse ? Element.VOID.glint : r.el.light);
		EnchantLook l = new EnchantLook( r.code, r.of, r.fam, r.el, r.glow, edge, r.eye != -1 ? r.eye : edge,
				r.motif, r.profile, r.sound, r.vol, r.pitch, r.own, r.rune, r.cycle, r.curse, r.glyph );
		BY_CODE[r.code] = l;
		BY_CLASS.put( r.of, l );
	}
}
