package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * 濡湿附魔
 * 造成0-等级的魔法伤害，25%概率使敌人虚弱3回合
 */
public class Wet extends Enchantment {

    private static final int GREEN = 0x00AA88;

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int lvl = weapon.buffedLvl();
        if (lvl > 0 && attacker.buff(MagicImmune.class) == null) {
            // 魔法伤害：0-等级
            int magicDmg = Random.IntRange(0, lvl);
            defender.damage(magicDmg, this);

            // 25%概率虚弱
            if (Random.Float() < 0.25f) {
                Buff.prolong(defender, Weakness.class, Weakness.DURATION);
            }
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing(GREEN);
    }

    @Override
    public String name() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }
}