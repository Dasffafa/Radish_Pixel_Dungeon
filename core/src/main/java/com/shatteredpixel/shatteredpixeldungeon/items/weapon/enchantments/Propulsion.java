package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.PathFinder;

/**
 * 推进附魔：用这把武器命中远处目标后，立刻突进到目标身边；
 * 受到远程攻击后，也会突进到攻击者面前。
 */
public class Propulsion extends Weapon.Enchantment {

    private static final ItemSprite.Glowing AZURE = new ItemSprite.Glowing( 0x1E90FF );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @SubscribeEvent(event = AttackHitEvent.class, priority = 0)
    public static void onAttackHit( AttackHitEvent event ) {
        if (!(event.getAttacker() instanceof Hero)) {
            return;
        }
        if (!(event.getAttackerWeapon() instanceof Weapon)) {
            return;
        }
        if (!(((Weapon) event.getAttackerWeapon()).enchantment instanceof Propulsion)) {
            return;
        }
        dashAdjacent( (Hero) event.getAttacker(), event.getDefender() );
    }

    @SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
    public static void onDamaged( CharFinalDamageEvent event ) {
        if (!(event.getTarget() instanceof Hero)) {
            return;
        }
        Hero hero = (Hero) event.getTarget();
        if (!(hero.belongings.weapon() instanceof Weapon)) {
            return;
        }
        if (!(((Weapon) hero.belongings.weapon()).enchantment instanceof Propulsion)) {
            return;
        }
        Char attacker = event.getAttacker();
        if (attacker == null || attacker == hero) {
            return;
        }
        dashAdjacent( hero, attacker );
    }

    /** 传送到目标相邻的空格。 */
    private static void dashAdjacent( Hero hero, Char target ) {
        if (hero == null || target == null || !hero.isAlive() || !target.isAlive()
                || Dungeon.level == null) {
            return;
        }
        if (Dungeon.level.distance( hero.pos, target.pos ) < 2) {
            return;
        }

        int w = Dungeon.level.width();
        int tx = target.pos % w, ty = target.pos / w;
        for (int offset : PathFinder.NEIGHBOURS8) {
            int cell = target.pos + offset;
            if (cell < 0 || cell >= Dungeon.level.length()) {
                continue;
            }
            if (Math.abs( cell % w - tx ) > 1 || Math.abs( cell / w - ty ) > 1) {
                continue;
            }
            if (!Dungeon.level.passable[cell] || Dungeon.level.solid[cell]) {
                continue;
            }
            if (Actor.findChar( cell ) != null) {
                continue;
            }

            hero.pos = cell;
            hero.sprite.place( cell );
            hero.sprite.visible = true;
            Dungeon.level.pressCell( cell );
            Dungeon.observe();
            return;
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return AZURE;
    }
}
