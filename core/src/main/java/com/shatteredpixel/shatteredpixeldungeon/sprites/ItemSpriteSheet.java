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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class ItemSpriteSheet {

	private static final int WIDTH = 32;
	public static final int SIZE = 16;

	public static TextureFilm film = new TextureFilm( Assets.Sprites.ITEMS, SIZE, SIZE );

	private static int xy(int x, int y){
		x -= 1; y -= 1;
		return x + WIDTH*y;
	}

	// TheCatist 2026/7/25 通过自动检测每个物品贴图的外接矩形的办法 移除了所有对assignItemRect的调用
	private static final int PLACEHOLDERS   =                               xy(1, 1);   //16 slots
	//SOMETHING is the default item sprite at position 0. May show up ingame if there are bugs.
	public static final int SOMETHING       = PLACEHOLDERS+0;
	public static final int WEAPON_HOLDER   = PLACEHOLDERS+1;
	public static final int ARMOR_HOLDER    = PLACEHOLDERS+2;
	public static final int MISSILE_HOLDER  = PLACEHOLDERS+3;
	public static final int WAND_HOLDER     = PLACEHOLDERS+4;
	public static final int RING_HOLDER     = PLACEHOLDERS+5;
	public static final int ARTIFACT_HOLDER = PLACEHOLDERS+6;
	public static final int TRINKET_HOLDER  = PLACEHOLDERS+7;
	public static final int FOOD_HOLDER     = PLACEHOLDERS+8;
	public static final int BOMB_HOLDER     = PLACEHOLDERS+9;
	public static final int POTION_HOLDER   = PLACEHOLDERS+10;
	public static final int SEED_HOLDER     = PLACEHOLDERS+11;
	public static final int SCROLL_HOLDER   = PLACEHOLDERS+12;
	public static final int STONE_HOLDER    = PLACEHOLDERS+13;
	public static final int ELIXIR_HOLDER   = PLACEHOLDERS+14;
	public static final int SPELL_HOLDER    = PLACEHOLDERS+15;
//	public static final int SNAKE_BITE      = SPELL_HOLDER; // Snake Bite challenge item icon (placeholder)

	public static final int MOB_HOLDER    	= PLACEHOLDERS+17;
	public static final int DOCUMENT_HOLDER	= PLACEHOLDERS+18;

	private static final int UNCOLLECTIBLE  =                               xy(1, 2);   //16 slots
	public static final int GOLD            = UNCOLLECTIBLE+0;
	public static final int ENERGY          = UNCOLLECTIBLE+1;

	public static final int DEWDROP         = UNCOLLECTIBLE+3;
	public static final int PETAL           = UNCOLLECTIBLE+4;
	public static final int SANDBAG         = UNCOLLECTIBLE+5;
	public static final int SPIRIT_ARROW    = UNCOLLECTIBLE+6;
	public static final int SPIRIT_ALT_ARROW    = UNCOLLECTIBLE+7;
	
	public static final int TENGU_BOMB      = UNCOLLECTIBLE+8;
	public static final int TENGU_SHOCKER   = UNCOLLECTIBLE+9;
	public static final int GEO_BOULDER     = UNCOLLECTIBLE+10;

	public static final int POUL_WATER    = UNCOLLECTIBLE+12;

	private static final int CONTAINERS     =                               xy(1, 3);   //16 slots
	public static final int BONES           = CONTAINERS+0;
	public static final int REMAINS         = CONTAINERS+1;
	public static final int TOMB            = CONTAINERS+2;
	public static final int GRAVE           = CONTAINERS+3;
	public static final int CHEST           = CONTAINERS+4;
	public static final int LOCKED_CHEST    = CONTAINERS+5;
	public static final int CRYSTAL_CHEST   = CONTAINERS+6;
	public static final int EBONY_CHEST     = CONTAINERS+7;

	private static final int MISC_CONSUMABLE =                              xy(1, 4);   //32 slots
	public static final int ANKH            = MISC_CONSUMABLE +0;
	public static final int STYLUS          = MISC_CONSUMABLE +1;
	public static final int SEAL            = MISC_CONSUMABLE +2;
	public static final int TORCH           = MISC_CONSUMABLE +3;
	public static final int BEACON          = MISC_CONSUMABLE +4;
	public static final int HONEYPOT        = MISC_CONSUMABLE +5;
	public static final int SHATTPOT        = MISC_CONSUMABLE +6;
	public static final int IRON_KEY        = MISC_CONSUMABLE +7;
	public static final int GOLDEN_KEY      = MISC_CONSUMABLE +8;
	public static final int CRYSTAL_KEY     = MISC_CONSUMABLE +9;
	public static final int SKELETON_KEY    = MISC_CONSUMABLE +10;
	public static final int MASK            = MISC_CONSUMABLE +11;
	public static final int CROWN           = MISC_CONSUMABLE +12;
	public static final int AMULET          = MISC_CONSUMABLE +13;
	public static final int MASTERY         = MISC_CONSUMABLE +14;
	public static final int KIT             = MISC_CONSUMABLE +15;
	public static final int SEAL_SHARD      = MISC_CONSUMABLE +16;
	public static final int BROKEN_STAFF    = MISC_CONSUMABLE +17;
	public static final int CLOAK_SCRAP     = MISC_CONSUMABLE +18;
	public static final int BOW_FRAGMENT    = MISC_CONSUMABLE +19;
	public static final int BROKEN_HILT     = MISC_CONSUMABLE +20;
	public static final int TRINKET_CATA    = MISC_CONSUMABLE +21;
	public static final int TAL_MASTERY     = MISC_CONSUMABLE +22;

	private static final int BOMBS          =                               xy(1, 6);   //16 slots
	public static final int BOMB            = BOMBS+0;
	public static final int DBL_BOMB        = BOMBS+1;
	public static final int FIRE_BOMB       = BOMBS+2;
	public static final int FROST_BOMB      = BOMBS+3;
	public static final int REGROWTH_BOMB   = BOMBS+4;
	public static final int FLASHBANG       = BOMBS+5;
	public static final int SHOCK_BOMB      = BOMBS+6;
	public static final int HOLY_BOMB       = BOMBS+7;
	public static final int WOOLY_BOMB      = BOMBS+8;
	public static final int NOISEMAKER      = BOMBS+9;
	public static final int ARCANE_BOMB     = BOMBS+10;
	public static final int SHRAPNEL_BOMB   = BOMBS+11;

	private static final int WEP_TIER1      =                               xy(1, 7);   //8 slots
	public static final int WORN_SHORTSWORD = WEP_TIER1+0;
	public static final int CUDGEL          = WEP_TIER1+1;
	public static final int GLOVES          = WEP_TIER1+2;
	public static final int RAPIER          = WEP_TIER1+3;
	public static final int DAGGER          = WEP_TIER1+4;
	public static final int MAGES_STAFF     = WEP_TIER1+5;
	public static final int HOLYANKH		= WEP_TIER1+6;

	private static final int WEP_TIER2      =                               xy(9, 7);   //8 slots
	public static final int SHORTSWORD      = WEP_TIER2+0;
	public static final int HAND_AXE        = WEP_TIER2+1;
	public static final int SPEAR           = WEP_TIER2+2;
	public static final int QUARTERSTAFF    = WEP_TIER2+3;
	public static final int DIRK            = WEP_TIER2+4;
	public static final int SICKLE          = WEP_TIER2+5;


	//Radish Image WEP_TIER2
	public static final int KATAR           = WEP_TIER2+8;
	public static final int BLADESHIELD		= WEP_TIER2+9;
	public static final int GLASSSWORD1		= WEP_TIER2+10;
	public static final int GLASSSWORD2		= WEP_TIER2+11;
	public static final int GLASSSWORD3		= WEP_TIER2+12;
	public static final int SILVER_STING    = WEP_TIER2+13;
	public static final int ROTTEN_LANCE    = WEP_TIER2+14;
	public static final int REPAIRV2        = WEP_TIER2+15;
	public static final int BONE_CLAW		= WEP_TIER2+16;


	public static final int RLYEH_BOOK		= WEP_TIER2+19;

	public static final int RUNE_SLADE		= WEP_TIER2+20;

	private static final int WEP_TIER3      =                               xy(1, 8);   //8 slots
	public static final int SWORD           = WEP_TIER3+0;
	public static final int MACE            = WEP_TIER3+1;
	public static final int SCIMITAR        = WEP_TIER3+2;
	public static final int ROUND_SHIELD    = WEP_TIER3+3;
	public static final int SAI             = WEP_TIER3+4;
	public static final int WHIP            = WEP_TIER3+5;

	private static final int WEP_TIER4      =                               xy(9, 8);   //8 slots
	public static final int LONGSWORD       = WEP_TIER4+0;
	public static final int BATTLE_AXE      = WEP_TIER4+1;
	public static final int FLAIL           = WEP_TIER4+2;
	public static final int RUNIC_BLADE     = WEP_TIER4+3;
	public static final int ASSASSINS_BLADE = WEP_TIER4+4;
	public static final int CROSSBOW        = WEP_TIER4+5;
	public static final int KATANA          = WEP_TIER4+6;

	//Radish Image WEP_TIER3

	public static final int BEECOMB			= WEP_TIER4+9;
	public static final int WATERWHEEL		= WEP_TIER4+10;
	public static final int WINGSWORD		= WEP_TIER4+11;

	public static final int LOCK_CHAIN		= WEP_TIER4+12;

	public static final int DAGGER_S		= WEP_TIER4+13;
	public static final int SNAKESPEAR		= WEP_TIER4+14;

	public static final int PNEGLOVE_FIVE   = WEP_TIER4+15;
	public static final int PNEGLOVE_ACTIVE   = WEP_TIER4+16;


	public static final int IAMSB_FLAG	    = WEP_TIER4+19;
	public static final int LONG_STARK	    = WEP_TIER4+20;

	private static final int WEP_TIER5      =                               xy(1, 9);   //8 slots
	public static final int GREATSWORD      = WEP_TIER5+0;
	public static final int WAR_HAMMER      = WEP_TIER5+1;
	public static final int GLAIVE          = WEP_TIER5+2;
	public static final int GREATAXE        = WEP_TIER5+3;
	public static final int GREATSHIELD     = WEP_TIER5+4;
	public static final int GAUNTLETS       = WEP_TIER5+5;
	public static final int WAR_SCYTHE      = WEP_TIER5+6;
	//Radish Image WEP_TIER4
	private static final int RAD_TIER4      =                               xy(17, 9);

	public static final int DARKSWORD   	= RAD_TIER4-1;
	public static final int CRBSC           = RAD_TIER4;
	public static final int SKYSPS          = RAD_TIER4+1;
	public static final int BLOODBLADE1     = RAD_TIER4+2;
	public static final int BLOODBLADE2     = RAD_TIER4+3;
	public static final int BLOODBLADE3     = RAD_TIER4+4;
	public static final int DIRK_B          = RAD_TIER4+5;
	public static final int SEEKING			= RAD_TIER4+6;
	public static final int HEADCLEAVER		= RAD_TIER4+7;



	public static final int ENDDAY_KILL		= RAD_TIER4+11;
	public static final int MORELLO_BOOK	= RAD_TIER4+12;
	public static final int SHADOW_BOOK		= RAD_TIER4+13; 	//8 free slots

	private static final int MISSILE_WEP    =                               xy(1, 10);  //16 slots. 3 per tier + bow
	public static final int SPIRIT_BOW      = MISSILE_WEP+0;
	
	public static final int THROWING_SPIKE  = MISSILE_WEP+1;
	public static final int THROWING_KNIFE  = MISSILE_WEP+2;
	public static final int THROWING_STONE  = MISSILE_WEP+3;
	
	public static final int FISHING_SPEAR   = MISSILE_WEP+4;
	public static final int SHURIKEN        = MISSILE_WEP+5;
	public static final int THROWING_CLUB   = MISSILE_WEP+6;
	
	public static final int THROWING_SPEAR  = MISSILE_WEP+7;
	public static final int BOLAS           = MISSILE_WEP+8;
	public static final int KUNAI           = MISSILE_WEP+9;
	
	public static final int JAVELIN         = MISSILE_WEP+10;
	public static final int TOMAHAWK        = MISSILE_WEP+11;
	public static final int BOOMERANG       = MISSILE_WEP+12;
	
	public static final int TRIDENT         = MISSILE_WEP+13;
	public static final int THROWING_HAMMER = MISSILE_WEP+14;
	public static final int FORCE_CUBE      = MISSILE_WEP+15;

	//Radish Image WEP_TIER5
	private static final int RAD_TIER5      =                               xy(17, 10);
	public static final int CROSSBOW_S		= RAD_TIER5+0;
	public static final int FOGSWORD		= RAD_TIER5+1;



	public static final int TAIKIG			= RAD_TIER5+5;
	public static final int CALLHAMR		= RAD_TIER5+6;
	public static final int GIANTKILL		= RAD_TIER5+7;

	public static final int CUTTERHEAD		= RAD_TIER5+9;
	public static final int KILL_BOAT		= RAD_TIER5+10;
	public static final int AXE_D			= RAD_TIER5+11;
	public static final int TONFA			= RAD_TIER5+12;
	public static final int SCYTHE			= RAD_TIER5+13;

	public static final int KNIGHT_GS			= RAD_TIER5+14;

	public static final int DARTS    =                                      xy(1, 11);  //16 slots
	public static final int DART            = DARTS+0;
	public static final int ROT_DART        = DARTS+1;
	public static final int INCENDIARY_DART = DARTS+2;
	public static final int ADRENALINE_DART = DARTS+3;
	public static final int HEALING_DART    = DARTS+4;
	public static final int CHILLING_DART   = DARTS+5;
	public static final int SHOCKING_DART   = DARTS+6;
	public static final int POISON_DART     = DARTS+7;
	public static final int CLEANSING_DART  = DARTS+8;
	public static final int PARALYTIC_DART  = DARTS+9;
	public static final int HOLY_DART       = DARTS+10;
	public static final int DISPLACING_DART = DARTS+11;
	public static final int BLINDING_DART   = DARTS+12;
	
	private static final int ARMOR            =                               xy(1, 12);  //16 slots
	public static final int ARMOR_CLOTH       = ARMOR+0;
	public static final int ARMOR_LEATHER     = ARMOR+1;
	public static final int ARMOR_MAIL        = ARMOR+2;
	public static final int ARMOR_SCALE       = ARMOR+3;
	public static final int ARMOR_PLATE       = ARMOR+4;
	public static final int ARMOR_WARRIOR     = ARMOR+5;
	public static final int ARMOR_MAGE        = ARMOR+6;
	public static final int ARMOR_ROGUE       = ARMOR+7;
	public static final int ARMOR_HUNTRESS    = ARMOR+8;
	public static final int ARMOR_DUELIST     = ARMOR+9;
	public static final int ARMOR_CRAB        = ARMOR+10;
	public static final int ARMOR_AFTERGLOW   = ARMOR+11;
	public static final int ARMOR_RAT         = ARMOR+12;
	public static final int ARMOR_GREYFEATHER = ARMOR+13;
	public static final int ARMOR_AFTERIMAGE  = ARMOR+14;
	public static final int ARMOR_PRISON      = ARMOR+15;
	public static final int ARMOR_ENERGY1     = ARMOR+16;
	public static final int ARMOR_ENERGY2     = ARMOR+17;
	public static final int ARMOR_SILVERSCALE = ARMOR+18;
	public static final int ARMOR_BLACKCOAT   = ARMOR+19;
	public static final int ARMOR_MOONLIGHT   = ARMOR+20;

	private static final int WANDS              =                           xy(1, 14);  //16 slots
	public static final int WAND_MAGIC_MISSILE  = WANDS+0;
	public static final int WAND_FIREBOLT       = WANDS+1;
	public static final int WAND_FROST          = WANDS+2;
	public static final int WAND_LIGHTNING      = WANDS+3;
	public static final int WAND_DISINTEGRATION = WANDS+4;
	public static final int WAND_PRISMATIC_LIGHT= WANDS+5;
	public static final int WAND_CORROSION      = WANDS+6;
	public static final int WAND_LIVING_EARTH   = WANDS+7;
	public static final int WAND_BLAST_WAVE     = WANDS+8;
	public static final int WAND_CORRUPTION     = WANDS+9;
	public static final int WAND_WARDING        = WANDS+10;
	public static final int WAND_REGROWTH       = WANDS+11;
	public static final int WAND_TRANSFUSION    = WANDS+12;
	public static final int WAND_GNOLL   = WANDS+13;
	public static final int WAND_BOMBWAVES  = WANDS+14;
	public static final int WAND_NEWSTAR  	= WANDS+15;

	private static final int RINGS          =                               xy(1, 15);  //16 slots
	public static final int RING_GARNET     = RINGS+0;
	public static final int RING_RUBY       = RINGS+1;
	public static final int RING_TOPAZ      = RINGS+2;
	public static final int RING_EMERALD    = RINGS+3;
	public static final int RING_ONYX       = RINGS+4;
	public static final int RING_OPAL       = RINGS+5;
	public static final int RING_TOURMALINE = RINGS+6;
	public static final int RING_SAPPHIRE   = RINGS+7;
	public static final int RING_AMETHYST   = RINGS+8;
	public static final int RING_QUARTZ     = RINGS+9;
	public static final int RING_AGATE      = RINGS+10;
	public static final int RING_DIAMOND    = RINGS+11;
	public static final int RING_GOLD		= RINGS+12;
	public static final int RING_CORAL		= RINGS+13;
	public static final int RING_PEARL		= RINGS+14;

	public static final int RING_SKYLUE		= RINGS+18;

	private static final int ARTIFACTS          =                            xy(1, 16);  //32 slots
	public static final int ARTIFACT_CLOAK      = ARTIFACTS+0;
	public static final int ARTIFACT_ARMBAND    = ARTIFACTS+1;
	public static final int ARTIFACT_CAPE       = ARTIFACTS+2;
	public static final int ARTIFACT_TALISMAN   = ARTIFACTS+3;
	public static final int ARTIFACT_HOURGLASS  = ARTIFACTS+4;
	public static final int ARTIFACT_TOOLKIT    = ARTIFACTS+5;
	public static final int ARTIFACT_SPELLBOOK  = ARTIFACTS+6;
	public static final int ARTIFACT_BEACON     = ARTIFACTS+7;
	public static final int ARTIFACT_CHAINS     = ARTIFACTS+8;
	public static final int ARTIFACT_HORN1      = ARTIFACTS+9;
	public static final int ARTIFACT_HORN2      = ARTIFACTS+10;
	public static final int ARTIFACT_HORN3      = ARTIFACTS+11;
	public static final int ARTIFACT_HORN4      = ARTIFACTS+12;
	public static final int ARTIFACT_CHALICE1   = ARTIFACTS+13;
	public static final int ARTIFACT_CHALICE2   = ARTIFACTS+14;
	public static final int ARTIFACT_CHALICE3   = ARTIFACTS+15;
	//ARTIFACTS Second Line
	public static final int ARTIFACT_SANDALS    = ARTIFACTS+16+16;
	public static final int ARTIFACT_SHOES      = ARTIFACTS+17+16;
	public static final int ARTIFACT_BOOTS      = ARTIFACTS+18+16;
	public static final int ARTIFACT_GREAVES    = ARTIFACTS+19+16;
	public static final int ARTIFACT_ROSE1      = ARTIFACTS+20+16;
	public static final int ARTIFACT_ROSE2      = ARTIFACTS+21+16;
	public static final int ARTIFACT_ROSE3      = ARTIFACTS+22+16;

	public static final int ARTIFACT_CONCEAL    = ARTIFACTS+24+16;
	public static final int ARTIFACT_ELTIE1     = ARTIFACTS+25+16;
	public static final int ARTIFACT_ELTIE2     = ARTIFACTS+26+16;
	public static final int ARTIFACT_ELTIE3     = ARTIFACTS+27+16;
	public static final int ARTIFACT_ELTIE4     = ARTIFACTS+28+16;
	public static final int ARTIFACT_ELTIE5     = ARTIFACTS+29+16;
	public static final int ARTIFACT_ELTIE6     = ARTIFACTS+30+16;
	public static final int ARTIFACT_ELTIE7     = ARTIFACTS+31+16;

	public static final int MAGNETIC_CROWN     = ARTIFACTS+48;

	public static final int BLESS_SCROLL     = ARTIFACTS+50;
	public static final int SNAKE_BITED_YENDOR = ARTIFACTS+51;
	public static final int SNAKE_BITE = ARTIFACTS + 52;
	public static final int SNAKE_BITE_AMULET = ARTIFACTS + 53;
	public static final int ARTIFACT_WHEELCHAIR = ARTIFACTS + 54;

	private static final int TRINKETS        =                               xy(17, 28);  //24 slots
	public static final int RAT_SKULL       = TRINKETS+0;
	public static final int PARCHMENT_SCRAP = TRINKETS+1;
	public static final int PETRIFIED_SEED  = TRINKETS+2;
	public static final int EXOTIC_CRYSTALS = TRINKETS+3;
	public static final int MOSSY_CLUMP     = TRINKETS+4;
	public static final int SUNDIAL         = TRINKETS+5;
	public static final int CLOVER          = TRINKETS+6;
	public static final int TRAP_MECHANISM  = TRINKETS+7;
	public static final int MIMIC_TOOTH     = TRINKETS+8;
	public static final int WONDROUS_RESIN  = TRINKETS+9;
	public static final int EYE_OF_NEWT     = TRINKETS+10;
	public static final int SALT_CUBE       = TRINKETS+11;
	public static final int OBLIVION_SHARD  = TRINKETS+12;
	public static final int CHAOTIC_CENSER  = TRINKETS+13;

	public static final int RADISH 			= TRINKETS+14;
	public static final int GOLD_RADISH 	= TRINKETS+15;

	public static final int LIGHT_KING 	= TRINKETS+32;
	public static final int RIVER_GLASS 	= TRINKETS+33;

	private static final int SCROLLS        =                               xy(1, 19);  //16 slots
	public static final int SCROLL_KAUNAN   = SCROLLS+0;
	public static final int SCROLL_SOWILO   = SCROLLS+1;
	public static final int SCROLL_LAGUZ    = SCROLLS+2;
	public static final int SCROLL_YNGVI    = SCROLLS+3;
	public static final int SCROLL_GYFU     = SCROLLS+4;
	public static final int SCROLL_RAIDO    = SCROLLS+5;
	public static final int SCROLL_ISAZ     = SCROLLS+6;
	public static final int SCROLL_MANNAZ   = SCROLLS+7;
	public static final int SCROLL_NAUDIZ   = SCROLLS+8;
	public static final int SCROLL_BERKANAN = SCROLLS+9;
	public static final int SCROLL_ODAL     = SCROLLS+10;
	public static final int SCROLL_TIWAZ    = SCROLLS+11;

	public static final int ARCANE_RESIN    = SCROLLS+13;

	private static final int EXOTIC_SCROLLS =                               xy(1, 20);  //16 slots
	public static final int EXOTIC_KAUNAN   = EXOTIC_SCROLLS+0;
	public static final int EXOTIC_SOWILO   = EXOTIC_SCROLLS+1;
	public static final int EXOTIC_LAGUZ    = EXOTIC_SCROLLS+2;
	public static final int EXOTIC_YNGVI    = EXOTIC_SCROLLS+3;
	public static final int EXOTIC_GYFU     = EXOTIC_SCROLLS+4;
	public static final int EXOTIC_RAIDO    = EXOTIC_SCROLLS+5;
	public static final int EXOTIC_ISAZ     = EXOTIC_SCROLLS+6;
	public static final int EXOTIC_MANNAZ   = EXOTIC_SCROLLS+7;
	public static final int EXOTIC_NAUDIZ   = EXOTIC_SCROLLS+8;
	public static final int EXOTIC_BERKANAN = EXOTIC_SCROLLS+9;
	public static final int EXOTIC_ODAL     = EXOTIC_SCROLLS+10;
	public static final int EXOTIC_TIWAZ    = EXOTIC_SCROLLS+11;

	public static final int SPELL_QUEUE_ON	= EXOTIC_SCROLLS+13;
	public static final int SPELL_QUEUE_OFF	= EXOTIC_SCROLLS+14;

	private static final int STONES             =                           xy(1, 21);  //16 slots
	public static final int STONE_AGGRESSION    = STONES+0;
	public static final int STONE_AUGMENTATION  = STONES+1;
	public static final int STONE_FEAR          = STONES+2;
	public static final int STONE_BLAST         = STONES+3;
	public static final int STONE_BLINK         = STONES+4;
	public static final int STONE_CLAIRVOYANCE  = STONES+5;
	public static final int STONE_SLEEP         = STONES+6;
	public static final int STONE_DISARM        = STONES+7;
	public static final int STONE_ENCHANT       = STONES+8;
	public static final int STONE_FLOCK         = STONES+9;
	public static final int STONE_INTUITION     = STONES+10;
	public static final int STONE_SHOCK         = STONES+11;

	private static final int POTIONS        =                               xy(1, 22);  //16 slots
	public static final int POTION_CRIMSON  = POTIONS+0;
	public static final int POTION_AMBER    = POTIONS+1;
	public static final int POTION_GOLDEN   = POTIONS+2;
	public static final int POTION_JADE     = POTIONS+3;
	public static final int POTION_TURQUOISE= POTIONS+4;
	public static final int POTION_AZURE    = POTIONS+5;
	public static final int POTION_INDIGO   = POTIONS+6;
	public static final int POTION_MAGENTA  = POTIONS+7;
	public static final int POTION_BISTRE   = POTIONS+8;
	public static final int POTION_CHARCOAL = POTIONS+9;
	public static final int POTION_SILVER   = POTIONS+10;
	public static final int POTION_IVORY    = POTIONS+11;

	public static final int LIQUID_METAL    = POTIONS+13;
	public static final int POTION_CATALYST = POTIONS+14;
	
	private static final int EXOTIC_POTIONS =                               xy(1, 23);  //16 slots
	public static final int EXOTIC_CRIMSON  = EXOTIC_POTIONS+0;
	public static final int EXOTIC_AMBER    = EXOTIC_POTIONS+1;
	public static final int EXOTIC_GOLDEN   = EXOTIC_POTIONS+2;
	public static final int EXOTIC_JADE     = EXOTIC_POTIONS+3;
	public static final int EXOTIC_TURQUOISE= EXOTIC_POTIONS+4;
	public static final int EXOTIC_AZURE    = EXOTIC_POTIONS+5;
	public static final int EXOTIC_INDIGO   = EXOTIC_POTIONS+6;
	public static final int EXOTIC_MAGENTA  = EXOTIC_POTIONS+7;
	public static final int EXOTIC_BISTRE   = EXOTIC_POTIONS+8;
	public static final int EXOTIC_CHARCOAL = EXOTIC_POTIONS+9;
	public static final int EXOTIC_SILVER   = EXOTIC_POTIONS+10;
	public static final int EXOTIC_IVORY    = EXOTIC_POTIONS+11;

	private static final int SEEDS              =                           xy(1, 24);  //16 slots
	public static final int SEED_ROTBERRY       = SEEDS+0;
	public static final int SEED_FIREBLOOM      = SEEDS+1;
	public static final int SEED_SWIFTTHISTLE   = SEEDS+2;
	public static final int SEED_SUNGRASS       = SEEDS+3;
	public static final int SEED_ICECAP         = SEEDS+4;
	public static final int SEED_STORMVINE      = SEEDS+5;
	public static final int SEED_SORROWMOSS     = SEEDS+6;
	public static final int SEED_MAGEROYAL = SEEDS+7;
	public static final int SEED_EARTHROOT      = SEEDS+8;
	public static final int SEED_STARFLOWER     = SEEDS+9;
	public static final int SEED_FADELEAF       = SEEDS+10;
	public static final int SEED_BLINDWEED      = SEEDS+11;
	private static final int BREWS          =                               xy(1, 25);  //8 slots
	public static final int BREW_INFERNAL   = BREWS+0;
	public static final int BREW_BLIZZARD   = BREWS+1;
	public static final int BREW_SHOCKING   = BREWS+2;
	public static final int BREW_CAUSTIC    = BREWS+3;
	public static final int BREW_AQUA       = BREWS+4;
	public static final int BREW_UNSTABLE   = BREWS+5;

	public static final int MAGIC_ROOT      = BREWS+7;
	
	private static final int ELIXIRS        =                               xy(9, 25);  //8 slots
	public static final int ELIXIR_HONEY    = ELIXIRS+0;
	public static final int ELIXIR_AQUA     = ELIXIRS+1;
	public static final int ELIXIR_MIGHT    = ELIXIRS+2;
	public static final int ELIXIR_DRAGON   = ELIXIRS+3;
	public static final int ELIXIR_TOXIC    = ELIXIRS+4;
	public static final int ELIXIR_ICY      = ELIXIRS+5;
	public static final int ELIXIR_ARCANE   = ELIXIRS+6;
	public static final int ELIXIR_FEATHER  = ELIXIRS+7;
	private static final int SPELLS         =                               xy(1, 27);  //16 slots
	public static final int WILD_ENERGY     = SPELLS+0;
	public static final int PHASE_SHIFT     = SPELLS+1;
	public static final int TELE_GRAB       = SPELLS+2;
	public static final int UNSTABLE_SPELL  = SPELLS+3;

	public static final int CURSE_INFUSE    = SPELLS+5;
	public static final int MAGIC_INFUSE    = SPELLS+6;
	public static final int ALCHEMIZE       = SPELLS+7;
	public static final int RECYCLE         = SPELLS+8;

	public static final int RECLAIM_TRAP    = SPELLS+10;
	public static final int RETURN_BEACON   = SPELLS+11;
	public static final int SUMMON_ELE      = SPELLS+12;

	//浮空
	public static final int FEATHER_FALL    = SPELLS+13;
	public static final int AQUA_BLAST      = SPELLS+14;

	private static final int FOOD       =                                   xy(1, 28);  //16 slots
	public static final int MEAT            = FOOD+0;
	public static final int STEAK           = FOOD+1;
	public static final int STEWED          = FOOD+2;
	public static final int OVERPRICED      = FOOD+3;
	public static final int CARPACCIO       = FOOD+4;
	public static final int RATION          = FOOD+5;
	public static final int PASTY           = FOOD+6;
	public static final int MEAT_PIE        = FOOD+7;
	public static final int BLANDFRUIT      = FOOD+8;
	public static final int BLAND_CHUNKS    = FOOD+9;
	public static final int BERRY           = FOOD+10;
	public static final int PHANTOM_MEAT    = FOOD+11;
	public static final int SUPPLY_RATION   = FOOD+12;
	private static final int HOLIDAY_FOOD   =                               xy(1, 29);  //16 slots
	public static final int STEAMED_FISH    = HOLIDAY_FOOD+0;
	public static final int FISH_LEFTOVER   = HOLIDAY_FOOD+1;
	public static final int CHOC_AMULET     = HOLIDAY_FOOD+2;
	public static final int EASTER_EGG      = HOLIDAY_FOOD+3;
	public static final int RAINBOW_POTION  = HOLIDAY_FOOD+4;
	public static final int SHATTERED_CAKE  = HOLIDAY_FOOD+5;
	public static final int PUMPKIN_PIE     = HOLIDAY_FOOD+6;
	public static final int VANILLA_CAKE    = HOLIDAY_FOOD+7;
	public static final int CANDY_CANE      = HOLIDAY_FOOD+8;
	public static final int SPARKLING_POTION= HOLIDAY_FOOD+9;
	private static final int QUEST  =                                       xy(1, 30);  //16 slots
	public static final int DUST    = QUEST+1;
	public static final int CANDLE  = QUEST+2;
	public static final int EMBER   = QUEST+3;
	public static final int PICKAXE = QUEST+4;
	public static final int ORE     = QUEST+5;
	public static final int TOKEN   = QUEST+6;
	public static final int BLOB    = QUEST+7;
	public static final int SHARD   = QUEST+8;

	public static final int SPOTOA   = QUEST+9;

	private static final int BAGS       =                                   xy(1, 31);  //16 slots
	public static final int WATERSKIN   = BAGS+0;
	public static final int BACKPACK    = BAGS+1;
	public static final int POUCH       = BAGS+2;
	public static final int HOLDER      = BAGS+3;
	public static final int BANDOLIER   = BAGS+4;
	public static final int HOLSTER     = BAGS+5;
	public static final int VIAL        = BAGS+6;
	public static final int HERB_MAKER  = BAGS+7;
	public static final int HERB	    = BAGS+8;

	public static final int STONE_CRAD	    = BAGS+10;
	public static final int SEED_CARD	    = BAGS+11;

	public static final int CORRECT			= BAGS+13;
	public static final int LIGHTIMUEE		= BAGS+14;
	public static final int CLEAN			= BAGS+15;
	public static final int PRAYERS			= BAGS+16;

	public static final int APOWER			= BAGS+18;
	public static final int BACKMESSAGE	    = BAGS+19;
	public static final int DEADMODE		= BAGS+20;


	public static final int BLESS			= BAGS+22;
	public static final int HOLYFIRE		= BAGS+23;
	public static final int HOLYLAND		= BAGS+24;

	private static final int DOCUMENTS  =                                   xy(1, 32);  //16 slots
	public static final int GUIDE_PAGE  = DOCUMENTS+0;
	public static final int ALCH_PAGE   = DOCUMENTS+1;
	public static final int SEWER_PAGE  = DOCUMENTS+2;
	public static final int PRISON_PAGE = DOCUMENTS+3;
	public static final int CAVES_PAGE  = DOCUMENTS+4;
	public static final int CITY_PAGE   = DOCUMENTS+5;
	public static final int HALLS_PAGE  = DOCUMENTS+6;
	public static final int LENGDS_PAGE  = DOCUMENTS+7;

	//for smaller 8x8 icons that often accompany an item sprite
	public static class Icons {

		private static final int WIDTH = 16;
		public static final int SIZE = 8;

		public static TextureFilm film = new TextureFilm( Assets.Sprites.ITEM_ICONS, SIZE, SIZE );

		private static int xy(int x, int y){
			x -= 1; y -= 1;
			return x + WIDTH*y;
		}

		private static void assignIconRect( int item, int width, int height ){
			int x = (item % WIDTH) * SIZE;
			int y = (item / WIDTH) * SIZE;
			film.add( item, x, y, x+width, y+height);
		}

		private static final int RINGS          =                            xy(1, 1);  //16 slots
		public static final int RING_ACCURACY   = RINGS+0;
		public static final int RING_ARCANA     = RINGS+1;
		public static final int RING_ELEMENTS   = RINGS+2;
		public static final int RING_ENERGY     = RINGS+3;
		public static final int RING_EVASION    = RINGS+4;
		public static final int RING_FORCE      = RINGS+5;
		public static final int RING_FUROR      = RINGS+6;
		public static final int RING_HASTE      = RINGS+7;
		public static final int RING_MIGHT      = RINGS+8;
		public static final int RING_SHARPSHOOT = RINGS+9;
		public static final int RING_TENACITY   = RINGS+10;
		public static final int RING_WEALTH     = RINGS+11;

		public static final int RING_KING     = RINGS+12; 		//16 free slots

		private static final int SCROLLS        =                            xy(1, 3);  //16 slots
		public static final int SCROLL_UPGRADE  = SCROLLS+0;
		public static final int SCROLL_IDENTIFY = SCROLLS+1;
		public static final int SCROLL_REMCURSE = SCROLLS+2;
		public static final int SCROLL_MIRRORIMG= SCROLLS+3;
		public static final int SCROLL_RECHARGE = SCROLLS+4;
		public static final int SCROLL_TELEPORT = SCROLLS+5;
		public static final int SCROLL_LULLABY  = SCROLLS+6;
		public static final int SCROLL_MAGICMAP = SCROLLS+7;
		public static final int SCROLL_RAGE     = SCROLLS+8;
		public static final int SCROLL_RETRIB   = SCROLLS+9;
		public static final int SCROLL_TERROR   = SCROLLS+10;
		public static final int SCROLL_TRANSMUTE= SCROLLS+11;
		private static final int EXOTIC_SCROLLS =                            xy(1, 4);  //16 slots
		public static final int SCROLL_ENCHANT  = EXOTIC_SCROLLS+0;
		public static final int SCROLL_DIVINATE = EXOTIC_SCROLLS+1;
		public static final int SCROLL_ANTIMAGIC= EXOTIC_SCROLLS+2;
		public static final int SCROLL_PRISIMG  = EXOTIC_SCROLLS+3;
		public static final int SCROLL_MYSTENRG = EXOTIC_SCROLLS+4;
		public static final int SCROLL_PASSAGE  = EXOTIC_SCROLLS+5;
		public static final int SCROLL_SIREN    = EXOTIC_SCROLLS+6;
		public static final int SCROLL_FORESIGHT= EXOTIC_SCROLLS+7;
		public static final int SCROLL_CHALLENGE= EXOTIC_SCROLLS+8;
		public static final int SCROLL_PSIBLAST = EXOTIC_SCROLLS+9;
		public static final int SCROLL_DREAD    = EXOTIC_SCROLLS+10;
		public static final int SCROLL_METAMORPH= EXOTIC_SCROLLS+11;

		private static final int POTIONS        =                            xy(1, 6);  //16 slots
		public static final int POTION_STRENGTH = POTIONS+0;
		public static final int POTION_HEALING  = POTIONS+1;
		public static final int POTION_MINDVIS  = POTIONS+2;
		public static final int POTION_FROST    = POTIONS+3;
		public static final int POTION_LIQFLAME = POTIONS+4;
		public static final int POTION_TOXICGAS = POTIONS+5;
		public static final int POTION_HASTE    = POTIONS+6;
		public static final int POTION_INVIS    = POTIONS+7;
		public static final int POTION_LEVITATE = POTIONS+8;
		public static final int POTION_PARAGAS  = POTIONS+9;
		public static final int POTION_PURITY   = POTIONS+10;
		public static final int POTION_EXP      = POTIONS+11;

		private static final int EXOTIC_POTIONS =    xy(1, 7);  //16 slots
		public static final int POTION_MASTERY  = EXOTIC_POTIONS+0;
		public static final int POTION_SHIELDING= EXOTIC_POTIONS+1;
		public static final int POTION_MAGISIGHT= EXOTIC_POTIONS+2;
		public static final int POTION_SNAPFREEZ= EXOTIC_POTIONS+3;
		public static final int POTION_DRGBREATH= EXOTIC_POTIONS+4;
		public static final int POTION_CORROGAS = EXOTIC_POTIONS+5;
		public static final int POTION_STAMINA  = EXOTIC_POTIONS+6;
		public static final int POTION_SHROUDFOG= EXOTIC_POTIONS+7;
		public static final int POTION_STRMCLOUD= EXOTIC_POTIONS+8;
		public static final int POTION_EARTHARMR= EXOTIC_POTIONS+9;
		public static final int POTION_CLEANSE  = EXOTIC_POTIONS+10;
		public static final int POTION_DIVINE   = EXOTIC_POTIONS+11;
	}

	static {
		// 可通过设置 debugMode = true 来查看优化日志
		// ItemSpriteOptimizer.debugMode = true;
		ItemSpriteOptimizer.optimize(film);
	}

}