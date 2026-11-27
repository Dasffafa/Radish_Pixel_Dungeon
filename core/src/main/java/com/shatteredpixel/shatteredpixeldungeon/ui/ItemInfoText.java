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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Game;


/**
 * 物品窗里名字旁边的那两块：附魔（护甲则是铭文）与类型。
 * 附魔只在对玩家已知时显示——已鉴定，或诅咒已确认，与名字颜色、光晕同一规则。
 */
public final class ItemInfoText {

	private ItemInfoText(){}

	public static final int COLOR_DESC = 0xE8D9A8;

	/** 武器上的附魔或护甲上的铭文，没有则 null。 */
	public static Object enchantOf( Item it ){
		if (it instanceof Weapon) return ((Weapon)it).enchantment;
		if (it instanceof Armor) return ((Armor)it).glyph;
		return null;
	}

	public static boolean enchantKnown( Item it ){
		if (it == null) return false;
		if (it.isIdentified()) return true;
		Object ench = enchantOf( it );
		return ench != null && it.cursedKnown && isCurse( ench );
	}

	private static boolean isCurse( Object ench ){
		if (ench instanceof Weapon.Enchantment) return ((Weapon.Enchantment)ench).curse();
		if (ench instanceof Armor.Glyph) return ((Armor.Glyph)ench).curse();
		return false;
	}

	/** 附魔的一个词（"烈焰"），没有则空串。 */
	public static String enchantWord( Item it ){
		if (!enchantKnown( it )) return "";
		Object ench = enchantOf( it );
		if (ench == null) return "";
		//名字是"烈焰%s"这类围绕物品名的模式，%s 之前的就是它自己的词
		String raw = Messages.get( ench, "name" );
		int cut = raw.indexOf( "%s" );
		return (cut >= 0 ? raw.substring( 0, cut ) : raw).trim();
	}

	/** 附魔分节的标题：附魔、诅咒，普通装备为空。 */
	public static String enchantTitle( Item it ){
		Object ench = enchantOf( it );
		if (ench == null || !enchantKnown( it )) return "";
		return isCurse( ench ) ? "诅咒" : "附魔";
	}

	/** 附魔分节的正文。 */
	public static String enchantBody( Item it ){
		Object ench = enchantOf( it );
		if (ench == null || !enchantKnown( it )) return "";
		String word = enchantWord( it );
		String desc = Messages.get( ench, "desc" );
		return word.isEmpty() ? desc : "\u00b7 " + word + "\n" + desc;
	}

	/** 名字与分节标题的颜色，-1 表示用窗口自己的。 */
	public static int enchantColor( Item it ){
		Object ench = enchantOf( it );
		if (ench == null || !enchantKnown( it )) return -1;
		if (ench instanceof Weapon.Enchantment && ((Weapon.Enchantment)ench).glowing() != null){
			return ((Weapon.Enchantment)ench).glowing().color;
		}
		if (ench instanceof Armor.Glyph && ((Armor.Glyph)ench).glowing() != null){
			return ((Armor.Glyph)ench).glowing().color;
		}
		return -1;
	}

	public static boolean isGear( Item it ){
		return it instanceof Weapon || it instanceof Armor || it instanceof ItemArmorAttachable;
	}

	//武器类型（这东西是什么）与伤害类型（它怎么伤人）是两回事，分开判
	private static final int K_DAGGER = 0, K_SWORD = 1, K_AXE = 2, K_BLUNT = 3, K_POLEARM = 4,
			K_WHIP = 5, K_WHEEL = 6, K_BOW = 7, K_CROSSBOW = 8, K_THROWN = 9, K_STAFF = 10,
			K_WAND = 11, K_SHIELD = 12, K_LIGHT_ARMOR = 13, K_MID_ARMOR = 14, K_HEAVY_ARMOR = 15,
			K_RING = 16, K_TOY = 17, K_OTHER = 18;

	private static final int D_BLUNT = 0, D_SLASH = 1, D_PIERCE = 2, D_NONE = -1;

