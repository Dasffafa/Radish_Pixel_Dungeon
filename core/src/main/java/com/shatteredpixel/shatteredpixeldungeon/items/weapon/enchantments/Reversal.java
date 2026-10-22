package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 逆转附魔：命中时伤害归零，未命中时反而正常造成一次武器伤害。
 */
public class Reversal extends Weapon.Enchantment {

    private static final ItemSprite.Glowing VIOLET = new ItemSprite.Glowing( 0x8A2BE2 );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @Override
    public void modifyDamageInfo( Weapon weapon, Char attacker, Char defender, DamageInfo info ) {
        // 命中分支：伤害归零
        info.addFinalMultModifier( 0f, "reversal" );
    }

    /** 未命中分支：补一次正常武器伤害。 */
    @SubscribeEvent(event = AttackMissEvent.class, priority = 0)
    public static void onMiss( AttackMissEvent event ) {
        if (!(event.getAttackerWeapon() instanceof Weapon)) {
            return;
        }
        Weapon weapon = (Weapon) event.getAttackerWeapon();
        if (!(weapon.enchantment instanceof Reversal)) {
            return;
        }

        Char attacker = event.getAttacker();
        Char defender = event.getDefender();
        if (defender == null || !defender.isAlive() || attacker == null || !attacker.isAlive()) {
            return;
        }

        int dmg = Math.max( 1, attacker.damageRoll() );
        defender.damage( new DamageInfo( dmg, DamageType.PHYSICAL, attacker, weapon, attacker ) );
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return VIOLET;
    }
}
