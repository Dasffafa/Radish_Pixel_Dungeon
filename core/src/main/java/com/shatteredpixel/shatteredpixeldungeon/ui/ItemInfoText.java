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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

	private static final int DAGGER = 0, SHORTSWORD = 1, LONGSWORD = 2, GREATSWORD = 3, AXE = 4, BLUNT = 5,
			POLEARM = 6, WHIP = 7, WHEEL = 8, BOW = 9, CROSSBOW = 10, THROWN = 11, STAFF = 12, WAND = 13,
			RING = 14, LIGHT_ARMOR = 15, MID_ARMOR = 16, HEAVY_ARMOR = 17, SHIELD = 18,
			DUAL = 19, SPECIAL = 20, TOY = 21, OTHER = 22;

	/** 类型分类：先看是什么（护甲/法杖/戒指/投掷），再看类名里的词。 */
	private static int family( Item it ){
		if (it instanceof Armor){
			int t = ((Armor)it).tier;
			return t <= 2 ? LIGHT_ARMOR : t <= 4 ? MID_ARMOR : HEAVY_ARMOR;
		}
		if (it instanceof Wand) return WAND;
		if (it instanceof Ring) return RING;
		if (it instanceof ItemArmorAttachable) return TOY;
		String n = it.getClass().getSimpleName().toLowerCase();
		if (n.contains( "dual" ) || n.contains( "twin" ) || n.contains( "paired" )) return DUAL;
		if (SPECIAL_WEAPONS.contains( n )) return SPECIAL;
		if (n.contains( "crossbow" )) return CROSSBOW;
		if (n.contains( "bow" )) return BOW;
		if (n.contains( "circle" ) || n.contains( "waterwheel" )) return WHEEL;
		if (n.contains( "shield" )) return SHIELD;
		if (n.contains( "whip" )) return WHIP;
		if (n.contains( "staff" )) return STAFF;
		if (it instanceof MissileWeapon) return THROWN;
		if (n.contains( "dagger" ) || n.contains( "dirk" ) || n.contains( "katar" )
				|| n.contains( "sai" ) || n.contains( "sting" )) return DAGGER;
		if (n.contains( "shortsword" ) || n.contains( "rapier" ) || n.contains( "scimitar" )) return SHORTSWORD;
		if (n.contains( "greatsword" ) || n.contains( "knightgs" ) || n.contains( "kinggodsword" )
				|| n.contains( "ripper" ) || n.contains( "cleaver" )) return GREATSWORD;
		if (n.contains( "axe" )) return AXE;
		if (n.contains( "spear" ) || n.contains( "glaive" ) || n.contains( "lance" )
				|| n.contains( "scythe" ) || n.contains( "stick" ) || n.contains( "quarterstaff" )
				|| n.contains( "grappling" ) || n.contains( "hook" )) return POLEARM;
		if (n.contains( "hammer" ) || n.contains( "mace" ) || n.contains( "flail" )
				|| n.contains( "crowbar" ) || n.contains( "tonfa" ) || n.contains( "spanner" )
				|| n.contains( "glove" ) || n.contains( "gauntlet" ) || n.contains( "fist" )
				|| n.contains( "claw" ) || n.contains( "cross" )) return BLUNT;
		if (it instanceof MeleeWeapon) return LONGSWORD;
		return OTHER;
	}

	public static String familyTitle( Item it ){
		if (!isGear( it )) return "";
		switch (family( it )){
			case DAGGER:      return "匕首";
			case SHORTSWORD:  return "短剑";
			case LONGSWORD:   return "长剑";
			case GREATSWORD:  return "巨剑";
			case AXE:         return "斧";
			case BLUNT:       return "钝器";
			case POLEARM:     return "长柄武器";
			case WHIP:        return "鞭";
			case WHEEL:       return "轮刃";
			case BOW:         return "弓";
			case CROSSBOW:    return "弩";
			case THROWN:      return "投掷武器";
			case STAFF:       return "法杖";
			case WAND:        return "魔杖";
			case RING:        return "戒指";
			case LIGHT_ARMOR: return "轻甲";
			case MID_ARMOR:   return "中甲";
			case HEAVY_ARMOR: return "重甲";
			case SHIELD:      return "盾";
			case DUAL:        return "双刃";
			case SPECIAL:     return "特殊武器";
			case TOY:         return "玩具";
			default:          return "杂项";
		}
	}

	public static String familyText( Item it ){
		if (!isGear( it )) return "";
		switch (family( it )){
			case DAGGER:      return "极快且轻小，伤害偏低，靠速度与背刺取胜。";
			case SHORTSWORD:  return "轻便的单手近战武器，攻速与伤害都居中。";
			case LONGSWORD:   return "通用的近战武器，攻击与速度均衡，适合大多数情况。";
			case GREATSWORD:  return "沉重的大剑，每一击都很重，但出手较慢。";
			case AXE:         return "伤害很高但速度偏慢，靠重击压制对手。";
			case BLUNT:       return "破甲能力强，击中后容易造成硬直。";
			case POLEARM:     return "攻击距离更远，适合保持距离作战。";
			case WHIP:        return "能在两格外抽打，但伤害稍低。";
			case WHEEL:       return "本作的特色武器，投出后会旋回，也可近身斩击。";
			case BOW:         return "需要箭矢的远程武器，距离越远越安全。";
			case CROSSBOW:    return "装填慢但威力大的远程武器。";
			case THROWN:      return "一次性使用，远距投出后需要抢回。";
			case STAFF:       return "以魔力驱动的武器，注重法术与远程。";
			case WAND:        return "以充能施放法术，需要灵活使用。";
			case RING:        return "以持续加成回报佩戴者。";
			case LIGHT_ARMOR: return "防护偏低但不碍行动，适合机动作战。";
			case MID_ARMOR:   return "防护与重量取得平衡的常规护甲。";
			case HEAVY_ARMOR: return "防护最高，但对力量要求也最高。";
			case SHIELD:      return "专门用于格挡与防守。";
			case DUAL:        return "双手各持一把，连击更快更密集。";
			case SPECIAL:     return "来历或用法独特的武器，规则往往与常规武器不同。";
			case TOY:         return "装在护甲上的附装，不占武器位，提供一项特殊效果。";
			default:          return "不属于常规武器或护甲。";
		}
	}

	private static final Set<String> SPECIAL_WEAPONS = new HashSet<>(Arrays.asList(
			"aberforth", "beecomb", "belladonna", "celestialsphere", "chibayari", "darkshadowsword", "endguard",
			"enemyflag", "headcleaver", "holyankh", "juttechampionweapon", "killboatsword", "metalcross", "morello",
			"multitool", "papercutter", "replacepoint", "rlyeh", "shadowbooks", "silversting", "spacefragment",
			"wingsword" ));

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