	private static final String[] BLUNT_WORDS = {
			"hammer", "mace", "flail", "club", "stick", "quarterstaff",
			"gauntlet", "glove", "fist", "taijutsu", "tonfa", "spanner", "crowbar",
			"chain", "sphere", "cross", "ankh", "book", "beecomb",
			"morello", "multitool", "tool", "endguard", "fragment", "rlyeh",
			"replace", "decree", "jutte", "cube", "bolas", "stone", "lock"
	};
	private static final String[] SLASH_WORDS = {
			"sword", "blade", "axe", "scythe", "sickle", "scimitar", "katana",
			"yamato", "whip", "cutter", "cleaver", "ripper", "wheel", "circle",
			"tomahawk", "claw", "saber", "sabre", "thorn"
	};
	private static final String[] PIERCE_WORDS = {
			"dagger", "dirk", "katar", "sai", "sting", "rapier", "spear", "lance",
			"glaive", "trident", "javelin", "kunai", "senbon", "shuriken", "knife",
			"spike", "belladonna", "perfidy", "grappling", "hook", "crossbow",
			"cannon", "slingshot", "point", "bow"
	};
	private static final String[] DAGGER_WORDS = { "dagger", "dirk", "katar", "sai", "sting" };
	private static final String[] SWORD_WORDS = { "sword", "blade", "katana", "yamato", "saber", "sabre", "rapier" };
	private static final String[] POLEARM_WORDS = { "spear", "lance", "glaive", "scythe", "trident", "javelin", "hook", "grappling" };

	private static boolean has( String n, String[] words ){
		for (String w : words){
			if (n.contains( w )) return true;
		}
		return false;
	}

	/** 武器类型：这东西是什么。 */
	private static int kind( Item it ){
		if (it instanceof Armor){
			int t = ((Armor)it).tier;
			return t <= 2 ? K_LIGHT_ARMOR : t <= 4 ? K_MID_ARMOR : K_HEAVY_ARMOR;
		}
		if (it instanceof Wand) return K_WAND;
		if (it instanceof Ring) return K_RING;
		if (it instanceof ItemArmorAttachable) return K_TOY;
		String n = it.getClass().getSimpleName().toLowerCase();
		if (n.contains( "shield" )) return K_SHIELD;
		if (n.contains( "staff" ) && !n.contains( "quarter" )) return K_STAFF;
		if (it instanceof MissileWeapon) return K_THROWN;
		if (has( n, DAGGER_WORDS )) return K_DAGGER;
		if (has( n, SWORD_WORDS )) return K_SWORD;
		if (n.contains( "axe" )) return K_AXE;
		if (has( n, POLEARM_WORDS )) return K_POLEARM;
		if (n.contains( "whip" )) return K_WHIP;
		if (n.contains( "wheel" ) || n.contains( "circle" )) return K_WHEEL;
		if (n.contains( "crossbow" ) || n.contains( "cannon" )) return K_CROSSBOW;
		if (n.contains( "bow" ) || n.contains( "slingshot" )) return K_BOW;
		if (has( n, BLUNT_WORDS )) return K_BLUNT;
		if (it instanceof MeleeWeapon) return K_SWORD;
		return K_OTHER;
	}

	/** 伤害类型：它怎么伤人。只有武器有。 */
	private static int damage( Item it ){
		if (!(it instanceof Weapon)) return D_NONE;
		String n = it.getClass().getSimpleName().toLowerCase();
		boolean blunt = has( n, BLUNT_WORDS );
		boolean slash = has( n, SLASH_WORDS );
		boolean pierce = has( n, PIERCE_WORDS );
		if (blunt && !slash && !pierce) return D_BLUNT;
		if (slash && !blunt && !pierce) return D_SLASH;
		if (pierce && !blunt && !slash) return D_PIERCE;
		if (pierce) return D_PIERCE;
		if (slash) return D_SLASH;
		if (blunt) return D_BLUNT;
		//名字看不出来就按武器类型兜底：匕首/长柄/远程穿刺，斧挥砍，其余钝击
		switch (kind( it )){
			case K_DAGGER: case K_POLEARM: case K_BOW: case K_CROSSBOW: case K_THROWN: return D_PIERCE;
			case K_AXE: case K_WHIP: case K_WHEEL: return D_SLASH;
			default: return D_BLUNT;
		}
	}

