/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 投掷物效果：淬毒
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 「淬毒」效果：攻击命中后给目标挂毒。
 *
 * <p>用附魔槽实现（{@link Item#diyEnchants}），所以只在武器被装备、
 * 真的攻击到目标时才会触发，天然是「装备后生效」。</p>
 */
public class DiyPoison extends Weapon.Enchantment {

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		if (defender != null && defender.isAlive()) {
			Buff.affect(defender, Poison.class).set(Math.max(3f, 3f + weapon.buffedLvl() * 0.5f));
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing(0x44FF44);
	}

	@Override
	public String name() {
		return "淬毒";
	}

	@Override
	public String desc() {
		return "攻击命中时给目标挂上毒素。";
	}
}
