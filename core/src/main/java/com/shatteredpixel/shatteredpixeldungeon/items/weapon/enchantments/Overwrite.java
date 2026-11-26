package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;

/**
 * 覆写附魔：记录对同一目标造成过的最高伤害，20回合内后续覆写附魔攻击不会低于这个数值。
 * 命中会刷新持续时间，记录随目标存档保存。
 * 记录与武器类绑定，目标死亡或换下武器后清除。
 */
public class Overwrite extends Weapon.Enchantment {

    private static final ItemSprite.Glowing SILVER = new ItemSprite.Glowing( 0xC0C0C0 );
    private static final float DURATION = 20f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (damage <= 0 || defender == null) {
            return damage;
        }

        OverwriteMark mark = defender.buff( OverwriteMark.class );
        if (mark == null) {
            mark = Buff.affect( defender, OverwriteMark.class );
            mark.floor = damage;
        } else {
            if (damage < mark.floor) {
                damage = mark.floor;
            } else {
                mark.floor = damage;
            }
        }
        mark.refresh();
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return SILVER;
    }

    /** 对单个目标记录的最低伤害下限。 */
    public static class OverwriteMark extends FlavourBuff {

        {
            type = buffType.NEGATIVE;
            announced = false;
        }

        public int floor = 0;

        private static final String FLOOR = "floor";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( FLOOR, floor );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            floor = bundle.getInt( FLOOR );
        }

        public void refresh() {
            postpone( DURATION );
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.TARGETED;
        }
    }
}
