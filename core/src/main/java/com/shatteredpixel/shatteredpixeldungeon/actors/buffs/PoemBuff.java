package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 诗 Buff — 对名称和武器或护甲押韵的目标造成额外最终伤害。
 */
public class PoemBuff extends Buff {

	public static final float RHYME_DAMAGE_MULTIPLIER = 1.66f;

	{
		type = buffType.POSITIVE;
	}

	public int applyFinalDamage(Hero hero, Char enemy, int damage) {
		if (hero == null || enemy == null || damage <= 0) return damage;

		String targetName = enemy.name();
		String sourceName = rhymingEquipmentName(hero, targetName);
		if (sourceName == null) return damage;

		GLog.p(Messages.get(this, "rhymed", targetName, sourceName));
		return Math.round(damage * RHYME_DAMAGE_MULTIPLIER);
	}

	private String rhymingEquipmentName(Hero hero, String targetName) {
		KindOfWeapon weapon = hero.belongings.attackingWeapon();
		if (weapon != null && rhymes(targetName, weapon.name())) {
			return weapon.name();
		}

		Armor armor = hero.belongings.armor();
		if (armor != null && rhymes(targetName, armor.name())) {
			return armor.name();
		}

		return null;
	}

	private static boolean rhymes(String targetName, String equipmentName) {
		if (targetName == null || equipmentName == null) return false;

		int targetChinese = lastChineseChar(targetName);
		int equipChinese = lastChineseChar(equipmentName);
		if (targetChinese != -1 && equipChinese != -1) {
			String targetRhyme = chineseRhyme(targetChinese);
			String equipRhyme = chineseRhyme(equipChinese);
			if (targetRhyme == null || equipRhyme == null) {
				return targetChinese == equipChinese;
			}
			return targetRhyme.equals(equipRhyme);
		}

		int targetFirst = firstLetterOrDigit(targetName);
		int equipFirst = firstLetterOrDigit(equipmentName);
		return targetFirst != -1 && Character.toLowerCase(targetFirst) == Character.toLowerCase(equipFirst);
	}

	private static int lastChineseChar(String text) {
		for (int i = text.length() - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (isChinese(c)) return c;
		}
		return -1;
	}

	private static int firstLetterOrDigit(String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c)) return c;
		}
		return -1;
	}

	private static boolean isChinese(char c) {
		return c >= '\u4E00' && c <= '\u9FFF';
	}

	private static String chineseRhyme(int c) {
		switch (c) {
			case '八': case '巴': case '疤': case '叉': case '茶': case '沙': case '杀': case '煞': case '甲': case '铠':
				return "a";
			case '白': case '败': case '怪': case '坏': case '迈': case '派': case '骸':
				return "ai";
			case '暗': case '岸': case '斩': case '砍': case '蓝': case '兰': case '燃': case '岩': case '眼': case '剑': case '箭': case '焰':
				return "an";
			case '昂': case '狼': case '王': case '亡': case '光': case '霜': case '杖': case '枪': case '伤': case '象': case '像':
				return "ang";
			case '刀': case '暴': case '豹': case '矛': case '袍': case '爪': case '妖':
				return "ao";
			case '蛇': case '恶': case '泪': case '雷': case '镭':
				return "e";
			case '北': case '飞': case '匪': case '鬼': case '灰': case '盔': case '胚': case '贼': case '锤': case '槌':
				return "ei";
			case '本': case '根': case '痕': case '盾': case '刃': case '人': case '神': case '身': case '尘': case '魂': case '棍':
				return "en";
			case '兵': case '灵': case '精': case '影': case '鹰': case '星': case '形': case '晶': case '心': case '银': case '鳞': case '林': case '民': case '金':
				return "in";
			case '冰': case '丁': case '钉': case '龙': case '种': case '钟': case '痛':
				return "ing";
			case '火': case '魔': case '骡': case '螺': case '罗': case '破': case '锁': case '朵': case '铎':
				return "o";
			case '兽': case '手': case '狗': case '偶': case '喉': case '头': case '肉': case '咒':
				return "ou";
			case '骨': case '毒': case '斧': case '虎': case '弩': case '鼠': case '术': case '书': case '蛛': case '珠': case '足':
				return "u";
			case '骑': case '袭': case '皮': case '尸': case '矢': case '士': case '师': case '石': case '刺': case '齿': case '翼': case '衣': case '弋':
				return "i";
			case '月': case '血': case '靴': case '雪': case '雀': case '爵':
				return "ue";
			case '狂': case '黄': case '皇': case '荒': case '双':
				return "uang";
			case '风': case '蜂': case '锋': case '翁': case '虫': case '弓': case '熊':
				return "eng";
			default:
				return null;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((RHYME_DAMAGE_MULTIPLIER - 1) * 100));
	}
}
