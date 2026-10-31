package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 横扫附魔：近战命中时，同时击中目标两侧的敌人。
 * 副目标伤害为基础伤害的 35%，奥术之戒等附魔倍率会提高该比例。
 */
public class Sweeping extends Weapon.Enchantment {

    private static final ItemSprite.Glowing CRIMSON = new ItemSprite.Glowing( 0xC03030 );

    private static final float BASE_RATIO = 0.35f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (damage <= 0 || defender == null) {
            return damage;
        }

        float ratio = BASE_RATIO * com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana
                .enchantPowerMultiplier( attacker );
        int sideDamage = Math.round( damage * ratio );
        if (sideDamage <= 0) {
            return damage;
        }

        //只波及目标两侧的敌对单位，不误伤其他阵营
        for (Char ch : Actor.chars()) {
            if (ch == attacker || ch == defender || !ch.isAlive()) {
                continue;
            }
            if (ch.alignment != defender.alignment) {
                continue;
            }
            if (!Dungeon.level.adjacent( defender.pos, ch.pos )) {
                continue;
            }
            ch.damage( new DamageInfo( sideDamage, DamageType.PHYSICAL, attacker, weapon, this ) );
        }

        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return CRIMSON;
    }
}
