package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollShamanKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class GnollShamanKing extends Mob implements Callback {

    {
        spriteClass = GnollShamanKingSprite.class;
        HT = HP = 150;
        defenseSkill = 15;

        maxLvl = 30;

        properties.add(Property.BOSS);
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
    }


    @Override
    protected boolean canAttack( Char enemy ) {
        return super.canAttack(enemy)
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }

    protected boolean doAttack(Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

            return super.doAttack( enemy );

        } else {

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }
    }


    public void onZapComplete() {
        zap();
        next();
    }

    @Override
    public void call() {
        next();
    }

    private void zap() {
        spend( 1f );

        Invisibility.dispel(this);
        Char enemy = this.enemy;
        if (hit( this, enemy, true )) {

            if (Random.Int( 2 ) == 0) {

                if (enemy == Dungeon.hero) Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
            }

            int dmg = Char.combatRoll( 6, 15 );
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            enemy.damage( dmg, new Shaman.EarthenBolt() );

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Badges.validateDeathFromEnemyMagic();
                Dungeon.fail( this );
                GLog.n( Messages.get(this, "bolt_kill") );
            }
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }

}
