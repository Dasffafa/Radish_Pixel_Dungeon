package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

/**
 * 推进附魔：命中远处目标或受到远程攻击后，询问玩家是否突进到目标身边。
 * 玩家可以拒绝，不会自动位移。
 */
public class Propulsion extends Weapon.Enchantment {

    private static final ItemSprite.Glowing AZURE = new ItemSprite.Glowing( 0x1E90FF );

    private static Char pendingTarget = null;
    private static boolean prompting = false;

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
        Hero hero = (Hero) event.getAttacker();
        Char target = event.getDefender();
        if (target != null && Dungeon.level != null
                && Dungeon.level.distance( hero.pos, target.pos ) >= 2) {
            queue( hero, target );
        }
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
        if (Dungeon.level != null && Dungeon.level.distance( hero.pos, attacker.pos ) >= 2) {
            queue( hero, attacker );
        }
    }

    /** 英雄就绪后补上待处理的询问。 */
    public static void onHeroReady( Hero hero ) {
        tryPrompt( hero );
    }

    private static void queue( Hero hero, Char target ) {
        if (hero == null || target == null || target == hero || !target.isAlive()) {
            return;
        }
        pendingTarget = target;
        tryPrompt( hero );
    }

    private static void tryPrompt( Hero hero ) {
        if (pendingTarget == null || prompting) {
            return;
        }
        if (hero == null || !hero.ready || hero.curAction != null
                || !hero.isAlive() || Dungeon.level == null) {
            return;
        }

        final Char target = pendingTarget;
        pendingTarget = null;
        prompting = true;
        Game.runOnRenderThread( new Callback() {
            @Override
            public void call() {
                prompting = false;
                if (Dungeon.hero == null || !Dungeon.hero.ready
                        || !Dungeon.hero.isAlive() || !target.isAlive()) {
                    return;
                }
                GameScene.show( new WndOptions(
                        Messages.get( Propulsion.class, "prompt_title" ),
                        Messages.get( Propulsion.class, "prompt_text" ),
                        Messages.get( Propulsion.class, "prompt_yes" ),
                        Messages.get( Propulsion.class, "prompt_no" ) ) {
                    @Override
                    protected void onSelect( int index ) {
                        if (index == 0) {
                            dashAdjacent( Dungeon.hero, target );
                        }
                    }
                } );
            }
        } );
    }

    /** 传送到目标相邻的空格。 */
    private static void dashAdjacent( Hero hero, Char target ) {
        if (hero == null || target == null || !hero.isAlive() || !target.isAlive()
                || Dungeon.level == null || hero.rooted) {
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