	public static String familyTitle( Item it ){
		if (!isGear( it )) return "";
		switch (kind( it )){
			case K_DAGGER:      return "匕首";
			case K_SWORD:       return "剑";
			case K_AXE:         return "斧";
			case K_BLUNT:       return "钝器";
			case K_POLEARM:     return "长柄";
			case K_WHIP:        return "鞭";
			case K_WHEEL:       return "轮刃";
			case K_BOW:         return "弓";
			case K_CROSSBOW:    return "弩";
			case K_THROWN:      return "投掷";
			case K_STAFF:       return "法杖";
			case K_WAND:        return "魔杖";
			case K_SHIELD:      return "盾";
			case K_LIGHT_ARMOR: return "轻甲";
			case K_MID_ARMOR:   return "中甲";
			case K_HEAVY_ARMOR: return "重甲";
			case K_RING:        return "戒指";
			case K_TOY:         return "玩具";
			default:            return "杂项";
		}
	}

	public static String familyText( Item it ){
		if (!isGear( it )) return "";
		switch (kind( it )){
			case K_DAGGER:      return "短小轻便，靠贴身与速度取胜。";
			case K_SWORD:       return "通用的近战武器，攻守均衡。";
			case K_AXE:         return "重型开山武器，一击很重但出手偏慢。";
			case K_BLUNT:       return "锤棍一类，靠重量砸击，破甲强。";
			case K_POLEARM:     return "长柄武器，攻击距离更远。";
			case K_WHIP:        return "能在两格外抽打，伤害稍低。";
			case K_WHEEL:       return "本作的特色武器，投出后会旋回。";
			case K_BOW:         return "需要箭矢的远程武器。";
			case K_CROSSBOW:    return "装填慢但威力大的远程武器。";
			case K_THROWN:      return "一次性投出，之后需要抢回。";
			case K_STAFF:       return "以魔力驱动的长杖。";
			case K_WAND:        return "以充能施放法术。";
			case K_SHIELD:      return "专门用于格挡与防守。";
			case K_LIGHT_ARMOR: return "防护偏低但不碍行动。";
			case K_MID_ARMOR:   return "防护与重量平衡的常规护甲。";
			case K_HEAVY_ARMOR: return "防护最高，但对力量要求也最高。";
			case K_RING:        return "以持续加成回报佩戴者。";
			case K_TOY:         return "装在护甲上的附装，不占武器位。";
			default:            return "不属于常规武器或护甲。";
		}
	}

	/** 伤害类型的名字，非武器为空。 */
	public static String damageTitle( Item it ){
		switch (damage( it )){
			case D_BLUNT:  return "钝击";
			case D_SLASH:  return "挥砍";
			case D_PIERCE: return "穿刺";
			default:       return "";
		}
	}

	/** 伤害类型的说明，非武器为空。 */
	public static String damageText( Item it ){
		switch (damage( it )){
			case D_BLUNT:  return "靠重量与整体冲击破防，容易打出硬直，破甲最好。";
			case D_SLASH:  return "靠刃口划过，范围与连贯性好，适合正面交锋。";
			case D_PIERCE: return "靠尖端刺入，命中精准，更容易造成暴击与致命伤。";
			default:       return "";
		}
	}

	/** 把附魔本色调柔，用于标题与文字：纯色在深色面板上太扎眼，向面板米色靠一靠。 */
	public static int soften( int color ){
		if (color < 0) return 0xE8D9A8;
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		float mix = 0.45f;
		r = (int)( r * (1 - mix) + 0xE0 * mix );
		g = (int)( g * (1 - mix) + 0xCF * mix );
		b = (int)( b * (1 - mix) + 0xA8 * mix );
		//再压一压最亮通道，深底上不刺眼
		int max = Math.max( r, Math.max( g, b ) );
		if (max > 210){
			float k = 210f / max;
			r = (int)( r * k );
			g = (int)( g * k );
			b = (int)( b * k );
		}
		return (r << 16) | (g << 8) | b;
	}

	/** 附魔物品名字的呼吸波（0~1，约 2.4 秒一次）。 */
	public static float pulse(){
		return 0.5f + 0.5f * (float)Math.sin( Game.timeTotal * 2.6f );
	}

	public static int lighten( int color, float f ){
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		r = Math.min( 255, r + (int)((255 - r) * f) );
		g = Math.min( 255, g + (int)((255 - g) * f) );
		b = Math.min( 255, b + (int)((255 - b) * f) );
		return (r << 16) | (g << 8) | b;
	}
}
